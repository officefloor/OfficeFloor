package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.common.MvcException;

public class RestAdviceErrorService {
    public void service() throws MvcException {
        throw new MvcException("TEST_CODE", "Test error message");
    }
}
