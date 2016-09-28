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

package com.palantir.tslint.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 *
 *
 * @author alpapad
 */
public class QuickFixer implements IMarkerResolutionGenerator2 {
    private final static IMarkerResolution[] empty = {};
    @Override
    public IMarkerResolution[] getResolutions(IMarker mk) {
       if(!this.hasResolutions(mk)){
           return empty;
       }

       try {
          Object problem = mk.getAttribute(IMarker.MESSAGE);
          return new IMarkerResolution[] {
             new QuickFix("Fix for "+problem)
          };
       }
       catch (CoreException e) {
          return new IMarkerResolution[0];
       }
    }

    @Override
    public boolean hasResolutions(IMarker marker) {
        try {
            return (marker.getAttribute("tsViolation") != null);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

    }
 }