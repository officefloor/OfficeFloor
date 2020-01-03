package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link SectionOutputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionOutputTypeImpl implements SectionOutputType {

	/**
	 * Name of the {@link SectionOutput}.
	 */
	private final String outputName;

	/**
	 * Argument type of the {@link SectionOutput}.
	 */
	private final String argumentType;

	/**
	 * Flag indicating if {@link Escalation} only.
	 */
	private final boolean isEscalationOnly;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutput}.
	 * @param argumentType
	 *            Argument type of the {@link SectionOutput}.
	 * @param isEscalationOnly
	 *            Flag indicating if {@link Escalation} only.
	 * @param annotations
	 *            Annotations.
	 */
	public SectionOutputTypeImpl(String outputName, String argumentType, boolean isEscalationOnly,
			Object[] annotations) {
		this.outputName = outputName;
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
		this.annotations = annotations;
	}

	/*
	 * ====================== SectionOutputType =============================
	 */

	@Override
	public String getSectionOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	@Override
	public boolean isEscalationOnly() {
		return this.isEscalationOnly;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}