package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.Translator;

/**
 * Writes an XML attribute with value sourced from object.
 * 
 * @author Daniel Sagenschneider
 */
public class AttributeXmlMapping extends AbstractValueXmlMapping {

	/**
	 * From super.
	 * 
	 * @param tagName
	 *            Name of the XML tag.
	 * @param getMethod
	 *            {@link Method} to obtain the value.
	 * @param translator
	 *            {@link Translator}.
	 * @param isUseRaw
	 *            Indicates to use raw.
	 */
	public AttributeXmlMapping(String tagName, Method getMethod,
			Translator translator, boolean isUseRaw) {
		super(tagName, getMethod, translator, isUseRaw);
	}

	@Override
	protected void writeXml(String tagName, String value, XmlOutput output)
			throws XmlMarshallException {
		// Write the attribute
		XmlMarshallerUtil.writeXml(" ", output);
		XmlMarshallerUtil.writeXml(tagName, output);
		XmlMarshallerUtil.writeXml("=\"", output);
		XmlMarshallerUtil.writeXml(value, output);
		XmlMarshallerUtil.writeXml("\"", output);
	}

}
