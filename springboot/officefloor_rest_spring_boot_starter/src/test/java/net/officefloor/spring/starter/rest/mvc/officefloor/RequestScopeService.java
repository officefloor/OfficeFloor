package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.spring.RequestScopedBean;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class RequestScopeService {
    public void service(@RequestParam String token,
                        RequestScopedBean requestScopedBean,
                        ObjectResponse<String> response) {
        requestScopedBean.setToken(token);
        response.send(requestScopedBean.getToken());
    }
}
