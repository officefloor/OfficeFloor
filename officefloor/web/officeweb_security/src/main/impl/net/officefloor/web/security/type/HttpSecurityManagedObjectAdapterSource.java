/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.spi.security.HttpSecurityDependencyMetaData;
import net.officefloor.web.spi.security.HttpSecurityFlowMetaData;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySourceProperty;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * Adapts the {@link HttpSecuritySource} to be a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpSecurityManagedObjectAdapterSource<O extends Enum<O>> implements ManagedObjectSource<O, Indexed> {

	/**
	 * Undertakes the operation for the {@link HttpSecuritySource}.
	 * 
	 * @param <A>                Authentication type.
	 * @param <AC>               Access control type.
	 * @param <C>                Credentials type.
	 * @param <O>                Dependency keys type.
	 * @param <F>                {@link Flow} keys type.
	 * @param httpSecuritySource {@link HttpSecuritySource}.
	 * @param operation          {@link Runnable} containing the operation to
	 *                           undertake.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> void doOperation(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, Runnable operation) {

		// Make safe given that using static field
		synchronized (HttpSecurityManagedObjectAdapterSource.class) {

			// Run the operation
			try {
				operationInstance = httpSecuritySource;
				operation.run();

			} finally {
				// Ensure clear instance
				operationInstance = null;
			}
		}
	}

	/**
	 * {@link HttpSecuritySource} for the operation.
	 */
	private static HttpSecuritySource<?, ?, ?, ?, ?> operationInstance = null;

	/**
	 * {@link HttpSecuritySource} to provide type/functionality.
	 */
	private final HttpSecuritySource<?, ?, ?, O, ?> securitySource;

	/**
	 * Factory to create a {@link PropertyList}.
	 */
	private final Supplier<PropertyList> propertyListFactory;

	/**
	 * {@link HttpSecuritySupportingManagedObjectImpl} instances added by
	 * {@link HttpSecuritySource}.
	 */
	private final List<HttpSecuritySupportingManagedObjectImpl<?>> supportingManagedObjects = new LinkedList<>();

	/**
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private HttpSecuritySourceMetaData<?, ?, ?, O, ?> securitySourceMetaData;

	/**
	 * Should only be used within
	 * {@link #doOperation(HttpSecuritySource, Runnable)}.
	 * 
	 * @throws IllegalStateException If not being loaded within operation context.
	 */
	@SuppressWarnings("unchecked")
	public HttpSecurityManagedObjectAdapterSource() throws IllegalStateException {
		synchronized (HttpSecurityManagedObjectAdapterSource.class) {

			// Ensure with operation context
			if (operationInstance == null) {
				throw new IllegalStateException("Must be within "
						+ HttpSecurityManagedObjectAdapterSource.class.getSimpleName() + " operation context");
			}

			// Specify the HTTP security source
			this.securitySource = (HttpSecuritySource<?, ?, ?, O, ?>) operationInstance;
			this.propertyListFactory = () -> OfficeFloorCompiler.newPropertyList();
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource  {@link HttpSecuritySource}.
	 * @param propertyListFactory Factory to create a {@link PropertyList}.
	 */
	public HttpSecurityManagedObjectAdapterSource(HttpSecuritySource<?, ?, ?, O, ?> httpSecuritySource,
			Supplier<PropertyList> propertyListFactory) {
		this.securitySource = httpSecuritySource;
		this.propertyListFactory = propertyListFactory;
	}

	/**
	 * Obtains the {@link HttpSecuritySourceMetaData}.
	 * 
	 * @return {@link HttpSecuritySourceMetaData}.
	 */
	public HttpSecuritySourceMetaData<?, ?, ?, O, ?> getHttpSecuritySourceMetaData() {
		return this.securitySourceMetaData;
	}

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObjectImpl} instances for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySupportingManagedObjectImpl} instances for the
	 *         {@link HttpSecuritySource}.
	 */
	public HttpSecuritySupportingManagedObjectImpl<?>[] getHttpSecuritySupportingManagedObjects() {
		return this.supportingManagedObjects.toArray(new HttpSecuritySupportingManagedObjectImpl[0]);
	}

	/*
	 * ==================== ManagedObjectSource ========================
	 */

	@Override
	public ManagedObjectSourceSpecification getSpecification() {
		return AdaptFactory.adaptObject(this.securitySource.getSpecification(),
				new AdaptFactory<ManagedObjectSourceSpecification, HttpSecuritySourceSpecification>() {
					@Override
					public ManagedObjectSourceSpecification createAdaptedObject(
							HttpSecuritySourceSpecification delegate) {
						return new HttpSecurityManagedObjectSourceSpecification(delegate);
					}
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectSourceMetaData<O, Indexed> init(ManagedObjectSourceContext<Indexed> context) throws Exception {

		// Initialise
		this.securitySourceMetaData = this.securitySource
				.init(new ManagedObjectHttpSecuritySourceContext<Indexed>(true, context));

		// Obtain the security source
		return AdaptFactory.adaptObject(this.securitySourceMetaData,
				new AdaptFactory<ManagedObjectSourceMetaData, HttpSecuritySourceMetaData>() {
					@Override
					public ManagedObjectSourceMetaData createAdaptedObject(HttpSecuritySourceMetaData delegate) {
						return new HttpSecurityManagedObjectSourceMetaData(delegate);
					}
				});
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
		throw new IllegalStateException(this.getClass().getName() + " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		throw new IllegalStateException(this.getClass().getName() + " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	@Override
	public void stop() {
		throw new IllegalStateException(this.getClass().getName() + " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceSpecification}.
	 */
	private static class HttpSecurityManagedObjectSourceSpecification implements ManagedObjectSourceSpecification {

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		private final HttpSecuritySourceSpecification specification;

		/**
		 * Initiate.
		 * 
		 * @param specification {@link HttpSecuritySourceSpecification}.
		 */
		public HttpSecurityManagedObjectSourceSpecification(HttpSecuritySourceSpecification specification) {
			this.specification = specification;
		}

		/*
		 * ================ ManagedObjectSourceSpecification =============
		 */

		@Override
		public ManagedObjectSourceProperty[] getProperties() {
			return AdaptFactory.adaptArray(this.specification.getProperties(), ManagedObjectSourceProperty.class,
					new AdaptFactory<ManagedObjectSourceProperty, HttpSecuritySourceProperty>() {
						@Override
						public ManagedObjectSourceProperty createAdaptedObject(HttpSecuritySourceProperty delegate) {
							return new HttpSecurityManagedObjectSourceProperty(delegate);
						}
					});
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceProperty}.
	 */
	private static class HttpSecurityManagedObjectSourceProperty implements ManagedObjectSourceProperty {

		/**
		 * {@link HttpSecuritySourceProperty}.
		 */
		private final HttpSecuritySourceProperty property;

		/**
		 * Initiate.
		 * 
		 * @param property {@link HttpSecuritySourceProperty}.
		 */
		public HttpSecurityManagedObjectSourceProperty(HttpSecuritySourceProperty property) {
			this.property = property;
		}

		/*
		 * ================= ManagedObjectSourceProperty ===================
		 */

		@Override
		public String getName() {
			return this.property.getName();
		}

		@Override
		public String getLabel() {
			return this.property.getLabel();
		}
	}

	/**
	 * {@link HttpSecuritySourceContext} adapting the
	 * {@link ManagedObjectSourceContext}.
	 */
	private class ManagedObjectHttpSecuritySourceContext<F extends Enum<F>> extends SourceContextImpl
			implements HttpSecuritySourceContext {

		/**
		 * Initiate.
		 * 
		 * @param isLoadingType Indicates if loading type.
		 * @param context       {@link ManagedObjectSourceContext}.
		 */
		public ManagedObjectHttpSecuritySourceContext(boolean isLoadingType, ManagedObjectSourceContext<F> context) {
			super(context.getName(), isLoadingType, null, context, context);
		}

		/*
		 * ================== HttpSecuritySourceContext ===================
		 */

		@Override
		public <D extends Enum<D>> HttpSecuritySupportingManagedObject<D> addSupportingManagedObject(
				String managedObjectName, ManagedObjectSource<D, ?> managedObjectSource,
				ManagedObjectScope managedObjectScope) {

			// Create and register the supporting managed object
			HttpSecuritySupportingManagedObjectImpl<D> supportingManagedObject = new HttpSecuritySupportingManagedObjectImpl<>(
					managedObjectName, managedObjectSource,
					HttpSecurityManagedObjectAdapterSource.this.propertyListFactory, managedObjectScope);
			HttpSecurityManagedObjectAdapterSource.this.supportingManagedObjects.add(supportingManagedObject);

			// Return the supporting managed object
			return supportingManagedObject;
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceMetaData}.
	 */
	private static class HttpSecurityManagedObjectSourceMetaData<O extends Enum<O>, F extends Enum<F>>
			implements ManagedObjectSourceMetaData<O, F> {

		/**
		 * {@link HttpSecuritySourceMetaData}.
		 */
		private final HttpSecuritySourceMetaData<?, ?, ?, O, ?> metaData;

		/**
		 * Initiate.
		 * 
		 * @param metaData {@link HttpSecuritySourceMetaData}.
		 */
		public HttpSecurityManagedObjectSourceMetaData(HttpSecuritySourceMetaData<?, ?, ?, O, ?> metaData) {
			this.metaData = metaData;
		}

		/*
		 * ================== ManagedObjectSourceMetaData ==================
		 */

		@Override
		public Class<? extends ManagedObject> getManagedObjectClass() {
			return ManagedObject.class;
		}

		@Override
		public Class<?> getObjectClass() {
			return Void.class;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectDependencyMetaData<O>[] getDependencyMetaData() {
			return AdaptFactory.adaptArray(this.metaData.getDependencyMetaData(), ManagedObjectDependencyMetaData.class,
					(delegate) -> new HttpSecurityManagedObjectDependencyMetaData<>(delegate));
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectFlowMetaData<F>[] getFlowMetaData() {
			return (ManagedObjectFlowMetaData<F>[]) AdaptFactory.adaptArray(this.metaData.getFlowMetaData(),
					ManagedObjectFlowMetaData.class,
					(delegate) -> new HttpSecurityManagedObjectFlowMetaData<>(delegate));
		}

		@Override
		public ManagedObjectExecutionMetaData[] getExecutionMetaData() {
			// No execution strategies
			return null;
		}

		@Override
		public ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData() {
			// No extension interfaces
			return null;
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectFlowMetaData}.
	 */
	private static class HttpSecurityManagedObjectFlowMetaData<F extends Enum<F>>
			implements ManagedObjectFlowMetaData<F> {

		/**
		 * {@link HttpSecurityFlowMetaData}.
		 */
		private final HttpSecurityFlowMetaData<F> flow;

		/**
		 * Initiate.
		 * 
		 * @param flow {@link HttpSecurityFlowMetaData}.
		 */
		public HttpSecurityManagedObjectFlowMetaData(HttpSecurityFlowMetaData<F> flow) {
			this.flow = flow;
		}

		/*
		 * ==================== ManagedObjectFlowMetaData ==================
		 */

		@Override
		public F getKey() {
			return this.flow.getKey();
		}

		@Override
		public Class<?> getArgumentType() {
			return this.flow.getArgumentType();
		}

		@Override
		public String getLabel() {
			return this.flow.getLabel();
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectDependencyMetaData}.
	 */
	private static class HttpSecurityManagedObjectDependencyMetaData<D extends Enum<D>>
			implements ManagedObjectDependencyMetaData<D> {

		/**
		 * {@link HttpSecurityDependencyMetaData}.
		 */
		private final HttpSecurityDependencyMetaData<D> dependency;

		/**
		 * Initiate.
		 * 
		 * @param dependency {@link HttpSecurityDependencyMetaData}.
		 */
		public HttpSecurityManagedObjectDependencyMetaData(HttpSecurityDependencyMetaData<D> dependency) {
			this.dependency = dependency;
		}

		/*
		 * =================== ManagedObjectDependencyMetaData ===============
		 */

		@Override
		public D getKey() {
			return this.dependency.getKey();
		}

		@Override
		public Class<?> getType() {
			return this.dependency.getType();
		}

		@Override
		public String getTypeQualifier() {
			return this.dependency.getTypeQualifier();
		}

		@Override
		public String getLabel() {
			return this.dependency.getLabel();
		}

		@Override
		public Object[] getAnnotations() {
			return new Object[0];
		}
	}

}
