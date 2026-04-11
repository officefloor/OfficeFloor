package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Provides test helper methods to allow naming conventions on test {@link Class} names
 * and resulting test URL paths.
 */
public abstract class AbstractVerification {

    /**
     * Allow simplifying configuration.
     */
    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("officefloor.rest.config.testpackage", AbstractVerification.class::getPackageName);
    }

    /**
     * Convenience to allow tests to handle JSON content.
     */
    protected @Autowired ObjectMapper mapper;

    /**
     * Service implementations available.
     */
    protected enum ServiceImplementation {
        SPRING, OFFICEFLOOR
    }

    /**
     * Test implementation must indicate which service implementation.
     *
     * @return {@link AbstractMockMvcVerification.ServiceImplementation}.
     */
    protected AbstractMockMvcVerification.ServiceImplementation getServiceImplementation() {
        String simpleClassName = this.getClass().getSimpleName();
        if (simpleClassName.startsWith("NativeSpring")) {
            return AbstractMockMvcVerification.ServiceImplementation.SPRING;
        } else if (simpleClassName.startsWith("OfficeFloor")) {
            return AbstractMockMvcVerification.ServiceImplementation.OFFICEFLOOR;
        } else {
            return fail("Unable to determine service implementation from class " + simpleClassName);
        }
    };

    /**
     * Use package name to determine <code>spring-boot-starter</code> being tested.
     *
     * @return Path part to identify the <code>spring-boot-starter</code> being tested.
     */
    protected String getBootStarterName() {
        String topLevelPackageName = AbstractMockMvcVerification.class.getPackageName();
        String packageName = this.getClass().getPackageName();
        String starterName = packageName.substring(topLevelPackageName.length() + ".".length());
        return starterName.replace('.', '/');
    }

    /**
     * Obtain path specific to {@link AbstractMockMvcVerification.ServiceImplementation} and <code>spring-boot-starter</code> being tested.
     *
     * @param path Test specific path.
     * @return Path for test to use.
     */
    protected String getPath(String path) {
        return "/" + this.getServiceImplementation().name().toLowerCase() + "/" + this.getBootStarterName() + path;
    }

    /**
     * <p>
     * Obtains the test URL path with consideration that generically handled by Spring.
     * <p>
     * For example, Spring Security login is used as is within {@link OfficeFloorSpringBootExtension}.
     * <p>
     * Note that in most cases this is used with <code>true</code> so just returns the path.  However, included for
     * consistency in tests and clarity in reading tests.
     *
     * @param path            Test specific path.
     * @param isSpringHandled Indicates if handled by Spring without {@link OfficeFloorSpringBootExtension}.
     * @return Path for test to use.
     */
    protected String getPath(String path, boolean isSpringHandled) {
        return isSpringHandled ? path : this.getPath(path);
    }

}
