package net.officefloor.gef.configurer;

/**
 * Builder of item configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ItemBuilder<M> {

	/**
	 * Adds text property to be configured.
	 * 
	 * @param label
	 *            Label.
	 * @return {@link TextBuilder}.
	 */
	TextBuilder<M> text(String label);

	/**
	 * Adds flag property to be configured.
	 * 
	 * @param label
	 *            Label.
	 * @return {@link FlagBuilder}.
	 */
	FlagBuilder<M> flag(String label);

}