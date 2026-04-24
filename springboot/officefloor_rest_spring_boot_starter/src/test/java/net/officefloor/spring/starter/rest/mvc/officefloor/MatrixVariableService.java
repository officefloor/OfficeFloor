package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;

public class MatrixVariableService {
    public void service(@PathVariable String segment,
//                        @MatrixVariable String city,
                        ObjectResponse<String> response) {
//        response.send(city);
        response.send("TODO implement @MatrixVariable");
    }
}
