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

package net.officefloor.compile.test.supplier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.SuppliedManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.impl.supplier.SuppliedManagedObjectSourceTypeImpl;
import net.officefloor.compile.impl.supplier.SupplierThreadLocalTypeImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompletionContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Utility class for testing a {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierLoaderUtil {

	/**
	 * <p>
	 * Safely obtains the {@link SupplierCompletionContext} from the
	 * {@link SupplierSourceContext}.
	 * <p>
	 * This aids in testing of {@link SupplierCompileCompletion}.
	 * 
	 * @param context {@link SupplierSourceContext}.
	 * @return {@link SupplierCompletionContext} corresponding to the
	 *         {@link SupplierSourceContext}.
	 */
	public static SupplierCompletionContext getSupplierCompletionContext(SupplierSourceContext context) {
		return (SupplierCompletionContext) context;
	}

	/**
	 * Validates the {@link SupplierSourceSpecification} for the
	 * {@link SupplierSource}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @param propertyNameLabels  Listing of name/label pairs for the
	 *                            {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends SupplierSource> PropertyList validateSpecification(Class<S> supplierSourceClass,
			String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null).getSupplierLoader()
				.loadSpecification(supplierSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link SupplierTypeBuilder} to create the expected
	 * {@link InitialSupplierType}.
	 * 
	 * @return {@link SupplierTypeBuilder} to build the expected
	 *         {@link InitialSupplierType}.
	 */
	public static SupplierTypeBuilder createSupplierTypeBuilder() {
		return new SupplierTypeBuilderImpl();
	}

	/**
	 * Convenience method that validates the {@link InitialSupplierType} loaded from
	 * the input {@link SupplierSource} against the expected
	 * {@link InitialSupplierType} from the {@link SupplierTypeBuilder}.
	 * 
	 * @param <S>                  {@link SupplierSource} type.
	 * @param expectedSupplierType {@link SupplierTypeBuilder} that has had the
	 *                             expected {@link InitialSupplierType} built
	 *                             against it.
	 * @param supplierSourceClass  {@link SupplierSource} class.
	 * @param propertyNameValues   Listing of name/value pairs that comprise the
	 *                             properties for the {@link SupplierSource}.
	 * @return Loaded {@link InitialSupplierType}.
	 */
	public static <S extends SupplierSource> InitialSupplierType validateInitialSupplierType(
			SupplierTypeBuilder expectedSupplierType, Class<S> supplierSourceClass, String... propertyNameValues) {

		// Load the supplier type
		InitialSupplierType aType = loadInitialSupplierType(supplierSourceClass, propertyNameValues);

		// Undertake valuation
		validateSupplierType(expectedSupplierType, aType);

		// Return the initial supplier type
		return aType;
	}

	/**
	 * Convenience method that validates the {@link SupplierType} loaded from the
	 * input {@link SupplierSource} against the expected {@link SupplierType} from
	 * the {@link SupplierTypeBuilder}.
	 * 
	 * @param <S>                  {@link SupplierSource} type.
	 * @param expectedSupplierType {@link SupplierTypeBuilder} that has had the
	 *                             expected {@link SupplierType} built against it.
	 * @param supplierSourceClass  {@link SupplierSource} class.
	 * @param propertyNameValues   Listing of name/value pairs that comprise the
	 *                             properties for the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType validateSupplierType(SupplierTypeBuilder expectedSupplierType,
			Class<S> supplierSourceClass, String... propertyNameValues) {

		// Load the supplier type
		SupplierType aType = loadSupplierType(supplierSourceClass, propertyNameValues);

		// Undertake valuation
		validateSupplierType(expectedSupplierType, aType);

		// Return the initial supplier type
		return aType;
	}

	/**
	 * Validates the {@link SupplierType}.
	 * 
	 * @param <S>                  {@link SupplierSource} type.
	 * @param expectedSupplierType {@link SupplierTypeBuilder} that has had the
	 *                             expected {@link InitialSupplierType} built
	 *                             against it.
	 * @param aType                Actual {@link SupplierType}.
	 */
	private static <S extends SupplierSource> void validateSupplierType(SupplierTypeBuilder expectedSupplierType,
			SupplierType aType) {

		// Cast to obtain expected supplier type
		if (!(expectedSupplierType instanceof SupplierType)) {
			TestCase.fail("expectedSupplierType must be created from createSupplierTypeBuilder");
		}
		SupplierType eType = (SupplierType) expectedSupplierType;

		// Ensure the set of supplier thread locals match
		Function<SupplierType, Map<String, SupplierThreadLocalType>> extractThreadLocals = (type) -> {
			Map<String, SupplierThreadLocalType> threadLocalTypes = new HashMap<>();
			for (SupplierThreadLocalType threadLocalType : type.getSupplierThreadLocalTypes()) {
				String name = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(threadLocalType.getQualifier(),
						threadLocalType.getObjectType().getName());
				Assert.assertNull(
						SupplierThreadLocalType.class.getSimpleName() + " already registered for qualifier "
								+ threadLocalType.getQualifier() + " type " + threadLocalType.getObjectType().getName(),
						threadLocalTypes.get(name));
				threadLocalTypes.put(name, threadLocalType);
			}
			return threadLocalTypes;
		};
		Map<String, SupplierThreadLocalType> eThreadLocals = extractThreadLocals.apply(eType);
		Map<String, SupplierThreadLocalType> aThreadLocals = extractThreadLocals.apply(aType);
		Assert.assertEquals("Incorrect number of " + SupplierThreadLocalType.class.getSimpleName() + " instances",
				eThreadLocals.size(), aThreadLocals.size());
		for (String name : eThreadLocals.keySet()) {
			SupplierThreadLocalType eThreadLocal = eThreadLocals.get(name);
			SupplierThreadLocalType aThreadLocal = aThreadLocals.get(name);

			// Ensure have actual for the expected
			Assert.assertNotNull(
					"No " + SupplierThreadLocalType.class.getSimpleName() + " for qualifier "
							+ eThreadLocal.getQualifier() + " type " + eThreadLocal.getObjectType().getName(),
					aThreadLocal);

			// Only qualifier and type so already checked by existing
		}

		// Ensure correct number of thread synchronisers
		ThreadSynchroniserFactory[] eThreadSynchronisers = eType.getThreadSynchronisers();
		ThreadSynchroniserFactory[] aThreadSynchronisers = aType.getThreadSynchronisers();
		Assert.assertEquals("Incorrect number of " + ThreadSynchroniserFactory.class.getSimpleName() + " instances",
				eThreadSynchronisers.length, aThreadSynchronisers.length);

		// Ensure correct number of internal suppliers
		InternalSupplier[] eInternalSuppliers = eType.getInternalSuppliers();
		InternalSupplier[] aInternalSuppliers = aType.getInternalSuppliers();
		JUnitAgnosticAssert.assertEquals(eInternalSuppliers.length, aInternalSuppliers.length,
				"Incorrect number of " + InternalSupplier.class.getSimpleName() + " instances");

		// Ensure the set of supplied managed object sources match
		Function<SupplierType, Map<String, SuppliedManagedObjectSourceType>> extractManagedObjectSources = (type) -> {
			Map<String, SuppliedManagedObjectSourceType> mosTypes = new HashMap<>();
			for (SuppliedManagedObjectSourceType mosType : type.getSuppliedManagedObjectTypes()) {
				String name = SuppliedManagedObjectSourceNodeImpl
						.getSuppliedManagedObjectSourceName(mosType.getQualifier(), mosType.getObjectType().getName());
				Assert.assertNull(
						SuppliedManagedObjectSourceType.class.getSimpleName() + " already registered for qualifier "
								+ mosType.getQualifier() + " type " + mosType.getObjectType().getName(),
						mosTypes.get(name));
				mosTypes.put(name, mosType);
			}
			return mosTypes;
		};
		Map<String, SuppliedManagedObjectSourceType> eManagedObjectSources = extractManagedObjectSources.apply(eType);
		Map<String, SuppliedManagedObjectSourceType> aManagedObjectSources = extractManagedObjectSources.apply(aType);
		Assert.assertEquals(
				"Incorrect number of " + SuppliedManagedObjectSourceType.class.getSimpleName() + " instances",
				eManagedObjectSources.size(), aManagedObjectSources.size());
		for (String name : eManagedObjectSources.keySet()) {
			SuppliedManagedObjectSourceType eMos = eManagedObjectSources.get(name);
			SuppliedManagedObjectSourceType aMos = aManagedObjectSources.get(name);

			// Assert have actual for expected
			String suffix = " for qualifier " + eMos.getQualifier() + " type " + eMos.getObjectType().getName();
			Assert.assertNotNull("No " + SuppliedManagedObjectSourceType.class.getSimpleName() + suffix, aMos);
			// As here, qualifier and type are verified

			// Verify remaining details
			ManagedObjectSource<?, ?> aManagedObjectSource = aMos.getManagedObjectSource();
			Assert.assertNotNull("Should have " + ManagedObjectSource.class.getSimpleName() + suffix,
					aManagedObjectSource);
			ManagedObjectSource<?, ?> eMosSource = eMos.getManagedObjectSource();
			if (eMosSource != null) {
				// Only check if provided (null to match anything)
				Assert.assertEquals("Incorrect " + ManagedObjectSource.class.getSimpleName() + suffix,
						eMos.getManagedObjectSource().getClass(), aManagedObjectSource.getClass());
			}

			// Ensure correct properties
			Iterator<Property> eProperties = eMos.getPropertyList().iterator();
			Iterator<Property> aProperties = aMos.getPropertyList().iterator();
			int index = 0;
			while (eProperties.hasNext()) {
				Assert.assertTrue("Expecting more properties after " + index + suffix, aProperties.hasNext());
				Property eProperty = eProperties.next();
				Property aProperty = aProperties.next();
				Assert.assertEquals("Incorrect name for property " + index + suffix, eProperty.getName(),
						aProperty.getName());
				Assert.assertEquals("Incorrect value for property " + index + suffix, eProperty.getValue(),
						aProperty.getValue());
				Assert.assertEquals("Incorrect label for property " + index + suffix, eProperty.getLabel(),
						aProperty.getLabel());
				index++;
			}
		}
	}

	/**
	 * Convenience method that loads the {@link InitialSupplierType} by obtaining
	 * the {@link ClassLoader} from the {@link SupplierSource} class.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @param propertyNameValues  Listing of name/value pairs that comprise the
	 *                            properties for the {@link SupplierSource}.
	 * @return Loaded {@link InitialSupplierType}.
	 */
	public static <S extends SupplierSource> InitialSupplierType loadInitialSupplierType(Class<S> supplierSourceClass,
			String... propertyNameValues) {
		// Return the loaded supplier
		return loadInitialSupplierType(supplierSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link InitialSupplierType} with the
	 * provided {@link OfficeFloorCompiler}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues  Listing of name/value pairs that comprise the
	 *                            properties for the {@link SupplierSource}.
	 * @return Loaded {@link InitialSupplierType}.
	 */
	public static <S extends SupplierSource> InitialSupplierType loadInitialSupplierType(Class<S> supplierSourceClass,
			OfficeFloorCompiler compiler, String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Return the loaded supplier
		return getOfficeFloorCompiler(compiler).getSupplierLoader().loadInitialSupplierType(supplierSourceClass,
				propertyList);
	}

	/**
	 * Convenience method that loads the {@link SupplierType} by obtaining the
	 * {@link ClassLoader} from the {@link SupplierSource} class.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @param propertyNameValues  Listing of name/value pairs that comprise the
	 *                            properties for the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType loadSupplierType(Class<S> supplierSourceClass,
			String... propertyNameValues) {
		// Return the loaded supplier
		return loadSupplierType(supplierSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link SupplierType} with the provided
	 * {@link OfficeFloorCompiler}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues  Listing of name/value pairs that comprise the
	 *                            properties for the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType loadSupplierType(Class<S> supplierSourceClass,
			OfficeFloorCompiler compiler, String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Return the loaded supplier
		SupplierLoader supplierLoader = getOfficeFloorCompiler(compiler).getSupplierLoader();
		InitialSupplierType initialSupplierType = supplierLoader.loadInitialSupplierType(supplierSourceClass,
				propertyList);
		return supplierLoader.loadSupplierType(initialSupplierType);
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(OfficeFloorCompiler compiler) {
		if (compiler == null) {
			// Create the office floor compiler that fails on first issue
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private SupplierLoaderUtil() {
	}

	/**
	 * {@link SupplierTypeBuilder} implementation.
	 */
	private static class SupplierTypeBuilderImpl implements SupplierTypeBuilder, SupplierType,
			ThreadSynchroniserFactory, InternalSupplier, SupplierCompileCompletion {

		/**
		 * {@link SupplierThreadLocalType} instances.
		 */
		private final List<SupplierThreadLocalType> supplierThreadLocalTypes = new LinkedList<>();

		/**
		 * {@link ThreadSynchroniserFactory} instances.
		 */
		private final List<ThreadSynchroniserFactory> threadSynchronisers = new LinkedList<>();

		/**
		 * {@link SuppliedManagedObjectSourceType} instances.
		 */
		private final List<SuppliedManagedObjectSourceType> suppliedManagedObjectSourceTypes = new LinkedList<>();

		/**
		 * {@link InternalSupplier} instances.
		 */
		private final List<InternalSupplier> internalSuppliers = new LinkedList<>();

		/*
		 * ================ SupplierTypeBuilder ==================
		 */

		@Override
		public void addSupplierThreadLocal(String qualifier, Class<?> objectType) {
			SupplierThreadLocalTypeImpl<?> threadLocal = new SupplierThreadLocalTypeImpl<>(qualifier, objectType);
			this.supplierThreadLocalTypes.add(threadLocal);
		}

		@Override
		public void addThreadSynchroniser() {
			this.threadSynchronisers.add(this);
		}

		@Override
		public void addInternalSupplier() {
			this.internalSuppliers.add(this);
		}

		@Override
		public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> PropertyList addSuppliedManagedObjectSource(
				String qualifier, Class<?> objectType, MS managedObjectSource) {
			PropertyList properties = SupplierLoaderUtil.getOfficeFloorCompiler(null).createPropertyList();
			SuppliedManagedObjectSourceTypeImpl mos = new SuppliedManagedObjectSourceTypeImpl(objectType, qualifier,
					managedObjectSource, properties);
			this.suppliedManagedObjectSourceTypes.add(mos);
			return properties;
		}

		/*
		 * =================== SupplierType ======================
		 */

		@Override
		public SupplierThreadLocalType[] getSupplierThreadLocalTypes() {
			return this.supplierThreadLocalTypes
					.toArray(new SupplierThreadLocalType[this.supplierThreadLocalTypes.size()]);
		}

		@Override
		public ThreadSynchroniserFactory[] getThreadSynchronisers() {
			return this.threadSynchronisers.toArray(new ThreadSynchroniserFactory[this.threadSynchronisers.size()]);
		}

		@Override
		public SuppliedManagedObjectSourceType[] getSuppliedManagedObjectTypes() {
			return this.suppliedManagedObjectSourceTypes
					.toArray(new SuppliedManagedObjectSourceType[this.suppliedManagedObjectSourceTypes.size()]);
		}

		@Override
		public InternalSupplier[] getInternalSuppliers() {
			return this.internalSuppliers.toArray(new InternalSupplier[this.internalSuppliers.size()]);
		}

		/*
		 * ============= ThreadSynchroniserFactory ================
		 */

		@Override
		public ThreadSynchroniser createThreadSynchroniser() {
			throw new IllegalStateException("Mock " + ThreadSynchroniser.class.getSimpleName() + " for "
					+ SupplierTypeBuilder.class.getSimpleName() + " can not be used");
		}

		/*
		 * =================== InternalSupplier ====================
		 */

		@Override
		public boolean isObjectAvailable(String qualifier, Class<?> objectType) {
			throw new IllegalStateException("Mock " + InternalSupplier.class.getSimpleName() + " for "
					+ SupplierTypeBuilder.class.getSimpleName() + " can not be used");
		}

		@Override
		public <O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user)
				throws UnknownObjectException {
			throw new IllegalStateException("Mock " + InternalSupplier.class.getSimpleName() + " for "
					+ SupplierTypeBuilder.class.getSimpleName() + " can not be used");
		}

		/*
		 * ============== SupplierCompileCompletion ===============
		 */

		@Override
		public void complete(SupplierCompletionContext context) throws Exception {
			throw new IllegalStateException("Mock " + SupplierCompileCompletion.class.getSimpleName() + " for "
					+ SupplierTypeBuilder.class.getSimpleName() + " can not be used");
		}
	}

}
