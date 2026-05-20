package net.officefloor.web.rest.build;

/**
 * Context for the REST path.
 */
public interface RestPathContext {

    /**
     * Obtains the path.
     *
     * @return Path.
     */
    String getPath();

    /**
     * Obtains the parent {@link RestPathContext}.
     *
     * @return Parent {@link RestPathContext} or <code>null</code> if root path.
     */
    RestPathContext getParentPath();

    /**
     * <p>
     * Obtains additional configuration for the REST path.
     * <p>
     * This for example is CORS specific configuration for the REST path.
     *
     * @param itemName Name of configuration item.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration item.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

}
