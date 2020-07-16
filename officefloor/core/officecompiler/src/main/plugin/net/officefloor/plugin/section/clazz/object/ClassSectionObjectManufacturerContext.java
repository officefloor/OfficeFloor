package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.spi.office.ManagedFunctionAugmentor;
import net.officefloor.compile.spi.section.SectionDependencyRequireNode;
import net.officefloor.compile.type.AnnotatedType;

/**
 * Context for the {@link ClassSectionObjectManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectManufacturerContext extends ClassSectionObjectContext {

	/**
	 * Obtains the {@link AnnotatedType} of {@link SectionDependencyRequireNode}.
	 * 
	 * @return {@link AnnotatedType} of {@link SectionDependencyRequire}.
	 */
	AnnotatedType getAnnotatedType();

	/**
	 * Flags the dependency is being provided by a {@link ManagedFunctionAugmentor}.
	 */
	void flagAugmented();

}