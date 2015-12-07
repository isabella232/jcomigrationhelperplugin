package com.sap.ims.isa.jcomigrationhelper.markers.ruler;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

import com.sap.ims.isa.jcomigrationhelper.markers.JCoMarkerFactory;
import com.sap.ims.isa.jcomigrationhelper.markers.MarkerTypes;

/**
 * Removes the marker when clicking via vertical rules in the java editor.
 * @author Iwan Zarembo
 *
 */
public class RemoveJCoMarkerAction extends SelectMarkerRulerAction {


    public RemoveJCoMarkerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo rulerInfo) {
        super(bundle, prefix, editor, rulerInfo);
    }

    @Override
    public void update() {
        super.update();
        @SuppressWarnings("unchecked")
        List<IMarker> markers = this.getMarkers();
        if(markers.size() > 0) {
            // only activate if at least one of our markers is available
            boolean available = markers.stream()
                    .anyMatch(m -> MarkerUtilities.isMarkerType(m, MarkerTypes.MARKER.getType()));
            this.setEnabled(available);
        }
    }

    @Override
    public void run() {
        @SuppressWarnings("unchecked")
        List<IMarker> markers = this.getMarkers();
        if (markers.size() > 0) {
            AbstractMarkerAnnotationModel annotationModel = this.getAnnotationModel();
            IDocument document = this.getDocument();
            markers.forEach(m -> {
                Position markerPosition = annotationModel.getMarkerPosition(m);
                if (markerPosition != null
                        && m.exists()
                        && this.includesRulerLine(markerPosition, document)
                        && m.getAttribute(IMarker.USER_EDITABLE, true)) {
                    JCoMarkerFactory.deleteMarker(m);
                }
            });
        }
    }
}
