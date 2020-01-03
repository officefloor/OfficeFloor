package net.officefloor.model.generate.model;

import net.officefloor.model.generate.GraphNodeMetaData;

/**
 * List meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class ListMetaData extends AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public ListMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name
	 *            Name.
	 * @param type
	 *            Type.
	 * @param description
	 *            Description.
	 */
	public ListMetaData(String name, String type, String description) {
		super(name, type, description);
	}

	/**
	 * Plural name.
	 */
	private String plural;

	public String getPluralName() {
		if (plural == null) {
			return this.getCamelCaseName() + "s";
		} else {
			return GraphNodeMetaData.camelCase(plural);
		}
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}
}
