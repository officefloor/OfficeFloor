package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link TreeXmlMarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
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

		// Ensure the file is available
		this.findFile(this.getClass(), configurationFileName);
		String configurationFilePath = this.getPackageRelativePath(this
				.getClass())
				+ "/" + configurationFileName;

		// Play
		this.replayMockObjects();

		// Source the managed object
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader
				.addProperty(
						TreeXmlMarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME,
						configurationFilePath);
		TreeXmlMarshallerManagedObjectSource source = loader
				.loadManagedObjectSource(TreeXmlMarshallerManagedObjectSource.class);

		// Return the TreeXmlMarshaller
		return (TreeXmlMarshaller) new ManagedObjectUserStandAlone()
				.sourceManagedObject(source).getObject();
	}

}