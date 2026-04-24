package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.QualifiedService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("secondary")
public class SecondaryQualifiedService implements QualifiedService {

    @Override
    public String getValue() {
        return "SECONDARY";
    }
}
