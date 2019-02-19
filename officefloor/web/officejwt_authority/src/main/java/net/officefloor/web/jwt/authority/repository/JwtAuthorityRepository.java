package net.officefloor.web.jwt.authority.repository;

import java.security.Key;
import java.util.List;

import net.officefloor.web.jwt.authority.jwks.JwksKeyWriter;
import net.officefloor.web.jwt.jwks.JwksKeyParser;

/**
 * JWT repository.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthorityRepository {

	/**
	 * Context for retrieving keys.
	 */
	public static interface RetrieveKeysContext {

		/**
		 * Obtains the time in seconds since Epoch for keys to be active.
		 * 
		 * @return Time in seconds since Epoch for keys to be active.
		 */
		long getActiveAfter();

		/**
		 * Convenience method to use the registered {@link JwksKeyParser} instances to
		 * deserialise the {@link Key}.
		 * 
		 * @param serialisedKeyContent Serialised {@link Key} content.
		 * @return {@link Key}.
		 */
		Key deserialise(String serialisedKeyContent);
	}

	/**
	 * Context for saving keys.
	 */
	public static interface SaveKeysContext {

		/**
		 * Convenience method to use the registered {@link JwksKeyWriter} instances to
		 * serialise the {@link Key}.
		 * 
		 * @param key {@link Key} to be serialised.
		 * @return Serialised {@link Key} content.
		 */
		String serialise(Key key);
	}

	/**
	 * Retrieves the list of {@link JwtAccessKey} instances.
	 * 
	 * @param context {@link RetrieveKeysContext}.
	 * @return {@link JwtAccessKey} instances.
	 * @throws Exception Possible failure in retrieving the {@link JwtAccessKey}
	 *                   instances.
	 */
	List<JwtAccessKey> retrieveJwtAccessKeys(RetrieveKeysContext context) throws Exception;

	/**
	 * Saves new {@link JwtAccessKey} instances.
	 * 
	 * @param context    {@link SaveKeysContext}.
	 * @param accessKeys New {@link JwtAccessKey} instances.
	 * @throws Exception If fails to save the {@link JwtAccessKey} instance.
	 */
	void saveJwtAccessKeys(SaveKeysContext context, JwtAccessKey... accessKeys) throws Exception;

	/**
	 * Retrieves the list of {@link JwtRefreshKey} instances.
	 * 
	 * @param context {@link RetrieveKeysContext}.
	 * @return {@link JwtRefreshKey} instances.
	 * @throws Exception Possible failure in retrieving the {@link JwtRefreshKey}
	 *                   instances.
	 */
	List<JwtRefreshKey> retrieveJwtRefreshKeys(RetrieveKeysContext context) throws Exception;

	/**
	 * Saves new {@link JwtRefreshKey} instances.
	 * 
	 * @param context     {@link SaveKeysContext}.
	 * @param refreshKeys New {@link JwtRefreshKey} instances.
	 * @throws Exception If fails to save the {@link JwtRefreshKey} instance.
	 */
	void saveJwtRefreshKeys(SaveKeysContext context, JwtRefreshKey... refreshKeys);

	/**
	 * <p>
	 * Allows overriding to take distributed locks within the cluster to avoid
	 * duplicate keys being generated. This is <strong>optional</strong> to
	 * implement.
	 * <p>
	 * Default is to allow duplicate keys to be created. This is handled by logic of
	 * JWT allowing multiple active keys.
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