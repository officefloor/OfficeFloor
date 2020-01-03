package net.officefloor.plugin.xml.unmarshall.flat;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.load.DynamicValueLoader;
import net.officefloor.plugin.xml.unmarshall.load.ValueLoaderFactory;

/**
 * Meta-data for a {@link FlatXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlatXmlUnmarshallerMetaData {

	/**
	 * Map of {@link DynamicValueLoader} objects to load values to the target
	 * object.
	 */
	private final Map<String, DynamicValueLoader> valueLoaders;

	/**
	 * Initiate.
	 * 
	 * @param valueLoaderFactory
	 *            Factory for the {@link DynamicValueLoader}.
	 * @param mappings
	 *            Mappings.
	 * @throws XmlMarshallException
	 *             Indicates failure to initiate.
	 */
	public FlatXmlUnmarshallerMetaData(ValueLoaderFactory valueLoaderFactory,
			XmlMapping[] mappings) throws XmlMarshallException {
		this.valueLoaders = new HashMap<String, DynamicValueLoader>();

		// Iterate over mappings loading them
		for (int i = 0; i < mappings.length; i++) {
			// Obtain the current mapping
			XmlMapping currentMapping = mappings[i];

			// Obtain the dynamic value loader
			DynamicValueLoader valueLoader = valueLoaderFactory
					.createDynamicValueLoader(currentMapping
							.getLoadMethodName());

			// Register the value loader by element name
			this.valueLoaders.put(currentMapping.getElementName(), valueLoader);
		}
	}

	/**
	 * Obtain the {@link DynamicValueLoader} for the input element.
	 * 
	 * @param elementName
	 *            Name of element.
	 * @return {@link DynamicValueLoader} to load value to target object.
	 */
	public DynamicValueLoader getValueLoader(String elementName) {
		return (DynamicValueLoader) this.valueLoaders.get(elementName);
	}

}