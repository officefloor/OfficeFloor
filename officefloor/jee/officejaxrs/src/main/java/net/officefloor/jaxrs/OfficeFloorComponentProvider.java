package net.officefloor.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link OfficeFloor} {@link ComponentProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorComponentProvider implements ComponentProvider {

	/**
	 * {@link OfficeFloorDependencies}.
	 */
	private final OfficeFloorDependencies dependencies = new OfficeFloorDependencies();

	/**
	 * {@link InjectionManager}.
	 */
	private InjectionManager injectionManager;

	/*
	 * ================== ComponentProvider ===================
	 */

	@Override
	public void initialize(InjectionManager injectionManager) {
		this.injectionManager = injectionManager;

		// Register OfficeFloor dependencies
		this.injectionManager.register(Bindings.injectionResolver(new DependencyInjectionResolver(this.dependencies)));

		// Register to have OfficeFloor fulfill remaining dependencies
		ImmediateHk2InjectionManager immediateInjectionManager = (ImmediateHk2InjectionManager) injectionManager;
		ServiceLocator serviceLocator = immediateInjectionManager.getServiceLocator();
		if (serviceLocator.getBestDescriptor(
				BuilderHelper.createContractFilter(OfficeFloorIntoHk2Bridge.class.getName())) == null) {

			// Add the OfficeFloor bridge
			DynamicConfigurationService configurationService = serviceLocator
					.getService(DynamicConfigurationService.class);
			if (configurationService != null) {
				DynamicConfiguration configuration = configurationService.createDynamicConfiguration();
				configuration.addActiveDescriptor(OfficeFloorIntoHk2BridgeImpl.class);
				configuration.commit();
			}
		}
		OfficeFloorIntoHk2Bridge officeFloorBridge = injectionManager.getInstance(OfficeFloorIntoHk2Bridge.class);
		officeFloorBridge.bridgeOfficeFloor(this.dependencies);
	}

	@Override
	public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {

		// Obtain the servlet manager
		ServletManager servletManager = ServletSupplierSource.getServletManager();
		AvailableType[] availableTypes = servletManager.getAvailableTypes();

		// Iterate through fields determining if dependency from OfficeFloor
		while (component != null) {
			NEXT_FIELD: for (Field field : component.getDeclaredFields()) {

				// Details for injection
				String qualifier = null;
				Class<?> type = null;

				// Determine if inject dependency
				if (field.isAnnotationPresent(Inject.class)) {

					// CDI Inject, so obtain details
					type = field.getType();

					// Determine qualifier
					for (Annotation annotation : field.getAnnotations()) {
						Class<?> annotationType = annotation.annotationType();
						if (annotationType.isAnnotationPresent(javax.inject.Qualifier.class)) {
							qualifier = annotationType.getName();
						}
					}

				} else if (field.isAnnotationPresent(Dependency.class)) {

					// OfficeFloor dependency, so obtain details
					type = field.getType();

					// Determine qualifier
					try {
						String dependencyName = field.getDeclaringClass().getName() + "#" + field.getName();
						qualifier = DependencyMetaData.getTypeQualifier(dependencyName,
								Arrays.asList(field.getAnnotations()));
					} catch (Exception ex) {
						throw new IllegalStateException(ex);
					}
				}

				// Determine if inject
				if (type == null) {
					continue NEXT_FIELD; // not inject
				}

				// Search to see if qualified available
				if (qualifier != null) {
					for (AvailableType availableType : availableTypes) {
						if ((qualifier.equals(availableType.getQualifier())
								&& (type.isAssignableFrom(availableType.getClass())))) {

							// Obtain the qualified dependency
							Object dependency = servletManager.getDependency(qualifier, type);

							// Register the dependency
							this.dependencies.registerFieldDependency(field, dependency);

							// Match qualified
							continue NEXT_FIELD;
						}
					}
				}

				// Search to see if match unqualified
				for (AvailableType availableType : availableTypes) {
					if ((availableType.getQualifier() == null) && (type.isAssignableFrom(availableType.getType()))) {

						// Obtain the dependency
						Object dependency = servletManager.getDependency(null, type);

						// Register the dependency
						this.dependencies.registerFieldDependency(field, dependency);

						// Match qualified
						continue NEXT_FIELD;
					}
				}
			}

			// Search through parent classes
			component = component.getSuperclass();
		}

		// Only provides injection
		return false;
	}

	@Override
	public void done() {
		// Nothing to complete
	}

}