package net.officefloor.compile.impl.properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;

/**
 * {@link Property} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyImpl implements Property {

	/**
	 * Display label for the property.
	 */
	private final String label;

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value of the property.
	 */
	private String value = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param label
	 *            Display label for the property. Should this be blank, it is
	 *            defaulted to <code>name</code>.
	 */
	public PropertyImpl(String name, String label) {
		this.name = name;
		this.label = (CompileUtil.isBlank(label) ? name : label);
	}

	/**
	 * Instantiates the label as the name.
	 * 
	 * @param name
	 *            Name of the property which is also used as the display label.
	 */
	public PropertyImpl(String name) {
		this(name, name);
	}

	/*
	 * ==================== Property =======================================
	 */

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

}