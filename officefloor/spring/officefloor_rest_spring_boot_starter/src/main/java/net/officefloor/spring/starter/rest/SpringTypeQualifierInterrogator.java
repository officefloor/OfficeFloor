package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogator;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * {@link TypeQualifierInterrogator} for Spring annotations.
 */
public class SpringTypeQualifierInterrogator implements TypeQualifierInterrogator, TypeQualifierInterrogatorServiceFactory {

    /**
     * Obtains the type qualifier for a Spring annotated parameter.
     *
     * @param method         {@link Method}.
     * @param parameterIndex Parameter index.
     * @return Type qualifier.
     */
    public static String getSpringTypeQualifier(Method method, int parameterIndex) {
        return "SPRING_" + method.getDeclaringClass().getName() + "." + method.getName() + "_" + parameterIndex;
    }

    /*
     * ====================== ServiceFactory =======================
     */

    @Override
    public TypeQualifierInterrogator createService(ServiceContext context) throws Throwable {
        return this;
    }

    /*
     * ================ TypeQualifierInterrogator ===================
     */

    @Override
    public String interrogate(TypeQualifierInterrogatorContext context) throws Exception {

        // Determine if method
        Executable executable = context.getExecutable();
        if (executable instanceof Method) {

            // Determine if have spring annotation
            if (context.getAnnotatedElement().isAnnotationPresent(RequestParam.class)) {
                return getSpringTypeQualifier((Method) executable, context.getExecutableParameterIndex());
            }
        }

        // Not spring annotated
        return null;
    }
}
