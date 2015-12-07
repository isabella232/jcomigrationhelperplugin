package com.sap.ims.isa.jcomigrationhelper.popup.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaVariableOccurrence;
import com.sap.ims.isa.jcomigrationhelper.markers.JCoMarkerFactory;

@SuppressWarnings("restriction")
public class SwitchParametersAction implements IObjectActionDelegate {

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
    @SuppressWarnings("unchecked")
    @Override
    public void run(IAction action) {
        TreeSelection selection = JCoMarkerFactory.getTreeSelection();
        if (selection != null) {
            System.out.println("Here is my selection!");
            return;
        }
        boolean switchNotDone = true;
        JavaVariableOccurrence occurrences = JavaEditorUtils.getJavaVariableOccurrence(null);
        MultiTextEdit multiTextEdit = new MultiTextEdit();
        if(occurrences != null) {
            List<ASTNode> nodes = occurrences.getNodes();
            if (nodes.size() > 0) {
                AST ast = nodes.get(0).getRoot().getAST();
                for (ASTNode n : occurrences.getNodes()) {
                    // we only need the findings where a method of the variable is called
                    if (JavaEditorUtils.isValidForProcessing(n)) {

                        List<ASTNode> args = (List<ASTNode>) n.getParent().getStructuralProperty(MethodInvocation.ARGUMENTS_PROPERTY);
                        if(args.size() != 2) {
                            // only do the replacement if there is only one node, because then it is a call like getImportParameterList().setValue(...
                            if(occurrences.getNodes().size() != 1) {
                                continue;
                            }
                            args = (List<ASTNode>) n.getParent().getParent().getStructuralProperty(MethodInvocation.ARGUMENTS_PROPERTY);
                        }
                        ASTNode arg0 = args.get(0);
                        ASTNode arg1 = args.get(1);

                        try {

                            // do the replacement one after another
                            ASTRewrite rewriteParam0 = ASTRewrite.create(ast);
                            rewriteParam0.replace(arg0, arg1, null);
                            TextEdit textEdit0 = rewriteParam0.rewriteAST();

                            ASTRewrite rewriteParam1 = ASTRewrite.create(ast);
                            rewriteParam1.replace(arg1, arg0, null);
                            TextEdit textEdit1 = rewriteParam1.rewriteAST();

                            // multi edit is required, because we do two changes,
                            // otherwise the result is unpredictable.
                            multiTextEdit.addChild(textEdit0);
                            multiTextEdit.addChild(textEdit1);

                            // set the switch that at least one replacement worked.
                            switchNotDone = false;
                        }
                        catch (JavaModelException | IllegalArgumentException | MalformedTreeException e) {
                            JCoMigrationHelperPlugin.logErrorMessage(Messages.switch_operation_parameter_init_failed_log, e);
                            MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                                    Messages.switch_operation_msg_error_title, Messages.switch_operation_error_init_switch_failed);
                            break;
                        } // end catch
                    } // end if isValidForProcessing
                } // end loop
            } // end if(nodes.size() > 0)
        } // end if occurrences != null
        if(switchNotDone == true) {
            boolean answer = MessageDialog.openQuestion(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.switch_operation_nothing_changed_title, Messages.switch_operation_nothing_changed_content);
            if(answer == true) {
                JCoMarkerFactory.DELETE_MARKER_FOR_NODE.accept(occurrences.getNodes().get(0));
            }
        } else {
            try {
                multiTextEdit.apply(occurrences.getDoc());
                // delete the marker if available
                occurrences.getNodes().stream()
                // filter the stream to match only the ones with the variable declarations
                .filter(node -> node.getParent() instanceof VariableDeclarationFragment
                        || node.getParent() instanceof MethodInvocation
                        || node.getParent() instanceof SingleVariableDeclaration)
                // now remove all available markers for the filtered nodes
                .forEach(JCoMarkerFactory.DELETE_MARKER_FOR_NODE);
                try {
                    JCoMarkerFactory.getAnnotationModel(null).updateMarkers(occurrences.getDoc());
                }
                catch (CoreException e) {
                    JCoMigrationHelperPlugin.logWarningMessage(Messages.marker_warn_update_failed_log, e);
                    MessageDialog.openWarning(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                            Messages.marker_warn_update_title, Messages.marker_warn_update_failed);
                }
            } catch (BadLocationException e) {
                JCoMigrationHelperPlugin.logErrorMessage(Messages.switch_operation_error_apply_changes_log, e);
                MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                        Messages.switch_operation_msg_error_title, Messages.switch_operation_error_apply_changes);
            }
        }
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

    @Override
    public void selectionChanged(IAction action, ISelection selection) {

        IJavaElement javaElement = JavaEditorUtils.getCurrentSelectedJaveElement();
        if (JavaEditorUtils.isJavaElementSupported(javaElement) == true) {
            action.setEnabled(true);
            return;
        }
        action.setEnabled(false);
    }
}
