package com.sap.ims.isa.jcomigrationhelper.internal.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.IDocument;

/**
 * A bean which contains a the list of occurrences of a selected local variable. You can also perform document
 * operations with the values inside.<br>
 * The instance of this object must only be created by {@link JavaEditorUtils}.
 *
 * @author Iwan Zarembo
 *
 */
public class JavaVariableOccurrence {

    /**
     * The node for the calculated occurrence.
     */
    private ASTNode       rootNode;
    private IDocument     doc;
    private List<ASTNode> nodes;

    /**
     * The instance of this object must only be created by {@link JavaEditorUtils}.
     * @param doc The document where the search was performed.
     * @param nodes The found nodes.
     */
    public JavaVariableOccurrence(IDocument doc, List<ASTNode> nodes) {
        super();
        this.doc = doc;
        this.nodes = nodes;
    }

    /**
     * The instance of this object must only be created by {@link JavaEditorUtils}.
     * @param doc The document where the search was performed.
     * @param nodes The found nodes.
     */
    public JavaVariableOccurrence(IDocument doc, ASTNode[] nodes) {
        super();
        this.doc = doc;
        this.nodes = Arrays.asList(nodes);
    }

    public IDocument getDoc() {
        return this.doc;
    }

    public List<ASTNode> getNodes() {
        return this.nodes;
    }

    @Override
    public String toString() {
        return "JavaVariableOccurrence [doc=" + this.doc + ", nodes=" + this.nodes + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.doc == null ? 0 : this.doc.hashCode());
        result = prime * result + (this.nodes == null ? 0 : this.nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        JavaVariableOccurrence other = (JavaVariableOccurrence) obj;
        if (this.doc == null) {
            if (other.doc != null) {
                return false;
            }
        } else if (!this.doc.equals(other.doc)) {
            return false;
        }
        if (this.nodes == null) {
            if (other.nodes != null) {
                return false;
            }
        } else if (!this.nodes.equals(other.nodes)) {
            return false;
        }
        return true;
    }
}
