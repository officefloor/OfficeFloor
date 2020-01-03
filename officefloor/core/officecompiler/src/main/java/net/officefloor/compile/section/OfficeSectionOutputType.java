package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * <code>Type definition</code> of the {@link OfficeSectionOutput}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionOutputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link OfficeSectionOutput}.
	 * 
	 * @return Name of this {@link OfficeSectionOutput}.
	 */
	String getOfficeSectionOutputName();

	/**
	 * Obtains the argument type from this {@link OfficeSectionOutput}.
	 * 
	 * @return Argument type.
	 */
	String getArgumentType();

	/**
	 * Indicates if this {@link OfficeSectionOutput} is escalation only. In other
	 * words it can be handled by an {@link Office} {@link EscalationProcedure}.
	 * 
	 * @return <code>true</code> if escalation only.
	 */
	boolean isEscalationOnly();

}