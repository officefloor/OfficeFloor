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
	 * {@link ServiceLocator}.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * Instantiate.
	 * 
	 * @param serviceLocator {@link ServiceLocator}.
	 */
	public OfficeFloorJustInTimeInjectionResolver(ServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	/*
	 * =================== JustInTimeInjectionResolver ===================
	 */

	@Override
	public boolean justInTimeResolution(Injectee failedInjectionPoint) {

		// TODO provide resolution
		if (failedInjectionPoint.getRequiredType() instanceof Class) {
			Class<?> requiredType = (Class<?>) failedInjectionPoint.getRequiredType();
			if (requiredType.getSimpleName().startsWith("JustInTime")) {
				System.out.println("justInTimeResolution " + requiredType.getName());
				try {
					Object value = requiredType.getConstructor().newInstance();

					// TODO determine name
					String name = requiredType.getName();

					// Bind in the object
					Set<Type> contracts = new HashSet<Type>(Arrays.asList(requiredType));
					Set<Annotation> qualifiers = new HashSet<Annotation>(failedInjectionPoint.getRequiredQualifiers());
					OfficeFloorHk2Object<Object> officeFloorHk2Object = new OfficeFloorHk2Object<Object>(name,
							contracts, qualifiers, requiredType, value);
					ServiceLocatorUtilities.addOneDescriptor(this.serviceLocator, officeFloorHk2Object, false);

					return true;

				} catch (Exception ex) {
					System.out.println("  (not loaded for OfficeFloor just in time)");
				}
			}
		}

		// As here did not find
		return false;
	}

}