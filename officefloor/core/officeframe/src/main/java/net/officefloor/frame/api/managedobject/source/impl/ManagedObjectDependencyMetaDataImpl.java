/*-
 * #%L
 * OfficeFrame
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
