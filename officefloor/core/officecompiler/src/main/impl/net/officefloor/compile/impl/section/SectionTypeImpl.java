package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;

/**
 * {@link SectionType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionTypeImpl implements SectionType {

	/**
	 * {@link SectionInputType} instances.
	 */
	private final SectionInputType[] inputTypes;

	/**
	 * {@link SectionOutputType} instances.
	 */
	private final SectionOutputType[] outputTypes;

	/**
	 * {@link SectionObjectType} instances.
	 */
	private final SectionObjectType[] objectTypes;

	/**
	 * Instantiate.
	 * 
	 * @param inputTypes
	 *            {@link SectionInputType} instances.
	 * @param outputTypes
	 *            {@link SectionOutputType} instances.
	 * @param objectTypes
	 *            {@link SectionObjectType} instances.
	 */
	public SectionTypeImpl(SectionInputType[] inputTypes,
			SectionOutputType[] outputTypes, SectionObjectType[] objectTypes) {
		this.inputTypes = inputTypes;
		this.outputTypes = outputTypes;
		this.objectTypes = objectTypes;
	}

	/*
	 * ================== SectionType =========================
	 */

	@Override
	public SectionInputType[] getSectionInputTypes() {
		return this.inputTypes;
	}

	@Override
	public SectionOutputType[] getSectionOutputTypes() {
		return this.outputTypes;
	}

	@Override
	public SectionObjectType[] getSectionObjectTypes() {
		return this.objectTypes;
	}

}