package net.officefloor.plugin.section.clazz.parameter;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Utility to determine if {@link Parameter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionParameterInterrogation implements ClassSectionParameterInterrogatorContext {

	/**
	 * Indicates if parameter.
	 * 
	 * @param functionObject {@link ManagedFunctionObjectType}.
	 * @param sourceContext  {@link SourceContext}.
	 * @param interrogator   {@link ClassSectionParameterInterrogator}.
	 * @return <code>true</code> if parameter.
	 * @throws Exception If fails to determine if parameter.
	 */
	public static boolean isParameter(ManagedFunctionObjectType<?> functionObject, SourceContext sourceContext,
			ClassSectionParameterInterrogator interrogator) throws Exception {
		return interrogator.isParameter(new ClassSectionParameterInterrogation(functionObject, sourceContext));
	}

	/**
	 * {@link ManagedFunctionObjectType}.
	 */
	private ManagedFunctionObjectType<?> functionObject;

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context;

	/**
	 * Instantiate.
	 * 
	 * @param functionObject {@link ManagedFunctionObjectType}.
	 * @param context        {@link SourceContext}.
	 */
	public ClassSectionParameterInterrogation(ManagedFunctionObjectType<?> functionObject, SourceContext context) {
		this.functionObject = functionObject;
		this.context = context;
	}

	/*
	 * ================ ClassSectionParameterInterrogatorContext ================
	 */

	@Override
	public ManagedFunctionObjectType<?> getManagedFunctionObjectType() {
		return this.functionObject;
	}

	@Override
	public SourceContext getSourceContext() {
		return this.context;
	}

}