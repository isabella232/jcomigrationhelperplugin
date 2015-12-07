package com.sap.ims.isa.jcomigrationhelper.imports;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.text.edits.TextEdit;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;
import com.sap.ims.isa.jcomigrationhelper.markers.helpers.UserCancelRequestException;

public class ImportOrganizerTask implements IRunnableWithProgress {

    private TreeSelection selection;
    private IProgressMonitor monitor;

    public ImportOrganizerTask(TreeSelection selection) {
        this.selection = selection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        if(this.selection == null) {
            monitor.done();
        }
        this.monitor = monitor;
        monitor.beginTask(Messages.task_output_imports_starting_orga, IProgressMonitor.UNKNOWN);
        try {
            this.selection.iterator().forEachRemaining(obj -> {

                if (obj instanceof IJavaProject) {
                    IJavaProject project = (IJavaProject) obj;
                    monitor.subTask(Messages.bind(Messages.task_output_imports_starting_orga_for_project, project.getProject().getName()));
                    try {
                        IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
                        // check if the user clicked on cancel
                        if(monitor.isCanceled()) {
                            return;
                        }
                        Arrays.stream(roots).forEach(r -> this.processFragmentRoots(r));
                    }
                    catch (JavaModelException e) {
                        JCoMigrationHelperPlugin.logErrorMessage(Messages.markers_error_getting_src_folders_log, e);
                        MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                                Messages.imports_error_title, Messages.markers_error_getting_src_folders);
                    }
                } else if (obj instanceof ICompilationUnit) {
                    this.processCompulationUnit((ICompilationUnit) obj);
                } else if (obj instanceof IPackageFragment) {
                    this.processFragments((IPackageFragment) obj);

                } else if(obj instanceof IPackageFragmentRoot) {
                    this.processFragmentRoots((IPackageFragmentRoot) obj);
                }
            });
        }catch (UserCancelRequestException e) {
            // user cancelled the generation, so stop everything.
        }
        monitor.done();
    }

    /**
     * Checks if the current task has been already cancelled and if not, then it will show the current subtask in the monitor.
     *
     * @param msg The message to display at the monitor.
     * @throws InterruptedException Thrown when user hits the cancel button.
     */
    public void sendMsgToMonitor(String msg) throws UserCancelRequestException {
        if(this.monitor != null && this.monitor.isCanceled()) {
            throw new UserCancelRequestException(Messages.task_output_cancel_requested);
        }
        if(this.monitor != null) {
            this.monitor.subTask(msg);
        }
    }

    /**
     * Generates the markers in the given compilation unit.
     */
    public void processCompulationUnit(ICompilationUnit cu) {
        this.sendMsgToMonitor(Messages.bind(Messages.task_output_imports_starting_orga_for_compilation_unit, cu.getResource().getName()));
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        try {
            cu.becomeWorkingCopy(this.monitor);
        }
        catch (JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(
                    Messages.bind(Messages.task_output_imports_error_create_workingcopy, cu.getResource()), e);
            return;
        }
        parser.setSource(cu);
        // parser.setBindingsRecovery(true);
        // parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit parsedCu = (CompilationUnit) parser.createAST(null);
        parsedCu.recordModifications();

        ImportsVisitor visitor = new ImportsVisitor(parsedCu, cu.getResource());
        parsedCu.accept(visitor);

        List<ASTNode> matches = visitor.getMatches();
        if (!matches.isEmpty()) {
            final Document doc;
            try {
                doc = new Document(cu.getSource());
            }
            catch (JavaModelException e) {
                JCoMigrationHelperPlugin.logErrorMessage(
                        Messages
                        .bind(Messages.task_output_imports_error_create_doc_instance, cu.getResource()), e);
                return;
            }

            AST ast = parsedCu.getAST();
            ASTRewrite rewrite = ASTRewrite.create(ast);
            for (ASTNode selectedNode : matches) {

                if (selectedNode instanceof ImportDeclaration) {
                    // it means it is a import declaration
                    ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
                    newImportDeclaration.setName(ast.newName(new String[] { "com", "sap", "conn", "jco" }));
                    newImportDeclaration.setOnDemand(true);
                    newImportDeclaration.setFlags(selectedNode.getFlags());

                    rewrite.replace(selectedNode, newImportDeclaration, null);
                } else if (selectedNode instanceof SimpleType) {
                    SimpleType type = (SimpleType) selectedNode;

                    String typeName = type.getName().getFullyQualifiedName();
                    typeName = JavaEditorUtils.getMappedType(typeName);
                    SimpleType newType = ast.newSimpleType(ast.newName(typeName));
                    newType.setFlags(type.getFlags());

                    rewrite.replace(type, newType, null);
                }

            }  // end for (ASTNode selectedNode : matches)
            this.applyChanges(cu, parsedCu, doc, rewrite);
        }  // end if(!generatedMarkers.isEmpty())
    }

    protected void applyChanges(ICompilationUnit cu, CompilationUnit parsedCu, final Document doc, ASTRewrite rewrite) {
        try {
            parsedCu.rewrite(doc, null);
            TextEdit edit = rewrite.rewriteAST(doc, cu.getJavaProject().getOptions(true));
            edit.apply(doc);

            cu.getBuffer().setContents(doc.get());
            cu.reconcile(ICompilationUnit.NO_AST, false, null, this.monitor);
            cu.commitWorkingCopy(true, this.monitor);
        }
        catch (BadLocationException | JavaModelException | IllegalArgumentException e) {
            JCoMigrationHelperPlugin.logWarningMessage(Messages.marker_warn_update_failed_log, e);
            JCoMigrationHelperPlugin.showErrorMessage(
                    Messages.marker_warn_update_title, Messages.marker_warn_update_failed);
            return;
        }
    }

    /**
     * Loops over all compilation units and generates the markers for it. It is using {@link #APPLY_TO_CU} for the loop.
     */
    public void processFragments(IPackageFragment pack) {
        try {
            if(pack.containsJavaResources()) {
                this.sendMsgToMonitor(Messages.bind(Messages.task_output_imports_starting_orga_for_package, pack.getElementName()));
                Arrays.stream(pack.getCompilationUnits()).forEach(cu -> this.processCompulationUnit(cu));
            }
        } catch(JavaModelException e) {
            JCoMigrationHelperPlugin
            .logErrorMessage(Messages.bind(Messages.markers_error_getting_cus_log, pack.getElementName()), e);
            JCoMigrationHelperPlugin.showErrorMessage(
                    Messages.imports_error_title, Messages.bind(Messages.markers_error_getting_cus, pack.getElementName()));
        }
    }

    /**
     * Loops over all sub packages and calls {@link #APPLY_TO_FRAGMENTS} to generate the markers.
     */
    public void processFragmentRoots(IPackageFragmentRoot root) {
        if(root.isArchive()) {
            return;
        }
        try {
            this.sendMsgToMonitor(Messages.bind(Messages.task_output_imports_starting_orga_for_package, root.getElementName()));

            Arrays.stream(root.getChildren()).map(je -> (IPackageFragment) je).forEach(pf -> this.processFragments(pf));
        } catch(JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(
                    Messages.bind(Messages.markers_error_getting_packages_log, root.getElementName()), e);
            JCoMigrationHelperPlugin.showErrorMessage(
                    Messages.imports_error_title, Messages.bind(Messages.markers_error_getting_packages, root.getElementName()));
        }
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
}

