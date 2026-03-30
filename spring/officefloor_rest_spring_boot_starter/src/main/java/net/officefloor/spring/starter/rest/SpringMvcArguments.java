package net.officefloor.spring.starter.rest;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

        // Determine if Spring Security is available
        Class<?> authenticationPrincipalClass = context.loadOptionalClass(AUTHENTICATION_PRINCIPAL_CLASS_NAME);

        // Return the argument annotations
        List<Class<?>> argumentAnnotations = new ArrayList<>(Arrays.asList(
                PathVariable.class,
                RequestParam.class,
                RequestHeader.class,
                CookieValue.class,
                RequestBody.class,
                ModelAttribute.class,
                Value.class,
                RequestPart.class));
        if (authenticationPrincipalClass != null) {
            argumentAnnotations.add(authenticationPrincipalClass);
        }
        return argumentAnnotations.toArray(Class[]::new);
    }

    @Override
    public Class<?>[] getArgumentTypes(SourceContext context) {
        return new Class[] {
                BindingResult.class,
                Model.class
        };
    }

}
