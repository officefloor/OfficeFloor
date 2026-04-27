package net.officefloor.activity.compose.build;

/**
 * Listens for compositions from a directory.
 */
public interface ComposeListener<T> {

    /**
     * Handles a composition.
     *
     * @param compositionName Name of the composition.
     * @param composition     Composition built.
     */
    void composition(String compositionName, T composition);

}
