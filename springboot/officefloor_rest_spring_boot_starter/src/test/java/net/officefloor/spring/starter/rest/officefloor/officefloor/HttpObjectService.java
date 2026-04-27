package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.spring.starter.rest.officefloor.RequestEntity;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

public class HttpObjectService {
    public void service(@HttpObject RequestEntity entity, ObjectResponse<String> response) {
        response.send(entity.getRequest());
    }
}
