package net.officefloor.model.generate.model;

import net.officefloor.model.generate.GraphNodeMetaData;

/**
 * Abstract property meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public AbstractPropertyMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name        Name.
	 * @param type        Type.
	 * @param description Description.
	 */
	public AbstractPropertyMetaData(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	/**
	 * Obtains the capitalised name.
	 * 
	 * @return Prpoerty name capitalised.
	 */
	public String getCapitalisedName() {
		return GraphNodeMetaData.capitalise(this.name);
	}

	/**
	 * Obtains the camel case name.
	 * 
	 * @return Property name in camel case.
	 */
	public String getCamelCaseName() {
		return GraphNodeMetaData.camelCase(this.name);
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return Property name.
	 */
	public String getPropertyName() {
		return GraphNodeMetaData.propertyCase(this.name);
	}

	/**
	 * Name.
	 */
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Type.
	 */
	private String type;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Flag indicating to cascade remove.
	 */
	private String cascadeRemove;

	public String getCascadeRemove() {
		return this.cascadeRemove;
	}

	public void setCascadeRemove(String cascadeRemove) {
		this.cascadeRemove = cascadeRemove;
	}

	public boolean isCascadeRemove() {
		return Boolean.valueOf(this.cascadeRemove).booleanValue();
	}

	/**
	 * Description.
	 */
	private String description;

	public String getDescription() {
		if (this.description == null) {
			// Construct description from name
			return GraphNodeMetaData.titleCase(this.name) + ".";
		} else {
			return this.description;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
