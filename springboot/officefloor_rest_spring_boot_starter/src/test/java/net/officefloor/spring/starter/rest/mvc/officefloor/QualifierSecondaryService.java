package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.common.QualifiedService;
import net.officefloor.web.ObjectResponse;
import org.springframework.beans.factory.annotation.Qualifier;

public class QualifierSecondaryService {
    public void service(@Qualifier("secondary") QualifiedService qualifiedService,
                        ObjectResponse<String> response) {
        response.send(qualifiedService.getValue());
    }
}
