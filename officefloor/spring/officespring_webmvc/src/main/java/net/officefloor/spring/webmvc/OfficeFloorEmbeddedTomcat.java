package net.officefloor.spring.webmvc;

import java.util.stream.Collectors;

import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Configuration} to embed {@link OfficeFloor} managed {@link Tomcat}.
 * 
 * @author Daniel Sagenschneider
 */
@Configuration(proxyBeanMethods = false)
public class OfficeFloorEmbeddedTomcat {

	@Bean
	@Primary
	public TomcatServletWebServerFactory tomcatServletWebServerFactory(
			ObjectProvider<TomcatContextCustomizer> contextCustomizers) {
		TomcatServletWebServerFactory factory = new OfficeFloorServletWebServerFactory();
		factory.getTomcatContextCustomizers().addAll(contextCustomizers.orderedStream().collect(Collectors.toList()));
		return factory;
	}

}