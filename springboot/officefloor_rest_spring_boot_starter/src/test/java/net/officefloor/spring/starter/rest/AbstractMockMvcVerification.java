package net.officefloor.spring.starter.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Includes {@link AbstractVerification} helper methods and convenience of {@link MockMvc} available.
 */
public abstract class AbstractMockMvcVerification extends AbstractVerification {

    /**
     * Available as majority tests require.
     */
    protected @Autowired MockMvc mvc;

}
