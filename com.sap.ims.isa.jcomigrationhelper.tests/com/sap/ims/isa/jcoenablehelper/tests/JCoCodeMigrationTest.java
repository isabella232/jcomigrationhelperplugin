package com.sap.ims.isa.jcomigrationhelper.tests;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.Before;
import org.junit.Test;

public class JCoCodeMigrationTest {


    protected IPackageFragmentRoot srcFolder;

    @Test
    public void jcoCodeMigrationForCompilationUnit() throws Exception {


    }

    @Before
    protected void setUp() throws CoreException, JavaModelException {
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