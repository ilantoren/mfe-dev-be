package com.mfe;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mfe.model.ingredient.EntityMapping;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.utils.IngredientPOJOService;


@Component
public class FileBasedIngredientPOJOService implements IngredientPOJOService {
	
	@Autowired
	IngredientPojoRepository ingredientPojoRepository;
	
	
	@Autowired
	EntityMappingRepository entityMappingRepository;
	
	Map<String,EntityMapping> entityMap;
	Map<String,IngredientPOJO> ingredientMap;
	
	public FileBasedIngredientPOJOService() throws JsonParseException, JsonMappingException, IOException {
		//ObjectMapper obm = new ObjectMapper();
		
		//ClassPathResource entities = new ClassPathResource("data/entityMapping.json");
		//List<EntityMapping> entitiesList = obm.readValue( entities.getInputStream(), new TypeReference<List<EntityMapping>>(){});
		
	}
	
	@PostConstruct
	public void init() {
		
		ClassPathResource ingredients = new ClassPathResource("data/ingredientPOJO.json");
		List<IngredientPOJO> ingredientList = ingredientPojoRepository.findAll();
		HashSet<String> uniqueUid = new HashSet<>();
		ingredientMap = ingredientList.stream()
				.filter( a -> uniqueUid.add(  a.getUid() ))
				.collect( Collectors.toMap(IngredientPOJO::getUid, Function.identity()));
		
		List<EntityMapping> entitiesList = entityMappingRepository.findAll();
		
		entityMap = entitiesList.stream().collect( Collectors.toMap(EntityMapping::getId, Function.identity()));

	}

	@Override
	public IngredientPOJO getById(String id) {
		if ( ingredientMap.containsKey(id)) {
			return ingredientMap.get( id );
		}
		return null;
	}

	@Override
	public IngredientPOJO getByEntityMapping(String entityId) {
		if( entityMap.containsKey(entityId)) {
			EntityMapping entity =  entityMap.get(entityId);
			if ( entity.getNdb_no() != null) {
				return getById( entity.getNdb_no() );
			}
		}
		
		return null;
	}

}
