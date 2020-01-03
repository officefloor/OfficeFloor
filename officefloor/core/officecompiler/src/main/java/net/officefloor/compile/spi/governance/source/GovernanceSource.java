package net.officefloor.compile.spi.governance.source;

import net.officefloor.frame.api.governance.Governance;

/**
 * Source to obtain the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSource<I, F extends Enum<F>> {

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
	GovernanceSourceSpecification getSpecification();

	/**
	 * Initialises the {@link GovernanceSource}.
	 * 
	 * @param context
	 *            {@link GovernanceSourceContext} to initialise this instance of
	 *            the {@link GovernanceSource}.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link GovernanceSource} fail to configure itself
	 *             from the input properties.
	 */
	GovernanceSourceMetaData<I, F> init(GovernanceSourceContext context) throws Exception;

}