/*
 * Copyright 2013 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.tslint;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.palantir.tslint.failure.RuleFailure;
import com.palantir.tslint.failure.RuleFailurePosition;
import com.palantir.tslint.services.Bridge;
import com.palantir.tslint.services.Request;

final class Linter {

    public static final String MARKER_TYPE = "com.palantir.tslint.tslintProblem";

    private Bridge bridge;

    public Linter() {
        this.bridge = null;
    }

    public void lint(IResource resource, String configurationPath) throws IOException {
        String resourceName = resource.getName();
        if (resource instanceof IFile && resourceName.endsWith(".ts") && !resourceName.endsWith(".d.ts")) {
            IFile file = (IFile) resource;
            String resourcePath = resource.getRawLocation().toOSString();

            // remove any pre-existing markers for the given file
            deleteMarkers(file);

            // get a bridge
            if (this.bridge == null) {
                String configuration = Files.toString(new File(configurationPath), Charsets.UTF_8);
                Request configurationRequest = new Request("setConfiguration", configuration);

                this.bridge = new Bridge();
                this.bridge.call(configurationRequest, Void.class);
            }

            Request request = new Request("lint", resourcePath);
            String response = this.bridge.call(request, String.class);

            if (response != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                RuleFailure[] ruleFailures = objectMapper.readValue(response, RuleFailure[].class);
                for (RuleFailure ruleFailure : ruleFailures) {
                    addMarker(ruleFailure);
                }
            }
        }
    }

    private void addMarker(RuleFailure ruleViolation) {
        try {
            Path path = new Path(ruleViolation.getName());
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

            RuleFailurePosition startPosition = ruleViolation.getStartPosition();
            RuleFailurePosition endPosition = ruleViolation.getEndPosition();

            Map<String, Object> attributes = Maps.newHashMap();
            attributes.put(IMarker.LINE_NUMBER, startPosition.getLine() + 1);
            attributes.put(IMarker.CHAR_START, startPosition.getPosition());
            attributes.put(IMarker.CHAR_END, endPosition.getPosition());
            attributes.put(IMarker.MESSAGE, ruleViolation.getFailure());
            attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
            attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

            MarkerUtilities.createMarker(file, attributes, MARKER_TYPE);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
