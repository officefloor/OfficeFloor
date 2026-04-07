package net.officefloor.activity.compose.build;

/**
 * Listens for compositions from a directory.
 */
public interface ComposeListener<T> {

    /**
     * Handles a composition.
     *
     * @param compositionName Name of the composition being the full name minus extension.
     * @param item            Item built.
     * @throws Exception If fails to handle composition.
     */
    void composition(String compositionName, T item) throws Exception;

}
