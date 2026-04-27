package net.officefloor.spring.starter.rest.argument;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogator;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * {@link TypeQualifierInterrogator} for Spring's {@link Qualifier}.
 */
public class QualifierTypeQualifierInterrogator implements TypeQualifierInterrogatorServiceFactory, TypeQualifierInterrogator {

    @Override
    public TypeQualifierInterrogator createService(ServiceContext context) throws Throwable {
        return this;
    }

    @Override
    public String interrogate(TypeQualifierInterrogatorContext context) throws Exception {
        Qualifier qualifier = context.getAnnotatedElement().getAnnotation(Qualifier.class);
        return (qualifier != null) ? qualifier.value() : null;
    }

}
