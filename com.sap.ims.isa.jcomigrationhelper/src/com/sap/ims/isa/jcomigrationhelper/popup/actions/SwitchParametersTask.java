package com.sap.ims.isa.jcomigrationhelper.popup.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.imports.ImportOrganizerTask;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaVariableOccurrence;
import com.sap.ims.isa.jcomigrationhelper.markers.JCoMarkerFactory;
import com.sap.ims.isa.jcomigrationhelper.markers.MarkerTypes;

/**
 * The only difference is how the compilation unit is changed.
 *
 * @author Iwan Zarembo, SAP SE
 *
 */
public class SwitchParametersTask extends ImportOrganizerTask {

    public SwitchParametersTask(TreeSelection selection) {
        super(selection);
    }

    @Override
    public void processCompilationUnit(ICompilationUnit cu) {
        this.sendMsgToMonitor(
                Messages.bind(Messages.task_output_switching_params_for_compilation_unit,
                        cu.getResource().getName()));

        List<IMarker> markers = JCoMarkerFactory.findAllMarkers(cu.getResource()).stream()
                .filter(m -> {
                    return MarkerTypes.MARKER.getType().equals(MarkerUtilities.getMarkerType(m));
                })
                .collect(Collectors.toList());
        if (markers.isEmpty()) {
            return;
        }
        final CompilationUnit parsedCu = this.getParsedCU(cu);
        if (parsedCu == null) {
            return;
        }
        parsedCu.recordModifications();

        final IDocument doc = this.getDocumentFromCU(cu);
        if (doc == null) {
            return;
        }

        AST ast = parsedCu.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        List<String> nodes = new ArrayList<>();
        markers.forEach(m -> {
            ASTNode selNode = NodeFinder.perform(parsedCu, MarkerUtilities.getCharStart(m), 1);
            nodes.add(((SimpleName) selNode).resolveBinding().getKey());
            JavaVariableOccurrence occ = null;
            // methods are handeled differently
            if (selNode.getParent().getNodeType() == ASTNode.METHOD_INVOCATION) {
                occ = new JavaVariableOccurrence(doc, new ASTNode[] { selNode });
            } else {
                ASTNode[] sameNodes = LinkedNodeFinder.findByNode(parsedCu, (SimpleName) selNode);
                occ = new JavaVariableOccurrence(doc, sameNodes);
            }
            this.switchParameters(occ, rewrite, true);
        });

        // this.applyInEditor(doc, rewrite);
        this.applyChanges(cu, parsedCu, doc, rewrite);

        // reparse the unit again
        final CompilationUnit reParsedCu = this.getParsedCU(cu);
        if (reParsedCu == null) {
            return;
        }
        final IDocument docNew = this.getDocumentFromCU(cu);
        if (docNew == null) {
            return;
        }

        nodes.forEach(node -> {

            ASTNode astNode = reParsedCu.findDeclaringNode(node);
            if (astNode == null) {
                return;
            }

            JCoMarkerFactory.replaceMarkerOrAddMarker(astNode, cu.getResource(), docNew);
        });
        // // update the markers after processing the switch
        // parsedCu.accept(new MarkerGeneratorVisitor(reParsedCu, cu.getResource(), MarkerTypes.MARKER_DONE));
    }

    /**
     * Takes the selection from the editor and tries to switch the parameters.
     */
    @SuppressWarnings("unchecked")
    public ASTRewrite switchParameters(JavaVariableOccurrence occurrences, ASTRewrite rewrite, boolean quite) {
        boolean switchNotDone = true;
        ASTRewrite internalRewrite = rewrite;
        if (occurrences != null) {
            List<ASTNode> nodes = occurrences.getNodes();
            if (nodes.size() > 0) {
                if (rewrite == null) {
                    AST ast = nodes.get(0).getRoot().getAST();
                    internalRewrite = ASTRewrite.create(ast);
                }
                for (ASTNode n : occurrences.getNodes()) {
                    // we only need the findings where a method of the variable is called
                    if (JavaEditorUtils.isValidForProcessing(n)) {

                        List<ASTNode> args = (List<ASTNode>) n.getParent()
                                .getStructuralProperty(MethodInvocation.ARGUMENTS_PROPERTY);
                        if (args.size() != 2) {
                            // only do the replacement if there is only one node, because then it is a call like
                            // getImportParameterList().setValue(...
                            if (occurrences.getNodes().size() != 1) {
                                continue;
                            }
                            args = (List<ASTNode>) n.getParent().getParent()
                                    .getStructuralProperty(MethodInvocation.ARGUMENTS_PROPERTY);
                        }
                        ASTNode arg0 = args.get(0);
                        ASTNode arg1 = args.get(1);

                        try {

                            ASTNode moveArg0 = internalRewrite.createMoveTarget(arg0);
                            ASTNode moveArg1 = internalRewrite.createMoveTarget(arg1);

                            // do the replacement one after another
                            internalRewrite.replace(arg0, moveArg1, null);
                            internalRewrite.replace(arg1, moveArg0, null);
                            // set the switch that at least one replacement worked.
                            switchNotDone = false;
                        }
                        catch (IllegalArgumentException | MalformedTreeException e) {
                            JCoMigrationHelperPlugin
                            .logErrorMessage(Messages.switch_operation_parameter_init_failed_log, e);
                            MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                                    Messages.switch_operation_msg_error_title,
                                    Messages.switch_operation_error_init_switch_failed);
                            break;
                        } // end catch
                    } // end if isValidForProcessing
                } // end loop
            } // end if(nodes.size() > 0)
        } // end if occurrences != null
        if (switchNotDone == true && quite == false) {
            boolean answer = MessageDialog.openQuestion(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.switch_operation_nothing_changed_title, Messages.switch_operation_nothing_changed_content);
            if (answer == true && occurrences.getNodes() != null) {
                JCoMarkerFactory.replaceMarkerOrAddMarker(occurrences.getNodes().get(0), null, occurrences.getDoc());
            }
        }
        return internalRewrite;
    }

    /**
     * this method should be used if the switch has been executed in the editor. An error will be shown in case an error
     * occurred applying the changes.
     *
     * @param doc
     *            The document from the editor.
     * @param internalRewrite
     *            The rewrite object how the resource shall be changed.
     */
    public void applyInEditor(IDocument doc, ASTRewrite internalRewrite) {
        try {
            if (internalRewrite != null) {
                TextEdit undoEdit = internalRewrite.rewriteAST(doc, null);
                undoEdit.apply(doc);
            }
        }
        catch (BadLocationException e) {
            JCoMigrationHelperPlugin.logErrorMessage(Messages.switch_operation_error_apply_changes_log, e);
            MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.switch_operation_msg_error_title, Messages.switch_operation_error_apply_changes);
        }
    }

    /**
     * If will search for available markers at the given nodes and replace them with the {@link MarkerTypes#MARKER_DONE}
     * .
     *
     * @param doc
     *            The document where the markers shall be updated.
     * @param nodes
     *            The nodes with the markers.
     */
    public void updateProcessedMarkers(IDocument doc, List<ASTNode> nodes) {
        // delete the marker if available
        nodes.stream()
        // now remove all available markers for the filtered nodes
        .forEach(n -> JCoMarkerFactory.replaceMarkerOrAddMarker(n, null, doc));
        try {
            JCoMarkerFactory.getAnnotationModel(null).updateMarkers(doc);
        }
        catch (CoreException e) {
            JCoMigrationHelperPlugin.logWarningMessage(Messages.marker_warn_update_failed_log, e);
            MessageDialog.openWarning(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.marker_warn_update_title, Messages.marker_warn_update_failed);
        }
    }
}
