package com.sap.ims.isa.jcomigrationhelper.internal.utils;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sap.ims.isa.jcomigrationhelper.JCoMigrationHelperPlugin;
import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;

/**
 * A central class to work with the JavaEditor and the JavaPlugin.
 *
 * @author d041773
 *
 */
@SuppressWarnings("restriction")
public class JavaEditorUtils {

    private JavaEditorUtils() {}

    /**
     * In case a location could not be found, then this object is returned.
     */
    private static ISourceRange UNKNOWN_RANGE = new SourceRange(-1, 0);

    /**
     * Checks if the given java element is an instance of {@link ILocalVariable} and it is supported by the plugin
     * itself. Currently supported variables:
     * <ul>
     * <li>JCoParameterList</li>
     * <li>JCoTable</li>
     * <li>JCoStructure</li>
     * <li>JCoRecord</li>
     * </ul>
     *
     * @param element
     *            The element to check against.
     *
     * @return <code>true</code> if the local variable type is supported, otherwise <code>false</code>.
     */
    public static boolean isJavaElementSupported(IJavaElement element) {
        if (element == null) {
            return false;
        }
        String varType = null;
        if (element instanceof ILocalVariable) {
            ILocalVariable localVar = (ILocalVariable) element;
            varType = localVar.getTypeSignature();
        } else if (element instanceof IMethod) {
            IMethod method = (IMethod) element;
            String methodName = method.getElementName();
            if (isMethodSupported(methodName) && method.getParent().getElementType() == IJavaElement.TYPE) {
                return true;
            }
        } else if (element instanceof IField) {
            try {
                varType = ((IField) element).getTypeSignature();
            }
            catch (JavaModelException e) {
                // the type is not supported then
                JCoMigrationHelperPlugin.logWarningMessage(Messages.bind(Messages.warn_type_not_determineable, element.getElementName()), e);
            }
        } else {
            // unknown type, not supported!
            return false;
        }

        switch (varType) {
            case "JCoParameterList": //$NON-NLS-1$
            case "QJCoParameterList;": //$NON-NLS-1$
            case "QJCoTable;": //$NON-NLS-1$
            case "QJCoStructure;": //$NON-NLS-1$
            case "QJCoRecord;": //$NON-NLS-1$
                return true;

            default:
                break;
        }
        return false;
    }

