package net.officefloor.web.build;

/**
 * Factory for the creation of {@link HttpObjectParser} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectParserFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectParser} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectParser} for the {@link Object}.
	 * 
	 * @param <T>
	 *            Object type.
	 * @param objectClass
	 *            {@link Object} {@link Class}.
	 * @return {@link HttpObjectParser} for the {@link Object}. May return
	 *         <code>null</code> if does not support parsing out the particular
	 *         {@link Object}.
	 * @throws Exception
	 *             If fails to create the {@link HttpObjectParser} for the
	 *             {@link Object}.
	 */
	<T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception;

}