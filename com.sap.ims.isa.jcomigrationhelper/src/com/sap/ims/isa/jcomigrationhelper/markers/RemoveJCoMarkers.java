package com.sap.ims.isa.jcomigrationhelper.markers;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;

public class RemoveJCoMarkers extends CreateJCoMarkers implements IObjectActionDelegate {

    public RemoveJCoMarkers() {
        super();
    }

    @Override
    public void run(IAction paramIAction) {
        ITextEditor editor = JCoMigrationHelperPlugin.getEditor();
        AbstractMarkerAnnotationModel annotationModel = JCoMarkerFactory.getAnnotationModel(editor);
        if (annotationModel != null) {
            List<IMarker> markers = JCoMarkerFactory.findMarkers(selectedResource);
            markers.forEach(m -> {
                Position markerPosition = annotationModel.getMarkerPosition(m);
                if (markerPosition != null 
                        && m.exists()
                        && selectedElementRange.getOffset() == markerPosition.getOffset()
                        && m.getAttribute(IMarker.USER_EDITABLE, true)) {
                    JCoMarkerFactory.deleteMarker(m);
                }
            });
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isMarkerAlreadyAvailable());
    }

    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}

    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {}

}
