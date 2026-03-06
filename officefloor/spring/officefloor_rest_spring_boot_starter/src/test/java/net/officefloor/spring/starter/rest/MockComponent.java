package net.officefloor.spring.starter.rest;

import org.springframework.stereotype.Component;

@Component
public class MockComponent {

    public String getValue() {
        return "COMPONENT";
    }
}
