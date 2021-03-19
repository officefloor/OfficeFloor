/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.managedobject.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;

/**
 * Implementation of the {@link ManagedObjectDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyMetaDataImpl<O extends Enum<O>> implements ManagedObjectDependencyMetaData<O> {

	/**
	 * Key identifying the dependency.
	 */
	private final O key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Annotations for the dependency.
	 */
	private final List<Object> annotations = new LinkedList<>();

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
	 * @param key  Key identifying the dependency.
	 * @param type Type of dependency.
	 */
	public ManagedObjectDependencyMetaDataImpl(O key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Specifies a label to describe the dependency.
	 * 
	 * @param label Label to describe the dependency.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier Type qualifier.
	 */
	public void setTypeQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * Adds an annotation to describe the dependency.
	 * 
	 * @param annotation Annotation to describe the dependency.
	 */
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
	}

	/*
	 * ================= ManagedObjectDependencyMetaData =================
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
	public Object[] getAnnotations() {
		return this.annotations.toArray(new Object[this.annotations.size()]);
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
