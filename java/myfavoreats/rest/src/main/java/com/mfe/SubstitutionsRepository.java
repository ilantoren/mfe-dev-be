package com.mfe;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mfe.model.recipe.Substitutions;


public interface SubstitutionsRepository extends MongoRepository<Substitutions, String> {
	
	@Query( value="{recipeId: ?0}")
	@Cacheable("substitutions")
	public Substitutions findByRecipeId(String recipeId );
	
	@Query( value="{\"subs.instanceId\":{$exists:1}}", fields="{\"subs.source\":1, \"subs.sourceId\":1, \"subs.options.target\":1 ,\"subs.options.targetId\":1}")
	Stream<Substitutions> getSubstitutionFromSystem();
	
	@Query( value = "{ $and: [ { 'subs.sourceId': ?0 }, { 'subs.options.targetId': ?1 }]}", fields="{_id:1, recipeId:1}" )
	Stream<Substitutions> findBySourceAndTarget( String sourceId, String targetId );
}
