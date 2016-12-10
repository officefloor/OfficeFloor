/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.repository.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.easymock.AbstractMatcher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Test;

/**
 * Tests the {@link ProjectConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProjectConfigurationContextTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link IProject}.
	 */
	private final IProject project = this.createMock(IProject.class);

	/**
	 * Mock {@link IProgressMonitor}.
	 */
	private final IProgressMonitor monitor = this
			.createMock(IProgressMonitor.class);

	/**
	 * {@link ProjectConfigurationContext} to test.
	 */
	private final ConfigurationContext context = new ProjectConfigurationContext(
			this.project, this.monitor);

	/**
	 * Should be able to edit the {@link IProject}.
	 */
	@Test
	public void testNotReadOnly() {
		assertFalse("Should be able to edit project", this.context.isReadOnly());
	}

	/**
	 * Ensure failure if {@link IFile} already exists.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testCreateItem_FileAlreadyExists() throws Exception {

		// Record file already existing
		this.recordGetFile("Resource.xml", true);

		// Test
		this.replayMockObjects();
		try {
			this.context.createConfigurationItem("Resource.xml", null);
			fail("Should not be successful");
		} catch (IOException ex) {
			assertEquals("Incorrect exception",
					"File 'Resource.xml' can not be created as already exists",
					ex.getMessage());
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to create item under {@link IProject} by creating the
	 * necessary folders.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testCreateItem_EnsureFoldersExist() throws Exception {

		final InputStream CONTENTS = new ByteArrayInputStream("TEST".getBytes());

		final IFolder folder = this.createMock(IFolder.class);
		final IFolder parentFolder = this.createMock(IFolder.class);
		final IFolder grandParentFolder = this.createMock(IFolder.class);

		// Record obtaining the file
		final IFile file = this.recordGetFile(
				"src/main/resources/Resource.xml", false);

		// Record ensuring all folders exist
		this.recordReturn(file, file.getParent(), folder);
		this.recordReturn(folder, folder.exists(), false);
		this.recordReturn(folder, folder.getParent(), parentFolder);
		this.recordReturn(parentFolder, parentFolder.exists(), false);
		this.recordReturn(parentFolder, parentFolder.getParent(),
				grandParentFolder);
		this.recordReturn(grandParentFolder, grandParentFolder.exists(), false);
		this.recordReturn(grandParentFolder, grandParentFolder.getParent(),
				this.project);
		grandParentFolder.create(true, true, this.monitor);
		parentFolder.create(true, true, this.monitor);
		folder.create(true, true, this.monitor);

		// Record creating the file
		final InputStream[] contents = new InputStream[1];
		file.create(null, true, this.monitor);
		this.control(file).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				contents[0] = (InputStream) actual[0];
				assertEquals("Should force", true, actual[1]);
				assertEquals("Incorrect progress monitor", monitor, actual[2]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.context.createConfigurationItem("src/main/resources/Resource.xml",
				CONTENTS);
		this.verifyMockObjects();

		// Validate expected content
		assertSame("Incorrect contents for file", CONTENTS, contents[0]);
	}

	/**
	 * Ensure does not nothing if non-existent file.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testDeleteNonExistentFile() throws Exception {

		// Record obtaining file (no existing)
		this.recordGetFile("path", false);

		// Test
		this.replayMockObjects();
		this.context.deleteConfigurationItem("path");
		this.verifyMockObjects();
	}

	/**
	 * Ensure deletes file.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testDeleteFile() throws Exception {

		// Record obtaining file (no existing)
		IFile file = this.recordGetFile("path", true);
		file.delete(true, this.monitor);

		// Test
		this.replayMockObjects();
		this.context.deleteConfigurationItem("path");
		this.verifyMockObjects();
	}

	/**
	 * Ensure get file.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testGetFile() throws Exception {

		// Record obtaining file (existing)
		this.recordGetFile("path", true);

		// Test
		this.replayMockObjects();
		ConfigurationItem item = this.context.getConfigurationItem("path");
		this.verifyMockObjects();

		// Ensure item obtained
		assertNotNull("Should obtain configuration item", item);
	}

	/**
	 * Ensure get unknown file.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testGetUnknownFile() throws Exception {

		// Record obtaining file (not existing)
		this.recordGetFile("path", false);

		// Test
		this.replayMockObjects();
		ConfigurationItem item = this.context.getConfigurationItem("path");
		this.verifyMockObjects();

		// Ensure item not obtained
		assertNull("Should not obtain configuration item", item);
	}

	/**
	 * Records obtaining the {@link IFile}.
	 * 
	 * @param expectedPath
	 *            {@link IFile} path.
	 * @param isExist
	 *            Indicates if {@link IFile} exists.
	 */
	private IFile recordGetFile(final String expectedPath, boolean isExist) {

		// Create the file
		final IFile file = this.createMock(IFile.class);

		// Record obtaining the file
		this.recordReturn(this.project, this.project.getFile((IPath) null),
				file, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						IPath path = (IPath) actual[0];
						assertEquals("Incorrect path", expectedPath,
								path.toPortableString());
						return true;
					}
				});
		this.recordReturn(file, file.exists(), isExist);

		// Return the file
		return file;
	}

}