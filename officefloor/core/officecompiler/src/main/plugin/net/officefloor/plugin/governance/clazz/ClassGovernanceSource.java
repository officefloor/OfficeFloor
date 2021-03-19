/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.governance.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.GovernanceSourceService;
import net.officefloor.compile.GovernanceSourceServiceFactory;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link GovernanceSource} that uses a {@link Class} to reflectively provide
 * the functionality for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernanceSource extends AbstractGovernanceSource<Object, Indexed>
		implements GovernanceSourceService<Object, Indexed, ClassGovernanceSource>, GovernanceSourceServiceFactory {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * =============== GovernanceSourceService ===================
	 */

	@Override
	public GovernanceSourceService<?, ?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getGovernanceSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassGovernanceSource> getGovernanceSourceClass() {
		return ClassGovernanceSource.class;
	}

	/*
	 * ================== GovernanceSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<Object, Indexed> context) throws Exception {
		GovernanceSourceContext govContext = context.getGovernanceSourceContext();

		// Obtain the class
		String className = govContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> clazz = govContext.loadClass(className);

		// Load the escalations (only once)
		Set<Class<?>> loadedEscalations = new HashSet<Class<?>>();

		// Obtain the govern method
		Method governMethod = this.getMethod(clazz, Govern.class);
		Class<Object> extensionInterface = (Class<Object>) this.validateMethodAndLoadEscalations(governMethod, true,
				Govern.class, 1, loadedEscalations, context)[0];

		// Specify the extension interface
		context.setExtensionInterface(extensionInterface);

		// Obtain the enforce method
		Method enforceMethod = this.getMethod(clazz, Enforce.class);
		this.validateMethodAndLoadEscalations(enforceMethod, true, Enforce.class, 0, loadedEscalations, context);

		// Obtain the disregard method
		Method disregardMethod = this.getMethod(clazz, Disregard.class);
		this.validateMethodAndLoadEscalations(disregardMethod, false, Disregard.class, 0, loadedEscalations, context);

		// Provide the governance factory
		context.setGovernanceFactory(new ClassGovernanceFactory(clazz, governMethod, enforceMethod, disregardMethod));
	}

	/**
	 * Obtain the {@link Method} with the {@link Annotation}.
	 * 
	 * @param clazz          {@link Class}.
	 * @param annotationType {@link Annotation}.
	 * @return {@link Method} with the {@link Annotation} or <code>null</code> if no
	 *         {@link Method}.
	 */
	private Method getMethod(Class<?> clazz, Class<? extends Annotation> annotationType) {

		// Obtain the annotated method
		Method annotatedMethod = null;
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotationType)) {

				// Ensure not already annotated method
				if (annotatedMethod != null) {
					throw new IllegalStateException(
							"Only one method may be annotated with " + annotationType.getSimpleName());
				}

				// Have the annotated method
				annotatedMethod = method;
			}
		}

		// Return the annotated method
		return annotatedMethod;
	}

	/**
	 * Validates the {@link Method}.
	 * 
	 * @param method             {@link Method}.
	 * @param annotationType     {@link Annotation} for the {@link Method}.
	 * @param numberOfParameters Number of parameters expected for the
	 *                           {@link Method}.
	 * @param loadedEscalations  Already loaded {@link Escalation} instances.
	 * @param context            {@link MetaDataContext}.
	 * @return Parameter types for the {@link Method}.
	 */
	private Class<?>[] validateMethodAndLoadEscalations(Method method, boolean isMethodRequired,
			Class<? extends Annotation> annotationType, int numberOfParameters, Set<Class<?>> loadedEscalations,
			MetaDataContext<Object, Indexed> context) {

		// Ensure have the method
		if (method == null) {
			if (isMethodRequired) {
				// Method required
				throw new IllegalStateException("A method must be annotated with @" + annotationType.getSimpleName());
			} else {
				// No method to check
				return null;
			}
		}

		// Ensure have appropriate parameters
		Class<?>[] methodParams = method.getParameterTypes();
		if (methodParams.length != numberOfParameters) {
			throw new IllegalStateException(annotationType.getSimpleName() + " method must have " + numberOfParameters
					+ " parameter" + (numberOfParameters != 1 ? "s" : 0));
		}

		// Load the escalations
		for (Class<?> escalation : method.getExceptionTypes()) {

			// Determine if already loaded
			if (loadedEscalations.contains(escalation)) {
				continue; // next escalation
			}

			// Load the escalation
			context.addEscalation(escalation);
			loadedEscalations.add(escalation);
		}

		// Return the method parameters
		return methodParams;
	}

}
