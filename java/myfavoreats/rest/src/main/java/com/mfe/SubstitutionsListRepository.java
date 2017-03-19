package com.mfe;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.recipe.Substitutions;

public interface SubstitutionsListRepository extends MongoRepository<SubstitutionsList, String> {
	
	@Query( value = "{$and: [{ 'subs.sourceId': ?0 }, {'subs.options.targetId' : ?1 }]}")
	List<Substitutions> findBySourceIdAndTargetId( String sourceId, String targetId );

}
