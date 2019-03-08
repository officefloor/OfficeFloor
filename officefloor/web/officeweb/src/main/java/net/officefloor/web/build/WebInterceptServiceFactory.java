package net.officefloor.web.build;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpRequest;

/**
 * <p>
 * {@link ServiceFactory} to provide a single {@link Method} {@link Class} for
 * intercepting all {@link HttpRequest} instances before they are serviced by
 * WoOF.
 * <p>
 * This is typically useful for adding additional non-application logic
 * information to responses, such as CORS headers.
 * <p>
 * The {@link Class} is loaded with a {@link ClassSectionSource} and must have
 * only one {@link SectionInput} and one {@link SectionOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebInterceptServiceFactory extends ServiceFactory<Class<?>> {
}