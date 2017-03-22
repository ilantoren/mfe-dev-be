package com.mfe;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mfe.frontend.IngredientSorters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mfe.model.demo.DemoRecipeSearch;
import com.mfe.model.demo.DropDownTitle;
import com.mfe.model.demo.RecipeTitle;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.RecipeChange;
import com.mfe.model.recipe.RecipeComparator;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSub;
import com.mfe.model.recipe.SubstitutionResults;
import com.mfe.model.recipe.Substitutions;
import com.mfe.model.utils.IngredientSubstitution;
import com.mfe.model.utils.RecipeChangeService;


@Controller
@RestController
@RepositoryRestController
public class MfeDemoController {
	
	
	@Autowired
	MongoOperations mongoOperations;
	
	@Autowired
	ImageRepository imageRepository;
	
	@Autowired SubstitutionsRepository substitutionsRepository;
	
	@Autowired
	RecipeWithSubstituteService recipeSubstitutionService;
	
	@Autowired
	SubstitutionsListRepository subsListRepository;
	
	@Autowired
	RecipeRepository recipes;
	
	@Autowired
	FileBasedIngredientPOJOService ingredientPojoService;

	@Autowired
	IngredientPOJOServiceImpl ingredientPojo;
	
	RecipeChangeService recipeChangeService;
	
	Log log  = LogFactory.getLog(MfeDemoController.class);
	
	public MfeDemoController() {
		recipeChangeService = new RecipeChangeService(  );
	}
	

	@RequestMapping("/recipes")
	public List<RecipePOJO> getRecipes() {
		return recipes.findAll().subList(0, 100);
	}
	
	@RequestMapping("/recipe/{id}")
	public RecipePOJO getRecipe( @PathVariable("id") String id) {
		log.info( "recipePOJO " + id );
		
		RecipePOJO pojo =  recipes.findRecipeById(id);
		try {
			recipeChangeService.calculateRecipeNutrition(pojo);
		} catch (BadParameterException e) {
			log.error("Could not calculate nutrition values" , e);
		}
		return pojo;
	}
	
	@RequestMapping("/ingredients")
	public List<RecipePOJO> getIngredients() {
		return recipes.findAll().subList(0, 100);
	}
	
	
	@RequestMapping("/muffins")
	public List<RecipePOJO> getMuffins() {
		return recipes.getMuffins();
	}
	
	@RequestMapping( method=RequestMethod.GET, value = "/recipes/original")
	public List<RecipePOJO> getRecipesOriginal() {
		List<RecipePOJO> list  =  recipes.findUnmodifiedRecipes();
		
		 list.sort(new Comparator<RecipePOJO> (){

			@Override
			public int compare(RecipePOJO o1, RecipePOJO o2) {
				int i =  o1.getTitle().compareTo(  o2.getTitle() );
				return i;
			}
			
		});
		
		return list;
	}
	
	@RequestMapping( method=RequestMethod.GET, value = "/muffins/parentId/{parentId}")
	public List<RecipePOJO> getMuffinsByParentId(@PathVariable("parentId") String parentId) {
		log.info( "getRecipesByParentId   parent is "  + parentId );
		return recipes.findByParentId(parentId);
	}
	
	@RequestMapping( method = RequestMethod.GET, value = "/muffins/{id}/ingredients")
	public List<Line> getIngredientsByRecipe( @PathVariable("id") String id ) {
		RecipePOJO pojo = recipes.findRecipeById(id);
		List<Line> lines = (List<Line>) RecipePOJO.getIngredientLines(pojo);
		return lines;
	}
	
	@RequestMapping( method=RequestMethod.GET, value="/demo/titles" )
	public List<RecipePOJO> getDemoTitles() {
		List<RecipePOJO> results =  recipes.getMuffinTitles();
		return results;
	}
	
	@RequestMapping( method=RequestMethod.GET, value="/recipes/parent/{id}")
	public List<RecipePOJO> getRecipesByParentId( @PathVariable("id") String id ) {
		log.info( "Calling /recipes/parent/id with id of " + id );
		List<RecipePOJO> list = recipes.findByParentId(id );
		if ( list == null || list.isEmpty() ) {
			log.warn( "No results returned from /recipes/parent/id");
		}
		return list;
	}
	
