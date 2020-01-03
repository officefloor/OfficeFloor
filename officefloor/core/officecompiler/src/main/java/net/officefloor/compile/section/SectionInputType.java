package net.officefloor.compile.section;

import net.officefloor.compile.type.AnnotatedType;

/**
 * <code>Type definition</code> of an input for a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link SectionInputType}.
	 * 
	 * @return Name of this {@link SectionInputType}.
	 */
	String getSectionInputName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the parameter type for this
	 * {@link SectionInputType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be available
	 * to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the parameter type.
	 */
	String getParameterType();

}