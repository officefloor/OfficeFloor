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

import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;

/**
 * Implementation of {@link ManagedObjectExtensionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExtensionMetaDataImpl<E extends Object> implements ManagedObjectExtensionMetaData<E> {

	/**
	 * Extension type.
	 */
	private final Class<E> type;

	/**
	 * {@link ExtensionFactory}.
	 */
	private final ExtensionFactory<E> factory;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Extension type.
	 * @param factory
	 *            {@link ExtensionFactory}.
	 */
	public ManagedObjectExtensionMetaDataImpl(Class<E> type, ExtensionFactory<E> factory) {
		this.type = type;
		this.factory = factory;
	}

	/*
	 * ================== ManagedObjectExtensionMetaData ==============
	 */

	@Override
	public Class<E> getExtensionType() {
		return this.type;
	}

	@Override
	public ExtensionFactory<E> getExtensionFactory() {
		return this.factory;
	}

}
