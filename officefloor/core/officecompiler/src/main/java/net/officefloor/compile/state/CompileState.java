package net.officefloor.compile.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides compiler state to an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileState<S> {

	/**
	 * Registry of state.
	 */
	static ThreadLocal<Map<Class<?>, Object>> compileState = new ThreadLocal<Map<Class<?>, Object>>() {
		@Override
		protected Map<Class<?>, Object> initialValue() {
			return new HashMap<>();
		}
	};

	/**
	 * Obtains the compile state.
	 * 
	 * @return Compile state.
	 */
	@SuppressWarnings("unchecked")
	default S getCompileState() {
		return (S) compileState.get().get(this.getClass());
	}

	/**
	 * Runs the {@link CompileRunnable} with the state.
	 * 
	 * @param <T>   Possible failure type.
	 * @param state State.
	 * @param logic {@link CompileRunnable}.
	 * @throws T Possible failure.
	 */
	default <T extends Throwable> void compileInContext(S state, CompileRunnable<T> logic) throws T {
		this.compileInContext(state, () -> {
			logic.run();
			return null;
		});
	}

	/**
	 * Runs the {@link CompileSupplier} with the state.
	 * 
	 * @param <R>   Return type.
	 * @param <T>   Possible failure type.
	 * @param state State.
	 * @param logic {@link CompileSupplier}.
	 * @return Return from {@link CompileSupplier}.
	 * @throws T Possible failure.
	 */
	@SuppressWarnings("unchecked")
	default <R, T extends Throwable> R compileInContext(S state, CompileSupplier<R, T> logic) throws T {

		// Obtain state map and possible outer state
		Map<Class<?>, Object> stateMap = compileState.get();
		final Class<?> key = this.getClass();
		S outerState = (S) stateMap.get(key);

		// Merge with possible outer state
		if (outerState != null) {
			state = this.mergeCompileContext(outerState, state);
		}

		// Run the logic within context of state
		stateMap.put(this.getClass(), state);
		try {
			return logic.run();
		} finally {
			stateMap.remove(this.getClass());
		}
	}

	/**
	 * Enables merging state when additional inner context is run.
	 * 
	 * @param outer Outer state.
	 * @param inner Inner state.
	 * @return Merged state.
	 */
	default S mergeCompileContext(S outer, S inner) {
		return outer;
	}

}