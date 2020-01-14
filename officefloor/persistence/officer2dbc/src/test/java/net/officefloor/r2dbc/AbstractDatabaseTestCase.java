package net.officefloor.r2dbc;

import java.time.Duration;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract test functionality for working with database.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDatabaseTestCase extends OfficeFrameTestCase {

	/**
	 * Time out of blocking.
	 */
	protected final static Duration TIMEOUT = Duration.ofSeconds(3);

	/**
	 * Name of database.
	 */
	protected final static String DATABASE = "test";

	/**
	 * User.
	 */
	protected final static String USER = "SA";

	/**
	 * Password.
	 */
	protected final static String PASSWORD = "Password";

	/**
	 * Sets up the database.
	 */
	protected void setupDatabase() {
		ConnectionFactory connectionFactory = new H2ConnectionFactory(
				H2ConnectionConfiguration.builder().inMemory(DATABASE).property(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
						.username(USER).password(PASSWORD).build());
		Mono<Connection> connection = Mono.from(connectionFactory.create());
		Flux<Integer> setup = connection
				.flatMapMany(c -> Flux.from(c.createBatch().add("DROP TABLE IF EXISTS test")
						.add("CREATE TABLE test (id IDENTITY(1,1), message VARCHAR(50))")
						.add("INSERT INTO test (message) VALUES ('TEST')").execute()))
				.flatMap(r -> Flux.from(r.getRowsUpdated()))
				.doFinally((signal) -> connection.flatMap(c -> Mono.from(c.close())).subscribe());
		int insertCount = setup.blockLast(TIMEOUT);
		assertEquals("Should insert row", 1, insertCount);
	}

	/**
	 * Configures the {@link R2dbcManagedObjectSource}.
	 * 
	 * @param office   {@link OfficeArchitect}.
	 * @param r2dbcMos {@link R2dbcManagedObjectSource}.
	 * @param isPool   Indicates if to use {@link ConnectionPool}.
	 * @return {@link OfficeManagedObject} for the {@link R2dbcManagedObjectSource}.
	 */
	protected OfficeManagedObject configureR2dbcSource(OfficeArchitect office, R2dbcManagedObjectSource r2dbcMos,
			boolean isPool) {
		OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS", r2dbcMos);
		mos.addProperty(ConnectionFactoryOptions.DRIVER.name(), isPool ? "pool" : "h2");
		mos.addProperty(ConnectionFactoryOptions.PROTOCOL.name(), isPool ? "h2:mem" : "mem");
		mos.addProperty(ConnectionFactoryOptions.DATABASE.name(), DATABASE);
		mos.addProperty(ConnectionFactoryOptions.USER.name(), USER);
		mos.addProperty(ConnectionFactoryOptions.PASSWORD.name(), PASSWORD);
		return mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
	}

}