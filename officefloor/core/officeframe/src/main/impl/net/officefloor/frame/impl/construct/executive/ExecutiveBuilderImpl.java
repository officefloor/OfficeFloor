package net.officefloor.frame.impl.construct.executive;

import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Implements the {@link ExecutiveBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveBuilderImpl<XS extends ExecutiveSource>
		implements ExecutiveBuilder<XS>, ExecutiveConfiguration<XS> {

	/**
	 * {@link ExecutiveSource}.
	 */
	private final XS executiveSource;

	/**
	 * {@link Class} of the {@link ExecutiveSource}.
	 */
	private final Class<XS> executiveSourceClass;

	/**
	 * {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Initiate.
	 * 
	 * @param executiveSource {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(XS executiveSource) {
		this.executiveSource = executiveSource;
		this.executiveSourceClass = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param executiveSourceClass {@link Class} of the {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(Class<XS> executiveSourceClass) {
		this.executiveSource = null;
		this.executiveSourceClass = executiveSourceClass;
	}

	/*
	 * ================ ExecutiveBuilder =====================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/*
	 * ============== ExecutiveConfiguration ===================
	 */

	@Override
	public XS getExecutiveSource() {
		return this.executiveSource;
	}

	@Override
	public Class<XS> getExecutiveSourceClass() {
		return this.executiveSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

}