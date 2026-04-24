package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;

public class MatrixVariableMultipleService {
    public void service(@PathVariable String segment,
//                        @MatrixVariable String color,
//                        @MatrixVariable String year,
                        ObjectResponse<String> response) {
//        response.send(color + "-" + year);
        response.send("TODO implement @MatrixVariable");
    }
}
