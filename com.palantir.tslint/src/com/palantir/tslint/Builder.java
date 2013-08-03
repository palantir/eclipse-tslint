
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

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(Linter.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
    }

    protected void fullBuild(final IProgressMonitor monitor) {
        try {
            getProject().accept(new ResourceVisitor());
        } catch (CoreException e) {
        }
    }

    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        delta.accept(new DeltaVisitor());
    }

    private String getLintConfigurationPath() {
        return getProject().getFile(".tslintrc").getRawLocation().toOSString();
    }

    private class ResourceVisitor implements IResourceVisitor {
        @Override
        public boolean visit(IResource resource) {
            Builder.this.linter.lint(resource, getLintConfigurationPath());
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
                    Builder.this.linter.lint(resource, getLintConfigurationPath());
                    break;
            }

            // return true to continue visiting children.
            return true;
        }
    }

}
