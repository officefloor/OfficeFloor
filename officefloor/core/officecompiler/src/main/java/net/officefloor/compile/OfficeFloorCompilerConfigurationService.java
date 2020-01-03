package net.officefloor.compile;

import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} service to enable configuring the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerConfigurationService {

	/**
	 * Enables configuring the {@link OfficeFloorCompiler}.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @throws Exception
	 *             If fails to configured the {@link OfficeFloorCompiler}.
	 */
	void configureOfficeFloorCompiler(OfficeFloorCompiler compiler) throws Exception;

}