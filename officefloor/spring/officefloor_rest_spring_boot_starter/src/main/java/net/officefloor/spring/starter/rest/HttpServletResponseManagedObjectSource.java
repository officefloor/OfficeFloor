package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link HttpServletResponse}.
 */
public class HttpServletResponseManagedObjectSource extends AbstractSpringManagedObjectSource<HttpServletResponse> {

    public HttpServletResponseManagedObjectSource() {
        super(HttpServletResponse.class, SpringServerHttpConnection::getHttpServletResponse);
    }
}
