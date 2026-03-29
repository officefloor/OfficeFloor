package net.officefloor.tutorial.transactionhttpserver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Repository} for {@link Post}.
 * 
 * @author Daniel Sagenschneider
 */
@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {
}