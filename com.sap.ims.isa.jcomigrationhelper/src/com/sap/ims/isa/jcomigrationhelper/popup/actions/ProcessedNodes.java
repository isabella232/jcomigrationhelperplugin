package com.sap.ims.isa.jcomigrationhelper.popup.actions;

import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Used to find nodes after switching the parameters.
 *
 * @author Iwan Zarembo, SAP SE
 *
 */
public class ProcessedNodes {

    private String      bindingKey;
    private SourceRange range;

    public ProcessedNodes(SourceRange range) {
        super();
        this.range = range;
    }

    public ProcessedNodes(ASTNode node) {
        super();
        this.range = new SourceRange(node.getStartPosition(), node.getLength());
    }

    public ProcessedNodes(String bindingKey) {
        super();
        this.bindingKey = bindingKey;
    }

    public String getBindingKey() {
        return this.bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public SourceRange getRange() {
        return this.range;
    }

    public void setRange(SourceRange range) {
        this.range = range;
    }

    public boolean isBinding() {
        return this.bindingKey != null;
    }

    @Override
    public String toString() {
        return String.format("ProcessedNodes [bindingKey=%s, range=%s]", this.bindingKey, this.range);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.bindingKey == null ? 0 : this.bindingKey.hashCode());
        result = prime * result + (this.range == null ? 0 : this.range.hashCode());
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
        ProcessedNodes other = (ProcessedNodes) obj;
        if (this.bindingKey == null) {
            if (other.bindingKey != null) {
                return false;
            }
        } else if (!this.bindingKey.equals(other.bindingKey)) {
            return false;
        }
        if (this.range == null) {
            if (other.range != null) {
                return false;
            }
        } else if (!this.range.equals(other.range)) {
            return false;
        }
        return true;
    }

}
