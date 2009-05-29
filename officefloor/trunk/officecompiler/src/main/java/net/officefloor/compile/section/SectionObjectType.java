/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.section;

/**
 * <code>Type definition</code> of an {@link Object} dependency required by the
 * {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObjectType {

	/**
	 * Obtains the name of this {@link SectionObjectType}.
	 * 
	 * @return Name of this {@link SectionObjectType}.
	 */
	String getSectionObjectName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the {@link Object} type
	 * for this {@link SectionObjectType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be
	 * available to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the {@link Object} type.
	 */
	String getObjectType();

}