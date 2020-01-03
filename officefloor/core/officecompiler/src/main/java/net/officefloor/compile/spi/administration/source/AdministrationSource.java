package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;

/**
 * <p>
 * Source to obtain a particular type of {@link Administration}.
 * <p>
 * Implemented by the {@link Administration} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSource<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	AdministrationSourceSpecification getSpecification();

	/**
	 * Initialises the {@link AdministrationSource}.
	 * 
	 * @param context
	 *            {@link AdministrationSourceContext} to initialise this
	 *            instance of the {@link AdministrationSource}.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link AdministrationSource} fail to configure
	 *             itself from the input properties.
	 */
	AdministrationSourceMetaData<E, F, G> init(AdministrationSourceContext context) throws Exception;

}