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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

public class Builder extends IncrementalProjectBuilder {

	class DeltaVisitor implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				lint(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			lint(resource);
			return true;
		}
	}

	static class FilePosition {
		@SuppressWarnings("unused")
		private int character;
		private int line;
		private int position;

		public FilePosition(@JsonProperty("character") int character,
				@JsonProperty("line") int line,
				@JsonProperty("position") int position) {
			this.character = character;
			this.line = line;
			this.position = position;
		}
	}

	static class FailurePosition {
		private FilePosition start;
		private FilePosition end;

		public FailurePosition(@JsonProperty("start") FilePosition start,
				@JsonProperty("end") FilePosition end) {
			this.start = start;
			this.end = end;
		}
	}

	static class RuleViolation {
		private String failure;
		private String name;
		private FailurePosition position;

		public RuleViolation(@JsonProperty("failure") String failure,
				@JsonProperty("name") String name,
				@JsonProperty("failurePosition") FailurePosition position) {
			this.failure = failure;
			this.name = name;
			this.position = position;
		}
	}

	public static final String BUILDER_ID = "com.palantir.tslint.tslintBuilder";

	private static final String MARKER_TYPE = "com.palantir.tslint.tslintProblem";

	void lint(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".ts")) {
			IFile file = (IFile) resource;

			deleteMarkers(file);

			try {
				File bundleFile;
				bundleFile = FileLocator.getBundleFile(TSLintPlugin
						.getDefault().getBundle());

				File tslintFile = new File(bundleFile,
						"../node_modules/tslint/bin/tslint");
				String tslintPath = tslintFile.getAbsolutePath();

				String resourceFullPathString = resource.getRawLocation()
						.toOSString();
				String tslintrcString = getProject().getFile(".tslintrc")
						.getRawLocation().toOSString();
				// start tslint and get its output
				ProcessBuilder processBuilder = new ProcessBuilder(tslintPath,
						"-f", resourceFullPathString, "-t", "json", "-c",
						tslintrcString);

				// TODO: Take out the platform specific hack
				Map<String, String> processBuilderEnvironment = processBuilder
						.environment();
				String path = processBuilderEnvironment.get("PATH");
				if (path.length() != 0) {
					path = path + ":";
				}
				path = path + "/usr/local/bin";
				processBuilderEnvironment.put("PATH", path);

				Process process = processBuilder.start();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(),
								Charsets.UTF_8));
				String jsonString = reader.readLine();

				// Terminate process now that we have the complete output
				process.destroy();

				if (jsonString == null) {
					// If there are no errors, short-circuit so Jackson doesn't
					// trip up
					return;
				}

				ObjectMapper objectMapper = new ObjectMapper();

				RuleViolation[] ruleViolations = objectMapper.readValue(
						jsonString, RuleViolation[].class);
				for (RuleViolation ruleViolation : ruleViolations) {
					addMarker(ruleViolation);
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void addMarker(RuleViolation ruleViolation) {
		try {
			Path path = new Path(ruleViolation.name);
			IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFileForLocation(path);

			Map<String, Object> attributes = Maps.newHashMap();

			FilePosition startPosition = ruleViolation.position.start;
			FilePosition endPosition = ruleViolation.position.end;

			attributes.put(IMarker.LINE_NUMBER, startPosition.line + 1);
			attributes.put(IMarker.CHAR_START, startPosition.position);
			attributes.put(IMarker.CHAR_END, endPosition.position);
			attributes.put(IMarker.MESSAGE, ruleViolation.failure);
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

	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new ResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new DeltaVisitor());
	}
}
