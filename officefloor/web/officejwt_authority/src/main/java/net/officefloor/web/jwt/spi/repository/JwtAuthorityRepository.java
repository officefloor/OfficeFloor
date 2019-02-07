package net.officefloor.web.jwt.spi.repository;

import java.time.Instant;
import java.util.List;

import net.officefloor.web.jwt.spi.encode.JwtEncodeKey;
import net.officefloor.web.jwt.spi.refresh.JwtRefreshKey;

/**
 * JWT repository.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthorityRepository {

	/**
	 * Retrieves the list of {@link JwtEncodeKey} instances.
	 * 
	 * @param activeAfter Time in seconds to obtain all active {@link JwtEncodeKey}
	 *                    instances.
	 * @return {@link JwtEncodeKey} instances.
	 * @throws Exception Possible failure in retrieving the {@link JwtEncodeKey}
	 *                   instances.
	 */
	List<JwtEncodeKey> retrieveJwtEncodeKeys(Instant activeAfter) throws Exception;

	/**
	 * Saves new {@link JwtEncodeKey} instances.
	 * 
	 * @param encodeKeys New {@link JwtEncodeKey} instances.
	 * @throws Exception If fails to save the {@link JwtEncodeKey} instance.
	 */
	void saveJwtEncodeKeys(JwtEncodeKey... encodeKeys) throws Exception;

	/**
	 * Retrieves the list of {@link JwtRefreshKey} instances.
	 * 
	 * @param activeAfter Time in seconds to obtain all active {@link JwtRefreshKey}
	 *                    instances.
	 * @return {@link JwtRefreshKey} instances.
	 * @throws Exception Possible failure in retrieving the {@link JwtRefreshKey}
	 *                   instances.
	 */
	List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant activeAfter) throws Exception;

	/**
	 * Saves new {@link JwtRefreshKey} instances.
	 * 
	 * @param refreshKeys New {@link JwtRefreshKey} instances.
	 * @throws Exception If fails to save the {@link JwtRefreshKey} instance.
	 */
	void saveJwtRefreshKeys(JwtRefreshKey... refreshKeys);

	/**
	 * <p>
	 * Allows overriding to take distributed locks within the cluster to avoid
	 * duplicate keys being generated. This is <strong>optional</strong> to
	 * implement.
	 * <p>
	 * Default is to allow duplicate keys to be created. However, logic of JWT
	 * should allow handling multiple active keys.
	 * 
	 * @param clusterCriticalSection {@link ClusterCriticalSection}.
	 * @throws Exception If fails to undertake {@link ClusterCriticalSection}.
	 */
	default void doClusterCriticalSection(ClusterCriticalSection clusterCriticalSection) throws Exception {
		clusterCriticalSection.doClusterCriticalSection(this);
	}

	/**
	 * Critical section logic for the cluster.
	 */
	@FunctionalInterface
	public interface ClusterCriticalSection {

		/**
		 * Undertakes the critical section.
		 * 
		 * @param repository Allows overriding the {@link JwtAuthorityRepository} to
		 *                   provide context for the cluster locking. This
		 *                   {@link JwtAuthorityRepository} will be used for the
		 *                   critical section logic.
		 * @throws Exception Possible failure from critical section.
		 */
		void doClusterCriticalSection(JwtAuthorityRepository repository) throws Exception;
	}

}