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
package net.officefloor.plugin.web.http.security.type;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.plugin.web.http.security.HttpSecurityDependencyMetaData;
import net.officefloor.plugin.web.http.security.HttpSecurityFlowMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceProperty;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;

/**
 * Adapts the {@link HttpSecuritySource} to be a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectAdapterSource<D extends Enum<D>>
		implements ManagedObjectSource<D, Indexed> {

	/**
	 * Undertakes the operation for the {@link HttpSecuritySource}.
	 * 
	 * @param <S>
	 *            Security type.
	 * @param <C>
	 *            Credentials type.
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link JobSequence} keys type.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param operation
	 *            {@link Runnable} containing the operation to undertake.
	 */
	public static <S, C, D extends Enum<D>, F extends Enum<F>> void doOperation(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			Runnable operation) {

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
	private static HttpSecuritySource<?, ?, ?, ?> operationInstance = null;

	/**
	 * {@link HttpSecuritySource} to provide type/functionality.
	 */
	private final HttpSecuritySource<?, ?, D, ?> securitySource;

	/**
	 * Should only be used within
	 * {@link #doOperation(HttpSecuritySource, Runnable)}.
	 * 
	 * @throws IllegalStateException
	 *             If not being loaded within operation context.
	 */
	@SuppressWarnings("unchecked")
	public HttpSecurityManagedObjectAdapterSource()
			throws IllegalStateException {
		synchronized (HttpSecurityManagedObjectAdapterSource.class) {

			// Ensure with operation context
			if (operationInstance == null) {
				throw new IllegalStateException(
						"Must be within "
								+ HttpSecurityManagedObjectAdapterSource.class
										.getSimpleName() + " operation context");
			}

			// Specify the HTTP security source
			this.securitySource = (HttpSecuritySource<?, ?, D, ?>) operationInstance;
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public HttpSecurityManagedObjectAdapterSource(
			HttpSecuritySource<?, ?, D, ?> httpSecuritySource) {
		this.securitySource = httpSecuritySource;
	}

	/*
	 * ==================== ManagedObjectSource ========================
	 */

	@Override
	public ManagedObjectSourceSpecification getSpecification() {
		return AdaptFactory
				.adaptObject(
						this.securitySource.getSpecification(),
						new AdaptFactory<ManagedObjectSourceSpecification, HttpSecuritySourceSpecification>() {
							@Override
							public ManagedObjectSourceSpecification createAdaptedObject(
									HttpSecuritySourceSpecification delegate) {
								return new HttpSecurityManagedObjectSourceSpecification(
										delegate);
							}
						});
	}

	@Override
	public void init(ManagedObjectSourceContext<Indexed> context)
			throws Exception {
		this.securitySource
				.init(new ManagedObjectHttpSecuritySourceContext<Indexed>(true,
						context));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectSourceMetaData<D, Indexed> getMetaData() {
		return AdaptFactory
				.adaptObject(
						this.securitySource.getMetaData(),
						new AdaptFactory<ManagedObjectSourceMetaData, HttpSecuritySourceMetaData>() {
							@Override
							public ManagedObjectSourceMetaData createAdaptedObject(
									HttpSecuritySourceMetaData delegate) {
								return new HttpSecurityManagedObjectSourceMetaData(
										delegate);
							}
						});
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context)
			throws Exception {
		throw new IllegalStateException(this.getClass().getName()
				+ " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		throw new IllegalStateException(this.getClass().getName()
				+ " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	@Override
	public void stop() {
		throw new IllegalStateException(this.getClass().getName()
				+ " should only be used for loading the "
				+ HttpSecurityType.class.getSimpleName());
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceSpecification}.
	 */
	private static class HttpSecurityManagedObjectSourceSpecification implements
			ManagedObjectSourceSpecification {

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		private final HttpSecuritySourceSpecification specification;

		/**
		 * Initiate.
		 * 
		 * @param specification
		 *            {@link HttpSecuritySourceSpecification}.
		 */
		public HttpSecurityManagedObjectSourceSpecification(
				HttpSecuritySourceSpecification specification) {
			this.specification = specification;
		}

		/*
		 * ================ ManagedObjectSourceSpecification =============
		 */

		@Override
		public ManagedObjectSourceProperty[] getProperties() {
			return AdaptFactory
					.adaptArray(
							this.specification.getProperties(),
							ManagedObjectSourceProperty.class,
							new AdaptFactory<ManagedObjectSourceProperty, HttpSecuritySourceProperty>() {
								@Override
								public ManagedObjectSourceProperty createAdaptedObject(
										HttpSecuritySourceProperty delegate) {
									return new HttpSecurityManagedObjectSourceProperty(
											delegate);
								}
							});
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceProperty}.
	 */
	private static class HttpSecurityManagedObjectSourceProperty implements
			ManagedObjectSourceProperty {

		/**
		 * {@link HttpSecuritySourceProperty}.
		 */
		private final HttpSecuritySourceProperty property;

		/**
		 * Initiate.
		 * 
		 * @param property
		 *            {@link HttpSecuritySourceProperty}.
		 */
		public HttpSecurityManagedObjectSourceProperty(
				HttpSecuritySourceProperty property) {
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
	private static class ManagedObjectHttpSecuritySourceContext<F extends Enum<F>>
			extends SourceContextImpl implements HttpSecuritySourceContext {

		/**
		 * Initiate.
		 * 
		 * @param isLoadingType
		 *            Indicates if loading type.
		 * @param context
		 *            {@link ManagedObjectSourceContext}.
		 */
		public ManagedObjectHttpSecuritySourceContext(boolean isLoadingType,
				ManagedObjectSourceContext<F> context) {
			super(isLoadingType, context, context);
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceMetaData}.
	 */
	private static class HttpSecurityManagedObjectSourceMetaData<D extends Enum<D>, F extends Enum<F>>
			implements ManagedObjectSourceMetaData<D, F> {

		/**
		 * {@link HttpSecuritySourceMetaData}.
		 */
		private final HttpSecuritySourceMetaData<?, ?, D, ?> metaData;

		/**
		 * Initiate.
		 * 
		 * @param metaData
		 *            {@link HttpSecuritySourceMetaData}.
		 */
		public HttpSecurityManagedObjectSourceMetaData(
				HttpSecuritySourceMetaData<?, ?, D, ?> metaData) {
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
			return this.metaData.getSecurityClass();
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedObjectDependencyMetaData<D>[] getDependencyMetaData() {
			return AdaptFactory
					.adaptArray(
							this.metaData.getDependencyMetaData(),
							ManagedObjectDependencyMetaData.class,
							new AdaptFactory<ManagedObjectDependencyMetaData, HttpSecurityDependencyMetaData>() {
								@Override
								public ManagedObjectDependencyMetaData createAdaptedObject(
										HttpSecurityDependencyMetaData delegate) {
									return new HttpSecurityManagedObjectDependencyMetaData<D>(
											delegate);
								}
							});
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedObjectFlowMetaData<F>[] getFlowMetaData() {
			return AdaptFactory
					.adaptArray(
							this.metaData.getFlowMetaData(),
							ManagedObjectFlowMetaData.class,
							new AdaptFactory<ManagedObjectFlowMetaData, HttpSecurityFlowMetaData>() {
								@Override
								public ManagedObjectFlowMetaData<F> createAdaptedObject(
										HttpSecurityFlowMetaData delegate) {
									return new HttpSecurityManagedObjectFlowMetaData<F>(
											delegate);
								}
							});
		}

		@Override
		public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
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
		 * @param flow
		 *            {@link HttpSecurityFlowMetaData}.
		 */
		public HttpSecurityManagedObjectFlowMetaData(
				HttpSecurityFlowMetaData<F> flow) {
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
		 * @param dependency
		 *            {@link HttpSecurityDependencyMetaData}.
		 */
		public HttpSecurityManagedObjectDependencyMetaData(
				HttpSecurityDependencyMetaData<D> dependency) {
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
	}

}