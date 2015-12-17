package com.sap.ims.isa.jcomigrationhelper.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;
import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;

public class JCoMarkerFactory {

    /**
     * Replaces the {@link MarkerTypes#MARKER} with the {@link MarkerTypes#MARKER_DONE} for the given node. If no marker
     * is existing at the node, then the {@link MarkerTypes#MARKER_DONE} will be added. If the resource is
     * <code>null</code> then the one from the Java Editor will be used ( {@link JavaEditorUtils#getCurrentResource()}
     * ). The document is needed if the node does not have an existing marker yet and you want a line number to be added
     * to the marker.<br>
     * The parent of the node must be of type {@link ASTNode#VARIABLE_DECLARATION_FRAGMENT},
     * {@link ASTNode#SINGLE_VARIABLE_DECLARATION} or {@link ASTNode#METHOD_INVOCATION}.
     *
     * @param node
     *            The node to replace or add marker.
     * @param res
     *            The resource to work with.
     * @param doc
     *            The document where to add the marker.
     */
    public static void replaceMarkerOrAddMarker(ASTNode node, IResource res, IDocument doc) {
        // just to be sure
        int parentType = node.getParent().getNodeType();
        if(parentType == ASTNode.VARIABLE_DECLARATION_FRAGMENT
                || parentType == ASTNode.VARIABLE_DECLARATION_STATEMENT
                || parentType == ASTNode.METHOD_INVOCATION
                || parentType == ASTNode.METHOD_DECLARATION
                || parentType == ASTNode.FIELD_DECLARATION
                || parentType == ASTNode.SINGLE_VARIABLE_DECLARATION) {

            if(parentType == ASTNode.METHOD_INVOCATION) {
                // further checks required, only certain methods are supported!
                MethodInvocation m = (MethodInvocation) node.getParent();
                if(!JavaEditorUtils.isMethodSupported(m.getName().getFullyQualifiedName())) {
                    // the method is not supported and no marker operation is required!
                    return;
                }
            }

            if (parentType == ASTNode.VARIABLE_DECLARATION_STATEMENT || parentType == ASTNode.METHOD_DECLARATION) {
                node = ((VariableDeclaration) node).getName();
            }

            if (res == null) {
                res = JavaEditorUtils.getCurrentResource();
            }

            if (res == null) {
                return;
            }

            final ISourceRange selection = new SourceRange(node.getStartPosition(), node.getLength());
            List<IMarker> markersAtSelection = getMarkersAtSelection(res, selection);

            if (markersAtSelection.isEmpty()) {
                int lineNumber = -1;
                if (doc != null) {
                    lineNumber = getLineFromOffset(doc, selection.getOffset());
                }
                JCoMarkerFactory.createMarker(MarkerTypes.MARKER_DONE, res, selection, lineNumber);

            } else {
                markersAtSelection
                // delete all markers which are in the list
                .forEach(marker -> {
                    // create the new marker that the switch has been executed for this marker and delete the
                    // old one
                    int lineNumber = MarkerUtilities.getLineNumber(marker);
                    JCoMarkerFactory.deleteMarker(marker);
                    JCoMarkerFactory.createMarker(MarkerTypes.MARKER_DONE, marker.getResource(), selection,
                            lineNumber);
                });
            }
        }

    }
    /**
     * Creates a Marker of type {@link MarkerTypes#MARKER}. Calls the method
     * {@link #createMarker(MarkerTypes, IResource, ISourceRange, int, Map)} to create that type of marker.
     *
     * @param res
     *            the resource where to add the marker and annotation.
     * @param selection
     *            The selection to set the marker position, may be <code>null</code>.
     * @param documentLineNumber
     *            The line number where the selection is coming from.
     * @param documentLineNumber
     *            The line number in the document for the marker.
     * @param attrMap
     *            A map with optional attributes to set in the marker.
     * @return The created marker.
     * @throws CoreException
     *             In case of an error.
     * @see #createMarker(MarkerTypes, IResource, ISourceRange, int, Map)
     */
    public static IMarker createMarker(IResource res, ISourceRange selection, int documentLineNumber) {
        return createMarker(MarkerTypes.MARKER, res, selection, documentLineNumber);
    }

