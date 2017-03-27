package com.mfe;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.demo.DropDownTitle;

public interface DropDownTitleRepository extends MongoRepository< DropDownTitle, String> {
	
	@Query( value="{}")
	List<DropDownTitle> findAll();
}
