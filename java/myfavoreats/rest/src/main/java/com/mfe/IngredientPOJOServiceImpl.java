package com.mfe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mfe.model.ingredient.EntityMapping;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.ingredient.IngredientService;
import com.mfe.model.utils.IngredientPOJOService;


@Component
public class IngredientPOJOServiceImpl implements IngredientPOJOService {
	
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
		EntityMapping entity = entityMappingRepository.findById(entityId);
		if ( entity != null ) {
			String ndb = entity.getNdb_no();
			if ( ndb != null ) {
				return ingredientPojoRepository.findById(ndb);
			}
		}
		return null;
		
	}

}
