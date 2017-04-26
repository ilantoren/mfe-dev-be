package com.mfe;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.demo.DemoRecipeSearch;

public interface ImageRepository extends MongoRepository<DemoRecipeSearch, String> {
	@Query( "{recipeId: ?0}")
	DemoRecipeSearch findByRecipeid( String id );
	
	@Query( value="{}",fields="{recipeId:1, title:1, url:1 , site:1, imageUrl:1}" )
	Stream<DemoRecipeSearch> findSubstituteTitle( Pageable pageable);

	@Query( value = "{}", fields="{recipeId:1, title:1, url:1 , site:1, imageUrl:1}")
	Stream<DemoRecipeSearch> findAllTitles();
	
	@Query( value = "{website: \"strauss\" }")
	List<DemoRecipeSearch> findAllStrauss();
}
