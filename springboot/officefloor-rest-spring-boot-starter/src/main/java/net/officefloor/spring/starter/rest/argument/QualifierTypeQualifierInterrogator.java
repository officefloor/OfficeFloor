/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
