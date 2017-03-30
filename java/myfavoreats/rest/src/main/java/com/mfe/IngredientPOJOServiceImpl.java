package com.mfe;

import java.util.HashMap;
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
import org.springframework.stereotype.Component;

import com.mfe.model.ingredient.EntityMapping;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.utils.IngredientPOJOService;


@Component
public class IngredientPOJOServiceImpl implements IngredientPOJOService {
	
	public IngredientPOJOServiceImpl() {
		log.info( "IngredientPOJOServiceImpl has started");
	}
	
	@Autowired
	EntityMappingRepository entityMappingRepository;
	
	@Autowired
	IngredientPojoRepository ingredientPojoRepository;
	
	Log log  = LogFactory.getLog(IngredientPOJOServiceImpl.class);

	@Override
	public IngredientPOJO getById(String id) {
		
		return ingredientPojoRepository.findById( id );
	}

	@Override
	public IngredientPOJO getByEntityMapping(String entityId) {
		
		if ( entityId == null || entityMappingRepository == null  ) {
			log.error("Either entityId is null or the entityMappingRepository was not provided");
			return null;
		}
		EntityMapping entity = findEntityById(entityId);
		if ( entity != null ) {
			String ndb = entity.getNdb_no();
			if ( ndb != null ) {
				return findIngredientByUid(ndb);
			}
		}
		return null;
		
	}

	private IngredientPOJO findIngredientByUid(String uid) {
		return ingredients.get( uid  );
	}

	private EntityMapping findEntityById(String entityId) {
		return entities.get( entityId );
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
		executor.submit(createMap);
	}

}
