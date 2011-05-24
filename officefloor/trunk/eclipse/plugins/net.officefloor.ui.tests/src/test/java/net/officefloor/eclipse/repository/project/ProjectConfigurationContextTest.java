/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.easymock.AbstractMatcher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

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
	 * {@link ProjectConfigurationContext} to test.
	 */
	private final ConfigurationContext context = new ProjectConfigurationContext(
			this.project);

	/**
	 * Ensure able to create item under {@link IProject}.
	 */
	public void testCreateItemFromProject() throws Exception {

		final IProgressMonitor monitor = this
				.createMock(IProgressMonitor.class);
		final IFolder folder = this.createMock(IFolder.class);
		final IFolder parentFolder = this.createMock(IFolder.class);
		final IFolder grandParentFolder = this.createMock(IFolder.class);
		final IFile file = this.createMock(IFile.class);

		// Record creating all folders
		this.recordReturn(project, project.getFolder("src/main/resources"),
				folder);
		this.recordReturn(folder, folder.exists(), false);
		this.recordReturn(folder, folder.getParent(), parentFolder);
		this.recordReturn(parentFolder, parentFolder.exists(), false);
		this.recordReturn(parentFolder, parentFolder.getParent(),
				grandParentFolder);
		this.recordReturn(grandParentFolder, grandParentFolder.exists(), false);
		this.recordReturn(grandParentFolder, grandParentFolder.getParent(),
				project);
		grandParentFolder.create(true, true, monitor);
		parentFolder.create(true, true, monitor);
		folder.create(true, true, monitor);

		// Create the GWT Module
		final InputStream[] contents = new InputStream[1];
		this.recordReturn(folder, folder.getFile("Resource.xml"), file);
		file.create(null, true, monitor);
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
		ConfigurationItem configurationItem = this.context
				.createConfigurationItem("src/main/resources/Resources.xml",
						null);
		this.verifyMockObjects();
	}

	/**
	 * Obtains the content.
	 * 
	 * @param input
	 *            {@link InputStream}.
	 * @return Contents.
	 */
	private String getContents(InputStream input) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int value = input.read(); value != -1; value = input.read()) {
			buffer.write(value);
		}
		return buffer.toString();
	}

}