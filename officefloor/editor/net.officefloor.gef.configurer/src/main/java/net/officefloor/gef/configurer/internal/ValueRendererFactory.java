package net.officefloor.gef.configurer.internal;

/**
 * Factory for the {@link ValueRenderer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRendererFactory<M, I extends ValueInput> {

	/**
	 * Creates the {@link ValueRenderer}.
	 * 
	 * @param context
	 *            {@link ValueRendererContext}.
	 * @return {@link ValueRenderer}.
	 */
	ValueRenderer<M, I> createValueRenderer(ValueRendererContext<M> context);

}