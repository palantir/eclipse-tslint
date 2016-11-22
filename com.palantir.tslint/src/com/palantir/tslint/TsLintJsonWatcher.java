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
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

/**
 * @author alpapad
 */
public class TsLintJsonWatcher implements IResourceDeltaVisitor {
    private final OnConfigChange cb;
    private final IProject project;

    public TsLintJsonWatcher(IProject project, OnConfigChange cb) {
        super();
        this.project = project;
        this.cb = cb;
    }

    @Override
    public boolean visit(IResourceDelta delta) {
        IResource res = delta.getResource();
        if(res instanceof IProject) {
            if( !this.project.getName().equals(res.getName())){
                return false;
            }
        }
        if(res == null || res.getProject() == null ){
            return true;
        }
        if(!this.project.getName().equals(res.getProject().getName())){
            return false;
        }

        if(res.getFullPath().toOSString().endsWith("tslint.json")) {
            this.cb.apply(-1);
        }
        return true; // visit the children
    }
}
