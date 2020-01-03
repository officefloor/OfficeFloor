package net.officefloor.gef.editor;

import java.util.function.Function;

import net.officefloor.model.Model;

/**
 * Provides means to build the adapted model.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedBuilderContext {

	/**
	 * Specifies the root {@link Model}.
	 * 
	 * @param <R>
	 *            Root {@link Model} type.
	 * @param <O>
	 *            Operations type.
	 * @param rootModelClass
	 *            {@link Class} of the root {@link Model}.
	 * @param createOperations
	 *            {@link Function} to create the operations object to wrap the root
	 *            {@link Model}.
	 * @return {@link AdaptedRootBuilder}.
	 */
	<R extends Model, O> AdaptedRootBuilder<R, O> root(Class<R> rootModelClass, Function<R, O> createOperations);

}