package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsService {
    public void service(@AuthenticationPrincipal UserDetails userDetails, ObjectResponse<String> response) {
        response.send(userDetails.getUsername());
    }
}
