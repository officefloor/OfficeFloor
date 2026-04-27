package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

public class SaveService {
    public void service(@RequestBody User user,
                        UserRepository userRepository,
                        @HttpResponse(status = 201) ObjectResponse<String> response) {
        response.send(userRepository.save(user).getName());
    }
}
