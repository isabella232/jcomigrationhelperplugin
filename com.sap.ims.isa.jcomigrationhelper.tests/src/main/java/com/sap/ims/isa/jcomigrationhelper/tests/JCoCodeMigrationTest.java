package com.sap.ims.isa.jcomigrationhelper.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.ims.isa.jcomigrationhelper.imports.ImportOrganizerTask;

public class JCoCodeMigrationTest {


    protected IPackageFragmentRoot srcFolder;
    java.nio.file.Path             resourcesPath = Paths.get("src", "main", "resources");

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
        task.processCompulationUnit(cu);

        java.nio.file.Path resultSourceFilePath = this.resourcesPath.resolve("OldJCoAllInOne.java.resource.result");
        String migResult = new String(Files.readAllBytes(resultSourceFilePath), StandardCharsets.UTF_8);

        Assert.assertEquals("The migration seems to have different results. Actual: " + cu.getSource()
        + "\n\nExpected: " + migResult, migResult, cu.getSource());
    }

    @Before
    public void setUp() throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        final IProject project = workspace.getRoot().getProject("migrationTestProject");
        IWorkspaceRunnable create = monitor -> {
            project.create(null);
            project.open(null);

            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] {JavaCore.NATURE_ID});
            project.setDescription(description, null);

            IPath projectPath = project.getFullPath();

            // create source folder
            IPath sourcePath = new Path("src");
            project.getFolder(sourcePath).create(true, true, null);

            // create project output folder
            IPath outputPath = new Path("bin");
            project.getFolder(outputPath).create(true, true, null);

            // add the source folder
            IClasspathEntry sourceEntry = JavaCore.newSourceEntry(projectPath.append(sourcePath), null,
                    projectPath.append(outputPath));

            // make sure the settings are always the same for the tests
            @SuppressWarnings("unchecked")
            Hashtable<String, String> options = JavaCore.getOptions();
            options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
            options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
            JavaCore.setOptions(options);

            JavaProject javaProject = (JavaProject) JavaCore.create(project);
            javaProject.writeFileEntries(new IClasspathEntry[] { sourceEntry }, projectPath.append(outputPath));
            options = new Hashtable<>();
            options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
            options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
            options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
            javaProject.setOptions(options);

            this.srcFolder = javaProject.getPackageFragmentRoot(sourcePath);
        };
        workspace.run(create, null);
    }

}