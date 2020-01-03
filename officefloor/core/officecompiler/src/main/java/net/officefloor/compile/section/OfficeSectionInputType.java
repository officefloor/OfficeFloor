package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.type.AnnotatedType;

/**
 * <code>Type definition</code> of the {@link OfficeSectionInput}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionInputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link OfficeSectionInput}.
	 * 
	 * @return Name of this {@link OfficeSectionInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Obtains the parameter type for this {@link OfficeSectionInput}.
	 * 
	 * @return Parameter type for this {@link OfficeSectionInput}.
	 */
	String getParameterType();

}