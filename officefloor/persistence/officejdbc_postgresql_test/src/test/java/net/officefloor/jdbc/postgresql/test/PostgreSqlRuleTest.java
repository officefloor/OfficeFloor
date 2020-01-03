package net.officefloor.jdbc.postgresql.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.Statement;
import org.postgresql.ds.PGSimpleDataSource;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule.Configuration;

/**
 * Tests the {@link PostgreSqlRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlRuleTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to run PostgreSql.
	 */
	@UsesDockerTest
	public void testConnectivity() throws Throwable {

		// Configure rule
		PostgreSqlRule rule = new PostgreSqlRule(new Configuration().server("localhost").port(5433).database("database")
				.username("testuser").password("testpassword"));

		// Ensure can connect
		this.doRule(rule, () -> {

			// Ensure able to connect to database via rule
			try (Connection connection = rule.getConnection()) {
				connection.createStatement()
						.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
				connection.createStatement()
						.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			}

			// Ensure can connect to the create database in PostgreSql
			PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setPortNumber(5433);
			dataSource.setDatabaseName("database");
			dataSource.setUser("testuser");
			dataSource.setPassword("testpassword");
			try (Connection connection = dataSource.getConnection()) {
				ResultSet resultSet = connection.createStatement()
						.executeQuery("SELECT ID, NAME FROM OFFICE_FLOOR_JDBC_TEST");
				assertTrue("Should have row", resultSet.next());
				assertEquals("Incorrect row", 1, resultSet.getInt("ID"));
				assertEquals("Incorrect row value", "test", resultSet.getString("NAME"));
				assertFalse("Should only be one row", resultSet.next());
			}
		});
	}

	/**
	 * Ensure able to configure the max connections.
	 */
	@UsesDockerTest
	public void testAdjustMaxConnections() throws Throwable {

		final int MAX_CONNECTIONS = 50;

		// Configure rule
		PostgreSqlRule rule = new PostgreSqlRule(new Configuration().port(5433).maxConnections(MAX_CONNECTIONS));

		// Ensure max connections
		this.doRule(rule, () -> {

			// Ensure can connect to the create database in PostgreSql
			PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setPortNumber(5433);
			dataSource.setUser("testuser");
			dataSource.setPassword("testpassword");

			// Obtain the first connection
			List<Connection> connections = new ArrayList<>(MAX_CONNECTIONS);
			connections.add(rule.getConnection());

			// Ensure able to obtain max connections
			for (int i = 1; i < MAX_CONNECTIONS; i++) {
				connections.add(dataSource.getConnection());
			}
			try {

				// Should not be able to open further connection
				try {
					connections.add(dataSource.getConnection());
					fail("Should not successfully create connection");
				} catch (SQLException ex) {
					assertTrue("Incorrect cause: " + ex.getMessage(),
							ex.getMessage().contains("FATAL: sorry, too many clients already"));
				}

			} finally {
				// Close the connections
				for (Connection connection : connections) {
					connection.close();
				}
			}
		});
	}

	/**
	 * Logic for the {@link PostgreSqlRule}.
	 */
	@FunctionalInterface
	private static interface RuleLogic {

		void doLogic() throws Throwable;
	}

	/**
	 * Undertakes the rule.
	 * 
	 * @param rule      {@link PostgreSqlRule}.
	 * @param ruleLogic {@link RuleLogic}.
	 */
	private void doRule(PostgreSqlRule rule, RuleLogic ruleLogic) throws Throwable {

		// Ensure can connect to rule
		Closure<Boolean> isRun = new Closure<>(false);
		rule.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Undertake the logic
				ruleLogic.doLogic();

				// Run
				isRun.value = true;
			}
		}, null).evaluate();

		// Ensure the test is run
		assertTrue("Failed to run wrapped test for rule", isRun.value);
	}

}