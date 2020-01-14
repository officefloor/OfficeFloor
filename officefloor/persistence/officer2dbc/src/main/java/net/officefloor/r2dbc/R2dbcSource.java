package net.officefloor.r2dbc;

import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

/**
 * Obtains a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface R2dbcSource {

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Mono} providing the {@link Connection}.
	 */
	Mono<Connection> getConnection();

}