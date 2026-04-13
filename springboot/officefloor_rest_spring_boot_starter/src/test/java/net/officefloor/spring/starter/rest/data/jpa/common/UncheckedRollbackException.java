package net.officefloor.spring.starter.rest.data.jpa.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UncheckedRollbackException extends RuntimeException {
}
