package com.sap.ims.isa.jcomigrationhelper.markers;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;

public class CreateJCoMarkers implements IObjectActionDelegate {

    protected ISourceRange selectedElementRange;
    protected IResource selectedResource;

    public CreateJCoMarkers() {
        super();
    }

    @Override
    public void run(IAction paramIAction) {
        JCoMarkerFactory.createMarker(this.selectedResource, this.selectedElementRange,
                JCoMarkerFactory.getLineFromOffset(this.selectedElementRange.getOffset(), null));
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(!this.isMarkerAlreadyAvailable());
    }


    /**
     * Checks if a marker in the current line is already available or not.
     * @return <code>true</code> if a marker is already set, otherwise <code>false</code>.
     */
    protected boolean isMarkerAlreadyAvailable() {
        IJavaElement javaElement = JavaEditorUtils.getCurrentSelectedJaveElement();
        if (JavaEditorUtils.isJavaElementSupported(javaElement)) {
            if(javaElement instanceof ILocalVariable) {
                this.selectedResource = javaElement.getResource();
                this.selectedElementRange = ((ILocalVariable) javaElement).getNameRange();
            } else if(javaElement instanceof IMethod){ // can only be a method, because isJavaElementSupported was called before
                // the selection range for such elements is a bit tricky
                this.selectedElementRange = JavaEditorUtils.getCurrentLocation((IMethod) javaElement);
                if(this.selectedElementRange.getOffset() == -1) {
                    return false;
                }
                this.selectedResource = JavaEditorUtils.getCurrentResource();
            } else if(javaElement instanceof IField) {
                this.selectedResource = javaElement.getResource();
                try {
                    this.selectedElementRange = ((IField) javaElement).getNameRange();
                }
                catch (JavaModelException e) {
                    // the type is not supported then
                    JCoMigrationHelperPlugin.logWarningMessage(Messages.bind(Messages.warn_type_not_determineable, javaElement.getElementName()), e);
                }
            }

            if(this.selectedResource != null) {
                List<IMarker> markers = JCoMarkerFactory.findMarkers(this.selectedResource);
                AbstractMarkerAnnotationModel aModel = JCoMarkerFactory.getAnnotationModel(null);
                if(aModel != null) {
                    // check if a marker is really available in the selection
                    boolean hasAlreadyAMarker = markers.stream().anyMatch(m -> {
                        Position markerPosition = aModel.getMarkerPosition(m);
                        try {
                            return markerPosition != null
                                    && this.selectedElementRange.getOffset() == markerPosition.getOffset()
                                    && m.exists()
                                    && m.getAttribute(IMarker.USER_EDITABLE, true)
                                    && m.getType().equals(MarkerTypes.MARKER);
                        }
                        catch (CoreException e) {
                            // can be ignored, then the marker will not be deleted.
                        }
                        return false;
                    });
                    return hasAlreadyAMarker;
                }
            }
        }
        return false;
    }

    public void setActiveEditor(IAction paramIAction, IEditorPart paramIEditorPart) {}

    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {}

}
