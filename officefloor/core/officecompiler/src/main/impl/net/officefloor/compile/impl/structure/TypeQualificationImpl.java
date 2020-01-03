package net.officefloor.compile.impl.structure;

import net.officefloor.compile.section.TypeQualification;

/**
 * {@link TypeQualification} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeQualificationImpl implements TypeQualification {

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * Initiate.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param type
	 *            Type.
	 */
	public TypeQualificationImpl(String qualifier, String type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/*
	 * ==================== TypeQualification =======================
	 */

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public String getType() {
		return this.type;
	}

}