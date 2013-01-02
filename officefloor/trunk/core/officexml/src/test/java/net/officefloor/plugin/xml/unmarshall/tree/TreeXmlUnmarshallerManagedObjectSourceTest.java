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
package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link TreeXmlUnmarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshallerManagedObjectSourceTest extends
		AbstractTreeXmlUnmarshallerTestCase {

	/*
	 * ================= AbstractTreeXmlUnmarshallerTestCase =================
	 */

	@Override
	protected TreeXmlUnmarshaller createNonRecursiveTreeXmlUnmarshaller()
			throws Throwable {
		return this.createUnmarshaller("NonRecursiveMetaData.xml");
	}

	@Override
	protected TreeXmlUnmarshaller createRecursiveTreeXmlUnmarshaller()
			throws Throwable {
		return this.createUnmarshaller("RecursiveMetaData.xml");
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input configuration.
	 *
	 * @param configurationFileName
	 *            Name of file containing the mapping configuration.
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws Throwable
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	private TreeXmlUnmarshaller createUnmarshaller(String configurationFileName)
			throws Throwable {

		// Ensure the file is available
		this.findFile(this.getClass(), configurationFileName);
		String configurationFilePath = this.getPackageRelativePath(this
				.getClass())
				+ "/" + configurationFileName;

		// Play
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader
				.addProperty(
						TreeXmlUnmarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME,
						configurationFilePath);
		TreeXmlUnmarshallerManagedObjectSource source = loader
				.loadManagedObjectSource(TreeXmlUnmarshallerManagedObjectSource.class);

		// Return the TreeXmlUnmarshaller
		return (TreeXmlUnmarshaller) new ManagedObjectUserStandAlone()
				.sourceManagedObject(source).getObject();
	}

}