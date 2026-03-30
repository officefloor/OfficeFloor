package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.SourceContext;

public interface SpringArguments {

    /**
     * Listing of annotation types for Spring arguments.
     *
     * @param context {@link SourceContext}.
     * @return Annotation types for Spring arguments.
     */
    Class<?>[] getArgumentAnnotationTypes(SourceContext context);

    /**
     * Listing of types for Spring arguments.
     *
     * @param context {@link SourceContext}.
     * @return Types for Spring arguments.
     */
    Class<?>[] getArgumentTypes(SourceContext context);
}
