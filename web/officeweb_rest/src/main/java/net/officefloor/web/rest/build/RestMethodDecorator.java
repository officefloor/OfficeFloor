package net.officefloor.web.rest.build;

/**
 * Decorates the {@link RestMethod}.
 */
public interface RestMethodDecorator<M> {

    /**
     * Decorates the {@link RestMethod}.
     *
     * @param context {@link RestMethodDecoratorContext}.
     */
    void decorateRestMethod(RestMethodDecoratorContext<M> context);

}
