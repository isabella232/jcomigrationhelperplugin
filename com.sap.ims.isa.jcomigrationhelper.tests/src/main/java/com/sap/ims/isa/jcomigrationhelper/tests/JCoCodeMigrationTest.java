package com.sap.ims.isa.jcomigrationhelper.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.ims.isa.jcomigrationhelper.imports.ImportOrganizerTask;

public class JCoCodeMigrationTest extends TestsSuiteParent {


    @Test
    public void jcoCodeMigrationForCompilationUnit() throws Exception {

        IPackageFragment pkg = this.srcFolder.createPackageFragment("com.sap.ims.isa.tests.jcomigration.imports", false,
                null);
        java.nio.file.Path migrationSourceFilePath = this.resourcesPath.resolve("OldJCoAllInOne.java.resource");
        if (!Files.exists(migrationSourceFilePath)) {
            Assert.fail("The class to be migrated is missing at " + migrationSourceFilePath.toAbsolutePath().normalize() + " !!!!!!");
        }
        String migSrc = new String(Files.readAllBytes(migrationSourceFilePath), StandardCharsets.UTF_8);

        ICompilationUnit cu= pkg.createCompilationUnit("OldJCoAllInOne.java", migSrc, false, null);

        ImportOrganizerTask task = new ImportOrganizerTask(null);
        task.setMonitor(new DummyProgressMonitor());
        task.processCompilationUnit(cu);

        java.nio.file.Path resultSourceFilePath = this.resourcesPath.resolve("OldJCoAllInOne.java.resource.result");
        String migResult = new String(Files.readAllBytes(resultSourceFilePath), StandardCharsets.UTF_8);

        Assert.assertEquals("The migration seems to have different results. Actual: " + cu.getSource()
        + "\n\nExpected: " + migResult, migResult, cu.getSource());
    }

    @Before
    public void setUp() throws Exception {
        this.createProject("migrate_imports_project");
    }

}