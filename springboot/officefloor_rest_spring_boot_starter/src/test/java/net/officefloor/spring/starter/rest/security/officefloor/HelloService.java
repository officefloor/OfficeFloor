package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

public class HelloService {
    public void service(@PathVariable(name="name") String name, ObjectResponse<String> response) {
        response.send("Hello " + name);
    }
}
