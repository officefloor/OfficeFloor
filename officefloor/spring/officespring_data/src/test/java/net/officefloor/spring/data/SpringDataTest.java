/*-
 * #%L
 * Spring Data Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.spring.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.spring.SpringSupplierSource;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTest extends OfficeFrameTestCase {

	/**
	 * Creates the {@link ConfigurableApplicationContext}.
	 * 
	 * @return {@link ConfigurableApplicationContext}.
	 */
	private static ConfigurableApplicationContext createSpringContext() {
		return SpringApplication.run(MockSpringDataConfiguration.class);
	}

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	@Override
	protected void setUp() throws Exception {
		this.context = createSpringContext();
	}

	@Override
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Ensure can obtain Spring data beans.
	 */
	public void testSpringDataBeans() throws SQLException {

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			Object bean = this.context.getBean(name);
			System.out.println(
					"  " + name + "\t\t(" + bean.getClass().getName() + ") - " + (bean instanceof RowEntityRepository));
		}

		// Create another context
		ConfigurableApplicationContext otherContext = createSpringContext();

		// Ensure can obtain repository
		RowEntityRepository repository = this.context.getBean(RowEntityRepository.class);
		assertNotNull("Should obtain repository", repository);

		// Add rows
		repository.save(new RowEntity(null, "One"));
		repository.save(new RowEntity(null, "Two"));

		// Ensure can obtain the row
		List<RowEntity> rows = repository.findByName("One");
		assertEquals("Should find a row", 1, rows.size());

		// Ensure referring to same database
		try (Connection connection = this.context.getBean(DataSource.class).getConnection()) {
			try (Connection otherConnection = otherContext.getBean(DataSource.class).getConnection()) {
				assertRowExists("context", connection);
				assertRowExists("otherContext", otherConnection);
			}
		}

		// Ensure data available to other context
		List<RowEntity> otherContextRows = otherContext.getBean(RowEntityRepository.class).findByName("One");
		assertEquals("Should find by other context", 1, otherContextRows.size());
	}

	private static void assertRowExists(String message, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM ROW_ENTITY WHERE NAME = ?")) {
			statement.setString(1, "One");
			ResultSet resultSet = statement.executeQuery();
			assertTrue(message + ": should have row", resultSet.next());
		}
	}

	/**
	 * Ensure can run transaction with commit.
	 */
	public void testSpringCommitTransaction() {

		// Obtain the transaction manager
		PlatformTransactionManager transactionManager = this.context.getBean(PlatformTransactionManager.class);
		assertNotNull("Should obtain transaction manager", transactionManager);

		// Undertake transaction
		TransactionStatus transaction = transactionManager.getTransaction(null);

		// Save item within the transaction
		RowEntityRepository repository = this.context.getBean(RowEntityRepository.class);
		repository.save(new RowEntity(null, "One"));

		// Ensure can find row
		assertEquals("Should find row", 1, repository.findByName("One").size());

		// Commit transaction
		transactionManager.commit(transaction);

		// Ensure row available
		assertEquals("Should find row after commit", 1, repository.findByName("One").size());
	}

	/**
	 * Ensure can run transaction with rollback.
	 */
	public void testSpringRollbackTransaction() {

		// Obtain the transaction manager
		PlatformTransactionManager transactionManager = this.context.getBean(PlatformTransactionManager.class);
		assertNotNull("Should obtain transaction manager", transactionManager);

		// Undertake transaction
		TransactionStatus transaction = transactionManager.getTransaction(null);

		// Save item within the transaction
		RowEntityRepository repository = this.context.getBean(RowEntityRepository.class);
		repository.save(new RowEntity(null, "One"));

		// Ensure can find row
		assertEquals("Should find row", 1, repository.findByName("One").size());

		// Rollback transaction
		transactionManager.rollback(transaction);

		// Ensure row no longer available
		assertEquals("Should not find row after rollback", 0, repository.findByName("One").size());
	}

	/**
	 * Ensure can use {@link RowEntityRepository}.
	 */
	public void testInjectRepository() throws Throwable {

		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", GetRowSection.class);
			SpringSupplierSource.configure(context.getOfficeArchitect(), MockSpringDataConfiguration.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Create row
			RowEntity row = new RowEntity(null, "TEST");
			this.context.getBean(RowEntityRepository.class).save(row);

			// Trigger function to use repository within OfficeFloor
			Request request = new Request(row.getId());
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", request);

			// Ensure obtained row
			assertNotNull("Should obtain the row", request.row);
			assertEquals("Incorrect row", row.getId(), request.row.getId());
			assertEquals("Should match name", "TEST", request.row.getName());
		}
	}

	private static class Request {

		private final Long id;

		private volatile RowEntity row;

		private Request(Long id) {
			this.id = id;
		}
	}

	public static class GetRowSection {

		public void service(@Parameter Request request, RowEntityRepository repository) {
			request.row = repository.findById(request.id).get();
		}
	}

	/**
	 * Ensure can use {@link PlatformTransactionManager}.
	 */
	public void testTransactionInSameFunction() throws Throwable {
		this.doInjectTransactionTest(TransactionTestMode.SAME_FUNCTION);
	}

	/**
	 * Ensure can use {@link PlatformTransactionManager}.
	 */
	public void testTransactionSpanningFunctions() throws Throwable {
		this.doInjectTransactionTest(TransactionTestMode.SPAN_FUNCTIONS);
	}

	/**
	 * Ensure can use {@link PlatformTransactionManager} across different
	 * {@link Team} instances.
	 */
	public void testTransactionSpanningTeams() throws Throwable {
		this.doInjectTransactionTest(TransactionTestMode.SPAN_TEAMS);
	}

	/**
	 * Mode of testing transaction.
	 */
	public static enum TransactionTestMode {
		SAME_FUNCTION, SPAN_FUNCTIONS, SPAN_TEAMS
	}

	/**
	 * Undertakes the {@link PlatformTransactionManager} injected test.
	 * 
	 * @param testMode {@link TransactionTestMode}.
	 */
	private void doInjectTransactionTest(TransactionTestMode testMode) throws Throwable {

		// Determine states
		boolean isDifferentTeams = TransactionTestMode.SPAN_TEAMS.equals(testMode);

		// Obtain the repository
		RowEntityRepository repository = this.context.getBean(RowEntityRepository.class);

		// Test transaction within OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		if (isDifferentTeams) {
			compiler.officeFloor((context) -> {
				context.getOfficeFloorDeployer().addTeam("TEAM", OnePersonTeamSource.class.getName())
						.addTypeQualification(null, TeamMarker.class.getName());
			});
		}
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();
			architect.enableAutoWireTeams();
			context.addSection("SECTION", TransactionInjectSection.class);
			context.addManagedObject("MARKER", TeamMarker.class, ManagedObjectScope.THREAD);
			SpringSupplierSource.configure(architect, MockSpringDataConfiguration.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Team checks
			Runnable clearTeams = () -> {
				TransactionInjectSection.serviceThread = null;
				TransactionInjectSection.transactionThread = null;
			};
			Runnable checkTeams;
			switch (testMode) {
			case SAME_FUNCTION:
				checkTeams = () -> {
					assertNotNull("Should have service thread", TransactionInjectSection.serviceThread);
					assertNull("Should not have transaction thread", TransactionInjectSection.transactionThread);
				};
				break;

			case SPAN_FUNCTIONS:
				checkTeams = () -> {
					assertNotNull("Should have service thread", TransactionInjectSection.serviceThread);
					assertNotNull("Should have transaction thread", TransactionInjectSection.transactionThread);
					assertSame("Should be same thread", TransactionInjectSection.serviceThread,
							TransactionInjectSection.transactionThread);
				};
				break;

			case SPAN_TEAMS:
				checkTeams = () -> {
					assertNotNull("Should have service thread", TransactionInjectSection.serviceThread);
					assertNotNull("Should have transaction thread", TransactionInjectSection.transactionThread);
					assertNotSame("Should be different threads", TransactionInjectSection.serviceThread,
							TransactionInjectSection.transactionThread);
				};
				break;

			default:
				throw new IllegalStateException("Unknown test mode " + testMode);
			}

			// Create row
			clearTeams.run();
			TransactionInjectRequest commit = new TransactionInjectRequest(true, testMode);
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", commit);
			RowEntity committedRow = repository.findById(commit.row.getId()).get();
			assertNotNull("Should have committed row", committedRow);
			assertEquals("Incorrect committed row", "COMMIT", committedRow.getName());
			checkTeams.run();

			// Roll back creating the row
			clearTeams.run();
			TransactionInjectRequest rollback = new TransactionInjectRequest(false, testMode);
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", rollback);
			int rolledBackRowCount = repository.findByName("ROLLBACK").size();
			assertEquals("Should rollback creating row", 0, rolledBackRowCount);
			checkTeams.run();
		}
	}

	public static class TransactionInjectRequest {

		private final boolean isCommit;

		private final TransactionTestMode testMode;

		private volatile RowEntity row;

		private TransactionInjectRequest(boolean isCommit, TransactionTestMode testMode) {
			this.isCommit = isCommit;
			this.testMode = testMode;
		}
	}

	@FlowInterface
	public static interface TransactionFlows {

		void commit(TransactionStatus status);

		void rollback(TransactionStatus status);
	}

	public static class TeamMarker {
	}

	public static class TransactionInjectSection {

		private static volatile Thread serviceThread;

		private static volatile Thread transactionThread;

		public void service(@Parameter TransactionInjectRequest request, PlatformTransactionManager transactionManager,
				RowEntityRepository repository, TransactionFlows flows) {
			serviceThread = Thread.currentThread();
			TransactionStatus transaction = transactionManager.getTransaction(null);
			request.row = new RowEntity(null, request.isCommit ? "COMMIT" : "ROLLBACK");
			repository.save(request.row);
			if (request.isCommit) {
				switch (request.testMode) {
				case SAME_FUNCTION:
					transactionManager.commit(transaction);
					break;
				case SPAN_FUNCTIONS:
				case SPAN_TEAMS:
					flows.commit(transaction);
					break;
				default:
					fail("Unknown test mode " + request.testMode);
				}
			} else {
				switch (request.testMode) {
				case SAME_FUNCTION:
					transactionManager.rollback(transaction);
					break;
				case SPAN_FUNCTIONS:
				case SPAN_TEAMS:
					flows.rollback(transaction);
					break;
				default:
					fail("Unknown test mode " + request.testMode);
				}
			}
		}

		public void commit(@Parameter TransactionStatus transaction, PlatformTransactionManager transactionManager,
				RowEntityRepository repository, TeamMarker marker) {
			transactionThread = Thread.currentThread();
			assertEquals("Should have row before commit", 1, repository.findByName("COMMIT").size());
			transactionManager.commit(transaction);
			assertEquals("Should have row after commit", 1, repository.findByName("COMMIT").size());
		}

		public void rollback(@Parameter TransactionStatus transaction, PlatformTransactionManager transactionManager,
				RowEntityRepository repository, TeamMarker marker) {
			transactionThread = Thread.currentThread();
			assertEquals("Should have row before rollback", 1, repository.findByName("ROLLBACK").size());
			transactionManager.rollback(transaction);
			assertEquals("Should NOT have row after rollback", 0, repository.findByName("ROLLBACK").size());
		}
	}

	/**
	 * Ensure can provide transaction {@link Governance}.
	 */
	public void testTransactionGovernance() throws Throwable {
		this.doTransactionGovernanceTest(false);
	}

	/**
	 * Ensure can provide transaction {@link Governance} with different {@link Team}
	 * instances.
	 */
	public void testTransactionGovernanceWithTeams() throws Throwable {
		this.doTransactionGovernanceTest(true);
	}

	/**
	 * Undertakes transaction {@link Governance} test.
	 * 
	 * @param isDifferentTeam If use different {@link Team} to execute the
	 *                        {@link ManagedFunction} instances.
	 */
	private void doTransactionGovernanceTest(boolean isDifferentTeam) throws Throwable {

		// Obtain the repository
		RowEntityRepository repository = this.context.getBean(RowEntityRepository.class);

		// Test transaction within OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		if (isDifferentTeam) {
			compiler.officeFloor((context) -> {
				OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

				// Configure team for servicing
				deployer.addTeam("TEAM", OnePersonTeamSource.class.getName()).addTypeQualification(null,
						TeamMarker.class.getName());

				// Configure governance team
				OfficeFloorTeam govTeam = deployer.addTeam("GOV_TEAM", OnePersonTeamSource.class.getName());
				deployer.link(context.getDeployedOffice().getDeployedOfficeTeam("GOV_TEAM"), govTeam);
			});
		}
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();
			architect.enableAutoWireTeams();

			// Add marker and configure Spring
			context.addManagedObject("MARKER", TeamMarker.class, ManagedObjectScope.THREAD);
			SpringSupplierSource.configure(architect, MockSpringDataConfiguration.class);

			// Create the governance
			OfficeGovernance governance = architect.addOfficeGovernance("TRANSACTION",
					SpringDataTransactionGovernanceSource.class.getName());
			if (isDifferentTeam) {
				architect.link(governance, architect.addOfficeTeam("GOV_TEAM"));
			}
			governance.enableAutoWireExtensions();

			// Configure section under governance
			OfficeSection section = context.addSection("SECTION", TransactionGovernanceSection.class);
			section.addGovernance(governance);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Team checks
			Runnable clearTeams = () -> {
				TransactionGovernanceSection.serviceThread = null;
				TransactionGovernanceSection.nextThread = null;
			};
			Runnable ensureTeams = () -> {
				assertNotNull("Should have service thread", TransactionGovernanceSection.serviceThread);
				assertNotNull("Should have transaction thread", TransactionGovernanceSection.nextThread);
			};
			Runnable checkTeams = isDifferentTeam ? () -> {
				ensureTeams.run();
				assertNotSame("Should be different threads", TransactionGovernanceSection.serviceThread,
						TransactionGovernanceSection.nextThread);
			} : () -> {
				ensureTeams.run();
				assertSame("Should be same thread", TransactionGovernanceSection.serviceThread,
						TransactionGovernanceSection.nextThread);
			};

			// Create row
			clearTeams.run();
			TransactionGovernanceRequest commit = new TransactionGovernanceRequest(null);
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", commit);
			RowEntity committedRow = repository.findById(commit.row.getId()).get();
			assertNotNull("Should have committed row", committedRow);
			assertEquals("Incorrect committed row", "COMMIT", committedRow.getName());
			checkTeams.run();

			// Roll back creating the row
			clearTeams.run();
			final Exception failure = new Exception("TEST");
			TransactionGovernanceRequest rollback = new TransactionGovernanceRequest(failure);
			try {
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", rollback);
				fail("Should not be successful");
			} catch (Exception ex) {
				assertEquals("Incorrect cause", "TEST", ex.getMessage());
			}
			int rolledBackRowCount = repository.findByName("ROLLBACK").size();
			assertEquals("Should rollback creating row", 0, rolledBackRowCount);
			checkTeams.run();
		}
	}

	public static class TransactionGovernanceRequest {

		private final Exception failTransactionException;

		private volatile RowEntity row;

		private TransactionGovernanceRequest(Exception failTransactionException) {
			this.failTransactionException = failTransactionException;
		}
	}

	public static class TransactionGovernanceSection {

		private static volatile Thread serviceThread;

		private static volatile Thread nextThread;

		@Next("next")
		public TransactionGovernanceRequest service(RowEntityRepository repository,
				@Parameter TransactionGovernanceRequest request) {
			serviceThread = Thread.currentThread();
			request.row = new RowEntity(null, request.failTransactionException != null ? "ROLLBACK" : "COMMIT");
			repository.save(request.row);
			return request;
		}

		public void next(@Parameter TransactionGovernanceRequest request, TeamMarker marker) throws Exception {
			nextThread = Thread.currentThread();
			if (request.failTransactionException != null) {
				throw request.failTransactionException;
			}
		}
	}

}
