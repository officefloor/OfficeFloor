package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link SpringArguments} for MVC.
 */
public class SpringMvcArguments implements SpringArguments, SpringArgumentsServiceFactory {

    /**
     * Obtains the Spring argument annotation types.
     *
     * @param context {@link SourceContext}.
     * @return Spring argument annotation types.
     */
    public static Set<Class<?>> getSpringArgumentAnnotationTypes(SourceContext context) {
        Set<Class<?>> mvcArgumentAnnotationTypes = new HashSet<>();
        for (SpringArguments arguments : context.loadServices(SpringArgumentsServiceFactory.class, new SpringMvcArguments())) {
            mvcArgumentAnnotationTypes.addAll(Arrays.asList(arguments.getArgumentAnnotationTypes()));
        }
        return mvcArgumentAnnotationTypes;
    }

    /*
     * ================ SpringArguments ====================
     */

    @Override
    public SpringArguments createService(ServiceContext context) throws Throwable {
        return this;
    }

    @Override
    public Class<?>[] getArgumentAnnotationTypes() {
        return new Class[]{PathVariable.class, RequestParam.class};
    }

}
