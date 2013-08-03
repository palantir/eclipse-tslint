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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.palantir.tslint.failure.RuleFailure;
import com.palantir.tslint.failure.RuleFailurePosition;

public class Builder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = "com.palantir.tslint.tslintBuilder";

    private Linter linter;

    public Builder() {
        super();
        this.linter = new Linter();
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        if (kind == FULL_BUILD) {
            fullBuild();
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild();
            } else {
                incrementalBuild(delta);
            }
        }

        return null;
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(Linter.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
    }

    protected void fullBuild() throws CoreException {
        getProject().accept(new ResourceVisitor());
    }

    protected void incrementalBuild(IResourceDelta delta) throws CoreException {
        delta.accept(new DeltaVisitor());
    }

    private void lint(IResource resource) throws IOException {
        String configurationPath = getProject().getFile(".tslintrc").getRawLocation().toOSString();
        this.linter.lint(resource, configurationPath);
    }

    private class ResourceVisitor implements IResourceVisitor {
        @Override
        public boolean visit(IResource resource) {
            try {
                lint(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }
    }

    private class DeltaVisitor implements IResourceDeltaVisitor {
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();

            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                case IResourceDelta.CHANGED:
                    try {
                        lint(resource);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }

            return true;
        }
    }

}
