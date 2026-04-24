package net.officefloor.spring.starter.rest.mvc.common;

public class MvcException extends Exception {

    private final String code;

    public MvcException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
