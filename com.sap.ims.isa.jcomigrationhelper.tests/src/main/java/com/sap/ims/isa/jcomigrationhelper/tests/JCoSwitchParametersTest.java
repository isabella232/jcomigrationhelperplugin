package com.sap.ims.isa.jcomigrationhelper.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.ims.isa.jcomigrationhelper.markers.JCoMarkerFactory;
import com.sap.ims.isa.jcomigrationhelper.markers.MarkerGeneratorTask;
import com.sap.ims.isa.jcomigrationhelper.markers.MarkerTypes;
import com.sap.ims.isa.jcomigrationhelper.popup.actions.SwitchParametersTask;
import com.sap.ims.isa.jcomigrationhelper.tests.helpers.MarkerCheck;
import com.sap.ims.isa.jcomigrationhelper.tests.helpers.MarkerFilter;

public class JCoSwitchParametersTest extends TestsSuiteParent {

    /**
     * The sub folder within the resource path for the resources of this test class.
     */
    private String           swtichTests = "switch_tests";

    /**
     * The package to be used for the switch tests.
     */
    private IPackageFragment switchPackage;

    /**
     * The tested file contains a variable which is not using any set-value methods. So no markers must be generated
     * after processing the compilation unit. Used file is JCoMixsetValuesUnused.java.resource
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForUnusedVariable() throws Exception {
        String testClassName = "JCoMixsetValuesUnused.java";
        List<IMarker> markers = this.generateMarkersForClass(testClassName);
        Assert.assertEquals("The test expected NO markers!!!!", 0, markers.size());

    }

    /**
     * The tested file contains a method variable which is not using any set-value methods. So no markers must be
     * generated after processing the compilation unit. Used file is JCoMixsetValuesMethodUnused.java.resource
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForMethodUnused() throws Exception {
        String testClassName = "JCoMixsetValuesMethodUnused.java";
        List<IMarker> markers = this.generateMarkersForClass(testClassName);
        Assert.assertEquals("The test expected NO markers!!!!", 0, markers.size());

    }

    /**
     * The tested file contains a field which is not using any set-value methods. So no markers must be generated after
     * processing the compilation unit. Used file is JCoMixsetValuesFieldUnused.java.resource
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForUnusedField() throws Exception {
        String testClassName = "JCoMixsetValuesFieldUnused.java";
        List<IMarker> markers = this.generateMarkersForClass(testClassName);
        Assert.assertEquals("The test expected NO markers!!!!", 0, markers.size());

    }

    /**
     * The tested file contains three fields which are using any set-value methods. So 3 markers must be generated after
     * processing the compilation unit with the {@link MarkerGeneratorTask} and then the content must look like the reference
     * content from file JCoMixsetValues.java.resources.result and it must contain 3 markers of the {@link MarkerTypes#MARKER_DONE}.
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForVariables() throws Exception {
        String testClassName = "JCoMixsetValues.java";
        MarkerFilter markerFilter = new MarkerFilter();
        markerFilter.addMarckerCheck(
                new MarkerCheck(MarkerTypes.MARKER.getType(), 562, 574, 18),
                new MarkerCheck(MarkerTypes.MARKER.getType(), 814, 827, 26),
                new MarkerCheck(MarkerTypes.MARKER.getType(), 1041, 1053, 32),
                new MarkerCheck(MarkerTypes.MARKER.getType(), 1264, 1286, 38)
                );
        this.generateMarkersAndSwitchParameters(testClassName, markerFilter);
    }

    /**
     * The tested file contains three fields which are using any set-value methods. So 3 markers must be generated after
     * processing the compilation unit with the {@link MarkerGeneratorTask} and then the content must look like the
     * reference content from file JCoMixsetValuesField.java.resources.result and it must contain 3 markers of the
     * {@link MarkerTypes#MARKER_DONE}.
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForField() throws Exception {
        // settings for the class
        String testClassName = "JCoMixsetValuesField.java";
        MarkerFilter markerFilter = new MarkerFilter();
        markerFilter.addMarckerCheck(new MarkerCheck(MarkerTypes.MARKER.getType(), 285, 297, 10),
                new MarkerCheck(MarkerTypes.MARKER.getType(), 343, 358, 11));
        this.generateMarkersAndSwitchParameters(testClassName, markerFilter);
    }

    /**
     * The tested file contains three fields which are using any set-value methods. So 3 markers must be generated after
     * processing the compilation unit with the {@link MarkerGeneratorTask} and then the content must look like the
     * reference content from file JCoMixsetValuesField.java.resources.result and it must contain 3 markers of the
     * {@link MarkerTypes#MARKER_DONE}.
     *
     * @throws Exception
     *             In case of any error.
     */
    @Test
    public void generateMarkerForMethodParam() throws Exception {
        // settings for the class
        String testClassName = "JCoMixsetValuesMethod.java";
        MarkerFilter markerFilter = new MarkerFilter();
        markerFilter.addMarckerCheck(new MarkerCheck(MarkerTypes.MARKER.getType(), 198, 210, 7),
                new MarkerCheck(MarkerTypes.MARKER.getType(), 543, 561, 17));
        this.generateMarkersAndSwitchParameters(testClassName, markerFilter);
    }

