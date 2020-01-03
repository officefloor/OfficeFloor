package net.officefloor.web.security.store;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * <p>
 * Store containing the credentials.
 * <p>
 * This is a standard interface to allow various credential stores to be
 * utilised as a dependency for {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CredentialStore {

	/**
	 * Default algorithm.
	 */
	public static final String DEFAULT_ALGORITHM = "MD5";

	/**
	 * <p>
	 * Non-blank value indicating no algorithm.
	 * <p>
	 * This is useful for property configurations of {@link ManagedObjectSource}
	 * and {@link ManagedFunctionSource} instances that need to indicate an
	 * algorithm (and can not provide blank values for required properties).
	 */
	public static final String NO_ALGORITHM = "-";

	/**
	 * <p>
	 * Obtains the algorithm used to encrypt credentials within this
	 * {@link CredentialStore}.
	 * <p>
	 * Should the return be <code>null</code>, blank or {@link #NO_ALGORITHM}
	 * then the password is considered to be stored in plain text. This is
	 * however only useful for the <code>BASIC</code> authentication scheme due
	 * to the nature of the other authentication schemes (such as
	 * <code>DIGEST</code>).
	 * <p>
	 * It is expected that the credentials for <code>DIGEST</code> will be
	 * stored as the algorithm applied to <code>userId:realm:password</code> (as
	 * per RFC 2617). This is necessary as the password is never supplied and
	 * therefore for <code>DIGEST</code> this MUST return an algorithm.
	 * 
	 * @return Algorithm.
	 */
	String getAlgorithm();

	/**
	 * Retrieves the {@link CredentialEntry}.
	 * 
	 * @param userId
	 *            User identifier.
	 * @param realm
	 *            Realm. May be <code>null</code> (especially in the case for
	 *            <code>Basic</code> authentication).
	 * @return {@link CredentialEntry} or <code>null</code> if no
	 *         {@link CredentialEntry} exists for parameters.
	 * @throws HttpException
	 *             If fails to retrieve {@link CredentialEntry}.
	 */
	CredentialEntry retrieveCredentialEntry(String userId, String realm) throws HttpException;

}