package net.officefloor.spring.starter.rest.officefloor.officefloor;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.web.ObjectResponse;

public class HttpServletRequestService {
    public void service(HttpServletRequest request, ObjectResponse<String> response) {
        response.send(request.getParameter("name"));
    }
}