    /**
     * Creates a Marker of the given type with message and the line number of the selection. The line number is only set
     * if the line number is bigger than 0.<br>
     * There is also a check if there is already a marker existing which has the type {@link #MARKER} and the character
     * offset + character end are at the same values. Of course the check can only be performed if the selection
     * variable is not <code>null</code>. The attribute map is used to set all attributes (also the message). It means
     * the default marker message can be overwritten with this map.
     *
     * @param res
     *            the resource where to add the marker and annotation.
     * @param selection
     *            The selection to set the marker position, may be <code>null</code>.
     * @param documentLineNumber
     *            The line number where the selection is coming from.
     * @param documentLineNumber
     *            The line number in the document for the marker.
     * @param attrMap
     *            A map with optional attributes to set in the marker.
     * @return The created marker.
     * @throws CoreException
     *             In case of an error.
     */
    public static IMarker createMarker(MarkerTypes markerType, IResource res, ISourceRange selection,
            int documentLineNumber) {
        IMarker marker = null;
        try {
            // do not create markers at the same position multiple times
            if (selection != null) {
                List<IMarker> markersAtSelection = getMarkersAtSelection(res, selection);
                if (!markersAtSelection.isEmpty()) {
                    return markersAtSelection.get(0);
                }
            }
            // note: you use the id that is defined in your plugin.xml
            marker = res.createMarker(markerType.getType());
            String msg = Messages.marker_title;
            if (markerType == MarkerTypes.MARKER_DONE) {
                msg = Messages.marker_done_title;
            }
            marker.setAttribute(IMarker.MESSAGE, msg);

            if (selection != null) {
                MarkerUtilities.setCharStart(marker, selection.getOffset());
                MarkerUtilities.setCharEnd(marker, selection.getOffset() + selection.getLength());
            }
            if (documentLineNumber > 0) {
                MarkerUtilities.setLineNumber(marker, documentLineNumber);
            }
        }
        catch (CoreException e) {
            JCoMigrationHelperPlugin.logErrorMessage(Messages.markers_error_createmarkers_msg_title, e);
            MessageDialog.openInformation(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.markers_error_createmarkers_msg_title, Messages.markers_error_createmarkers_msg_content);
        }
        return marker;
    }

