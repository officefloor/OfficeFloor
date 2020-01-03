package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link TeamSource} {@link Class}
 * alias by including the extension {@link TeamSource} jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addTeamSourceAlias(String, Class)} will be invoked
 * for each found {@link TeamSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamSourceService<S extends TeamSource> {

	/**
	 * Obtains the alias for the {@link TeamSource} {@link Class}.
	 * 
	 * @return Alias for the {@link TeamSource} {@link Class}.
	 */
	String getTeamSourceAlias();

	/**
	 * Obtains the {@link TeamSource} {@link Class}.
	 * 
	 * @return {@link TeamSource} {@link Class}.
	 */
	Class<S> getTeamSourceClass();

}