/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.integrate.governance;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.managedobject.AbstractManagedObjectContainerImplTest;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.integrate.stress.AdministratorStressTest.Administration;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;

/**
 * Abstract {@link Governance} testing to test various multi-threaded
 * combinations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGovernanceTestCase extends
		AbstractOfficeConstructTestCase {

	/**
	 * Creates all meta-data combinations for the input {@link TestCase} class.
	 * 
	 * @param testCaseClass
	 *            {@link AbstractManagedObjectContainerImplTest} class.
	 * @return {@link TestSuite} for each meta-data combination.
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends AbstractGovernanceTestCase> TestSuite createMetaDataCombinationTestSuite(
			Class<T> testCaseClass) {

		// Obtain the list of tests
		TestSuite interrogate = new TestSuite(testCaseClass);
		interrogate.tests();

		// Create the test suite of all meta-data combinations
		TestSuite suite = new TestSuite(testCaseClass.getName());
		for (Enumeration<Test> methods = new TestSuite(testCaseClass).tests(); methods
				.hasMoreElements();) {
			T methodTest = (T) methods.nextElement();
			for (int task = 0; task < 2; task++) {
				for (int governance = 0; governance < 2; governance++) {
					for (int administration = 0; administration < 2; administration++) {

						// Determine meta-data
						boolean isMultiThreadedTask = (task == 1);
						boolean isMultiThreadedGovernance = (governance == 1);
						boolean isMultiThreadedAdministration = (administration == 1);

						// Create the test case
						T testCase;
						try {
							testCase = testCaseClass.newInstance();
						} catch (Throwable ex) {
							suite.addTest(new TestCase(testCaseClass.getName()) {
								@Override
								protected void runTest() {
									fail("Must provide public default constructor");
								}
							});
							return suite;
						}

						// Specify name of method for the test
						String methodName = methodTest.getName();
						testCase.testMethodName = methodName;

						// Specify state and indicate name
						StringBuilder testName = new StringBuilder();
						testName.append(methodName);
						if (isMultiThreadedTask) {
							testCase.isMultiThreadedTask = true;
							testName.append("-Task");
						}
						if (isMultiThreadedGovernance) {
							testCase.isMultiThreadedGovernance = true;
							testName.append("-Governance");
						}
						if (isMultiThreadedAdministration) {
							testCase.isMultiThreadedAdministration = true;
							testName.append("-Administration");
						}

						// Specify the name of the test case
						testCase.setName(testName.toString());

						// Add the test case
						suite.addTest(testCase);
					}
				}
			}
		}

		// Return the test suite
		return suite;
	}

	/**
	 * Name of {@link ManagedFunction} {@link Team}.
	 */
	protected static final String TEAM_TASK = "TEAM_TASK";

	/**
	 * Name of {@link Governance} {@link Team}.
	 */
	protected static final String TEAM_GOVERNANCE = "TEAM_GOVERNANCE";

	/**
	 * Name of {@link Administration} {@link Team}.
	 */
	protected static final String TEAM_ADMINISTRATION = "TEAM_ADMINISTRATION";

	/**
	 * Name of the {@link Test} method.
	 */
	protected String testMethodName;

	/**
	 * Indicates if multi-threaded {@link Team} responsible for {@link ManagedFunction}.
	 */
	protected boolean isMultiThreadedTask = false;

	/**
	 * Indicates if multi-threaded {@link Team} responsible for
	 * {@link Governance}.
	 */
	protected boolean isMultiThreadedGovernance = false;

	/**
	 * Indicates if multi-threaded {@link Team} responsible for
	 * {@link Administration}.
	 */
	protected boolean isMultiThreadedAdministration = false;

	/**
	 * Constructs the {@link Team} instances.
	 */
	protected void constructTeams() {

		// Create the teams
		Team passiveTeam = new PassiveTeam();
		Team taskTeam = (this.isMultiThreadedTask ? new OnePersonTeam(
				"TASK_TEAM", MockTeamSource.createTeamIdentifier(), 100)
				: passiveTeam);
		Team governanceTeam = (this.isMultiThreadedTask ? new OnePersonTeam(
				"GOVERNANCE_TEAM", MockTeamSource.createTeamIdentifier(), 100)
				: passiveTeam);
		Team administrationTeam = (this.isMultiThreadedTask ? new OnePersonTeam(
				"ADMINISTRATOR_TEAM", MockTeamSource.createTeamIdentifier(),
				100) : passiveTeam);

		// Construct the teams
		this.constructTeam(TEAM_TASK, taskTeam);
		this.constructTeam(TEAM_GOVERNANCE, governanceTeam);
		this.constructTeam(TEAM_ADMINISTRATION, administrationTeam);
	}

	/*
	 * ======================== TestCase ==========================
	 */

	@Override
	protected void runTest() throws Throwable {
		String testName = this.getName();
		try {
			// Override name to run test method
			this.setName(this.testMethodName);

			// Run the test
			super.runTest();

		} finally {
			// Ensure reset name
			this.setName(testName);
		}
	}

}