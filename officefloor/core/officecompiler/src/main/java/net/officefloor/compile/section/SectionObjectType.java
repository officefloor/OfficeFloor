package net.officefloor.compile.section;

/**
 * <code>Type definition</code> of an {@link Object} dependency required by the
 * {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObjectType {

	/**
	 * Obtains the name of this {@link SectionObjectType}.
	 * 
	 * @return Name of this {@link SectionObjectType}.
	 */
	String getSectionObjectName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the {@link Object} type
	 * for this {@link SectionObjectType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be
	 * available to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the {@link Object} type.
	 */
	String getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

}