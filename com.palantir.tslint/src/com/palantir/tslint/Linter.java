
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

public class Linter {

    public static final String MARKER_TYPE = "com.palantir.tslint.tslintProblem";

    public void lint(IResource resource, String configurationFilePath) {
        if (resource instanceof IFile && resource.getName().endsWith(".ts")) {
            IFile file = (IFile) resource;

            deleteMarkers(file);

            try {
                File bundleFile = FileLocator.getBundleFile(TSLintPlugin.getDefault().getBundle());
                File tslintFile = new File(bundleFile, "../node_modules/tslint/bin/tslint");
                String tslintPath = tslintFile.getAbsolutePath();

                String resourceFullPathString = resource.getRawLocation().toOSString();
                // start tslint and get its output
                ProcessBuilder processBuilder = new ProcessBuilder(tslintPath,
                    "-f", resourceFullPathString,
                    "-t", "json",
                    "-c", configurationFilePath);

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

                // Terminate process now that we have the complete output
                process.destroy();

                if (jsonString == null) {
                    // If there are no errors, short-circuit so Jackson doesn't trip up
                    return;
                }

                ObjectMapper objectMapper = new ObjectMapper();

                RuleFailure[] ruleFailures = objectMapper.readValue(jsonString, RuleFailure[].class);
                for (RuleFailure ruleFailure : ruleFailures) {
                    addMarker(ruleFailure);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addMarker(RuleFailure ruleViolation) {
        try {
            Path path = new Path(ruleViolation.getName());
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

            Map<String, Object> attributes = Maps.newHashMap();

            RuleFailurePosition startPosition = ruleViolation.getStartPosition();
            RuleFailurePosition endPosition = ruleViolation.getEndPosition();

            attributes.put(IMarker.LINE_NUMBER, startPosition.getLine() + 1);
            attributes.put(IMarker.CHAR_START, startPosition.getPosition());
            attributes.put(IMarker.CHAR_END, endPosition.getPosition());
            attributes.put(IMarker.MESSAGE, ruleViolation.getFailure());
            attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
            attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            MarkerUtilities.createMarker(file, attributes, MARKER_TYPE);
        } catch (CoreException e) {
        }
    }

    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException ce) {
        }
    }

}
