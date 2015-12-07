package com.sap.ims.isa.jcomigrationhelper.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class JCoMarkUpdater implements IMarkerUpdater {

    public JCoMarkUpdater() {}

    @Override
    public String[] getAttribute() {
        return null;
    }

    @Override
    public String getMarkerType() {
        return "com.sap.ims.isa.jcomigrationhelper.marker"; //$NON-NLS-1$
    }

    @Override
    public boolean updateMarker(IMarker marker, IDocument document, Position position) {
        return JCoMarkerFactory.updateMarker(marker, document, position);
    }

}
