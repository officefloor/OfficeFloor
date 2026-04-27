package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.access.prepost.PostAuthorize;

public class PostAuthorizeService {
    @PostAuthorize("hasRole('ACCESS')")
    public void service() {
    }

    public void send(ObjectResponse<String> response) {
        response.send("Accessed");
    }
}
