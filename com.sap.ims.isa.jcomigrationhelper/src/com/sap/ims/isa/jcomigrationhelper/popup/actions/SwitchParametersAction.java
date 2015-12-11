package com.sap.ims.isa.jcomigrationhelper.popup.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
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
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaVariableOccurrence;
import com.sap.ims.isa.jcomigrationhelper.markers.JCoMarkerFactory;

@SuppressWarnings("restriction")
public class SwitchParametersAction implements IEditorActionDelegate, IObjectActionDelegate {

    public SwitchParametersAction() {}

    /**
     * Switches the parameters of the currently selected local variable. It is using {@link JavaEditorUtils#getJavaVariableOccurrence(JavaEditor)}
     * to get the list of all selected nodes and checks its validity in {@link JavaEditorUtils#isValidForProcessing(ASTNode)}. Only then
     * it will switch the parameters.<br>
     * Also a message will be shown if no parameter calls could be switched. This usually indicates that the selection was on a variable, where
     * no set-Methods were called.
     * @see JavaEditorUtils
     * @see JavaEditorUtils#getJavaVariableOccurrence(JavaEditor)
     * @see JavaEditorUtils#isValidForProcessing(ASTNode)
     */
    @Override
    public void run(IAction action) {
        TreeSelection selection = JCoMarkerFactory.getTreeSelection();
        if (selection != null) {
            try {
                ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
                        JCoMigrationHelperPlugin.getActiveWorkbenchShell());
                progressMonitorDialog.setCancelable(true);
                progressMonitorDialog.run(true, true, new SwitchParametersTask(selection));
            }
            catch (InvocationTargetException e) {
                JCoMigrationHelperPlugin.logErrorMessage(Messages.switching_params_process_error_in_task, e);
                MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                        Messages.switching_params_process_error_in_task_title,
                        Messages.switching_params_process_error_in_task);
            }
            catch (InterruptedException e) {
                // Task has been cancelled.
            }
        } else {
            JavaVariableOccurrence occurrences = JavaEditorUtils.getJavaVariableOccurrence(null);
            SwitchParametersTask task = new SwitchParametersTask(null);
            ASTRewrite rewrite = task.switchParameters(occurrences, null, false);
            task.applyInEditor(occurrences.getDoc(), rewrite);
            task.updateProcessedMarkers(occurrences.getDoc(), occurrences.getNodes());
        }
    }

    /**
     * Empty, no implementation in this class.
     * 
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

    /**
     * Enables the action if there is a selection in the package explorer or a valid element has been selected in the
     * editor.
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection != null) {
            action.setEnabled(true);
            return;
        }
        IJavaElement javaElement = JavaEditorUtils.getCurrentSelectedJaveElement();
        if (JavaEditorUtils.isJavaElementSupported(javaElement) == true) {
            action.setEnabled(true);
            return;
        }
        action.setEnabled(false);
    }

    @Override
    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}
}
