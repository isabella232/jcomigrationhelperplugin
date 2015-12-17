package com.sap.ims.isa.jcomigrationhelper.tests;

import java.io.IOException;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.Assert;

public abstract class TestsSuiteParent {

    /**
     * An empty progress monitor which is required by some tasks, but without any functionality.
     */
    protected static final NullProgressMonitor npeMonitor    = new NullProgressMonitor();

    protected static final String              RESOURCE_POSTFIX        = ".resource";
    protected static final String              RESOURCE_RESULT_POSTFIX = RESOURCE_POSTFIX + ".result";

    /**
     * The source folder of the project which has been created.
     */
    protected IPackageFragmentRoot srcFolder;
    protected java.nio.file.Path   resourcesPath = Paths.get("src", "main", "resources");

    public TestsSuiteParent() {
    }

    /**
     * Creates a project with the specified name in the workspace which has a source (src) and an output (bin) folder.
     * The source folder will be put into the variable {@link #srcFolder}.
     *
     * @param projectName
     *            The project name to create.
     * @throws Exception
     *             In case of any error.
     */
    protected void createProject(String projectName) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(projectName);
        IWorkspaceRunnable create = monitor -> {
            IPath sourcePath = new Path("src");
            JavaProject javaProject = null;
            // do not create a project again, if it is not required.
            if (!project.exists()) {
                project.create(npeMonitor);
                project.open(npeMonitor);

                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] { JavaCore.NATURE_ID });
                project.setDescription(description, npeMonitor);

                IPath projectPath = project.getFullPath();

                // create source folder
                project.getFolder(sourcePath).create(true, true, npeMonitor);

                // create project output folder
                IPath outputPath = new Path("bin");
                project.getFolder(outputPath).create(true, true, npeMonitor);

                // add the source folder
                IClasspathEntry sourceEntry = JavaCore.newSourceEntry(projectPath.append(sourcePath), null,
                        projectPath.append(outputPath));

                // make sure the settings are always the same for the tests
                @SuppressWarnings("unchecked")
                Hashtable<String, String> options = JavaCore.getOptions();
                options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
                options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
                JavaCore.setOptions(options);

                javaProject = (JavaProject) JavaCore.create(project);
                javaProject.writeFileEntries(new IClasspathEntry[] { sourceEntry }, projectPath.append(outputPath));
                options = new Hashtable<>();
                options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
                options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
                options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
                javaProject.setOptions(options);
            } else {
                javaProject = (JavaProject) JavaCore.create(project);
            }
            this.srcFolder = javaProject.getPackageFragmentRoot(sourcePath);
        };
        workspace.run(create, npeMonitor);
    }

    /**
     * Resolves the relative path from {@link #resourcesPath} using the variable and checks if it is existing. Assertion
     * fail will be raised if the resource is not existing. If no parameter is specified, then the default resource path
     * will be returned.
     *
     * @param resRelPath
     *            The relative resource path to resolve.
     */
    protected java.nio.file.Path getTestResource(String... resRelPath) {
        java.nio.file.Path resourceFilePath = this.resourcesPath;
        if (resRelPath != null && resRelPath.length > 0) {
            for (String string : resRelPath) {
                resourceFilePath = resourceFilePath.resolve(string);
            }
            if (!Files.exists(resourceFilePath)) {
                Assert.fail(
                        "The resource path " + resourceFilePath.toAbsolutePath().normalize() + " is NOT existing!!!!!!");
            }
        }
        return resourceFilePath;
    }

    /**
     * Asserts with if the values are the same with a common message.
     * 
     * @param expected
     *            The expected value.
     * @param actual
     *            The actual value.
     */
    protected void assertEquals(String expected, String actual) {
        Assert.assertEquals(
                "The files seem to have different contents. Actual: " + actual + "\n\nExpected: " + expected, expected,
                actual);
    }

    /**
     * Reads the content from the test resource by using {@link #getTestResource(String...)} and read whole content into
     * a string which is returned.
     *
     * @param testResource
     *            The resource to read.
     * @return The file content of the resource.
     * @throws IOException
     *             In case of any error.
     * @see #getTestResource(String...)
     */
    protected String readContentFromTestResource(String... testResource) throws IOException {
        java.nio.file.Path resultSourceFilePath = this.getTestResource(testResource);
        return new String(Files.readAllBytes(resultSourceFilePath), StandardCharsets.UTF_8);
    }
}
