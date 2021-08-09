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

package net.officefloor.compile.internal.structure;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Auto-wire.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWire implements Comparable<AutoWire> {

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * <p>
	 * Type {@link Class}.
	 * <p>
	 * This is used (if available) in obtain the type {@link Class}, as some derived
	 * classes can not be loaded.
	 */
	private final Class<?> typeClass;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 */
	public AutoWire(String qualifier, String type) {
		this.qualifier = qualifier;
		this.type = type;
		this.typeClass = null;
	}

	/**
	 * Instantiate with only type.
	 * 
	 * @param type Type.
	 */
	public AutoWire(String type) {
		this(null, type);
	}

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier {@link Annotation}.
	 * @param type      Type.
	 */
	public AutoWire(Class<? extends Annotation> qualifier, Class<?> type) {
		this(qualifier.getName(), type);
	}

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier.
	 * @param type      Type.
	 */
	public AutoWire(String qualifier, Class<?> type) {
		this.qualifier = qualifier;
		this.type = type.getName();
		this.typeClass = type;
	}

	/**
	 * Instantiate.
	 * 
	 * @param type Type.
	 */
	public AutoWire(Class<?> type) {
		this((String) null, type);
	}

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	public String getQualifier() {
		return this.qualifier;
	}

	/**
	 * Obtains the type.
	 * 
	 * @return Type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Obtains the type.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return Type {@link Class} or <code>null</code> if unable to retrieve.
	 */
	public Class<?> getTypeClass(SourceContext sourceContext) {
		return this.typeClass != null ? this.typeClass : sourceContext.loadOptionalClass(this.type);
	}

	/*
	 * ================== Object =======================
	 */

	@Override
	public String toString() {
		return (this.qualifier != null ? this.qualifier + ":" : "") + this.type;
	}

	@Override
	public int hashCode() {
		return (this.qualifier == null ? 1 : this.qualifier.hashCode()) * 13 + this.type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		// Ensure correct type
		if (!(obj instanceof AutoWire)) {
			return false;
		}
		AutoWire that = (AutoWire) obj;

		// Ensure match on type
		if (!this.type.equals(that.type)) {
			return false;
		}

		// Match on qualifier
		if (this.qualifier != null) {
			// Must be same qualifier
			return this.qualifier.equals(that.qualifier);
		} else {
			// Must be no qualification
			return (that.qualifier == null);
		}
	}

	/*
	 * ================= Comparable ====================
	 */

	@Override
	public int compareTo(AutoWire that) {
		return String.CASE_INSENSITIVE_ORDER.compare(this.toString(), that.toString());
	}

}
