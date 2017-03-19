package com.mfe;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.ingredient.EntityMapping;

public interface EntityMappingRepository extends MongoRepository<EntityMapping, String> {
	@Query( value = "{id: ?0}")
	public EntityMapping findById( String id );
}