    /**
     * Generates the markers for the given class ( {@link #generateMarkersForClass(String)} ) and checks if the number
     * of markers is the same as the filter is required later. If this was successful, then the parameters are switched
     * and it is checked if all new markers have been set correctly.
     *
     * @param testClassName
     *            The class to test.
     * @param markerFilter
     *            The filter to check if everything was generated properly.
     * @throws Exception
     *             In case of any error.
     */
    protected void generateMarkersAndSwitchParameters(String testClassName, MarkerFilter markerFilter)
            throws Exception {
        List<IMarker> markers = this.generateMarkersForClass(testClassName);
        Assert.assertEquals("The test expected " + markerFilter.getCheckList().size() + " markers!!!!",
                markerFilter.getCheckList().size(), markers.size());

        // filter for the markers which we expect in the source code
        List<IMarker> filteredMarkers = markers.stream()
                .filter(markerFilter)
                .collect(Collectors.toList());
        Assert.assertEquals("The markers were generated on the wrong location or type!!!!", 0, filteredMarkers.size());

        ICompilationUnit cu = this.switchPackage.getCompilationUnit(testClassName);

        SwitchParametersTask task = new SwitchParametersTask(null);
        task.setMonitor(npeMonitor);
        task.processCompilationUnit(cu);

        String migResult = this.readContentFromTestResource(this.swtichTests, testClassName + RESOURCE_RESULT_POSTFIX);
        this.assertEquals(migResult, cu.getSource());

        markerFilter.changeMarkerCheckType(MarkerTypes.MARKER_DONE.getType());

        filteredMarkers = JCoMarkerFactory.findAllMarkers(cu.getResource()).stream()
                .filter(markerFilter)
                .collect(Collectors.toList());
        Assert.assertEquals("The markers were generated on the wrong location!!!!", 0, filteredMarkers.size());
    }

    /**
     * Generates markers for the given testClassName which will be read from the resource folder.<br>
     * The method does internally:
     * <ul>
     * <li>Reads the resource with the {@link #getTestResource(String...)} + {@link #swtichTests} + classname +
     * ".resource"</li>
     * <li>Create a class in the {@link #switchPackage} as compilation unit</li>
     * <li>Process the created compilation unit using the {@link SwitchParametersTask}</li>
     * <li>Finds all markers for the process compilation unit and returns the list</li>
     *
     * @param testClassName
     *            The name of the resource to read.
     * @return The list of markers processed using the {@link SwitchParametersTask}.
     * @throws Exception
     *             In case of any error.
     */
    protected List<IMarker> generateMarkersForClass(String testClassName) throws Exception {
        java.nio.file.Path unusedVarFilePath = this.getTestResource(this.swtichTests, testClassName + ".resource");

        String testClassSrc = new String(Files.readAllBytes(unusedVarFilePath), StandardCharsets.UTF_8);

        ICompilationUnit cu = this.switchPackage.createCompilationUnit(testClassName, testClassSrc, false, npeMonitor);

        MarkerGeneratorTask task = new MarkerGeneratorTask(null);
        task.setMonitor(npeMonitor);
        task.generateMarkerForCompilationUnit(cu);

        return JCoMarkerFactory.findAllMarkers(cu.getResource());
    }

    @Before
    public void setUp() throws Exception {
        if (this.srcFolder == null) {
            this.createProject("migrate_imports_project");
        }
        if (this.switchPackage == null) {
            this.switchPackage = this.srcFolder.createPackageFragment("com.sap.ims.isa.tests.jcomigration.switching",
                    false, npeMonitor);
        }
    }
}
