package com.sap.ims.isa.jcomigrationhelper.tests.helpers;

import static com.sap.ims.isa.jcomigrationhelper.tests.helpers.MarkerCheck.INITIAL_VALUE;
import static com.sap.ims.isa.jcomigrationhelper.tests.helpers.MarkerCheckMode.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class MarkerCheckTest {

    @Test
    public void markerCheckTest() {
        MarkerCheck check = new MarkerCheck();
        this.assertInternally(NONE, check.getCheckMode());

        check.setType("blub");
        this.assertInternally(TYPE_ONLY, check.getCheckMode());
        check.setCharStart(11);
        check.setCharEnd(12);
        this.assertInternally(TYPE_AND_RANGE, check.getCheckMode());
        check.setLineNumber(10);
        this.assertInternally(FULL, check.getCheckMode());
        check.setCharStart(INITIAL_VALUE);
        check.setCharEnd(INITIAL_VALUE);
        this.assertInternally(TYPE_AND_LINE, check.getCheckMode());
        check.setType(null);
        this.assertInternally(LINE_ONLY, check.getCheckMode());
        check.setCharStart(11);
        check.setCharEnd(12);
        this.assertInternally(RANGE_AND_LINE, check.getCheckMode());
        check.setLineNumber(INITIAL_VALUE);
        this.assertInternally(RANGE_ONLY, check.getCheckMode());
        check.setType("");
        check.setCharStart(INITIAL_VALUE);
        check.setCharEnd(INITIAL_VALUE);
        check.setLineNumber(10);
        this.assertInternally(LINE_ONLY, check.getCheckMode());
        check.setCharStart(11);
        check.setCharEnd(12);
        this.assertInternally(RANGE_AND_LINE, check.getCheckMode());
        check.setLineNumber(INITIAL_VALUE);
        this.assertInternally(RANGE_ONLY, check.getCheckMode());
    }

    private void assertInternally(MarkerCheckMode expectedMode, MarkerCheckMode actual) {
        assertEquals(String.format("Expected %s: %s%n", expectedMode, actual), expectedMode, actual);
    }
}
