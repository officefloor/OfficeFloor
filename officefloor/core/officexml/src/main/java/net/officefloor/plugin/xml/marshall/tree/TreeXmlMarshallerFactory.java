package net.officefloor.plugin.xml.marshall.tree;

import java.io.InputStream;

import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * <p>
 * Factory for the creation of a {@link TreeXmlMarshaller}.
 * <p>
 * This is to ease obtaining a {@link TreeXmlMarshaller} but if utilising office
 * floor framework should plug-in via
 * {@link TreeXmlMarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshallerFactory {

	/**
	 * Singleton.
	 */
	private static final TreeXmlMarshallerFactory INSTANCE = new TreeXmlMarshallerFactory();

	/**
	 * Enforce singleton.
	 */
	private TreeXmlMarshallerFactory() {
	}

	/**
	 * Obtains the singleton {@link TreeXmlMarshallerFactory}.
	 *
	 * @return Singleton {@link TreeXmlMarshallerFactory}.
	 */
	public static TreeXmlMarshallerFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} from the input configuration.
	 *
	 * @param configuration
	 *            Configuration of the {@link TreeXmlMarshaller}.
	 * @return Configured {@link TreeXmlMarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	public TreeXmlMarshaller createMarshaller(InputStream configuration)
			throws XmlMarshallException {

		// Create the managed object source
		TreeXmlMarshallerManagedObjectSource source = new TreeXmlMarshallerManagedObjectSource(
				configuration);

		// Source the marshaller
		try {
			return (TreeXmlMarshaller) new ManagedObjectUserStandAlone()
					.sourceManagedObject(source).getObject();
		} catch (Throwable ex) {
			throw new XmlMarshallException(ex.getMessage(), ex);
		}
	}

}