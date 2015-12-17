package com.sap.ims.isa.jcomigrationhelper.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.junit.Before;
import org.junit.Test;

import com.sap.ims.isa.jcomigrationhelper.imports.ImportOrganizerTask;

public class JCoCodeMigrationTest extends TestsSuiteParent {

    /**
     * The sub folder within the resource path for the resources of this test class.
     */
    private String migration = "migration";

    /**
     * Tests the migration of the imports and variables.
     *
     * @throws Exception
     *             In case of an error.
     */
    @Test
    public void jcoCodeMigrationForCompilationUnit() throws Exception {

        String usedResourceName = "OldJCoAllInOne.java";

        IPackageFragment pkg = this.srcFolder.createPackageFragment("com.sap.ims.isa.tests.jcomigration.imports", false,
                null);
        java.nio.file.Path migrationSourceFilePath = this.getTestResource(this.migration,
                usedResourceName + RESOURCE_POSTFIX);
        String migSrc = new String(Files.readAllBytes(migrationSourceFilePath), StandardCharsets.UTF_8);

        ICompilationUnit cu = pkg.createCompilationUnit(usedResourceName, migSrc, false, npeMonitor);

        ImportOrganizerTask task = new ImportOrganizerTask(null);
        task.setMonitor(npeMonitor);
        task.processCompilationUnit(cu);

        String migResult = this.readContentFromTestResource(this.migration, usedResourceName + RESOURCE_RESULT_POSTFIX);

        this.assertEquals(migResult, cu.getSource());
    }

    @Before
    public void setUp() throws Exception {
        if (this.srcFolder == null) {
            this.createProject("migrate_imports_project");
        }
    }

}