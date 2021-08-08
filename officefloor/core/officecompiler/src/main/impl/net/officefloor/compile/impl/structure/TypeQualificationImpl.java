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
