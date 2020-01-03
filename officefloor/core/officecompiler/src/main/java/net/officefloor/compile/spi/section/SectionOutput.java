package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.SectionOutputType;

/**
 * Output of a {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionOutput extends SectionFlowSinkNode {

	/**
	 * Obtains the name of this {@link SectionOutput}.
	 * 
	 * @return Name of this {@link SectionOutput}.
	 */
	String getSectionOutputName();

	/**
	 * <p>
	 * Adds the annotation for this {@link SectionOutput}.
	 * <p>
	 * This is exposed as is on the {@link SectionOutputType} interface for this
	 * {@link SectionOutput}.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}