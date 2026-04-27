package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

public class PaginationService {
    public void service(@RequestParam(name = "page") int page,
                        @RequestParam(name = "size") int size,
                        UserRepository userRepository,
                        ObjectResponse<String> response) {
        Page<User> result = userRepository.findByActive(true, PageRequest.of(page, size));
        response.send("total=" + result.getTotalElements() + ", pages=" + result.getTotalPages()
                + ", size=" + result.getSize() + ", elements=" + result.getNumberOfElements());
    }
}
