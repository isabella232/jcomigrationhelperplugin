package com.sap.ims.isa.jcomigrationhelper.tests.helpers;

import static com.sap.ims.isa.jcomigrationhelper.tests.helpers.MarkerCheckMode.*;

import java.io.Serializable;

/**
 * Used to filter the markers in {@link MarkerFilter}.
 *
 * @author Iwan Zarembo, SAP SE
 *
 */
public class MarkerCheck implements Serializable {

    /**
     *
     */
    private static final long  serialVersionUID = -2923418609730409409L;
    protected static final int INITIAL_VALUE    = -1;
    private String             type             = "";
    private int                charStart        = INITIAL_VALUE;
    private int                charEnd          = INITIAL_VALUE;
    private int                lineNumber       = INITIAL_VALUE;

    public MarkerCheck() {}

    public MarkerCheck(String type) {
        this(type, -1, -1, -1);
    }

    public MarkerCheck(String type, int charStart, int charEnd) {
        this(type, charStart, charEnd, -1);
    }

    public MarkerCheck(String type, int charStart, int charEnd, int lineNumber) {
        super();
        this.type = type;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.lineNumber = lineNumber;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCharStart() {
        return this.charStart;
    }

    public void setCharStart(int charStart) {
        this.charStart = charStart;
    }

    public int getCharEnd() {
        return this.charEnd;
    }

    public void setCharEnd(int charEnd) {
        this.charEnd = charEnd;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public MarkerCheckMode getCheckMode() {

        if (this.getType() != null && this.getType().length() > 0 && this.getCharStart() != INITIAL_VALUE
                && this.getCharEnd() != INITIAL_VALUE && this.getLineNumber() != INITIAL_VALUE) {
            return FULL;
        }

        if (this.getType() != null && this.getType().length() > 0 && this.getCharStart() == INITIAL_VALUE
                && this.getCharEnd() == INITIAL_VALUE && this.getLineNumber() == INITIAL_VALUE) {
            return TYPE_ONLY;
        }
        if (this.getType() != null && this.getType().length() > 0 && this.getCharStart() != INITIAL_VALUE
                && this.getCharEnd() != INITIAL_VALUE && this.getLineNumber() == INITIAL_VALUE) {
            return TYPE_AND_RANGE;
        }
        if (this.getType() != null && this.getType().length() > 0 && this.getCharStart() == INITIAL_VALUE
                && this.getCharEnd() == INITIAL_VALUE && this.getLineNumber() != INITIAL_VALUE) {
            return TYPE_AND_LINE;
        }

        if ((this.getType() == null || this.getType() != null && this.getType().length() == 0)
                && this.getCharStart() != INITIAL_VALUE && this.getCharEnd() != INITIAL_VALUE
                && this.getLineNumber() == INITIAL_VALUE) {
            return RANGE_ONLY;
        }

        if ((this.getType() == null || this.getType() != null && this.getType().length() == 0)
                && this.getCharStart() == INITIAL_VALUE && this.getCharEnd() == INITIAL_VALUE
                && this.getLineNumber() != INITIAL_VALUE) {
            return LINE_ONLY;
        }
        if ((this.getType() == null || this.getType() != null && this.getType().length() == 0)
                && this.getCharStart() != INITIAL_VALUE && this.getCharEnd() != INITIAL_VALUE
                && this.getLineNumber() != INITIAL_VALUE) {
            return RANGE_AND_LINE;
        }

        return MarkerCheckMode.NONE;
    }

    /**
     * Checks if all fields has no initial values, then it will return <code>true</code>.
     *
     * @return <code>true</code> if all fields are not initial.
     */
    public boolean isTypeOnlyCheck() {
        return this.getType() != null && this.getType().length() > 0 && this.getCharStart() != INITIAL_VALUE
                && this.getCharEnd() != INITIAL_VALUE && this.getLineNumber() != INITIAL_VALUE;
    }

    @Override
    public String toString() {
        return String.format("MarkerCheck [type=%s, charStart=%s, charEnd=%s, lineNumber=%s]", this.type,
                this.charStart, this.charEnd, this.lineNumber);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.charEnd;
        result = prime * result + this.charStart;
        result = prime * result + this.lineNumber;
        result = prime * result + (this.type == null ? 0 : this.type.hashCode());
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
        MarkerCheck other = (MarkerCheck) obj;
        if (this.charEnd != other.charEnd) {
            return false;
        }
        if (this.charStart != other.charStart) {
            return false;
        }
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public MarkerCheck clone() {
        return new MarkerCheck(this.getType(), this.charStart, this.charEnd, this.lineNumber);
    }
}
