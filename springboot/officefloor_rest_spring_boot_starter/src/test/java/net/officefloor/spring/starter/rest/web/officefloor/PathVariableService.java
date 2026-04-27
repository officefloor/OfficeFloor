package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

public class PathVariableService {
    public void service(@PathVariable(name = "id") String id, ObjectResponse<String> response) {
        response.send("ID=" + id);
    }
}
