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
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
                    this.processCompilationUnit((ICompilationUnit) obj);
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
    public void processCompilationUnit(ICompilationUnit cu) {
        this.sendMsgToMonitor(Messages.bind(Messages.task_output_imports_starting_orga_for_compilation_unit, cu.getResource().getName()));
        final CompilationUnit parsedCu = this.getParsedCU(cu);
        if (parsedCu == null) {
            return;
        }
        parsedCu.recordModifications();

        ImportsVisitor visitor = new ImportsVisitor(parsedCu, cu.getResource());
        parsedCu.accept(visitor);

        List<ASTNode> matches = visitor.getMatches();
        if (!matches.isEmpty()) {
            final IDocument doc = this.getDocumentFromCU(cu);
            if (doc == null) {
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

            // TODO add the markers where everything has been applied.
        }  // end if(!generatedMarkers.isEmpty())
    }

    /**
     * This is the correct way to create a document and modify the compilation unit. The other way using new
     * {@link Document} leaded to loss of all markers in a compilation unit.
     *
     * @param cu
     *            The compilation unit to get the document from.
     * @return An instance of the document to work with or <code>null</code> if something went wrong.
     */
    public IDocument getDocumentFromCU(ICompilationUnit cu) {
        try {
            return new DocumentAdapter(cu.getBuffer());
        }
        catch (JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(
                    Messages.bind(Messages.task_output_imports_error_create_doc_instance, cu.getResource()), e);
        }
        return null;
    }

    /**
     * Get a compilation unit from the interface to work with the AST parser later.
     *
     * @param cu
     *            The compilation unit interface to get the AST parsed version from.
     * @return A parsed compilation unit for the followed modifications or <code>null</code> if something went wrong.
     */
    protected CompilationUnit getParsedCU(ICompilationUnit cu) {
        ASTParser parser = ASTParser.newParser(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        try {
            cu.becomeWorkingCopy(this.getMonitor());
        }
        catch (JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(
                    Messages.bind(Messages.task_output_imports_error_create_workingcopy, cu.getResource()), e);
            return null;
        }
        parser.setSource(cu);

        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit parsedCu = (CompilationUnit) parser.createAST(this.getMonitor());
        return parsedCu;
    }

    /**
     * Applies the changes from the {@link ASTRewrite} instance to the given compilation unit.
     *
     * @param cu
     *            The compilation unit interface where parser has been started.
     * @param parsedCu
     *            The parsed CU from the interface, where the AST changes have been done.
     * @param doc
     *            The document from the CU interface.
     * @param rewrite
     *            All the modifications done with the parsed CU.
     * @see #getParsedCU(ICompilationUnit)
     */
    protected void applyChanges(ICompilationUnit cu, CompilationUnit parsedCu, final IDocument doc,
            ASTRewrite rewrite) {
        try {
            // parsedCu.rewrite(doc, null);
            TextEdit edit = rewrite.rewriteAST(doc, cu.getJavaProject().getOptions(true));
            cu.applyTextEdit(edit, this.monitor);

            cu.reconcile(AST.JLS8, false, null, this.monitor);
            cu.commitWorkingCopy(true, this.monitor);
        }
        catch (JavaModelException | IllegalArgumentException e) {
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
                Arrays.stream(pack.getCompilationUnits()).forEach(cu -> this.processCompilationUnit(cu));
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

    public IProgressMonitor getMonitor() {
        return this.monitor;
    }
}

