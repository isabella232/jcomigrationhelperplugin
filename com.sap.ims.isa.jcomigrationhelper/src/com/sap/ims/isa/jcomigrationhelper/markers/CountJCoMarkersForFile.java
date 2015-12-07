package com.sap.ims.isa.jcomigrationhelper.markers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;

public class CountJCoMarkersForFile implements IEditorActionDelegate {

    public CountJCoMarkersForFile() {
        super();
    }

    /*
     * Find markers in the current open file.
     * Then output the number of markers that are returned as a pop up.
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        IFile file = (IFile) JCoMigrationHelperPlugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                .getEditorInput().getAdapter(IFile.class);
        List<IMarker> markers = JCoMarkerFactory.findMarkers(file);
        MessageDialog dialog = new MessageDialog(JCoMigrationHelperPlugin.getActiveWorkbenchShell(), Messages.marker_counter_msg_title,
                null, Messages.bind(Messages.marker_counter_msg_result, markers.size()), MessageDialog.INFORMATION, new String[] { Messages.marker_counter_btn_ok }, 0);
        dialog.open();

    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {}

    @Override
    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}

}
