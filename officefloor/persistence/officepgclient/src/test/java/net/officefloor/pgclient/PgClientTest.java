package net.officefloor.pgclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class PgClientTest {

	private static final int REQUEST_COUNT = 10;

	private static final int THREAD_COUNT = 10;

	/**
	 * PostgreSql database.
	 */
	@RegisterExtension
	public static PostgreSqlExtension database = new PostgreSqlExtension(
			new Configuration().port(5433).database("db").username("testuser").password("testpassword"));

	@BeforeAll
	public static void setupDatabase() throws Exception {
		try (Connection connection = database.getConnection()) {
			connection.createStatement().execute("CREATE TABLE TEST ( ID INT, MESSAGE VARCHAR(40))");
			connection.createStatement().execute("INSERT INTO TEST (ID, MESSAGE) VALUES (1, 'TEST')");
		}
	}

	private final PgConnectOptions connectOptions = new PgConnectOptions().setPort(5433).setHost("localhost")
			.setUser("testuser").setPassword("testpassword");

	@Test
	public void testPool() {

		// Create the client pool
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		PgPool pool = PgPool.pool(this.connectOptions, poolOptions);

		SqlClient client = pool;

		// Pooled query
		RowSet<Row> rowSet = blockFuture(doQuery(client));
		assertEquals(1, rowSet.size(), "Incorrect number of rows");

		// Now close the pool
		blockFuture(client.close());
	}

	@Test
	public void testPooledConnection() {

		// Create the client pool
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		PgPool pool = PgPool.pool(this.connectOptions, poolOptions);

		RowSet<Row> rowSet = blockFuture(pool.getConnection().flatMap(conn -> {
			return doQuery(conn).flatMap(rows -> {
				return conn.close().flatMap(closed -> Future.succeededFuture(rows));
			});
		}));
		assertEquals(1, rowSet.size(), "Incorrect number of rows");
	}

	@Test
	public void testConnection() {

		// Create single connection
		Vertx vertx = Vertx.vertx();
		RowSet<Row> rowSet = blockFuture(PgConnection.connect(vertx, this.connectOptions).flatMap(conn -> {
			return doQuery(conn).flatMap(rows -> {
				return conn.close().flatMap(closed -> Future.succeededFuture(rows));
			});
		}));
		assertEquals(1, rowSet.size(), "Incorrect number of rows");
	}

	@Test
	public void testThreadedPooledConnection() {
		PoolOptions poolOptions = new PoolOptions().setMaxSize(THREAD_COUNT);
		PgPool pool = PgPool.pool(this.connectOptions, poolOptions);
		doThreadedTest(() -> pool.getConnection());
	}

	@Test
	public void testThreadedSamePooledConnection() {
		PoolOptions poolOptions = new PoolOptions().setMaxSize(THREAD_COUNT);
		PgPool pool = PgPool.pool(this.connectOptions, poolOptions);
		Future<? extends SqlClient> connection = pool.getConnection();
		doThreadedTest(() -> connection);
	}

	@Test
	public void testThreadedSameConnection() {
		Vertx vertx = Vertx.vertx();
		Future<? extends SqlClient> connection = PgConnection.connect(vertx, this.connectOptions);
		doThreadedTest(() -> connection);
	}

	private static void doThreadedTest(Supplier<Future<? extends SqlClient>> factory) {
		ThreadSafeClosure<Integer>[] validations = new ThreadSafeClosure[THREAD_COUNT];
		for (int i = 0; i < validations.length; i++) {
			final int index = i;
			validations[index] = new ThreadSafeClosure<>();
			new Thread(() -> {
				RowSet<Row> rowSet = blockFuture(factory.get().flatMap(conn -> doQuery(conn)));
				validations[index].set(rowSet.size());
			}).start();
		}

		for (int i = 0; i < validations.length; i++) {
			assertEquals(1, validations[i].waitAndGet(), "Incorrect size for " + i);
		}
	}

	@Test
	public void testRequestResponse() {
		Vertx vertx = Vertx.vertx();
		RowSet<Row> rowSet = blockFuture(PgConnection.connect(vertx, this.connectOptions).flatMap(conn -> {

			// Trigger multiple request / response
			Future<RowSet<Row>> query = doQuery(conn);
			for (int i = 0; i < REQUEST_COUNT; i++) {
				final int index = i;
				query = query.flatMap(assertRows("Connection", index)).flatMap(asserted -> doQuery(conn));
			}

			// Return further query (closing connection)
			return query.flatMap(rows -> {
				return conn.close().flatMap(closed -> Future.succeededFuture(rows));
			});
		}));
		assertEquals(1, rowSet.size(), "Incorrect number of rows");
	}

	@Test
	public void testPipeline() {

		Vertx vertx = Vertx.vertx();
		RowSet<Row> rowSet = blockFuture(PgConnection.connect(vertx, this.connectOptions).flatMap(conn -> {

			// Pipeline the queries
			Future<RowSet<Row>>[] queries = new Future[REQUEST_COUNT];
			for (int i = 0; i < queries.length; i++) {
				queries[i] = doQuery(conn);
			}

			// Confirm all queries
			Future<Void> confirm = Future.succeededFuture();
			for (int i = 0; i < queries.length; i++) {
				final int index = i;
				confirm = confirm.compose(confirmed -> queries[index].compose(assertRows("Pipeline", index)));
			}

			// Return further query (closing connection)
			return confirm.flatMap(rows -> {
				return conn.close().flatMap(closed -> queries[0]);
			});
		}));
		assertEquals(1, rowSet.size(), "Incorrect number of rows");
	}

	private static Future<RowSet<Row>> doQuery(SqlClient client) {
		return client.preparedQuery("SELECT * FROM TEST WHERE id=$1").execute(Tuple.of(1));
	}

	private static Function<RowSet<Row>, Future<Void>> assertRows(String prefix, int index) {
		return rows -> {
			System.out.println(prefix + " (" + index + ") rows " + rows.size());
			assertEquals(1, rows.size(), "Should have found rows");
			return Future.succeededFuture();
		};
	}

	private static <T> T blockFuture(Future<T> future) {
		ThreadSafeClosure<T> complete = new ThreadSafeClosure<>();
		future.onComplete(ar -> {
			if (ar.succeeded()) {
				complete.set(ar.result());
			} else {
				complete.failure(ar.cause());
			}
		});
		return complete.waitAndGet();
	}

}
