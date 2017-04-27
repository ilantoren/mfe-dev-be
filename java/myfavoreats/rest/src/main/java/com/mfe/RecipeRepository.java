package com.mfe;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.RecipePOJO;


@RepositoryRestResource( collectionResourceRel = "recipePOJO")
public interface RecipeRepository extends MongoRepository<RecipePOJO, String> {
	RecipePOJO findByTitle( @Param("title") String title );
	
	@Query( "{$and: [{ $text : { $search: \"muffin\"}}, { substitutionRule : {$exists: 0}}  ]}")
	List<RecipePOJO> getMuffins();
	
	@Query( "{ parentId : ?0 }")
	List<RecipePOJO> findByParentId( String parentId );
	
	@Query(value= "{ $text : {$search : ?0 }}" , fields= "{\"steps.lines\" : 1}")
	List<Line> getIngredientsByRecipe( String id );
	
	@Query(value =  " {id :  ?0 } ")
	RecipePOJO findRecipeById( String id );
	
	@Query( value= "{ $and: [{$text: {$search:\"muffin\"}}, { substitutionRule: { $exists: 0} }]}", fields="{title:1, site:1}" )
	List<RecipePOJO>   getMuffinTitles();

	@Query( value="{ substitutionRule: {$exists:0}}")
	List<RecipePOJO> findUnmodifiedRecipes();
	
	@Query( value="{ substitutionRule: {$exists:0}}", fields="{title:1, urn:1 , site:1}")
	List<RecipePOJO> findSubstituteTitleOld();
	
	
	@Query( value="{}",fields="{title:1, urn:1 ,website:1,  site:1, photos:1, categories:1}" )
	Stream<RecipePOJO> findRecipeTitles(Pageable pageable);
	
	@Query( "{ $or: [{ id: ?0 }, {parentId: ?0} ] }" )
	List<RecipePOJO> findParentAndChildren( String id );
	
	@Query(value = "{$and: [ {substitutionRule:{$exists:1}}, {substitutionRule: ?0}]}" , fields="{parentId:1, title:1, site:1}")
	List<RecipePOJO> findRecipesBySubstituteRule( String rule, Sort sort);
	
	@Query( value = "{ \"steps.lines.subs.description\" : ?0 }")
	List<RecipePOJO> findRecipesBySubstitution( String description );

	@Query( value="{$text: {$search: ?0}}", fields = "{ title:1, site:1, urn:1}")
	Stream<RecipePOJO> findBySearchPhrase( String phrase, Pageable pageable  );

	@Query( value="{title:{$regex: ?0}}", fields = "{ title:1, site:1, urn:1}")
	Stream<RecipePOJO> findTitleStartsWith( String expr );
	
	@Query( value = "{ \"steps.lines.subs.description\": {$exists:1}}", fields="{ \"steps.lines.subs.description\":1} ")
	List<RecipePOJO> findRecipeSubstitutions();
	
	@Query( value = "{}")
	public Stream<RecipePOJO> streamAllRecipes();
	
	@Query( value = "{ _id: {$in: ?0 }}", fields= "{ title:1, site:1, urn:1, photos:1, website:1, categories:1}")
	List<RecipePOJO>  findRecipesById( List<String> idList, Sort sort );
	
}