    /**
     * Checks if the used JCo Type is supported by this plugin or not. Currently supported JCo Types are:
     * <ul>
     * <li>JCoTable</li>
     * <li>JCoStructure</li>
     * <li>JCoParameterList</li>
     * <li>JCoRecord</li>
     * <li>getStructure</li>
     * </ul>
     *
     * @param jcoType
     *            The JCo type to check against.
     * @return <code>true</code> if the method is supported, otherwise <code>false</code>.
     */
    public static boolean isJCoTypeSupported(String jcoType) {
        switch (jcoType) {
            case "JCoTable": //$NON-NLS-1$
            case "com.sap.conn.jco.JCoTable": //$NON-NLS-1$
            case "JCoStructure": //$NON-NLS-1$
            case "com.sap.conn.jco.JCoStructure": //$NON-NLS-1$
            case "JCoParameterList": //$NON-NLS-1$
            case "com.sap.conn.jco.JCoParameterList": //$NON-NLS-1$
            case "JCoRecord": //$NON-NLS-1$
            case "com.sap.conn.jco.JCoRecord": //$NON-NLS-1$
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the used method is supported by this plugin or not. Currently supported methods are:
     * <ul>
     * <li>getImportParameterList</li>
     * <li>getStructure</li>
     * </ul>
     *
     * @param methodName
     *            The name of the method to check against.
     * @return <code>true</code> if the method is supported, otherwise <code>false</code>.
     */
    public static boolean isMethodSupported(String methodName) {
        switch (methodName) {
            case "getImportParameterList": //$NON-NLS-1$
            case "getStructure": //$NON-NLS-1$
            case "getTable": //$NON-NLS-1$
                return true;
            default:
                return false;
        }
    }

    /**
     * Takes the current selection from the workbench and tries to retrieve the selected java element. It will only work
     * if the used editor is a {@link JavaEditor}.
     *
     * @return The currently selected java element or <code>null</code>.
     */
    public static IJavaElement getCurrentSelectedJaveElement() {

        ITextEditor currEditor = JCoMigrationHelperPlugin.getEditor();
        if (currEditor != null && currEditor instanceof JavaEditor) {

            IJavaElement[] javaElements;
            try {
                javaElements = SelectionConverter.codeResolve((JavaEditor) currEditor);
                if (javaElements.length > 0) {
                    return javaElements[0];
                }
            }
            catch (JavaModelException e) {
                JCoMigrationHelperPlugin.logWarningMessage(Messages.bind(Messages.warn_no_javaelement_found,
                        currEditor.getTitle()));
            }
        }
        return null;
    }

    /**
     * Tries to find all occurrences of the selected variable in the given editor or the current editor, if the editor
     * parameter is <code>null</code>.
     *
     * @param editor
     *            The editor where to search for the variable, it may be <code>null</code>, then the current open editor
     *            will be used.
     * @return The bean with all found occurrences for the selected java element or <code>null</code> if it was not
     *         determinable.
     */
    public static JavaVariableOccurrence getJavaVariableOccurrence(JavaEditor editor) {

        if (editor == null) {
            ITextEditor curEditor = JCoMigrationHelperPlugin.getEditor();
            if (curEditor != null && curEditor instanceof JavaEditor) {
                editor = (JavaEditor) curEditor;
            } else {
                return null;
            }
        }

        CompilationUnit root = SharedASTProvider.getAST(EditorUtility.getEditorInputJavaElement(editor, false),
                SharedASTProvider.WAIT_YES, null);
        ISourceViewer viewer = editor.getViewer();
        IDocument document = viewer.getDocument();
        Point originalSelection = viewer.getSelectedRange();

        ASTNode selectedNode = NodeFinder.perform(root, originalSelection.x, originalSelection.y);

        if (!(selectedNode instanceof SimpleName)) {
            return null; // Only simple names are supported.
        }
        SimpleName nameNode = (SimpleName) selectedNode;

        IJavaElement currentSelectedJaveElement = getCurrentSelectedJaveElement();
        // methods are handled differently
        if(currentSelectedJaveElement instanceof IMethod) {
            // return only the one selected occurrence
            return new JavaVariableOccurrence(document, new ASTNode[]{nameNode});
        }

        ASTNode[] sameNodes = LinkedNodeFinder.findByNode(root, nameNode);

        Arrays.sort(sameNodes, (n1, n2) -> n1.getStartPosition() - n2.getStartPosition());
        return new JavaVariableOccurrence(document, sameNodes);
    }

    /**
     * The central check if the node is value to be processed by the plugin. The check is quite simple:
     * <ul>
     * <li>Is the parent of the node from type method invocation? Which means is the variable used to call a method.</li>
     * <li>Is the node used to call a method from this variable? It is not valid if it is used when calling a variable</li>
     * <li>Has the method which is called exactly two parameters?</li>
     * <li>Does the method which is called start with <b>set</b>?</li>
     * </ul>
     * Then the node is valid to be processed by this plugin.
     *
     * @param node
     *            The node to be checked if the it is valid to be processed.
     * @return <code>true</code> if all checks were successful, otherwise <code>false</code> is returned.
     */
    public static boolean isValidForProcessing(ASTNode node) {

        if (node.getParent().getNodeType() == ASTNode.METHOD_INVOCATION) {
            MethodInvocation parent = (MethodInvocation) node.getParent();
            // only setter methods required.
            boolean isValid = parent.getExpression() != null
                    && parent.getExpression().equals(node)
                    && parent.resolveMethodBinding().getParameterTypes().length == 2
                    && parent.getName().getFullyQualifiedName().startsWith("set"); //$NON-NLS-1$
            if(isValid) {
                return true;
            }
            if(parent.getParent() instanceof MethodInvocation) {
                MethodInvocation parentParent = (MethodInvocation) parent.getParent();
                return parentParent.getExpression() != null
                        && parentParent.getExpression().equals(parent)
                        && parentParent.resolveMethodBinding().getParameterTypes().length == 2
                        && parentParent.getName().getFullyQualifiedName().startsWith("set"); //$NON-NLS-1$
            }
        }
        return false;
    }

    /**
     * Determines the location of the current selected element.
     *
     * @return The {@link ISourceRange} of the current selected element or {@link #UNKNOWN_RANGE} if the location was
     *         not determinable.
     */
    public static ISourceRange getCurrentLocation(IMethod method) {
        try {
            ISourceRange range = method.getSourceRange();
            if(range.getOffset() != -1) {
                return range;
            }
        }
        catch (JavaModelException e) {
            // not nice, but might happen, try the other method.
        }

        JavaEditor editor = null;
        ITextEditor curEditor = JCoMigrationHelperPlugin.getEditor();
        if (curEditor != null && curEditor instanceof JavaEditor) {
            editor = (JavaEditor) curEditor;
        } else {
            return UNKNOWN_RANGE;
        }
        Point originalSelection = editor.getViewer().getSelectedRange();

        ASTNode selectedNode = NodeFinder.perform(SharedASTProvider.getAST(
                EditorUtility.getEditorInputJavaElement(editor, false), org.eclipse.jdt.ui.SharedASTProvider.WAIT_YES,
                null), originalSelection.x, originalSelection.y);
        if (selectedNode != null) {
            return new SourceRange(selectedNode.getStartPosition(), selectedNode.getLength());
        }

        return UNKNOWN_RANGE;
    }

    /**
     * Determines the current used resource in the editor.
     * @return the current used resource in the editor.
     * @see EditorUtility#getActiveEditorJavaInput()
     */
    public static IResource getCurrentResource() {
        return EditorUtility.getActiveEditorJavaInput().getResource();
    }

    /**
     * Takes the current editor and tries to get the compilation unit from it. It returns <code>null</code>
     * if the current editor is not an instance of {@link JavaEditor} and the editor does not have an opened
     * {@link ICompilationUnit}.
     * @return The current compilation unit or <code>null</code>.
     */
    public static ICompilationUnit getCurrentCompilationUnit() {
        ITextEditor editor = JCoMigrationHelperPlugin.getEditor();
        if(editor instanceof JavaEditor) {
            Object input = ((JavaEditor) editor).getViewPartInput();
            if(input instanceof ICompilationUnit) {
                return (ICompilationUnit) input;
            }
        }
        return null;
    }

    /**
     * Checks if the imports needs to be migrated to JCo 3 or not. It is usually checking if the import starts with <code>com.sap.mw.jco</code>.
     * @param importName The import to check.
     * @return <code>true</code> if the import is a migration candidate, otherwise false.
     */
    public static boolean isImportForMigration(String importName) {
        return importName != null && importName.startsWith("com.sap.mw.jco");
    }

    /**
     * Checks if the used Type is a candidate to be migrated or not. The check is if the type starts with
     * <code>JCO.</code> or with <code>com.sap.mw.jco.JCO.</code>. In that case you will need to change the
     * type by changing <code>JCO.</code> into <code>JCo</code>.
     *
     * @param jcoType
     *            The type to check if it needs to be migrated.
     * @return <code>true</code> if the type shall be migrated, otherwise <code>false</code>.
     */
    public static boolean isMigratableJCoType(String jcoType) {
        if(jcoType == null) {
            return false;
        }

        if(jcoType.startsWith("JCO.") || jcoType.startsWith("com.sap.mw.jco.JCO.")) {
            return true;
        }
        return false;
    }

    public static String getMappedType(String migrateableType) {
        if (migrateableType == null) {
            throw new IllegalArgumentException();
        }

        return migrateableType.replace("com.sap.mw.jco.", "com.sap.conn.jco.").replace("JCO.", "JCo");
    }
}
