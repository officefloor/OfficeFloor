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