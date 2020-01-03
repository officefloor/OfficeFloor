package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link SectionSource}
 * {@link Class} alias by including the extension {@link SectionSource} jar on
 * the class path.
 * <p>
 * {@link OfficeFloorCompiler#addSectionSourceAlias(String, Class)} will be
 * invoked for each found {@link SectionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceService<S extends SectionSource> {

	/**
	 * Obtains the alias for the {@link SectionSource} {@link Class}.
	 * 
	 * @return Alias for the {@link SectionSource} {@link Class}.
	 */
	String getSectionSourceAlias();

	/**
	 * Obtains the {@link SectionSource} {@link Class}.
	 * 
	 * @return {@link SectionSource} {@link Class}.
	 */
	Class<S> getSectionSourceClass();

}