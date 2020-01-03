package net.officefloor.spring.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Repository} for {@link RowEntity}.
 * 
 * @author Daniel Sagenschneider
 */
@Repository
public interface RowEntityRepository extends CrudRepository<RowEntity, Long> {

	List<RowEntity> findByName(String name);

}