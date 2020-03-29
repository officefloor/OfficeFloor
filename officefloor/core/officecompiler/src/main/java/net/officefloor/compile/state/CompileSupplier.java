package net.officefloor.compile.state;

/**
 * {@link Runnable} to undertake compiling with particular state.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface CompileSupplier<R, T extends Throwable> {

	/**
	 * Logic.
	 * 
	 * @return Return value.
	 * @throws T Possible failure.
	 */
	R run() throws T;

}