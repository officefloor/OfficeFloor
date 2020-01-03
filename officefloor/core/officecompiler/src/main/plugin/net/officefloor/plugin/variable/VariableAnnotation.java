package net.officefloor.plugin.variable;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;

/**
 * Annotation for {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableAnnotation {

	/**
	 * Extracts the possible variable name.
	 * 
	 * @param objectType {@link ManagedFunctionObjectType}.
	 * @return Variable name or <code>null</code> if not a variable.
	 */
	public static String extractPossibleVariableName(ManagedFunctionObjectType<?> objectType) {

		// Extract variable name
		VariableAnnotation annotation = objectType.getAnnotation(VariableAnnotation.class);
		if (annotation != null) {
			return annotation.getVariableName();
		}

		// As here, not variable
		return null;
	}

	/**
	 * Name for the {@link Var}.
	 */
	private final String name;

	/**
	 * Type for the {@link Var}.
	 */
	private final String type;

	/**
	 * Instantiate.
	 * 
	 * @param name Name for the {@link Var}.
	 * @param type Type for the {@link Var}.
	 */
	public VariableAnnotation(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Obtains the name of the {@link Var}.
	 * 
	 * @return Name of the {@link Var}.
	 */
	public String getVariableName() {
		return this.name;
	}

	/**
	 * Obtains the type of the {@link Var}.
	 * 
	 * @return Type of the {@link Var}.
	 */
	public String getVariableType() {
		return this.type;
	}

}