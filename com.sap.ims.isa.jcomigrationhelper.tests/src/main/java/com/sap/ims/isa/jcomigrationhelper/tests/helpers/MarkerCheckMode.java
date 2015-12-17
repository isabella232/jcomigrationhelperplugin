package com.sap.ims.isa.jcomigrationhelper.tests.helpers;

/**
 * The mode for the {@link MarkerFilter}.
 *
 * @author Iwan Zarembo, SAP SE
 *
 */
enum MarkerCheckMode {
    FULL, TYPE_ONLY, RANGE_ONLY, LINE_ONLY, TYPE_AND_RANGE, TYPE_AND_LINE, RANGE_AND_LINE, NONE;
}