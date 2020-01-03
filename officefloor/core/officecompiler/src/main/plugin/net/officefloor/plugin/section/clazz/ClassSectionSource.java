package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * <p>
 * Class {@link SectionSource}.
 * <p>
 * The implementation has been segregated into smaller methods to allow
 * overriding to re-use {@link ClassSectionSource} for other uses.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractFunctionSectionSource
		implements SectionSourceService<ClassSectionSource> {

	/*
	 * ================ SectionSourceService ========================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassSectionSource> getSectionSourceClass() {
		return ClassSectionSource.class;
	}

}