package net.officefloor.jdbc.pool;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

import org.h2.jdbcx.JdbcXAConnection;

import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.AbstractConnectionTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;

/**
 * Tests {@link PooledConnection} becoming invalid.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPoolInvalidateConnectionTest extends AbstractConnectionTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Compiles {@link OfficeFloor}.
	 */
	private OfficeFloor compileOfficeFloor() throws Exception {

		// Configure
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {

			// Connection
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("mo",
					ConnectionManagedObjectSource.class.getName());
			this.loadProperties(mos);
			mos.addOfficeFloorManagedObject("mo", ManagedObjectScope.THREAD);

			// Pool the connection
			OfficeFloorManagedObjectPool pool = context.getOfficeFloorDeployer().addManagedObjectPool("POOL",
					ThreadLocalJdbcConnectionPoolSource.class.getName());
			context.getOfficeFloorDeployer().link(mos, pool);
		});
		compiler.office((context) -> {
			context.addSection("SECTION", MockSection.class);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Return the OfficeFloor
		return this.officeFloor;
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			if (this.officeFloor != null) {

				// Close the OfficeFloor
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Ensure can invalidate a {@link PooledConnection}.
	 */
	@SuppressWarnings("unchecked")
	public void testInvalidateConnection() throws Throwable {

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor();

		// Run capturing connections
		Deque<PooledConnection> connections = CapturePooledConnectionsDecoratorFactory.connections;
		CapturePooledConnectionsDecoratorFactory.isActive = true;
		try {

			// Run a couple of times and should re-use connection
			for (int i = 0; i < 5; i++) {

				// Invoke function to obtain connection
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);

				// Obtain the pooled connection
				assertEquals("Should have pooled connection", 1, connections.size());
			}

			// Invalidate the connection
			PooledConnection connection = connections.poll();
			JdbcXAConnection rawConnection = (JdbcXAConnection) connection;

			// Reflectively obtain listeners
			Field listenersField = rawConnection.getClass().getDeclaredField("listeners");
			listenersField.setAccessible(true);
			List<ConnectionEventListener> listeners = (List<ConnectionEventListener>) listenersField.get(rawConnection);
			assertEquals("Should have ThreadLocal Pool listener", 1, listeners.size());
			ConnectionEventListener eventListener = listeners.get(0);

			// Trigger error (to not re-use connection)
			ConnectionEvent event = new ConnectionEvent(rawConnection, new SQLException("TEST"));
			eventListener.connectionErrorOccurred(event);

			// Ensure use new connection
			connections.clear();
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
			assertEquals("Should use new connection", 1, connections.size());
			PooledConnection newConnection = connections.poll();
			assertNotNull("Should have new connection", newConnection);
			assertNotSame("Should be different connection", connection, newConnection);

		} finally {
			CapturePooledConnectionsDecoratorFactory.isActive = false;
			connections.clear();
		}
	}

	public static class MockSection {

		public void function(Connection connection) throws SQLException {
			// Use connection to trigger creating one
			connection.getSchema();
		}
	}

}