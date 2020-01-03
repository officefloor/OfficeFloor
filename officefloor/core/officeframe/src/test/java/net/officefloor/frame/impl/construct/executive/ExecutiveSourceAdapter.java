package net.officefloor.frame.impl.construct.executive;

import junit.framework.TestCase;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Execution;

/**
 * Adapter providing empty {@link ExecutiveSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class ExecutiveSourceAdapter implements ExecutiveSource, Executive {

	/*
	 * =============== ExecutiveSource =====================
	 */

	@Override
	public ExecutiveSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ================== Executive ========================
	 */

	@Override
	public Object createProcessIdentifier() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public TeamOversight[] getTeamOversights() {
		TestCase.fail("Should not be invoked");
		return null;
	}

}