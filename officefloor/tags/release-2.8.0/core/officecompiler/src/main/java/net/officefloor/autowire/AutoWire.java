/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.autowire;

import java.lang.annotation.Annotation;

import net.officefloor.compile.impl.util.CompileUtil;

/**
 * Provides details of an auto-wire.
 * 
 * @author Daniel Sagenschneider
 */
public final class AutoWire {

	/**
	 * Creates the {@link AutoWire} from the qualified type.
	 * 
	 * @param qualifiedType
	 *            Qualified type.
	 * @return {@link AutoWire}.
	 */
	public static AutoWire valueOf(String qualifiedType) {

		// Determine if qualifier and type
		String qualifier;
		String type;
		int index = qualifiedType.lastIndexOf('-');
		if (index < 0) {
			// No qualifier, just type
			qualifier = null;
			type = qualifiedType;

		} else {
			// Parse out the qualifier (should not be empty string)
			qualifier = qualifiedType.substring(0, index);
			qualifier = (CompileUtil.isBlank(qualifier) ? null : qualifier);

			// Parse out the type
			type = qualifiedType.substring(index + 1); // ignore '-'
		}

		// Create and return the auto-wire
		return new AutoWire(qualifier, type);
	}

	/**
	 * <p>
	 * Qualifier of the {@link AutoWire}.
	 * <p>
	 * This enables distinguishing should there be dependencies of same type.
	 */
	private final String qualifier;

	/**
	 * Type of the {@link AutoWire}.
	 */
	private final String type;

	/**
	 * Allows type safe qualified instantiation.
	 * 
	 * @param qualifier
	 *            Qualifier {@link Annotation}.
	 * @param type
	 *            Type.
	 */
	public AutoWire(Class<? extends Annotation> qualifier, Class<?> type) {
		this(qualifier.getName(), type.getName());
	}

	/**
	 * Allows type safe default instantiation.
	 * 
	 * @param type
	 *            Type.
	 */
	public AutoWire(Class<?> type) {
		this(null, type.getName());
	}

	/**
	 * <p>
	 * Allows generic qualified instantiation.
	 * <p>
	 * This is available for configuration and preference for coded
	 * instantiation should be with the type safe constructors.
	 * 
	 * @param qualifier
	 *            Qualifier.
	 * @param type
	 *            Type.
	 */
	public AutoWire(String qualifier, String type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/**
	 * <p>
	 * Allows generic default instantiation.
	 * <p>
	 * This is available for configuration and preference for coded
	 * instantiation should be with the type safe constructors.
	 * 
	 * @param type
	 *            Type.
	 */
	public AutoWire(String type) {
		this(null, type);
	}

	/**
	 * <p>
	 * Obtains the qualifier.
	 * <p>
	 * This enables distinguishing should there be dependencies of same type.
	 * 
	 * @return Qualifier. Will be <code>null</code> for default type.
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
	 * Obtains the qualified type.
	 * 
	 * @return Qualified type.
	 */
	public String getQualifiedType() {
		return (this.qualifier == null ? "" : this.qualifier + "-") + this.type;
	}

	/*
	 * ========================== Object ===========================
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.qualifier == null) ? 0 : this.qualifier.hashCode());
		result = prime * result + this.type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		// Determine if same object
		if (this == obj)
			return true;

		// Ensure correct type
		if (!(obj instanceof AutoWire)) {
			return false;
		}
		AutoWire that = (AutoWire) obj;

		// Ensure types match
		if (!(this.type.equals(that.type))) {
			return false;
		}

		// Ensure qualifiers match
		if ((this.qualifier == null) && (that.qualifier == null)) {
			return true; // no qualifier match
		} else if ((this.qualifier != null) && (that.qualifier != null)) {
			// Match is same qualifier
			return (this.qualifier.equals(that.qualifier));
		} else {
			// Qualifiers do not match as one is null while other not null
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getQualifiedType();
	}

}