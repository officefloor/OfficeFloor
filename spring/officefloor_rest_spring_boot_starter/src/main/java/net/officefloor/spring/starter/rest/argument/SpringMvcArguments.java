package net.officefloor.spring.starter.rest.argument;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * {@link SpringArguments} for MVC.
 */
public class SpringMvcArguments implements SpringArguments, SpringArgumentsServiceFactory {

    /**
     * Only Spring MVC may be available on the class path.
     */
    public static String AUTHENTICATION_PRINCIPAL_CLASS_NAME = "org.springframework.security.core.annotation.AuthenticationPrincipal";
    public static String AUTHENTICATION_CLASS_NAME = "org.springframework.security.core.Authentication";

    /**
     * Obtains the Spring argument annotation types.
     *
     * @param context {@link SourceContext}.
     * @return Spring argument annotation types.
     */
    public static Set<Class<?>> getSpringArgumentAnnotationTypes(SourceContext context) {
        Set<Class<?>> mvcArgumentAnnotationTypes = new HashSet<>();
        for (SpringArguments arguments : context.loadServices(SpringArgumentsServiceFactory.class, new SpringMvcArguments())) {
            mvcArgumentAnnotationTypes.addAll(Arrays.asList(arguments.getArgumentAnnotationTypes(context)));
        }
        return mvcArgumentAnnotationTypes;
    }

    /**
     * Obtains the Spring argument types.
     *
     * @param context {@link SourceContext}.
     * @return Spring argument types.
     */
    public static Set<Class<?>> getSpringArgumentTypes(SourceContext context) {
        Set<Class<?>> mvcArgumentTypes = new HashSet<>();
        for (SpringArguments arguments : context.loadServices(SpringArgumentsServiceFactory.class, new SpringMvcArguments())) {
            mvcArgumentTypes.addAll(Arrays.asList(arguments.getArgumentTypes(context)));
        }
        return mvcArgumentTypes;
    }

    /**
     * Determines if a Spring argument.
     */
    @FunctionalInterface
    public static interface SpringArgumentChecker {
        boolean isSpringArgument(Class<?> argumentType, Object[] annotations);
    }

    /**
     * Obtains the {@link SpringArgumentChecker}.
     *
     * @param context {@link SourceContext}.
     * @return {@link SpringArgumentChecker}.
     */
    public static SpringArgumentChecker getSpringArgumentChecker(SourceContext context) {
        Set<Class<?>> mvcArgumentAnnotationTypes = getSpringArgumentAnnotationTypes(context);
        Set<Class<?>> mvcArgumentTypes = getSpringArgumentTypes(context);
        return (argumentType, annotations) -> {

            // Determine if handled by type (same or super type)
            for (Class<?> handledArgumentType : mvcArgumentTypes) {
                if (argumentType.isAssignableFrom(handledArgumentType)) {
                    return true; // Spring handled argument
                }
            }

            // Determine if handled by annotation
            for (Object annotation : annotations) {
                for (Class<?> argumentAnnotationType : mvcArgumentAnnotationTypes) {
                    if (argumentAnnotationType.isInstance(annotation)) {
                        return true; // Spring handled argument
                    }
                }
            }

            // As here, not handled by Spring
            return false;
        };
    }

    /*
     * ================ SpringArguments ====================
     */

    @Override
    public SpringArguments createService(ServiceContext context) throws Throwable {
        return this;
    }

    @Override
    public Class<?>[] getArgumentAnnotationTypes(SourceContext context) {
        return new TypesBuilder(context)
                .add(PathVariable.class)
                .add(RequestParam.class)
                .add(RequestHeader.class)
                .add(CookieValue.class)
                .add(RequestBody.class)
                .add(ModelAttribute.class)
                .add(Value.class)
                .add(RequestPart.class)
                .add(AUTHENTICATION_PRINCIPAL_CLASS_NAME)
                .getTypes();
    }

    @Override
    public Class<?>[] getArgumentTypes(SourceContext context) {
        return new TypesBuilder(context)
                .add(BindingResult.class)
                .add(Model.class)
                .add(AUTHENTICATION_CLASS_NAME)
                .getTypes();
    }

    /**
     * Builds types.
     */
    private static class TypesBuilder {

        private final List<Class<?>> classes = new LinkedList<>();

        private final SourceContext context;

        public TypesBuilder(SourceContext context) {
            this.context = context;
        }

        public TypesBuilder add(Class<?> clazz) {
            this.classes.add(clazz);
            return this;
        }

        public TypesBuilder add(String className) {
            Class<?> clazz = this.context.loadOptionalClass(className);
            if (clazz != null) {
                this.add(clazz);
            }
            return this;
        }

        public Class<?>[] getTypes() {
            return this.classes.toArray(Class[]::new);
        }
    }

}
