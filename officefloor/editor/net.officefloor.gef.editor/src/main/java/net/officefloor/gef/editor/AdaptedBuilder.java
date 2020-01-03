package net.officefloor.gef.editor;

/**
 * Builds the {@link AdaptedModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedBuilder {

	/**
	 * Builds the {@link AdaptedModel} instances.
	 * 
	 * @param context
	 *            {@link AdaptedBuilderContext}.
	 */
	void build(AdaptedBuilderContext context);

}