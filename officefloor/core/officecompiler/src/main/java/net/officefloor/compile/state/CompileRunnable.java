package net.officefloor.compile.state;

/**
 * {@link Runnable} to undertake compiling with particular state.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface CompileRunnable<T extends Throwable> {

	/**
	 * Logic.
	 * 
	 * @throws T Possible failure.
	 */
	void run() throws T;
}