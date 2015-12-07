package com.sap.ims.isa.jcomigrationhelper;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JCoMigrationHelperPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String           PLUGIN_ID = "com.sap.ims.isa.jcomigrationhelper"; //$NON-NLS-1$

    // The shared instance
    private static JCoMigrationHelperPlugin plugin;

    /**
     * The constructor
     */
    public JCoMigrationHelperPlugin() {}

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JCoMigrationHelperPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        Shell shell = null;
        if (window != null) {
            shell = window.getShell();
        }
        if (shell == null) {
            shell = Display.getDefault().getActiveShell();
        }
        return null;
    }

    /**
     * Returns the current editor from the workbench if it is an instance of {@link ITextEditor}.
     *
     * @return The current editor from the workbench, or <code>null</code> if it was called when no editor or not supported editor is open.
     */
    public static ITextEditor getEditor() {
        IEditorPart editor = Optional.ofNullable(getActiveWorkbenchWindow()).map(wb -> wb.getActivePage())
                .map(ap -> ap.getActiveEditor()).orElse(null);
        return editor instanceof ITextEditor ? (ITextEditor) editor : null;
    }

    public static String getPluginId() {
        return PLUGIN_ID;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void logErrorMessage(String message) {
        logErrorMessage(message, null);
    }

    public static void logErrorMessage(String message, Throwable t) {
        log(new Status(IStatus.ERROR, getPluginId(), message, t));
    }

    public static void logWarningMessage(String message) {
        logErrorMessage(message, null);
    }

    public static void logWarningMessage(String message, Throwable t) {
        log(new Status(IStatus.WARNING, getPluginId(), message, t));
    }

    /**
     * Shows an error message in the UI Thread. The method should be used by background processes.
     * 
     * @param title
     *            The title of the error message.
     * @param message
     *            The message of the error.
     */
    public static void showErrorMessage(final String title, final String message) {
        Display.getDefault().syncExec(() -> {
            Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            MessageDialog.openError(activeShell, title, message);
        });
    }
}
