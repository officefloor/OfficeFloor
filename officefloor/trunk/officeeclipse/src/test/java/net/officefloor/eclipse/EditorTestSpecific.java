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

/**
 * Specific details for the
 * {@link net.officefloor.eclipse.AbstractEditorTest}.
 * 
 * @author Daniel
 */
public class EditorTestSpecific {

	/**
	 * Obtains the ID for the {@link org.eclipse.ui.part.EditorPart}.
	 */
	private final String editorId;

	/**
	 * Name of file being editted by the {@link org.eclipse.ui.part.EditorPart}.
	 */
	private final String editorFileName;

	/**
	 * Files relative to the {@link junit.framework.TestCase} to be copied to
	 * the test project.
	 */
	private final String[] testFileNames;

	/**
	 * Initiate.
	 * 
	 * @param editorId
	 *            ID for the {@link org.eclipse.ui.part.EditorPart}.
	 * @param editorFileName
	 *            Name of file being editted.
	 * @param testFileNames
	 *            Files relative to the {@link junit.framework.TestCase} to be
	 *            copied to the test project.
	 */
	public EditorTestSpecific(String editorId, String editorFileName,
			String... testFileNames) {
		this.editorId = editorId;
		this.editorFileName = editorFileName;
		this.testFileNames = testFileNames;
	}

	/**
	 * Obtains the Id of the {@link org.eclipse.ui.part.EditorPart}.
	 * 
	 * @return Id of the {@link org.eclipse.ui.part.EditorPart}.
	 */
	public String getEditorId() {
		return this.editorId;
	}

	/**
	 * Obtains file name being editted.
	 * 
	 * @return File name being editted.
	 */
	public String getEditorFileName() {
		return this.editorFileName;
	}

	/**
	 * Obtains files to be copied to test project.
	 * 
	 * @return File names of files to be copied to the test project.
	 */
	public String[] getTestFileNames() {
		return this.testFileNames;
	}

}
