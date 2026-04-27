package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

public class FindByIdService {
    public void service(@PathVariable(name = "name") String name,
                        UserRepository userRepository,
                        ObjectResponse<String> response) {
        User user = userRepository.findByName(name).orElseThrow();
        response.send(userRepository.findById(user.getId())
                .map(User::getDescription)
                .orElse("Not Found"));
    }
}
