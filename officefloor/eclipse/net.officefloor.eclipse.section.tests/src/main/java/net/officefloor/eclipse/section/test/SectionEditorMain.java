/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.section.test;

import java.io.File;

import net.officefloor.eclipse.section.SectionEditor;

/**
 * Allows testing of the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditorMain extends SectionEditor {

	/**
	 * Runs the {@link SectionEditor} for testing.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If failure in running.
	 */
	public static void main(String[] args) throws Exception {
		SectionEditorMain.launch(new File(ClassLoader
				.getSystemResource(
						SectionEditorMain.class.getPackage().getName().replace('.', '/') + "/Test.section.xml")
				.toURI()));
	}
}