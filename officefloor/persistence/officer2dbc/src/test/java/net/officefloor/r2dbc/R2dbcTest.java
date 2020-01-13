/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2020 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.r2dbc;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import net.officefloor.frame.test.OfficeFrameTestCase;
import reactor.core.publisher.Mono;

/**
 * Tests the R2DBC integration.
 * 
 * @author Daniel Sagenschneider
 */
public class R2dbcTest extends OfficeFrameTestCase {

	/**
	 * Ensure can use r2dbc.
	 */
	public void testR2dbcIntegration() throws Exception {

		final boolean isFail = false;

		ConnectionFactory connectionFactory = new H2ConnectionFactory(H2ConnectionConfiguration.builder()
				.inMemory("test").property(H2ConnectionOption.DB_CLOSE_DELAY, "-1").build());

		ConnectionPool pool = new ConnectionPool(ConnectionPoolConfiguration.builder(connectionFactory)
				.maxIdleTime(Duration.ofSeconds(30)).maxSize(20).build());
		try {
			Mono<Connection> connection = Mono.from(pool.create());

			// Re-use same connection
			connection = connection.cache();

			// Create mono for completion
			final AtomicBoolean isComplete = new AtomicBoolean(false);
			final AtomicInteger activeConnectionCount = new AtomicInteger(0);
			final Mono<Connection> finalConnection = connection;
			Mono<?> close = Mono.from(finalConnection.flatMap(c -> {
				if (isComplete.get() && (activeConnectionCount.get() == 0)) {
					System.out.println("CLOSE CONNECTION: " + c);
					return Mono.from(c.close());
				}
				return Mono.empty(); // not yet close
			}));

			// Ensure close connection (once done)
			connection = connection.map(c -> {
				activeConnectionCount.incrementAndGet();
				return c;
			});
			connection = connection.doFinally(s -> {
				activeConnectionCount.decrementAndGet();
				close.subscribe().dispose();
			});

			for (int i = 0; i < 10; i++) {

				connection = connection
						.flatMap(c -> Mono.from(c.createStatement("DROP TABLE IF EXISTS test").execute()).map(r -> c));

				connection = connection.flatMap(c -> Mono
						.from(c.createStatement("CREATE TABLE test (id IDENTITY(1,1), message VARCHAR(50))").execute())
						.map(r -> c));

				connection = connection.flatMap(c -> Mono
						.from(c.createStatement("INSERT INTO test (message) VALUES ('TEST')").execute()).map(r -> c));

				Mono<Result> result = connection
						.flatMap(c -> Mono.from(c.createStatement("SELECT message FROM test").execute()));

				Mono<String> data = result.map(r -> r.map((row, meta) -> {
					String message = row.get("message", String.class);
					System.out.println("ROW: " + message);
					return message;
				})).flatMap(p -> Mono.from(p));

				data = data.flatMap(d -> {
					System.out.println("Sleep");
					try {
						Thread.sleep(10);
					} catch (Exception ex) {
					}
					System.out.println("Awake");
					return Mono.just(d);
				});

				data = data.flatMap(d -> {
					if (isFail) {
						System.out.println("Failing stream");
						throw new RuntimeException("FAILED");
					}
					return Mono.just(d);
				});

//				String message = data.block();
//				System.out.println("MESSAGE: " + message);

				data.subscribe(message -> {
					System.out.println("MESSAGE: " + message);
				});
			}

			// Flag closing (and attempt to close)
			isComplete.set(true);
			close.subscribe().dispose();

			Thread.sleep(1000);

		} finally {
			System.out.println("Closing pool");
			pool.close();
		}
	}

	private static Connection proxyConnection(Connection connection) {
		return (Connection) Proxy.newProxyInstance(R2dbcTest.class.getClassLoader(), new Class[] { Connection.class },
				(proxy, method, args) -> {
					if ("close".equals(method.getName())) {
						System.out.println("Proxy not close connection");
						return null;
					} else {
						return proxy.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(proxy,
								args);
					}
				});
	}

}