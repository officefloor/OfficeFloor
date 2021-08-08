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

package net.officefloor.compile.impl.supplier;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompileConfiguration;
import net.officefloor.compile.spi.supplier.source.SupplierCompletionContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link SupplierSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierSourceContextImpl extends SourceContextImpl
		implements SupplierSourceContext, SupplierCompletionContext, SupplierCompileConfiguration {

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link SupplierThreadLocalTypeImpl} instances.
	 */
	private final List<SupplierThreadLocalTypeImpl<?>> supplierThreadLocals = new LinkedList<>();

	/**
	 * {@link ThreadSynchroniserFactory} instances.
	 */
	private final List<ThreadSynchroniserFactory> threadSynchronisers = new LinkedList<>();

	/**
	 * {@link SupplierCompileCompletion} instances.
	 */
	private final List<SupplierCompileCompletion> compileCompletions = new LinkedList<>();

	/**
	 * {@link SuppliedManagedObjectSourceImpl} instances.
	 */
	private final List<SuppliedManagedObjectSourceTypeImpl> suppliedManagedObjectSources = new LinkedList<>();

	/**
	 * {@link InternalSupplier} instances.
	 */
	private final List<InternalSupplier> internalSuppliers = new LinkedList<>();

	/**
	 * {@link AvailableType} instances.
	 */
	private AvailableType[] availableTypes;

	/**
	 * Indicates whether completing. Once completing, can not add further
	 * {@link SupplierCompileCompletion} instances.
	 */
	private boolean isCompleting = false;

	/**
	 * Indicates whether loaded. Once loaded, no further configuration allowed.
	 */
	private boolean isLoaded = false;

	/**
	 * Initiate.
	 * 
	 * @param supplierSourceName Name of the {@link SupplierSource}.
	 * @param isLoadingType      Indicates if loading type.
	 * @param additionalProfiles Additional profiles.
	 * @param propertyList       {@link PropertyList}.
	 * @param context            {@link NodeContext}.
	 */
	public SupplierSourceContextImpl(String supplierSourceName, boolean isLoadingType, String[] additionalProfiles,
			PropertyList propertyList, NodeContext context) {
		super(supplierSourceName, isLoadingType, additionalProfiles, context.getRootSourceContext(),
				new PropertyListSourceProperties(propertyList));
		this.context = context;
	}

	/**
	 * Flags the {@link SupplierSource} as completing.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 */
	void flagCompleting(AvailableType[] availableTypes) {
		this.availableTypes = availableTypes;
		this.isCompleting = true;
	}

	/**
	 * Ensures the {@link SupplierSource} is not completing.
	 * 
	 * @param itemName Name to report if {@link SupplierSource} completing.
	 */
	private void ensureNotCompleting(String itemName) {
		if (this.isCompleting) {
			throw new IllegalStateException("Unable to add further " + itemName + " as "
					+ SupplierSource.class.getSimpleName() + " completing");
		}
	}

	/**
	 * Flags the {@link SupplierSource} as loaded.
	 */
	void flagLoaded() {
		this.isLoaded = true;
	}

	/**
	 * Ensures this {@link SupplierSource} is not loaded.
	 * 
	 * @param itemName Name to report if {@link SupplierSource} loaded.
	 */
	private void ensureNotLoaded(String itemName) {
		if (this.isLoaded) {
			throw new IllegalStateException("Unable to add further " + itemName + " as SupplierSource loaded");
		}
	}

	/*
	 * ====================== SupplierCompileConfiguration ======================
	 */

	@Override
	public SupplierThreadLocalType[] getSupplierThreadLocalTypes() {
		return this.supplierThreadLocals.stream().toArray(SupplierThreadLocalType[]::new);
	}

	@Override
	public ThreadSynchroniserFactory[] getThreadSynchronisers() {
		return this.threadSynchronisers.stream().toArray(ThreadSynchroniserFactory[]::new);
	}

	@Override
	public SuppliedManagedObjectSourceType[] getSuppliedManagedObjectSourceTypes() {
		return this.suppliedManagedObjectSources.stream().toArray(SuppliedManagedObjectSourceType[]::new);
	}

	@Override
	public InternalSupplier[] getInternalSuppliers() {
		return this.internalSuppliers.stream().toArray(InternalSupplier[]::new);
	}

	@Override
	public SupplierCompileCompletion[] getCompileCompletions() {
		return this.compileCompletions.stream().toArray(SupplierCompileCompletion[]::new);
	}

	/*
	 * ====================== SupplierCompileContext =====================
	 */

	@Override
	public <T> SupplierThreadLocal<T> addSupplierThreadLocal(String qualifier, Class<? extends T> type) {
		this.ensureNotLoaded(SupplierThreadLocal.class.getSimpleName());
		SupplierThreadLocalTypeImpl<T> supplierThreadLocal = new SupplierThreadLocalTypeImpl<>(qualifier, type);
		this.supplierThreadLocals.add(supplierThreadLocal);
		return supplierThreadLocal.getSupplierThreadLocal();
	}

	@Override
	public void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory) {
		this.ensureNotLoaded(ThreadSynchroniser.class.getSimpleName());
		this.threadSynchronisers.add(threadSynchroniserFactory);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> SuppliedManagedObjectSource addManagedObjectSource(String qualifier,
			Class<?> type, ManagedObjectSource<D, F> managedObjectSource) {
		this.ensureNotLoaded(ManagedObject.class.getSimpleName());

		// Create the supplied managed object source
		PropertyList properties = this.context.createPropertyList();
		SuppliedManagedObjectSourceTypeImpl supplied = new SuppliedManagedObjectSourceTypeImpl(type, qualifier,
				managedObjectSource, properties);

		// Register the supplied managed object source
		this.suppliedManagedObjectSources.add(supplied);

		// Return the managed object source for configuring
		return supplied;
	}

	@Override
	public void addInternalSupplier(InternalSupplier internalSupplier) {
		this.ensureNotLoaded(InternalSupplier.class.getSimpleName());

		// Register the internal supplier
		this.internalSuppliers.add(internalSupplier);
	}

	/*
	 * ====================== SupplierSourceContext =====================
	 */

	@Override
	public void addCompileCompletion(SupplierCompileCompletion completion) {
		this.ensureNotCompleting(SupplierCompileCompletion.class.getSimpleName());
		this.compileCompletions.add(completion);
	}

	/*
	 * ====================== SupplierCompletionContext =====================
	 */

	@Override
	public AvailableType[] getAvailableTypes() {
		return this.availableTypes;
	}

}
