package net.officefloor.spring.starter.rest.data;

import net.officefloor.spring.starter.rest.AbstractExtraSpringBootExtensionServiceFactory;

public class SpringBootDataExtensionServiceFactory extends AbstractExtraSpringBootExtensionServiceFactory {

    @Override
    public String getExtraKeyClassName() {
        return "org.springframework.transaction.PlatformTransactionManager";
    }

    @Override
    public String getOfficeFloorSpringBootExtensionClassName() {
        return "net.officefloor.spring.starter.rest.data.SpringBootDataExtension";
    }
}
