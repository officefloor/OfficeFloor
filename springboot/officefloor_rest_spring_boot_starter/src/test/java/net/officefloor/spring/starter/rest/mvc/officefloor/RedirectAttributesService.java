package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class RedirectAttributesService {
    public void service(RedirectAttributes attrs, ViewResponse response) {
        attrs.addAttribute("status", "ok");
        response.send("redirect:/mvc-destination");
    }
}
