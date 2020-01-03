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