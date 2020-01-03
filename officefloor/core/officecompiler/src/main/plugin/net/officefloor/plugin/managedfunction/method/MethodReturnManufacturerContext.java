package net.officefloor.plugin.managedfunction.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Context for the {@link MethodReturnManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnManufacturerContext<T> {

	/**
	 * Obtains the {@link Class} of the {@link Method} return.
	 * 
	 * @return {@link Class} of the {@link Method} return.
	 */
	Class<?> getReturnClass();

	/**
	 * <p>
	 * Overrides the return {@link Class} to the translated return {@link Class}.
	 * <p>
	 * Should this not be invoked, then the default {@link Method} return
	 * {@link Class} is used.
	 * 
	 * @param translatedReturnClass Translated return {@link Class}.
	 */
	void setTranslatedReturnClass(Class<? super T> translatedReturnClass);

	/**
	 * Obtains the {@link Annotation} instances for the {@link Method}.
	 * 
	 * @return {@link Annotation} instances for the {@link Method}.
	 */
	Annotation[] getMethodAnnotations();

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * <p>
	 * Obtains the {@link Method}.
	 * <p>
	 * Due to type erasure, the type information on the return {@link Class} may be
	 * lost. This allows more information to be derived about the return type.
	 * 
	 * @return {@link Method}.
	 */

	Method getMethod();

	/**
	 * <p>
	 * Adds a {@link ManagedFunctionEscalationTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * <p>
	 * It is possible the {@link MethodReturnTranslator} will throw an
	 * {@link Escalation}. While this should be avoided, this allows registering
	 * {@link Escalation} for being handled.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType Type to be handled by an {@link EscalationFlow}.
	 * @return {@link ManagedFunctionEscalationTypeBuilder} to provide the
	 *         <code>type definition</code>.
	 */
	<E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}