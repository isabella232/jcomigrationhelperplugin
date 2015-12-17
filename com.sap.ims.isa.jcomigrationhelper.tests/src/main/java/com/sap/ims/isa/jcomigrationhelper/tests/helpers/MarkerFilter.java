package com.sap.ims.isa.jcomigrationhelper.tests.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.junit.Assert;

public class MarkerFilter implements Predicate<IMarker> {

    private List<MarkerCheck> checkList = new ArrayList<>(0);

    public MarkerFilter() {
    }

    public MarkerFilter(List<MarkerCheck> checkList) {
        super();
        this.checkList = checkList;
    }

    /**
     * Returns <code>false</code> if the items matches otherwise <code>true</code>-
     */
    @Override
    public boolean test(IMarker m) {
        boolean toReturn = this.checkList.stream().anyMatch(c -> {
            boolean internalReturn = false;
            switch (c.getCheckMode()) {
                case FULL:
                    internalReturn = MarkerUtilities.getMarkerType(m).equals(c.getType())
                            && MarkerUtilities.getLineNumber(m) == c.getLineNumber()
                            && MarkerUtilities.getCharStart(m) == c.getCharStart()
                            && MarkerUtilities.getCharEnd(m) == c.getCharEnd();
                    break;
                case TYPE_ONLY:
                    internalReturn = MarkerUtilities.getMarkerType(m).equals(c.getType());
                    break;
                case LINE_ONLY:
                    internalReturn = MarkerUtilities.getLineNumber(m) == c.getLineNumber();
                    break;
                case RANGE_ONLY:
                    internalReturn = MarkerUtilities.getCharStart(m) == c.getCharStart()
                            && MarkerUtilities.getCharEnd(m) == c.getCharEnd();
                    break;
                case TYPE_AND_LINE:
                    internalReturn = MarkerUtilities.getMarkerType(m).equals(c.getType())
                            && MarkerUtilities.getLineNumber(m) == c.getLineNumber();
                    break;
                case TYPE_AND_RANGE:
                    internalReturn = MarkerUtilities.getMarkerType(m).equals(c.getType())
                            && MarkerUtilities.getCharStart(m) == c.getCharStart()
                            && MarkerUtilities.getCharEnd(m) == c.getCharEnd();
                    break;
                case RANGE_AND_LINE:
                    internalReturn = MarkerUtilities.getLineNumber(m) == c.getLineNumber()
                            && MarkerUtilities.getCharStart(m) == c.getCharStart()
                            && MarkerUtilities.getCharEnd(m) == c.getCharEnd();
                    break;
                default:
                    // do nothing
                    break;
            }
            return internalReturn;
        });
        return !toReturn;
    }

    /**
     * Adds a check to the list of checks to be executed. The check must <b>not</b> be <code>null</code>!
     *
     * @param check
     *            The check(s) to be executed.
     */
    public void addMarckerCheck(MarkerCheck... check) {
        Assert.assertNotNull(check);
        this.getCheckList().addAll(Arrays.asList(check));
    }

    /**
     * Changes the types in the {@link #checkList}.
     *
     * @param type
     *            The new type to use.
     */
    public void changeMarkerCheckType(String type) {
        this.checkList.stream().forEach(c -> c.setType(type));
    }

    /**
     * Returns a modifiable list of checks.
     *
     * @return A modifiable list of checks.
     */
    public List<MarkerCheck> getCheckList() {
        return this.checkList;
    }

    /**
     * Sets the check list which must NOT be <code>null</code>!
     *
     * @param checkList
     *            The check list to use internally.
     */
    public void setCheckList(List<MarkerCheck> checkList) {
        Assert.assertNotNull(checkList);
        this.checkList = checkList;
    }

    @Override
    public MarkerFilter clone() {
        List<MarkerCheck> newList = new ArrayList<>(this.checkList.size());
        this.checkList.forEach(c -> newList.add(c.clone()));
        return new MarkerFilter(newList);
    }

    @Override
    public String toString() {
        return String.format("MarkerFilter [checkList=%s]", this.checkList);
    }

}
