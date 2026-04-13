package net.officefloor.spring.starter.rest.officefloor.officefloor;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HttpServletResponseService {
    public void service(HttpServletResponse response) throws IOException {
        response.getWriter().write("Servlet");
    }
}
