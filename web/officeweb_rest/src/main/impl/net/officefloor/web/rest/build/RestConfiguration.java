package net.officefloor.web.rest.build;

/**
 * Obtains the configuration item.
 */
public interface RestConfiguration {

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
