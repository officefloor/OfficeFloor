/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import net.officefloor.frame.test.OfficeFrameTestCase;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Abstract test for an {@link org.eclipse.ui.part.EditorPart}.
 * 
 * @author Daniel
 */
public abstract class AbstractEditorTest extends OfficeFrameTestCase {

	/**
	 * Test properties.
	 */
	private final Properties testProperties = new Properties();

	/**
	 * {@link org.eclipse.ui.part.EditorPart}.
	 */
	private IEditorPart editorPart;

	/**
	 * {@link IProject}.
	 */
	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the properties for testing
		File propertyFileLocation = new File(new File(System
				.getProperty("user.home")), new File("officefloor",
				"eclipse-test.properties").getPath());
		if (!propertyFileLocation.exists()) {
			throw new FileNotFoundException(
					"Can not find test property file : "
							+ propertyFileLocation.getAbsolutePath());
		}
		this.testProperties.load(new FileInputStream(propertyFileLocation));

		// Progress monitor
		IProgressMonitor progressMonitor = new NullProgressMonitor();

		// Obtain the workspace root
		IWorkspace workspace = (IWorkspace) ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();

		// Obtain path to test project
		String PROJECT_NAME = "TestProject";
		File projectDir = new File(this
				.getTestProperty("workspace.test.project.dir"), PROJECT_NAME);
		IPath projectPath = new Path(projectDir.getAbsolutePath());

		// Obtain the project ensuring it exists
		this.project = workspaceRoot.getProject(PROJECT_NAME);
		if (!this.project.exists()) {
			// Create the project as not exists
			IProjectDescription projectDesc = new ProjectDescription();
			projectDesc.setName(PROJECT_NAME);
			projectDesc.setLocation(projectPath);
			projectDesc.setNatureIds(new String[] { JavaCore.NATURE_ID });
			this.project.create(projectDesc, progressMonitor);
		}

		// Ensure the project is open
		if (!this.project.isOpen()) {
			this.project.open(progressMonitor);
		}

		// Obtain the path to test files for this test
		String sourceTestDir = this.getTestProperty("src.test.dir");
		String testFilesDir = new File(sourceTestDir, this
				.getPackageRelativePath(this.getClass())).getAbsolutePath();

		// Obtain the editor test specific details
		EditorTestSpecific editorTestSpecific = this.getEditorTestSpecific();

		// Copy test project files to the project
		File testProjectDir = new File(sourceTestDir).getParentFile()
				.getParentFile();
		this
				.copyDirectory(new File(testProjectDir, "test-project"),
						projectDir);

		// Copy the officefloor.jar to lib directory
		this.copyDirectory(new File("D:/eclipse/systems/lib"), new File(
				projectDir, "lib"));

		// Copy test files to the project
		String[] testFileNames = editorTestSpecific.getTestFileNames();
		if (testFileNames != null) {
			for (String testFileName : testFileNames) {
				FileInputStream testFileContent = new FileInputStream(new File(
						testFilesDir, testFileName));
				File targetTestFile = new File(projectDir, testFileName);
				targetTestFile.delete();
				this.createFile(targetTestFile, testFileContent);
				testFileContent.close();
			}
		}

		// Refresh the project to pick up the file
		this.project.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);

		// Create the Java Project
		IJavaProject javaProject = JavaCore.create(this.project);

		// Build the Project
		this.project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
				JavaCore.BUILDER_ID, null, progressMonitor);

		// Ensure the Java Project is built for to obtain the classes
		assertTrue("Ensure the java project is built", javaProject
				.hasBuildState());

		// Access the file
		IFile file = this.project.getFile(editorTestSpecific
				.getEditorFileName());

		// Obtain the editor input
		IEditorInput editorInput = new FileEditorInput(file);

		// Open the editor
		IWorkbench workBench = PlatformUI.getWorkbench();
		IWorkbenchPage activeWorkbenchPage = workBench
				.getActiveWorkbenchWindow().getActivePage();
		this.editorPart = IDE.openEditor(activeWorkbenchPage, editorInput,
				editorTestSpecific.getEditorId());

		// Ensure successfully opened editor
		if (this.editorPart instanceof ErrorEditorPart) {
			// Obtain failure details (from error editor)
			ErrorEditorPart errorEditPart = (ErrorEditorPart) this.editorPart;
			Shell errorShell = new Shell();
			errorEditPart.createPartControl(errorShell);

			// Populate the full error details (press button on error editor)
			Button button = (Button) errorShell.getChildren()[2];
			button.notifyListeners(SWT.Selection, null);

			// Obtain the full error details (from error area)
			Composite errorComposite = (Composite) errorShell.getChildren()[3];
			Text fullErrorText = (Text) errorComposite.getChildren()[0];

			// Create error message
			String msg = "Failed to open editor '"
					+ editorTestSpecific.getEditorId() + "' on file '"
					+ editorTestSpecific.getEditorFileName() + "'";

			// Log error
			System.err.println(msg);
			System.err.println(fullErrorText.getText());

			// Fail test
			fail(msg);
		}
	}

	/**
	 * Obtains the {@link EditorTestSpecific}.
	 * 
	 * @return {@link EditorTestSpecific}.
	 */
	protected abstract EditorTestSpecific getEditorTestSpecific();

	/**
	 * Obtains the test property.
	 * 
	 * @param name
	 *            Name of the test property.
	 * @return Value of the test property.
	 * @throws Exception
	 */
	protected String getTestProperty(String name) throws Exception {
		// Ensure property is obtained
		String value = this.testProperties.getProperty(name);
		if (value == null) {
			throw new Exception("Test property '" + name
					+ "' must be specified");
		}
		return value;
	}

	/**
	 * Obtains the {@link org.eclipse.ui.part.EditorPart} being tested.
	 * 
	 * @return {@link org.eclipse.ui.part.EditorPart} being tested.
	 */
	protected IEditorPart getEditorPart() {
		return this.editorPart;
	}

	/**
	 * Obtains the {@link IProject}.
	 * 
	 * @return {@link IProject}.
	 */
	protected IProject getProject() {
		return this.project;
	}
}
