package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.spi.section.SectionManagedObject;

/**
 * Type qualifier for {@link SectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionTypeQualifier {

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier.
	 */
	String getQualifier();

	/**
	 * Obtains the type.
	 * 
	 * @return Type.
	 */
	Class<?> getType();

}