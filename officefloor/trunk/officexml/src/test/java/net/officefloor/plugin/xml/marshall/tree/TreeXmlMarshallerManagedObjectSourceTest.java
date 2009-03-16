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
package net.officefloor.plugin.xml.marshall.tree;

import java.io.InputStream;

import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.frame.util.ManagedObjectSourceLoader;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link TreeXmlMarshallerManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TreeXmlMarshallerManagedObjectSourceTest extends
		AbstractTreeXmlMarshallerTestCase {

	/*
	 * =============== AbstractTreeXmlMarshallerTestCase ==================
	 */
	
	@Override
	protected TreeXmlMarshaller createFlatMarshaller() throws Throwable {
		return this.createMarshaller("FlatMetaData.xml");
	}

	@Override
	protected TreeXmlMarshaller createTreeMarshaller() throws Throwable {
		return this.createMarshaller("TreeMetaData.xml");
	}

	@Override
	protected TreeXmlMarshaller createCollectionMarshaller() throws Throwable {
		return this.createMarshaller("CollectionMetaData.xml");
	}

	@Override
	protected TreeXmlMarshaller createTypeMarshaller() throws Throwable {
		return this.createMarshaller("TypeMetaData.xml");
	}

	@Override
	protected TreeXmlMarshaller createReferenceMarshaller() throws Throwable {
		return this.createMarshaller("ReferenceMetaData.xml");
	}

	@Override
	protected TreeXmlMarshaller createReferenceCollectionMarshaller()
			throws Throwable {
		return this.createMarshaller("CollectionReferenceMetaData.xml");
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} from the input configuration.
	 * 
	 * @param configurationFileName
	 *            Name of file containing the mapping configuration.
	 * @return {@link TreeXmlMarshaller}.
	 * @throws Throwable
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected TreeXmlMarshaller createMarshaller(String configurationFileName)
			throws Throwable {

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

		// Source the managed object
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
		loader
				.addProperty(
						TreeXmlMarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME,
						configurationFileName);
		loader.setResourceLocator(resourceLocator);
		TreeXmlMarshallerManagedObjectSource source = loader
				.loadManagedObjectSource(TreeXmlMarshallerManagedObjectSource.class);

		// Return the TreeXmlMarshaller
		return (TreeXmlMarshaller) ManagedObjectUserStandAlone
				.sourceManagedObject(source).getObject();
	}

}