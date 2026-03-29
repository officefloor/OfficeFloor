package net.officefloor.spring.starter.rest;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest({ MockRestController.class, MockController.class })
@Import(MockSecurityConfig.class)
public class BaselineSpringMvcTest extends AbstractBaselineSpringRestVerification {
}
