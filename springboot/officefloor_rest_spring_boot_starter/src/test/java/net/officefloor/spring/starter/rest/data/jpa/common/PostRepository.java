package net.officefloor.spring.starter.rest.data.jpa.common;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorName(String authorName);

    Optional<Post> findByTitle(String title);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p WHERE p.title = :title")
    Optional<Post> findByTitleWithAuthor(@Param("title") String title);
}
