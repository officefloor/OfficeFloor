package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.governance.source.GovernanceSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link GovernanceSource}
 * {@link Class} alias by including the extension {@link GovernanceSource} jar
 * on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addGovernanceSourceAlias(String, Class)} will be
 * invoked for each found {@link GovernanceSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceService<I, F extends Enum<F>, S extends GovernanceSource<I, F>> {

	/**
	 * Obtains the alias for the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return Alias for the {@link GovernanceSource} {@link Class}.
	 */
	String getGovernanceSourceAlias();

	/**
	 * Obtains the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return {@link GovernanceSource} {@link Class}.
	 */
	Class<S> getGovernanceSourceClass();

}