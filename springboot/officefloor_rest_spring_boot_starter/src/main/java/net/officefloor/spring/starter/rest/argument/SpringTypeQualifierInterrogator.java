package net.officefloor.spring.starter.rest.argument;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogator;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;

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

    /**
     * {@link SpringMvcArguments.SpringArgumentChecker}.
     */
    private SpringMvcArguments.SpringArgumentChecker springArgumentChecker;

    /*
     * ====================== ServiceFactory =======================
     */

    @Override
    public TypeQualifierInterrogator createService(ServiceContext context) throws Throwable {

        // Load the Spring argument checker
        this.springArgumentChecker = SpringMvcArguments.getSpringArgumentChecker(context);

        // Return interrogator
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
            Method method = (Method) executable;

            // Obtain details
            Class<?> objectType = method.getParameterTypes()[context.getExecutableParameterIndex()];
            Object[] annotations = method.getParameterAnnotations()[context.getExecutableParameterIndex()];

            // Determine if have spring annotation
            if (this.springArgumentChecker.isSpringArgument(objectType, annotations)) {
                return getSpringTypeQualifier(method, context.getExecutableParameterIndex());
            }
        }

        // Not spring annotated
        return null;
    }
}
