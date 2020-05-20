package net.officefloor.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link JustInTimeInjectionResolver}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJustInTimeInjectionResolver implements JustInTimeInjectionResolver {

	/**
	 * {@link OfficeFloorDependencies}.
	 */
	private final OfficeFloorDependencies dependencies;

	/**
	 * {@link ServiceLocator}.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * Instantiate.
	 * 
	 * @param dependencies   {@link OfficeFloorDependencies}.
	 * @param serviceLocator {@link ServiceLocator}.
	 */
	public OfficeFloorJustInTimeInjectionResolver(OfficeFloorDependencies dependencies, ServiceLocator serviceLocator) {
		this.dependencies = dependencies;
		this.serviceLocator = serviceLocator;
	}

	/*
	 * =================== JustInTimeInjectionResolver ===================
	 */

	@Override
	public boolean justInTimeResolution(Injectee failedInjectionPoint) {

		// Determine if able to supplied dependencies
		Object dependency = this.dependencies.getDependency(failedInjectionPoint.getParent());
		if (dependency == null) {
			return false; // did not find
		}

		// Bind in the object
		Class<?> requiredType = (Class<?>) failedInjectionPoint.getRequiredType();
		Set<Type> contracts = new HashSet<Type>(Arrays.asList(requiredType));
		Set<Annotation> qualifiers = new HashSet<Annotation>(failedInjectionPoint.getRequiredQualifiers());
		OfficeFloorHk2Object<Object> officeFloorHk2Object = new OfficeFloorHk2Object<Object>(requiredType.getName(),
				contracts, qualifiers, requiredType, dependency);
		ServiceLocatorUtilities.addOneDescriptor(this.serviceLocator, officeFloorHk2Object, false);

		// Successfully made available
		return true;
	}

}