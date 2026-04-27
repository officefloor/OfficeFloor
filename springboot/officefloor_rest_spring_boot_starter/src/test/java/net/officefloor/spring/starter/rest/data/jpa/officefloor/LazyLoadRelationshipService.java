package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.Post;
import net.officefloor.spring.starter.rest.data.jpa.common.PostRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

public class LazyLoadRelationshipService {
    public void service(@PathVariable(name = "postTitle") String postTitle,
                        PostRepository postRepository,
                        ObjectResponse<String> response) {
        Post post = postRepository.findByTitle(postTitle).orElseThrow();
        response.send(post.getAuthor().getName());
    }
}
