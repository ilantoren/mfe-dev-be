package com.mfe.frontend;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mfe.EntityMappingRepository;
import com.mfe.IngredientPojoRepository;
import com.mfe.model.ingredient.EntityMapping;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.utils.IngredientPOJOService;


@Component
public class SpringIngredientPOJOService  {
	
	@Autowired
	IngredientPojoRepository ingredientPojoRepository;
	
	
	@Autowired
	EntityMappingRepository entityMappingRepository;
	
	Log log = LogFactory.getLog( getClass() );
	
	public SpringIngredientPOJOService() throws JsonParseException, JsonMappingException, IOException {
		//ObjectMapper obm = new ObjectMapper();
		
		//ClassPathResource entities = new ClassPathResource("data/entityMapping.json");
		//List<EntityMapping> entitiesList = obm.readValue( entities.getInputStream(), new TypeReference<List<EntityMapping>>(){});
		
	}
	
	
	
	Map<String, IngredientPOJO> ingredients = new HashMap<>();
	Map<String, EntityMapping> entities = new HashMap<>();
	Callable<Boolean> createMap = () -> {
		entities=  entityMappingRepository.findAll().stream().collect( Collectors.toMap( a -> a.getId(), Function.identity() ));
		log.info( "entities map has been built");
		ingredients  = ingredientPojoRepository.findAll().stream().collect( Collectors.toMap( a-> a.getUid(), Function.identity() ));
		log.info( "ingredients map has been built");
		return Boolean.TRUE;
	};
	
	
	
	@PostConstruct
	public void postConstruct() throws InterruptedException, ExecutionException, TimeoutException {
		log.info( "calling postConstruct");
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Boolean> c = executor.submit(createMap);
	}


	//@Override
	public IngredientPOJO getById(String id) {
		if ( ingredients.containsKey(id)) {
			return ingredients.get( id );
		}
		return null;
	}

	//@Override
	public IngredientPOJO getByEntityMapping(String entityId) {
		if( entities.containsKey(entityId)) {
			EntityMapping entity =  entities.get(entityId);
			if ( entity.getNdb_no() != null) {
				return getById( entity.getNdb_no() );
			}
		}
		
		return null;
	}

}
