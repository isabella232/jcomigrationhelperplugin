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

    private IDocument     doc;
    private List<ASTNode> nodes;

    /**
     * The instance of this object must only be created by {@link JavaEditorUtils}.
     * @param doc The document where the search was performed.
     * @param nodes The found nodes.
     */
    JavaVariableOccurrence(IDocument doc, List<ASTNode> nodes) {
        super();
        this.doc = doc;
        this.nodes = nodes;
    }

    /**
     * The instance of this object must only be created by {@link JavaEditorUtils}.
     * @param doc The document where the search was performed.
     * @param nodes The found nodes.
     */
    JavaVariableOccurrence(IDocument doc, ASTNode[] nodes) {
        super();
        this.doc = doc;
        this.nodes = Arrays.asList(nodes);
    }

    public IDocument getDoc() {
        return doc;
    }

    public List<ASTNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "JavaVariableOccurrence [doc=" + doc + ", nodes=" + nodes + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((doc == null) ? 0 : doc.hashCode());
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JavaVariableOccurrence other = (JavaVariableOccurrence) obj;
        if (doc == null) {
            if (other.doc != null)
                return false;
        } else if (!doc.equals(other.doc))
            return false;
        if (nodes == null) {
            if (other.nodes != null)
                return false;
        } else if (!nodes.equals(other.nodes))
            return false;
        return true;
    }
}