	@RequestMapping( method=RequestMethod.GET, value="/ingredient/{id}")
	public List<IngredientPOJO> getIngredientByNdb( @PathVariable("id") String id ) {
		String search = String.format( "{ _id : \"%s\"}", id );
        Query query = new BasicQuery(search);
		return mongoOperations.find(query, IngredientPOJO.class);
	}
	
	@RequestMapping( method=RequestMethod.GET, value= "/ingredient/search/{word}")
	public List<IngredientPOJO>  searchIngredient( @PathVariable("word") String word ) {
		String search = String.format( "{ $text : { $search : \"%s\" }}", word );
        Query query = new BasicQuery(search);
		return mongoOperations.find(query, IngredientPOJO.class);
	}
	
	@RequestMapping( method=RequestMethod.GET, value= "/recipes/search/{word}")
	public List<RecipePOJO>  searchRecipes( @PathVariable("word") String word ) {
		String search = String.format( "{ $text : { $search : \"%s\" }}", word );
        Query query = new BasicQuery(search);
		List<RecipePOJO> pojos =  mongoOperations.find(query, RecipePOJO.class);
		Set<String> ids = new HashSet<>();
		Set<String> uniqueNames = new HashSet<>();
		List<RecipePOJO> result = pojos.stream().
				filter(x -> isUniqueSearchResult(x, ids, uniqueNames )).
				collect(Collectors.toList() );
		return result;
	}
	

	/*
	 * check id and if present the parentId if either has already  been added to results then skip
	 */
	private boolean isUniqueSearchResult(RecipePOJO x, Set<String> ids, Set<String> uniqueNames) {
		String id = x.getId();
		String parentid = x.getParentId();
		boolean result = ids.add( id );
		if ( result && parentid.isEmpty() && ( uniqueNames.add( x.getTitle()) || x.getTitle().equals( "no title"))  ) return true;
		return result && ids.add( parentid );
	
	}

	@RequestMapping( method=RequestMethod.GET, value="/recipes/changed/title") 
	public List<RecipeTitle> getChangedTitles() {
		// not the best practice
		recipeChangeService.setIngredientPOJOService(ingredientPojoService);
		Set<String> unique = new HashSet<>();
		List<RecipeTitle> titleList = new ArrayList<>();
		titleList.add( new RecipeTitle( "57e3a75af2ca8a7bd091c3de", "Tuna Wedges", "http://www.recipetips.com/recipe-cards/t--94629/tuna-wedges-microwave-cooking.asp", "epicurious", ""));
		PageRequest pageable = new PageRequest( 1, 50 );
		try ( Stream<DemoRecipeSearch> stream = imageRepository.findSubstituteTitle(pageable)) {
			List<RecipeTitle> titleListPart2 = stream.filter( x -> unique.add( x.getTitle() ) ).limit(100).map( x -> new RecipeTitle(x.getRecipeId(), x.getTitle() , x.getUrl() , x.getSite(), x.getImageUrl() )).collect( Collectors.toList() );
			titleList.addAll(titleListPart2);
		}
		//titleList.set(5,  new RecipeTitle( "57e3a75af2ca8a7bd091c3de", "Tuna Wedges", "http://www.recipetips.com/recipe-cards/t--94629/tuna-wedges-microwave-cooking.asp", "epicurious"));
		return titleList;
	}
	
	@RequestMapping( method=RequestMethod.GET, value="/recipes/substitute/{description}") 
	public List<RecipeTitle> getRecipesBySubstitutionRule( @PathVariable("description") String description ) {
		Date start = new Date();
		String[] searchValues = description.split(",");
		String sourceId = searchValues[0];
		String targetId = searchValues[1];
		log.info( "Searching for substitutions from " + sourceId + "  to  " +  targetId );
		List<RecipePOJO> pojos =  findRecipesWithSubstitute( sourceId, targetId );
		pojos.sort( new RecipeComparator());
		Set<String> uniqueTitle = new HashSet<>();
		List<RecipeTitle> result = pojos.stream().
				filter( x  -> uniqueTitle.add( x.getTitle() )).
				map( x -> new RecipeTitle( x.getId(), x.getTitle() ,x.getUrn() ,x.getSite(), x.getPhotos() )).
				collect( Collectors.toList());
		Date end = new Date();
		long millis = end.getTime() - start.getTime();
		log.info( "/recipes/substitute/" + description  + " results: " + pojos.size() + " time " + millis );
		return result;
		
	}
	
