package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.SectionInputType;

/**
 * Input to an {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInput extends SectionFlowSourceNode {

	/**
	 * Obtains the name of this {@link SectionInput}.
	 * 
	 * @return Name of this {@link SectionInput}.
	 */
	String getSectionInputName();

	/**
	 * <p>
	 * Adds the annotation for this {@link SectionInput}.
	 * <p>
	 * This is exposed as is on the {@link SectionInputType} interface for this
	 * {@link SectionInput}.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}