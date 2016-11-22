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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;

public final class Builder extends IncrementalProjectBuilder {

    private final static int RES_EVENTS = IResourceChangeEvent.PRE_CLOSE
            | IResourceChangeEvent.PRE_DELETE
            | IResourceChangeEvent.PRE_REFRESH
            | IResourceChangeEvent.POST_CHANGE;

    public static final String BUILDER_ID = "com.palantir.tslint.tslintBuilder";

    private Linter linter;

    private IResourceChangeListener projectListener;

    private String configurationPath;


    public Builder() {
        super();
        this.linter = new Linter();
    }

    /**
     * Initialize this builder. Add a resource listener to monitor configuration changes, and
     * project open/close/delete events so we can safely dispose the node bridge.
     */
    @Override
    protected void startupOnInitialize() {
        super.startupOnInitialize();
        updateConfigPath();

        this.projectListener = new ProjectChangeListener(this.getProject(), (event) -> {
            if (event == IResourceChangeEvent.PRE_CLOSE || event == IResourceChangeEvent.PRE_DELETE) {
                dispose();
            } else {
                updateConfigPath();
                try {
                    this.linter.dispose();
                } catch(Exception e) {

                } finally {
                    this.linter = new Linter();
                }
                rebuildProject();
            }
        });

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this.projectListener, RES_EVENTS);

        // Add a listener for configuration changes
        IProject project = this.getProject();
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences prefs = projectScope.getNode(TSLintPlugin.ID);
        prefs.addPreferenceChangeListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                updateConfigPath();
                rebuildProject();
            }
        });
    }

    public void dispose() {
        if (this.linter != null) {
            this.linter.dispose();
            this.linter = null;
        }
        if (this.projectListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.projectListener);
            this.projectListener = null;
        }
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
        this.linter.lint(resource, this.configurationPath);
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

    private void updateConfigPath() {
        IProject project = this.getProject();
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences prefs = projectScope.getNode(TSLintPlugin.ID);
        this.configurationPath = prefs.get("configPath", null);
        if (this.configurationPath != null && !this.configurationPath.equals("")) {
            File configFile = new File(this.configurationPath);
            // if we're given a relative path get the absolute path for it
            if (!configFile.isAbsolute()) {
                IPath projectLocation = project.getRawLocation();
                String projectLocationPath = projectLocation.toOSString();
                File projectFile = new File(projectLocationPath, this.configurationPath);
                this.configurationPath = projectFile.getAbsolutePath();
            }
        } else {
            this.configurationPath = project.getFile("tslint.json").getLocation().toOSString();
        }
    }

    private void rebuildProject() {
        final IProject project = this.getProject();

        Job job = new Job("Re-running tslint") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (project.isOpen() && project.hasNature(ProjectNature.NATURE_ID)) {
                        project.build(IncrementalProjectBuilder.CLEAN_BUILD, BUILDER_ID, null, monitor);
                        project.build(IncrementalProjectBuilder.FULL_BUILD, BUILDER_ID, null, monitor);
                    }
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    return e.getStatus();
                }
            }
        };
        job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        job.schedule();
    }
}
