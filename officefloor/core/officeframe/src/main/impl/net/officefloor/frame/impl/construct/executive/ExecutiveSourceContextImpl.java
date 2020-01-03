package net.officefloor.frame.impl.construct.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;

/**
 * {@link ExecutiveSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveSourceContextImpl extends SourceContextImpl implements ExecutiveSourceContext {

	/**
	 * Name for the {@link Executive}.
	 */
	public static final String EXECUTIVE_NAME = "Executive";

	/**
	 * {@link ThreadFactoryManufacturer}.
	 */
	private final ThreadFactoryManufacturer threadFactoryManufacturer;

	/**
	 * Default {@link Executive}.
	 */
	private final Executive defaultExecutive;

	/**
	 * Instantiate.
	 * 
	 * @param isLoadingType             Indicates if loading type.
	 * @param sourceContext             {@link SourceContext}.
	 * @param sourceProperties          {@link SourceProperties}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 */
	public ExecutiveSourceContextImpl(boolean isLoadingType, SourceContext sourceContext,
			SourceProperties sourceProperties, ThreadFactoryManufacturer threadFactoryManufacturer) {
		super(EXECUTIVE_NAME, isLoadingType, sourceContext, sourceProperties);
		this.threadFactoryManufacturer = threadFactoryManufacturer;

		// Create the default executive
		this.defaultExecutive = new DefaultExecutive(this.threadFactoryManufacturer);
	}

	/*
	 * ================ ExecutiveSourceContext ===========================
	 */

	@Override
	public ThreadFactory createThreadFactory(String executionStrategyName, Executive executive) {

		// Ensure have executive
		if (executive == null) {
			executive = this.defaultExecutive;
		}

		// Return the thread factory
		return this.threadFactoryManufacturer.manufactureThreadFactory(executionStrategyName, executive);
	}

}