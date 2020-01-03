package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;

/**
 * {@link HttpSecurityDependencyType} adapted from the
 * {@link ManagedObjectDependencyType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityDependencyTypeImpl<O extends Enum<O>> implements HttpSecurityDependencyType<O> {

	/**
	 * {@link ManagedObjectDependencyType}.
	 */
	private final ManagedObjectDependencyType<O> dependency;

	/**
	 * Initiate.
	 * 
	 * @param dependency {@link ManagedObjectDependencyType}.
	 */
	public HttpSecurityDependencyTypeImpl(ManagedObjectDependencyType<O> dependency) {
		this.dependency = dependency;
	}

	/*
	 * ============= HttpSecurityDependencyType =========================
	 */

	@Override
	public String getDependencyName() {
		return this.dependency.getDependencyName();
	}

	@Override
	public int getIndex() {
		return this.dependency.getIndex();
	}

	@Override
	public Class<?> getDependencyType() {
		return this.dependency.getDependencyType();
	}

	@Override
	public String getTypeQualifier() {
		return this.dependency.getTypeQualifier();
	}

	@Override
	public O getKey() {
		return this.dependency.getKey();
	}
}