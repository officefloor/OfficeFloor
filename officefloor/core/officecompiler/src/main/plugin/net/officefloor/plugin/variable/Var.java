package net.officefloor.plugin.variable;

/**
 * <p>
 * Represents a variable.
 * <p>
 * Typically only the {@link Out} and {@link Val} should be used. This allows to
 * obtain the variable value and update it (effectively mutation of the
 * variable).
 * 
 * @author Daniel Sagenschneider
 */
public interface Var<T> extends Out<T>, In<T> {
}