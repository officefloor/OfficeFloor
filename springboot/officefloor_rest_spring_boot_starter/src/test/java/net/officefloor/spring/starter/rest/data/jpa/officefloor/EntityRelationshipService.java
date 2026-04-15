package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.Post;
import net.officefloor.spring.starter.rest.data.jpa.common.PostRepository;
import net.officefloor.spring.starter.rest.data.jpa.common.PostRequest;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.stream.Collectors;

public class EntityRelationshipService {

    public void post(@PathVariable(name = "userName") String userName,
                        @RequestBody PostRequest postRequest,
                        UserRepository userRepository,
                        PostRepository postRepository,
                        @HttpResponse(status = 201) ObjectResponse<String> response) {
        User user = userRepository.findByName(userName).orElseThrow();
        Post post = new Post(null, postRequest.getTitle(), user);
        response.send(postRepository.save(post).getTitle());
    }

    public void get(@PathVariable(name = "userName") String userName,
                    PostRepository postRepository,
                    ObjectResponse<String> response) {
        response.send(postRepository.findByAuthorName(userName).stream()
                .map(Post::getTitle)
                .sorted()
                .collect(Collectors.joining(",")));
    }
}
