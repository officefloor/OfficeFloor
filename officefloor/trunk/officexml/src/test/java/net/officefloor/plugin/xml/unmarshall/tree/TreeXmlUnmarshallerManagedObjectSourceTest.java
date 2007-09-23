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
package net.officefloor.plugin.xml.unmarshall.tree;

import java.io.InputStream;

import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.frame.util.ManagedObjectSourceLoader;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TreeXmlUnmarshallerManagedObjectSourceTest extends
		AbstractTreeXmlUnmarshallerTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.AbstractTreeXmlUnmarshallerTestCase#createNonRecursiveTreeXmlUnmarshaller()
	 */
	protected TreeXmlUnmarshaller createNonRecursiveTreeXmlUnmarshaller()
			throws Exception {
		return this.createUnmarshaller("NonRecursiveMetaData.xml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.AbstractTreeXmlUnmarshallerTestCase#createRecursiveTreeXmlUnmarshaller()
	 */
	protected TreeXmlUnmarshaller createRecursiveTreeXmlUnmarshaller()
			throws Exception {
		return this.createUnmarshaller("RecursiveMetaData.xml");
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input configuration.
	 * 
	 * @param configurationFileName
	 *            Name of file containing the mapping configuration.
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	protected TreeXmlUnmarshaller createUnmarshaller(
			String configurationFileName) throws Exception {

		// Create the mock objects
		final ResourceLocator resourceLocator = this
				.createMock(ResourceLocator.class);

		// Obtain the input stream to the file
		InputStream configuration = this.findInputStream(this.getClass(),
				configurationFileName);

		// Record mock
		resourceLocator.locateInputStream(configurationFileName);
		this.control(resourceLocator).setReturnValue(configuration);

		// Play
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
		loader
				.addProperty(
						TreeXmlUnmarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME,
						configurationFileName);
		loader.setResourceLocator(resourceLocator);
		TreeXmlUnmarshallerManagedObjectSource<?, ?> source = loader
				.loadManagedObjectSource(TreeXmlUnmarshallerManagedObjectSource.class);

		// Return the TreeXmlUnmarshaller
		return (TreeXmlUnmarshaller) ManagedObjectUserStandAlone
				.sourceManagedObject(source).getObject();
	}

}
