package com.sap.ims.isa.jcomigrationhelper.markers;

/**
 * The SAP supported marker types.
 * 
 * @author Iwan Zarembo, SAP SE
 *
 */
public enum MarkerTypes {

    MARKER("com.sap.ims.isa.jcomigrationhelper.marker"),     //$NON-NLS-1$

    MARKER_DONE("com.sap.ims.isa.jcomigrationhelper.marker.done");    //$NON-NLS-1$

    private String type;

    private MarkerTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
