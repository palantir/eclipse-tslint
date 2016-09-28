/*
 * Copyright the authors
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;

/**
 * @author alpapad
 */
public class ProjectChangeListener implements IResourceChangeListener {

    private final IProject project;
    private final OnConfigChange cb;

    public ProjectChangeListener(IProject project, OnConfigChange cb) {
        super();
        this.project = project;
        this.cb = cb;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResource res = event.getResource();
        try {
            switch (event.getType()) {
                case IResourceChangeEvent.PRE_CLOSE:
                    if (this.project.getName().equals(res.getName())) {
                        this.cb.apply(event.getType());
                    }
                    break;
                case IResourceChangeEvent.PRE_DELETE:
                    if (this.project.getName().equals(res.getName())) {
                        this.cb.apply(event.getType());
                    }
                    break;
                case IResourceChangeEvent.POST_CHANGE:
                        event.getDelta().accept(new TsLintJsonWatcher(this.project, this.cb));
                    break;
                case IResourceChangeEvent.PRE_REFRESH:
                    if(event.getDelta() != null) {
                        event.getDelta().accept(new TsLintJsonWatcher(this.project, this.cb));
                    }
                    break;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
