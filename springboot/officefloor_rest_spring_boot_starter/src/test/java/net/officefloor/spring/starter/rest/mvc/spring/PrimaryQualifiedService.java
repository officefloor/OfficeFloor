package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.QualifiedService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("primary")
public class PrimaryQualifiedService implements QualifiedService {

    @Override
    public String getValue() {
        return "PRIMARY";
    }
}
