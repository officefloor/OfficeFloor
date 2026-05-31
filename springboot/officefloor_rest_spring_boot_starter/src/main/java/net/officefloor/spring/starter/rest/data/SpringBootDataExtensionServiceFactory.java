package net.officefloor.spring.starter.rest.data;

import net.officefloor.spring.starter.rest.AbstractExtraSpringBootExtensionServiceFactory;

/** {@link AbstractExtraSpringBootExtensionServiceFactory} for Spring Boot Data. */
public class SpringBootDataExtensionServiceFactory extends AbstractExtraSpringBootExtensionServiceFactory {

    @Override
    public String getExtraKeyClassName() {
        return "org.springframework.data.repository.Repository";
    }

    @Override
    public String getOfficeFloorSpringBootExtensionClassName() {
        return "net.officefloor.spring.starter.rest.data.SpringBootDataExtension";
    }
}
