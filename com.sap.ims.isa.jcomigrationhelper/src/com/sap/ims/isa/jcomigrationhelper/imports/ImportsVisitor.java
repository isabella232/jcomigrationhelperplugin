package com.sap.ims.isa.jcomigrationhelper.imports;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.sap.ims.isa.jcomigrationhelper.internal.utils.JavaEditorUtils;

public class ImportsVisitor extends ASTVisitor {

    /**
     * The generated markers will have this additional attribute <b>type</b>.
     */
    public static String MARKER_IMPORT_TYPE_ATTRIBUTE = "type";

    /**
     * The generated markers will have this additional attribute {@link #MARKER_IMPORT_TYPE_ATTRIBUTE} and the value
     * from this variable.
     */
    public static String MARKER_IMPORT_TYPE_ATTRIBUTE_VALUE = "import";

    /**
     * The document the visitor is working with. It is mostly used to get the line of the document.
     */
    private CompilationUnit document;

    /**
     * The resource where to add the marker. I was not able to get this information from the cumpilation unit.
     */
    private IResource res;

    private List<ASTNode>   matches                            = new LinkedList<>();

    /**
     * The visitor requires for its work the document and the resource. If the values are not set, then a
     * {@link NullPointerException} will be thrown!
     * @param document the document the AST has been created on.
     * @param res The resource which was used to generate the AST.
     * @throws NullPointerException If one of the arguments is null.
     */
    public ImportsVisitor(CompilationUnit document, IResource res) {
        Objects.requireNonNull(document, "The document must not be null!");
        Objects.requireNonNull(res, "The resource must not be null!");
        this.document = document;
        this.res = res;
    }

    /**
     * Is called to determine if the VariableDeclarationFragment should be visited at all or not
     */
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        Type type = node.getType();
        if(type instanceof SimpleType) {
            String varType = ((SimpleType) type).getName().getFullyQualifiedName();
            if(JavaEditorUtils.isMigratableJCoType(varType)) {
                @SuppressWarnings("rawtypes")
                java.util.List structuralProperty = (java.util.List) node.getStructuralProperty(VariableDeclarationStatement.FRAGMENTS_PROPERTY);
                if(structuralProperty.size() > 0 && structuralProperty.get(0) instanceof VariableDeclarationFragment) {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Will be called if the visit of {@link VariableDeclarationStatement} or of the {@link FieldDeclaration} returned <code>true</code>.
     * @return Always <code>false</code>, because no further visits are required.
     */
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        boolean isSupported = false;
        Type type = null;

        if (node.getParent() instanceof FieldDeclaration) {
            type = ((FieldDeclaration)node.getParent()).getType();
        } else if(node.getParent() instanceof VariableDeclarationStatement) {
            type = ((VariableDeclarationStatement)node.getParent()).getType();
        }
        if(type != null && type.isSimpleType()) {
            isSupported = JavaEditorUtils
                    .isMigratableJCoType(((SimpleType) type).getName().getFullyQualifiedName());
        }

        if(isSupported) {
            this.matches.add(type);
        }

        return true;
    }

    /**
     * Marks also variables which ware method parameters.
     */
    @Override
    public boolean visit(MethodDeclaration node) {
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = node.parameters();
        if(parameters.size() > 0) {
            parameters.forEach(param -> {
                Type type = param.getType();
                this.checkAndAddType(type);
            });
        }
        Type returnType = node.getReturnType2();
        this.checkAndAddType(returnType);

        @SuppressWarnings("unchecked")
        List<Type> thrownTypes = node.thrownExceptionTypes();
        thrownTypes.forEach(type -> this.checkAndAddType(type));

        return true;
    }

    /**
     * Checks it the type is supported for the migration and if yes adds it to the list for further processing.
     *
     * @param type
     *            The type to check.
     */
    protected void checkAndAddType(Type type) {
        if (type != null && type.isSimpleType()
                && JavaEditorUtils.isMigratableJCoType(((SimpleType) type).getName().getFullyQualifiedName())) {
            this.matches.add(type);
        }
    }

    @Override
    public boolean visit(ImportDeclaration node) {
        String impName = node.getName().getFullyQualifiedName();
        if(JavaEditorUtils.isImportForMigration(impName)) {
            this.matches.add(node);
        }
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        Type type = node.getException().getType();
        if (type.isUnionType()) {
            UnionType uType = (UnionType) type;
            @SuppressWarnings("unchecked")
            List<Type> types = uType.types();
            types.forEach(t -> this.checkAndAddType(t));
        } else if (type.isSimpleType()) {
            this.checkAndAddType(type);
        }
        return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        this.checkAndAddType(node.getType());
        return true;
    }

    @Override
    public boolean visit(CastExpression node) {
        this.checkAndAddType(node.getType());
        return true;
    }

    public List<ASTNode> getMatches() {
        return this.matches;
    }

    @Override
    public String toString() {
        return String.format("ImportsVisitor [document=%s, res=%s, generatedMarkers=%s]", this.document, this.res,
                this.matches);
    };

}
