package net.officefloor.web.value.load;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.value.load.PropertyKey;
import net.officefloor.web.value.load.StatelessValueLoader;
import net.officefloor.web.value.load.ValueLoader;

/**
 * {@link ValueLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderImpl implements ValueLoader {

	/**
	 * Object to load values on.
	 */
	private final Object object;

	/**
	 * State for loading values.
	 */
	private final Map<PropertyKey, Object> state = new HashMap<PropertyKey, Object>();

	/**
	 * {@link StatelessValueLoader} to undertake loading the values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Object to load values on.
	 * @param delegate
	 *            {@link StatelessValueLoader} to undertake loading the values.
	 */
	public ValueLoaderImpl(Object object, StatelessValueLoader delegate) {
		this.object = object;
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoader ==========================
	 */

	@Override
	public void loadValue(String name, String value, HttpValueLocation location) throws HttpException {
		// Load the value
		this.delegate.loadValue(this.object, name, 0, value, location, this.state);
	}

}