package net.officefloor.web.rest.build;

/**
 * Obtains the configuration item for a {@link RestEndpoint}.
 */
public interface RestEndpointConfiguration {

    /**
     * Obtains the configuration item.
     *
     * @param itemName Item name.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration or <code>null</code> if not configured or invalid.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

}
