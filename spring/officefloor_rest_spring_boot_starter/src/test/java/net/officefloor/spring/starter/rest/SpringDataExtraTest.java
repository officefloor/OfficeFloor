package net.officefloor.spring.starter.rest;

import net.officefloor.spring.starter.rest.data.SpringBootDataExtension;
import net.officefloor.spring.starter.rest.data.SpringBootDataExtensionServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringDataExtraTest {

    private final SpringBootDataExtensionServiceFactory serviceFactory = new SpringBootDataExtensionServiceFactory();

    @Test
    public void keyClass() {
        assertEquals(PlatformTransactionManager.class.getName(), serviceFactory.getExtraKeyClassName(), "Incorrect key class");
    }

    @Test
    public void source() {
        assertEquals(SpringBootDataExtension.class.getName(), serviceFactory.getOfficeFloorSpringBootExtensionClassName(), "Incorrect extension class name");
    }
}
