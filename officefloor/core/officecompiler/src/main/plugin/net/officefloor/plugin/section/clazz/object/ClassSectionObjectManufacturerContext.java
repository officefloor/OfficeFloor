package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentor;
import net.officefloor.compile.spi.section.SectionDependencyRequireNode;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ClassSectionObjectManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectManufacturerContext {

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

	/**
	 * Creates a {@link ClassSectionTypeQualifier}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @return {@link ClassSectionTypeQualifier}.
	 */
	ClassSectionTypeQualifier createTypeQualifier(String qualifier, Class<?> type);

	/**
	 * Gets or creates the {@link SectionManagedObject}.
	 * 
	 * @param managedObjectSourceClassName {@link ManagedObjectSource} {@link Class}
	 *                                     name.
	 * @param properties                   {@link PropertyList} for the
	 *                                     {@link SectionManagedObject}.
	 * @param typeQualifiers               {@link ClassSectionTypeQualifier}
	 *                                     instances.
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject getOrCreateManagedObject(String managedObjectSourceClassName, PropertyList properties,
			ClassSectionTypeQualifier... typeQualifiers);

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}