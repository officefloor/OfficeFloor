/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
