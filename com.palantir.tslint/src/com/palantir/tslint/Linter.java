
package com.palantir.tslint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.palantir.tslint.failure.RuleFailure;
import com.palantir.tslint.failure.RuleFailurePosition;

class Linter {

    public static final String MARKER_TYPE = "com.palantir.tslint.tslintProblem";

    public void lint(IResource resource, String configurationPath) throws IOException {
        if (resource instanceof IFile && resource.getName().endsWith(".ts")) {
            IFile file = (IFile) resource;
            String linterPath = TSLintPlugin.getLinterPath();
            String resourcePath = resource.getRawLocation().toOSString();

            // remove any pre-existing markers for this file
            deleteMarkers(file);

            // start tslint and get its output
            ProcessBuilder processBuilder = new ProcessBuilder(linterPath,
                "-f", resourcePath,
                "-t", "json",
                "-c", configurationPath);

            // TODO: Take out the platform specific hack
            Map<String, String> processBuilderEnvironment = processBuilder.environment();
            String path = processBuilderEnvironment.get("PATH");
            if (path.length() != 0) {
                path = path + ":";
            }
            path = path + "/usr/local/bin";
            processBuilderEnvironment.put("PATH", path);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
            String jsonString = reader.readLine();

            // Now that we have the complete output, terminate the process
            process.destroy();

            if (jsonString != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                RuleFailure[] ruleFailures = objectMapper.readValue(jsonString, RuleFailure[].class);
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
            attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

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
