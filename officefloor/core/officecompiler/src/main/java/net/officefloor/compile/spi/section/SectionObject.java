package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;

/**
 * Object required by the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObject extends SectionDependencyObjectNode {

	/**
	 * Obtains the name of this {@link SectionObject}.
	 * 
	 * @return Name of this {@link SectionObject}.
	 */
	String getSectionObjectName();

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

}