    /**
     * Searches for all markers of type {@link MarkerTypes#MARKER} and its subtypes and checks if the marker range
     * is the same as in the selection. If yes, then only matching markers will be returned otherwise an empty list.
     * {@link CoreException}s will be swallowed and logged.
     *
     * @param res The resource to search for markers.
     * @param selection The selection which we are interested in.
     * @return A list of matching markers or an empty list.
     */
    public static List<IMarker> getMarkersAtSelection(IResource res, ISourceRange selection) {
        IMarker[] markers;
        try {
            markers = res.findMarkers(MarkerTypes.MARKER.getType(), true, IResource.DEPTH_ONE);
            if (markers != null) {
                return Arrays.stream(markers)
                        .filter(m -> MarkerUtilities.getCharStart(m) == selection.getOffset()
                        && MarkerUtilities.getCharEnd(m) == selection.getOffset() + selection.getLength())
                        .collect(Collectors.toList());
            }
        }
        catch (CoreException e) {
            JCoMigrationHelperPlugin
            .logWarningMessage(Messages.bind(Messages.markers_error_finding_markers, res.getFullPath()), e);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the current test selection from the editor.
     *
     * @return The current test selection from the editor.
     */
    public static TextSelection getTextSelection() {

        ISelection selection = JCoMigrationHelperPlugin.getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (selection instanceof TextSelection) {
            return (TextSelection) selection;
        }
        return null;
    }

    /*
     * returns a list of a resources markers
     */
    public static List<IMarker> findMarkers(IResource resource) {
        return findMarkers(MarkerTypes.MARKER, resource);
    }

    public static List<IMarker> findMarkers(MarkerTypes marker, IResource resource) {
        try {
            return Arrays.asList(resource.findMarkers(marker.getType(), true, IResource.DEPTH_ZERO));
        }
        catch (CoreException e) {
            return new ArrayList<IMarker>();
        }
    }

    /**
     * Returns a list of markers from type {@link MarkerTypes#MARKER} that are linked to the resource or any sub
     * resource of the resource.
     *
     * @param resource
     *            The resource to find the marker inside.
     * @return A list of markers in this resource.
     * @see #findAllMarkers(MarkerTypes, IResource)
     */
    public static List<IMarker> findAllMarkers(IResource resource) {
        return findAllMarkers(MarkerTypes.MARKER, resource);
    }

    /**
     * Returns a list of markers from type {@link MarkerTypes#MARKER} that are linked to the resource or any sub
     * resource of the resource.
     *
     * @param marker
     *            The marker type to search for.
     * @param resource
     *            The resource to find the marker inside.
     *
     * @return A list of markers in this resource.
     */
    public static List<IMarker> findAllMarkers(MarkerTypes marker, IResource resource) {
        try {
            return Arrays.asList(resource.findMarkers(marker.getType(), true, IResource.DEPTH_INFINITE));
        }
        catch (CoreException e) {
            return new ArrayList<IMarker>();
        }
    }

    /**
     * Returns the {@link TreeSelection} of the package explorer or <code>null</code> if none could be found.
     */
    public static TreeSelection getTreeSelection() {

        ISelection selection = JCoMigrationHelperPlugin.getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (selection instanceof TreeSelection) {
            return (TreeSelection) selection;
        }
        return null;
    }

    /**
     * Get the <code>AbstractMarkerAnnotationModel</code> from the editor.
     *
     * @param editor
     *            The editor from where to get the document from, may be null, then
     *            {@link JCoMigrationHelperPlugin#getEditor()} will be used.
     * @return The marker annotation model or <code>null</code> if there is none
     */
    public static AbstractMarkerAnnotationModel getAnnotationModel(ITextEditor editor) {
        if (editor == null) {
            editor = JCoMigrationHelperPlugin.getEditor();
        }
        if(editor != null) {
            IDocumentProvider provider = editor.getDocumentProvider();
            IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
            if (model instanceof AbstractMarkerAnnotationModel) {
                return (AbstractMarkerAnnotationModel) model;
            }
        }
        return null;
    }

    /**
     * Get the current <code>IDocument</code> from the editor.
     *
     * @param editor
     *            The editor from where to get the document from, may be null, then
     *            {@link JCoMigrationHelperPlugin#getEditor()} will be used.
     * @return The current document.
     */
    public static IDocument getCurrentDocument(ITextEditor editor) {
        if (editor == null) {
            editor = JCoMigrationHelperPlugin.getEditor();
        }
        return editor.getDocumentProvider().getDocument(editor.getEditorInput());
    }

    /**
     * Deletes a marker and hows an error message if it fails.
     *
     * @param m
     *            The marker to be deleted.
     */
    public static void deleteMarker(IMarker m) {

        try {
            m.delete();
        }
        catch (CoreException e1) {
            JCoMigrationHelperPlugin.logWarningMessage(Messages.markers_warning_marker_not_removed);
            MessageDialog.openInformation(JCoMigrationHelperPlugin.getActiveWorkbenchShell(),
                    Messages.markers_error_removingmarkers_msg_title, Messages.markers_warning_marker_not_removed);
        }
    }

    /**
     * Converts the offset from a document to the line number. A log message is written in case of an internal exception
     * and <code>-1</code> is returned in that case.
     *
     * @param offset
     *            The offset to calculate the line number from.
     * @param editor
     *            The editor (which can be <code>null</code>) to be used.
     * @return The line number or <code>-1</code> in case of an internal error.
     *
     */
    public static int getLineFromOffset(int offset, ITextEditor editor) {
        return getLineFromOffset(getCurrentDocument(editor), offset);
    }

    /**
     * Converts the offset from a document to the line number. A log message is written in case of an internal exception
     * and <code>-1</code> is returned in that case.
     *
     * @param doc
     *            The document (which can be <code>null</code>) to be used.
     * @param offset
     *            The offset to calculate the line number from.
     * @return The line number or <code>-1</code> in case of an internal error.
     * @see #getLineFromOffset(int, ITextEditor)
     */
    public static int getLineFromOffset(IDocument doc, int offset) {
        if (doc == null) {
            doc = getCurrentDocument(null);
        }
        try {
            // +1 because the calculation ends with the line before.
            return doc.getLineOfOffset(offset) + 1;
        }
        catch (BadLocationException e) {
            JCoMigrationHelperPlugin.logWarningMessage(Messages.marker_warn_docline_not_retrieved, e);
        }
        return -1;
    }

    /**
     * Updates the marker.
     * @param marker The marker to be updated.
     * @param document The document the marker is inside.
     * @param position The new position of the marker.
     * @return <code>true</code> is everything could be updated, otherwise <code>false</code>.
     */
    public static boolean updateMarker(IMarker marker, IDocument document, Position position) {
        int start = position.getOffset();
        int end = position.getOffset() + position.getLength();
        MarkerUtilities.setCharStart(marker, start);
        MarkerUtilities.setCharEnd(marker, end);
        int line = getLineFromOffset(document, start);
        if (line > 0) {
            MarkerUtilities.setLineNumber(marker, line);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Generates the markers for the given compilation unit.
     * @param cu The compilation unit to generate markers at.
     */
    public static void generateMarker(ICompilationUnit cu) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(cu);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
        astRoot.accept(new MarkerGeneratorVisitor(astRoot, cu.getResource()));
    }

    /**
     * A silent method to return the value for the given attribute. Silent means that any error with the marker will be
     * shallowed, but logged.
     *
     * @param marker
     *            The marker from where to read the attribute.
     * @param attributeName
     *            The attribute name to read from the marker.
     * @return The object from the marker or <code>null</code> if it is not existing or an error occurred.
     */
    public static Object getMarkerAttribute(IMarker marker, String attributeName) {
        try {
            return marker.getAttribute(attributeName);
        }
        catch (CoreException e) {
            JCoMigrationHelperPlugin.logErrorMessage(
                    "An error occurred reading the attribute " + attributeName + " from the marker. Returning null.",
                    e);
        }
        return null;
    }

}
