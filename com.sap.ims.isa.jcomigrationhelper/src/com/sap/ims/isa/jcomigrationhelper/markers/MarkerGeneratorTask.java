package com.sap.ims.isa.jcomigrationhelper.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeSelection;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.markers.helpers.UserCancelRequestException;

public class MarkerGeneratorTask implements IRunnableWithProgress {

    private TreeSelection selection;
    private IProgressMonitor monitor;

    public MarkerGeneratorTask(TreeSelection selection) {
        this.selection = selection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        if(this.selection == null) {
            monitor.done();
        }
        this.monitor = monitor;
        monitor.beginTask(Messages.task_output_starting_generation, IProgressMonitor.UNKNOWN);
        try {
            this.selection.iterator().forEachRemaining(obj -> {

                if (obj instanceof IJavaProject) {
                    IJavaProject project = (IJavaProject) obj;
                    monitor.subTask(Messages.bind(Messages.task_output_starting_generation_for_project, project.getProject().getName()));
                    try {
                        IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
                        // check if the user clicked on cancel
                        if(monitor.isCanceled()) {
                            return;
                        }
                        Arrays.stream(roots).forEach(r -> this.generateMarkerForPackageRoot(r));
                    }
                    catch (JavaModelException e) {
                        JCoMigrationHelperPlugin.logErrorMessage(Messages.markers_error_getting_src_folders_log, e);
                        MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                                Messages.markers_error_title, Messages.markers_error_getting_src_folders);
                    }
                } else if (obj instanceof ICompilationUnit) {
                    this.generateMarkerForCompilationUnit((ICompilationUnit) obj);

                } else if (obj instanceof IPackageFragment) {
                    this.generateMarkerForPackage((IPackageFragment) obj);

                } else if(obj instanceof IPackageFragmentRoot) {
                    this.generateMarkerForPackageRoot((IPackageFragmentRoot) obj);
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
    public void generateMarkerForCompilationUnit(ICompilationUnit cu) {
        this.sendMsgToMonitor(Messages.bind(Messages.task_output_starting_generation_for_compilation_unit, cu.getResource().getName()));
        JCoMarkerFactory.generateMarker(cu);
    }

    /**
     * Loops over all compilation units and generates the markers for it. It is using {@link #APPLY_TO_CU} for the loop.
     */
    public void generateMarkerForPackage(IPackageFragment pack) {
        try {
            if(pack.containsJavaResources()) {
                this.sendMsgToMonitor(Messages.bind(Messages.task_output_starting_generation_for_package, pack.getElementName()));
                Arrays.stream(pack.getCompilationUnits()).forEach(cu -> this.generateMarkerForCompilationUnit(cu));
            }
        } catch(JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(Messages.bind(Messages.markers_error_getting_cus_log, pack.getElementName()), e);
            MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.markers_error_title, Messages.bind(Messages.markers_error_getting_cus, pack.getElementName()));
        }
    };

    /**
     * Loops over all sub packages and calls {@link #APPLY_TO_FRAGMENTS} to generate the markers.
     */
    public void generateMarkerForPackageRoot(IPackageFragmentRoot root) {
        if(root.isArchive()) {
            return;
        }
        try {
            this.sendMsgToMonitor(Messages.bind(Messages.task_output_starting_generation_for_package, root.getElementName()));
            Arrays.stream(root.getChildren()).map(je -> (IPackageFragment) je)
                    .forEach(p -> this.generateMarkerForPackage(p));
        } catch(JavaModelException e) {
            JCoMigrationHelperPlugin.logErrorMessage(Messages.bind(Messages.markers_error_getting_packages_log, root.getElementName()), e);
            MessageDialog.openError(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.markers_error_title, Messages.bind(Messages.markers_error_getting_packages, root.getElementName()));
        }
    };
}