	private List<RecipePOJO> findRecipesWithSubstitute(String sourceId, String targetId) {
		// get list of recipes from Substitutions
       return null;
	}


	@RequestMapping( method=RequestMethod.GET, value="/recipes/title/search/{phrase}")
	List<RecipeTitle>  getRecipeTitlesBySearchPhase( @PathVariable("phrase") String phrase ) {
		
		Date start = new Date();
		Set<String> recipeIdSet = recipes.findBySearchPhrase("*"+phrase).map(a -> a.getId()).collect( Collectors.toSet() );
		List<RecipeTitle> result = imageRepository.findAllTitles().filter( a -> recipeIdSet.contains(a.getRecipeId()) )
		.map(a -> new RecipeTitle( a.getRecipeId(), a.getTitle(), a.getUrl(), a.getSite(), a.getImageUrl() ) ).collect( Collectors.toList());
		Date queryEnd = new Date();
		long milis = queryEnd.getTime() - start.getTime();
		log.info("/recipes/title/search/{phrase} Phrase: " + phrase  + " duration "  + milis);
		
		return result;
	}

	@RequestMapping( method=RequestMethod.GET, value="/recipes/title/autocomplete/{phrase}")
	List<RecipeTitle>  getRecipeTitlesAutoComplete( @PathVariable("phrase") String chars ) {

		Date start = new Date();
		Set<String> recipeIdSet = recipes.findTitleStartsWith(chars).map(a -> a.getId()).collect( Collectors.toSet() );
		List<RecipeTitle> result = imageRepository.findAllTitles().filter( a -> recipeIdSet.contains(a.getRecipeId()) )
		.map(a -> new RecipeTitle( a.getRecipeId(), a.getTitle(), a.getUrl(), a.getSite(), a.getImageUrl() ) ).collect( Collectors.toList());
		Date queryEnd = new Date();
		long milis = queryEnd.getTime() - start.getTime();
		log.info("/recipes/title/search/{phrase} Phrase: " + chars  + " duration "  + milis);

		return result;
	}
	
