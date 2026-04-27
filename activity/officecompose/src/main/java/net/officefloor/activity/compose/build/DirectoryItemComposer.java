package net.officefloor.activity.compose.build;

/**
 * Enables different {@link ComposeSource} implementations per directory items.
 */
public interface DirectoryItemComposer<T> {

    /**
     * Undertakes composition for the directory item.
     *
     * @param context  {@link DirectoryItemComposerContext} to handle the directory item.
     * @param listener {@link ComposeListener} to receive compositions.
     * @throws Exception If fails to handle directory item.
     */
    void compose(DirectoryItemComposerContext context, ComposeListener<T> listener) throws Exception;

}