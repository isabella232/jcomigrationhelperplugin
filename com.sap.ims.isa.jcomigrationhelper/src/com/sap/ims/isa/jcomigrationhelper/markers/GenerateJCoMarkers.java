package com.sap.ims.isa.jcomigrationhelper.markers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;

public class GenerateJCoMarkers implements IEditorActionDelegate, IObjectActionDelegate {

    public GenerateJCoMarkers() {
        super();
    }

    /**
     * Searched for all markers for an IResource and any sub resources.
     * Then output the number of markers that are returned as a pop up.
     */
    @Override
    public void run(IAction action) {
        TreeSelection selection = JCoMarkerFactory.getTreeSelection();
        if (selection != null) {
            try {
                ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(JCoMigrationHelperPlugin.getActiveWorkbenchShell());
                progressMonitorDialog.setCancelable(true);
                progressMonitorDialog.run(true, true, new MarkerGeneratorTask(selection));
            } catch (InvocationTargetException e) {
                JCoMigrationHelperPlugin.logErrorMessage(Messages.marker_gen_error_in_task, e);
                MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                        Messages.marker_gen_info_not_generated_title, Messages.marker_gen_error_in_task);
            } catch (InterruptedException e) {
                // Task has been cancelled.
            }
        } else {
            ICompilationUnit cu = JavaEditorUtils.getCurrentCompilationUnit();
            if(cu != null) {
                JCoMarkerFactory.generateMarker(cu);
                return;
            }
            MessageDialog.openInformation(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.marker_gen_info_not_generated_title, Messages.marker_gen_info_not_generated_type_not_supported);
        }
    }

    
    @Override
    public void selectionChanged(IAction action, ISelection selection) {}

    @Override
    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}

    @Override
    public void setActivePart(IAction action, IWorkbenchPart workbench) {}
}
