package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class SaveAllService {
    public void service(@RequestBody List<User> users,
                        UserRepository userRepository,
                        @HttpResponse(status = 201) ObjectResponse<Integer> response) {
        response.send(userRepository.saveAll(users).size());
    }
}