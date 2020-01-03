package net.officefloor.web.spi.security.impl;

import net.officefloor.web.spi.security.HttpSecurityDependencyMetaData;

/**
 * Implementation of the {@link HttpSecurityDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityDependencyMetaDataImpl<O extends Enum<O>> implements HttpSecurityDependencyMetaData<O> {

	/**
	 * Key identifying the dependency.
	 */
	private final O key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Optional qualifier for the type.
	 */
	private String qualifier = null;

	/**
	 * Optional label to describe the dependency.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @param type
	 *            Type of dependency.
	 */
	public HttpSecurityDependencyMetaDataImpl(O key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Specifies a label to describe the dependency.
	 * 
	 * @param label
	 *            Label to describe the dependency.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	public void setTypeQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/*
	 * ================= HttpSecurityDependencyMetaData =================
	 */

	@Override
	public O getKey() {
		return this.key;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.qualifier;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}