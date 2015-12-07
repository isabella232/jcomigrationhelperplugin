package com.sap.ims.isa.jcomigrationhelper.markers;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;

public class CountJCoMarkersForFiles implements IEditorActionDelegate {

    public CountJCoMarkersForFiles() {
        super();
    }

    /*
     * Searched for all markers for an IResource and any sub resources.
     * Then output the number of markers that are returned as a pop up.
     */
    @Override
    public void run(IAction action) {
        TreeSelection selection = JCoMarkerFactory.getTreeSelection();
        IResource resource;
        if (selection != null && selection.getFirstElement() instanceof IOpenable) {
            resource = (IResource) ((org.eclipse.core.runtime.IAdaptable) selection.getFirstElement())
                    .getAdapter(IResource.class);
            List<IMarker> markers = JCoMarkerFactory.findAllMarkers(resource);
            MessageDialog dialog = new MessageDialog(JCoMigrationHelperPlugin.getActiveWorkbenchShell(), Messages.marker_counter_msg_title,
                    null, Messages.bind(Messages.marker_counter_msg_result, markers.size()), MessageDialog.INFORMATION, new String[] { Messages.marker_counter_btn_ok }, 0);
            dialog.open();
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {}

    @Override
    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}

}
