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

package com.palantir.tslint.fix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.tslint.failure.Replacement;
import com.palantir.tslint.failure.RuleFailure;

/**
 * The Class QuickFix.
 *
 * @author alpapad
 */
public class QuickFix extends WorkbenchMarkerResolution {

    /** The mapper. */
    private final ObjectMapper mapper = new ObjectMapper();

    /** The label. */
    String label;

    /**
     * Instantiates a new quick fix.
     *
     * @param label
     *            the label
     */
    QuickFix(String label) {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolution#getLabel()
     */
    @Override
    public String getLabel() {
        return this.label;
    }

    /**
     * Runs this resolution. Resolve all <code>markers</code>. <code>markers</code> must be a subset
     * of the markers returned by <code>findOtherMarkers(IMarker[])</code>.
     *
     * @param markers
     *            The markers to resolve, not null
     * @param monitor
     *            The monitor to report progress
     */
    @Override
    public void run(IMarker[] markers, IProgressMonitor monitor) {
        Map<IFile, List<Replacement>> replacements = new HashMap<>();

        for (int i = 0; i < markers.length; i++) {
            IMarker marker = markers[i];

            IResource resource = marker.getResource();
            if (resource instanceof IFile) {
                IFile f = IFile.class.cast(resource);

                RuleFailure failure = formMarker(markers[i]);
                if (failure != null && failure.getFix() != null && failure.getFix().getReplacements() != null && !failure.getFix().getReplacements().isEmpty()) {
                    if(!replacements.containsKey(f)){
                        replacements.put(f, new ArrayList<>());
                    }

                    replacements.get(f).addAll(failure.getFix().getReplacements());
                }
            }

        }

        for(Entry<IFile, List<Replacement>> entry: replacements.entrySet()) {
            entry.getValue().sort(new Comparator<Replacement>() {

                @Override
                public int compare(Replacement o1, Replacement o2) {
                    return o2.getStart() - o1.getStart();
                }
            });
            try {

                monitor.subTask(entry.getKey().getName());
                applyFixes(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
     */
    @Override
    public void run(IMarker marker) {
        RuleFailure failure = formMarker(marker);
        if (failure != null && failure.getFix() != null && failure.getFix().getReplacements() != null && !failure.getFix().getReplacements().isEmpty()) {
            IResource resource = marker.getResource();
            if (resource instanceof IFile) {
                IFile f = IFile.class.cast(resource);
                try {
                    applyFixes(f, failure.getFix().getReplacements());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        try {
            marker.delete();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the IDocument instance for the given file.
     * <p/>
     * Borrowed from org.eclipse.ant.internal.ui.AntUtil
     *
     * @param f
     *            the f
     * @param replacements
     *            the replacements
     * @throws Exception
     *             the exception
     */
    public static void applyFixes(IFile f, List<Replacement> replacements) throws Exception {

        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        IPath location = f.getLocation();
        System.err.println("File is:" + location.toOSString());
        boolean connected = false;
        try {
            ITextFileBuffer buffer = manager.getTextFileBuffer(location, LocationKind.LOCATION);
            if (buffer == null) {
                //no existing file buffer..create one
                manager.connect(location, LocationKind.LOCATION, new NullProgressMonitor());
                connected = true;
                buffer = manager.getTextFileBuffer(location, LocationKind.LOCATION);

            }
            buffer.requestSynchronizationContext();

            try {
                IDocument document = buffer.getDocument();
                for (Replacement r : replacements) {
                    document.replace(r.getStart(), r.getLength(), r.getText());
                }
            } finally {
                buffer.commit(new NullProgressMonitor(), true);
                buffer.releaseSynchronizationContext();
            }
        } finally {
            if (connected) {
                try {
                    manager.disconnect(location, LocationKind.LOCATION, new NullProgressMonitor());
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Quick fixes for tslint problems";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public IMarker[] findOtherMarkers(IMarker[] markers) {
        if (markers == null || markers.length == 0) {
            return markers;
        }

        List<MarkerRule> selected = new ArrayList<>();
        Set<RuleFailure> rf = new HashSet<>();
        for (IMarker m : markers) {
            RuleFailure f = formMarker(m);
            if (f != null && rf.add(f)) {
                selected.add(new MarkerRule(m, f));
            }
        }

        selected.sort(new Comparator<MarkerRule>() {
            @Override
            public int compare(MarkerRule o1, MarkerRule o2) {
                return (o1.failure.getStartPosition().getPosition() + o1.failure.getEndPosition().getPosition())
                        - (o2.failure.getStartPosition().getPosition() + o2.failure.getEndPosition().getPosition());
            }
        });

        return selected.stream().map(x -> x.marker).collect(Collectors.toList()).toArray(new IMarker[selected.size()]);
    }

    private RuleFailure formMarker(IMarker marker) {
        final String json = marker.getAttribute("tsViolation", null);
        if (json != null) {
            RuleFailure failure;
            try {
                failure = this.mapper.readValue(json, RuleFailure.class);
                return failure;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    static class MarkerRule {
        public final IMarker marker;
        public final RuleFailure failure;

        MarkerRule(IMarker marker, RuleFailure failure) {
            this.marker = marker;
            this.failure = failure;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.failure == null) ? 0 : this.failure.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MarkerRule other = (MarkerRule) obj;
            if (this.failure == null) {
                if (other.failure != null)
                    return false;
            } else if (!this.failure.equals(other.failure))
                return false;
            return true;
        }

    }
}
