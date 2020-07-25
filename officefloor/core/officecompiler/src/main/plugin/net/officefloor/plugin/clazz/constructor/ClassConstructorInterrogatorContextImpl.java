package net.officefloor.plugin.clazz.constructor;

/**
 * {@link ClassConstructorInterrogatorContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
class ClassConstructorInterrogatorContextImpl implements ClassConstructorInterrogatorContext {

	/**
	 * Object {@link Class}
	 */
	private final Class<?> objectClass;

	/**
	 * Error information.
	 */
	private String errorInformation;

	/**
	 * Instantiate.
	 * 
	 * @param objectClass Object {@link Class}.
	 */
	ClassConstructorInterrogatorContextImpl(Class<?> objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * Obtains the error information.
	 * 
	 * @return Error information.
	 */
	public String getErrorInformation() {
		return this.errorInformation;
	}

	/*
	 * ==================== ClassConstructorInterrogatorContext ====================
	 */

	@Override
	public Class<?> getObjectClass() {
		return this.objectClass;
	}

	@Override
	public void setErrorInformation(String errorInformation) {
		this.errorInformation = errorInformation;
	}

}