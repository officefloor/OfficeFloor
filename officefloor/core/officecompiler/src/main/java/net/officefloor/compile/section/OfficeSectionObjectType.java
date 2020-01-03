package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSectionObject;

/**
 * <code>Type definition</code> of the {@link OfficeSectionObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionObjectType {

	/**
	 * Obtains the name of this {@link OfficeSectionObject}.
	 * 
	 * @return Name of this {@link OfficeSectionObject}.
	 */
	String getOfficeSectionObjectName();

	/**
	 * Obtains the object type.
	 * 
	 * @return Object type.
	 */
	String getObjectType();

	/**
	 * Obtains the type qualifier.
	 * 
	 * @return Type qualifier.
	 */
	String getTypeQualifier();

}