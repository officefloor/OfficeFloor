package net.officefloor.web.value.load;

import net.officefloor.web.value.load.StatelessValueLoader;
import net.officefloor.web.value.load.ValueLoader;
import net.officefloor.web.value.load.ValueLoaderFactory;

/**
 * {@link ValueLoaderFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderFactoryImpl<T> implements ValueLoaderFactory<T> {

	/**
	 * Delegate {@link StatelessValueLoader} to load values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link StatelessValueLoader} to load values.
	 */
	public ValueLoaderFactoryImpl(StatelessValueLoader delegate) {
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoaderFactory ============================
	 */

	@Override
	public ValueLoader createValueLoader(T object) throws Exception {
		// Create and return the new value loader
		return new ValueLoaderImpl(object, this.delegate);
	}

}