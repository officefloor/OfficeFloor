package net.officefloor.gef.configurer;

/**
 * Builder of choices.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChoiceBuilder<M> extends Builder<M, Integer, ChoiceBuilder<M>> {

	/**
	 * Configures the choice.
	 * 
	 * @param label
	 *            Label for the choice.
	 * @return {@link ConfigurationBuilder} to configure the choice.
	 */
	ConfigurationBuilder<M> choice(String label);

}