	@RequestMapping( method=RequestMethod.GET, value = "/recipes/with-substitute/{id}")
	@ResponseBody
	String getRecipeWithSubstitute( @PathVariable("id") String id, @RequestParam(value="target", defaultValue="NONE") String targetId ){
		Date startTime = new Date();
		if ( targetId == null ) targetId = "NONE";
		if ( targetId.equals("NONE")) {
			log.info( "Start /recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id );
		}else {
			log.info( "/recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id + " and targetId " + targetId );
		}
		
		RecipePOJO parentRecipe = recipes.findRecipeById(id);
		if ( parentRecipe == null ) {
			log.warn("recipePOJO was not found for id " + id );
		}
		else {
			 List<RecipePOJO> myrecipes = recipeSubstitutionService.getRecipeAndSubstitute(parentRecipe, targetId);
			if ( myrecipes.isEmpty() ) {
				log.warn( "No results were obtained from the recipeSubstitutionService " + id );
			}
			else {
			RecipePOJO childRecipe = myrecipes.get(0);
			RecipeChange rc = childRecipe.getRecipeChange();
			RecipeChangeService.prepareForDisplay(rc);
			childRecipe.setRecipeChange(rc);
			XmlMapper map = new XmlMapper();
			ObjectWriter ow = map.writer();
			return serializeResult(ow, myrecipes);
			}
		}
		log.warn( "Empty result set returned for recipePojo " + id );
		Date stopTime = new Date();
		long elapsed =  stopTime.getTime() - startTime.getTime();
		log.info( "End " + Long.toString(elapsed) + "ms:   /recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id );
		return null;
	}

	@RequestMapping( method=RequestMethod.GET, value = "/recipes/full-substitute/{id}")
	@ResponseBody
	RecipePOJO getRecipeFullSubstitute( @PathVariable("id") String id, @RequestParam(value="sort", defaultValue="NONE") String sort ){

		RecipePOJO parentRecipe = recipes.findRecipeById(id);

		List<RecipePOJO> myrecipes = recipeSubstitutionService.getRecipeAndSubstitute(parentRecipe, null);
		if ( myrecipes.isEmpty() ) {
			log.warn( "No results were obtained from the recipeSubstitutionService " + id );
		}
		else{
			RecipePOJO subs = myrecipes.get(0);
			subs.getSubs().forEach(sub -> {
				sub.getOptions().forEach( option -> {
					option.setIngredient(ingredientPojo.getByEntityMapping((option.getTargetId())));
				});

				if ("carbs".equals(sort)){
					sub.getOptions().sort(new IngredientSorters.LowCarbsCompare());
				}

			});

			return subs;
		}

		return null;
	}

	
	@RequestMapping( method=RequestMethod.GET,  value = "/recipes/changed/pair/{id}")
	@ResponseBody
	String getRecipeChildAndParent(@PathVariable("id") String id, @RequestParam("child") String childIndicator,
			@RequestParam("title") String titleIn) {
		log.info(childIndicator.getClass().getName() + " " + childIndicator);
		
		final String title = titleIn.replaceAll("\\?.+", "");
		if (childIndicator.equals("1")) {
			log.info("/recipes/changed/pair/id called with title of " + title + " and an id of " + id);
		} else {
			log.info("/recipes/changed/pair/id called with an id of " + id);
		}
		XmlMapper map = new XmlMapper();
		ObjectWriter ow = map.writer();
		List<RecipePOJO> myrecipes = new ArrayList<>();
		List<RecipePOJO> parentAndChildren = recipes.findParentAndChildren(id);
		List<SubstitutionResults> subResults = parentAndChildren.stream().filter( y -> y.getRecipeChange() != null).map( y -> new SubstitutionResults( y.getId(), y.getRecipeChange())).collect( Collectors.toList() );

		Optional<RecipePOJO> parentOpt = parentAndChildren.stream().filter(x -> x.getSubstitutionRule() == null)
				.findFirst();
		
		Map<String,String> substitutionMap = getSubstitutionMap( parentAndChildren );
		
		Optional<RecipePOJO> childOpt;
		if (childIndicator.equals("1")) {
			childOpt = parentAndChildren.stream()
					.filter(x -> (x.getRecipeChange() != null && x.getSubstitutionRule().equals(title))).findFirst();
		} else {
			Optional<Double> minOpt = parentAndChildren.stream().filter(x -> x.getRecipeChange() != null)
					.map(x -> x.getRecipeChange().getProbability()).min(Double::compare);
			
			if ( minOpt.isPresent() ) {
				Double min = minOpt.get();
				childOpt = parentAndChildren.stream()
					.filter(x -> (x.getRecipeChange() != null && x.getRecipeChange().getProbability() <= min))
					.findFirst();
			}else {
				childOpt = parentAndChildren.stream()
						.filter(x -> (x.getRecipeChange() != null)).findFirst();
			}
		}

		if (parentOpt.isPresent() && childOpt.isPresent()) {
			RecipePOJO parentRecipe = parentOpt.get();
			RecipePOJO childRecipe = childOpt.get();
			childRecipe.setSubstitutionResult(subResults);
			myrecipes.add(childRecipe);
			RecipeChange rc = childOpt.get().getRecipeChange();
			RecipeChangeService.prepareForDisplay(rc);
			addSubstitutionMap( parentRecipe, substitutionMap );
			myrecipes.add( parentRecipe );
			return serializeResult(ow, myrecipes);
		}

		log.warn("INVALID  RESPONSE  recipeId: " + id);
		return null;
	}
	@RequestMapping( method=RequestMethod.GET,  value = "/recipes/changed/child/{id}")
	@ResponseBody
	String getRecipeChild(@PathVariable("id") String childId, @RequestParam("parentId") String parentId) {
		log.info( "getRecipeChild with child id: " + childId );
		XmlMapper map = new XmlMapper();
		ObjectWriter ow = map.writer();
		List<RecipePOJO> myrecipes = new ArrayList<>();
		List<RecipePOJO> parentAndChildren = recipes.findParentAndChildren(parentId);
		Optional<RecipePOJO> parentOpt = parentAndChildren.stream().filter(x -> x.getId().equals(parentId))
				.findFirst();
		
		Map<String,String> substitutionMap = getSubstitutionMap( parentAndChildren );
		
		Optional<RecipePOJO> childOpt;
		
			childOpt = parentAndChildren.stream()
					.filter(x -> x.getId().equals( childId)  ).findFirst();
			
			List<SubstitutionResults> subResults = parentAndChildren.stream().filter( y -> y.getRecipeChange() != null).map( y -> new SubstitutionResults( y.getId(), y.getRecipeChange())).collect( Collectors.toList() );
		
		if (parentOpt.isPresent() && childOpt.isPresent()) {
			RecipePOJO parentRecipe = parentOpt.get();
			RecipePOJO childRecipe = childOpt.get();
			childRecipe.setSubstitutionResult( subResults );
			myrecipes.add(childRecipe);
			RecipeChange rc = childRecipe.getRecipeChange();
		
			RecipeChangeService.prepareForDisplay(rc);
			addSubstitutionMap( parentRecipe, substitutionMap );
			myrecipes.add( parentRecipe );
			return serializeResult(ow, myrecipes);
		}

		log.warn("INVALID  RESPONSE  recipeId: " + childId);
		return null;
	}
	/*@Todo  change for this to occur for every ingredient in the RecipeSubs array
	 * For the display add the subs to one ingredient  
	 */
	private void addSubstitutionMap(final RecipePOJO parentRecipe, Map<String, String> substitutionMap) {
		Optional<Line> lineOpt =  RecipePOJO.getIngredientLines(parentRecipe).stream().filter( x -> x.getSubs().size() > 0 ).findFirst();
		
		if ( lineOpt.isPresent() ) {
			Line line = lineOpt.get();
		    List<IngredientSubstitution> subs = line.getSubs();
		    List<IngredientSubstitution> replaceSubs = new ArrayList<>();
		    subs.forEach(s -> {
		    	String searchString  = s.getSubstituteFor();
		    	Optional<String> match = substitutionMap.keySet().stream().filter(   key -> substitutionMap.get( key).contains( searchString ) ).findFirst();
		    	if ( match.isPresent() ) {
		    		String id = match.get();
		    		s.setId(id);
		    	}
		    	replaceSubs.add( s );
		    });
		    line.setSubs(replaceSubs);
		}
	}
		
	

	private Map<String, String> getSubstitutionMap(List<RecipePOJO> parentAndChildren) {
		return parentAndChildren.stream().filter( x -> x.getSubstitutionRule() != null ).collect(Collectors.toMap(RecipePOJO::getId, RecipePOJO::getSubstitutionRule ) );
	}

	@RequestMapping( method=RequestMethod.GET,  value = "/recipe/image/{id}")
	@ResponseBody
	public DemoRecipeSearch getRecipeImage( @PathVariable("id") String id) {
		DemoRecipeSearch obj = imageRepository.findByRecipeid(id);
		if ( obj == null  || obj.getImagePNG() == null ) {
			log.warn( "Null object in /recipe/image/id " + id);
			return null;
		}
		/*
		byte[] buf = obj.getImagePNG();
		ByteArrayInputStream bis = new ByteArrayInputStream( buf );
		try {
			BufferedImage img = ImageIO.read(bis );
			ImageIO.write(img, "png", new File("image.png"));
		} catch (IOException e) {
			log.warn( e );
		} */
		log.info( "returning image for id: " +   id);
		return obj;
	}
	
	
	protected String serializeResult(ObjectWriter ow, List<RecipePOJO> myrecipes ) {
		
	   StringWriter sw = new StringWriter();
		try {
			ow.with(SerializationFeature.WRAP_ROOT_VALUE).writeValue(sw, myrecipes);
			String xmlString = sw.toString();
			/*
			 * try ( FileWriter fw = new FileWriter( "../tmp.xml")) {
			 * ow.with( SerializationFeature.WRAP_ROOT_VALUE).writeValue(fw,
			 * myrecipes); } catch (IOException e1) { log.error(e1); }
			 */
			return xmlString;
		} catch (JsonProcessingException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}
	
	
	@RequestMapping( method=RequestMethod.GET,  value = "/recipe/old/substitutions" )
	@ResponseBody
	public String getRecipeSubstitutionsOld() {
		Date start = new Date();
		
		HashSet<String> set = new HashSet<>();
		recipes.findRecipeSubstitutions().forEach(r -> {
		   RecipePOJO.getIngredientLines(r).forEach(i -> {
			   i.getSubs().forEach(s -> set.add( s.getDescription() ));
		   });
		});
		Date end = new Date();
		long millis = end.getTime() - start.getTime();
		log.info( "/recipe/substitutions    time: " + millis + " set size: " + set.size() );
		List<String> titles = set.stream().sorted().collect( Collectors.toList());
		SubstitutionsList subslist = new SubstitutionsList();
		subslist.setList(subslist.getList());
		mongoOperations.save(subslist);
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		
	    String jsonString = gson.toJson(titles);

	    return  jsonString ;
	}
	
	@RequestMapping( method=RequestMethod.GET,  value = "/recipe/substitutions" )
	@ResponseBody
	public List<DropDownTitle> getRecipeSubstitutions() throws JsonParseException, JsonMappingException, IOException {
		Date start  = new Date();
		log.info( "call /recipe/substitutions");
		/*
		Optional<SubstitutionsList> subList = subsListRepository.findAll().stream().filter( s -> s.getList().size() > 0).findFirst();
		if ( subList.isPresent() ) {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		    String jsonString = gson.toJson(subList.get().getList());
		    return  jsonString ;
		}
		Set<DropDownTitle> substitutionList = new HashSet<>();
		try( Stream<Substitutions> stream = substitutionsRepository.getSubstitutionFromSystem() ) {
			
			stream.forEach(a -> {
				a.getSubs().forEach( b -> processSubstitutions(a, substitutionList ));
			});
		}
		HashSet<String> titles = new HashSet<>();
		List<DropDownTitle> sortedList = substitutionList.stream().filter( a -> titles.add( a.description)).sorted().collect( Collectors.toList());
		SubstitutionsList mysubs = new SubstitutionsList();
		//mysubs.setList(sortedList);
		//subsListRepository.save( mysubs );
		Gson gson = new GsonBuilder().create();
		
	    String jsonString = gson.toJson(sortedList);
	    Date end = new Date();
	    long diff = end.getTime() - start.getTime();
	    log.info( " /recipe/substitutions took " + diff +"ms" );
	    return  jsonString ;
	    */
		ClassPathResource resource = new ClassPathResource("data/substitutionsList.json");
		ObjectMapper mapper = new ObjectMapper();
		return  mapper.readValue( resource.getInputStream(),  new TypeReference<List<DropDownTitle>>(){} );
	}
	
	@RequestMapping( method=RequestMethod.GET,  value = "/recipe/substitution/{id}" )
	@ResponseBody
	public Substitutions findRecipeSubstitutions ( @PathVariable("id") String id)  {
		Substitutions substitutions = substitutionsRepository.findByRecipeId(id);
		if ( substitutions == null  ) {
			log.error( "'" + id  + "'  does not have a subtitutions matching it" );
			return null;
		}
		return substitutions;
	}
	
	private void processSubstitutions(Substitutions sub, Set<DropDownTitle> substitutionList) {
		sub.getSubs().forEach(recipeSub -> substitutionList.addAll(  getSubstitutionDescriptions( recipeSub)));
	}

	List<DropDownTitle> getSubstitutionDescriptions( RecipeSub sub ) {
		String source = sub.getSource();
		String sourceId = sub.getSourceId();
		return sub.getOptions().stream().map( a -> mapDropDownTitle( a.getTarget(), a.getTargetId(),  source, sourceId)).collect( Collectors.toList());
	}
	DropDownTitle mapDropDownTitle ( String target,String targetId,  String source, String sourceId )  {
		String description  =  String.format( "%s for %s", source, target);
		return new DropDownTitle( sourceId, targetId, description );
	}
	
	@RequestMapping( method=RequestMethod.GET, value="/admin/prepare")
	public String calculateAllRecipes() {
		ArrayList<RecipePOJO> buffer = new ArrayList<>();
 		recipes.streamAllRecipes().forEachOrdered(pojo -> {
 			try {
 				recipeChangeService.setIngredientPOJOService(ingredientPojoService);
				recipeChangeService.calculateRecipeNutrition(pojo);
				buffer.add( pojo );
				if ( buffer.size() > 1000 ) {
					recipes.save(buffer);
					buffer.clear();
				}
			} catch (BadParameterException e) {
			   log.error("Error in recipe "  + pojo.getId(), e);
			}
			
			
		});
 		recipes.save(buffer);
		return "DONE";
	}
	
	
}

