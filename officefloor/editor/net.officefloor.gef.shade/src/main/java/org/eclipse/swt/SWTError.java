package org.eclipse.swt;

/**
 * <p>
 * Avoiding importing SWT, which has O/S specific implementations.
 * <p>
 * Providing this only as necessary for GEF.
 * 
 * @author Daniel Sagenschneider
 */
public class SWTError extends Error {

	private static final long serialVersionUID = -9160311124292866538L;
}
