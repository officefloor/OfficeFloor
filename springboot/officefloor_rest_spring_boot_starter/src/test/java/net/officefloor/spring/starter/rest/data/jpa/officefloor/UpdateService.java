package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UpdateRequest;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public class UpdateService {
    public void service(@PathVariable(name = "name") String name,
                        @RequestBody UpdateRequest request,
                        UserRepository userRepository,
                        ObjectResponse<String> response) {
        User user = userRepository.findByName(name).orElseThrow();
        user.setDescription(request.getDescription());
        response.send(userRepository.save(user).getDescription());
    }
}
