package net.officefloor.spring.starter.rest;

import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;

public class RequestEntityHttpObjectResponderFactory implements HttpObjectResponderFactory {

    /*
     * ===================== HttpObjectResponderFactory ================
     */

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
        return null;
    }

    @Override
    public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
        return null; // don't handle escalations
    }
}
