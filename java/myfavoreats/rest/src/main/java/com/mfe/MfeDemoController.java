package com.mfe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mfe.frontend.IngredientSorters;
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
import com.mfe.model.utils.IngredientPOJOService;
import com.mfe.model.utils.IngredientSubstitution;
import com.mfe.model.utils.RecipeChangeService;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;

@Controller
@RestController
@RepositoryRestController
public class MfeDemoController {

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	ImageRepository imageRepository;

	@Autowired
	IngredientPOJOService ingredientPojoService;

	@Autowired
	SubstitutionsRepository substitutionsRepository;

	@Autowired
	RecipeWithSubstituteService recipeSubstitutionService;

	@Autowired
	SubstitutionsListRepository subsListRepository;

	@Autowired
	RecipeRepository recipes;

	ScriptOperations scriptOperations;

	ExecutableMongoScript substitutionsScript;

	RecipeChangeService recipeChangeService;

	Log log = LogFactory.getLog(MfeDemoController.class);

	public static Integer TITLE_LIMIT = 300;

	public MfeDemoController() {
	}

	@RequestMapping("/recipes")
	public List<RecipePOJO> getRecipes() {
		return recipes.findAll().subList(0, TITLE_LIMIT);
	}

	@RequestMapping("/recipe/{id}")
	public RecipePOJO getRecipe(@PathVariable("id") String id) {
		log.info("recipePOJO " + id);

		RecipePOJO pojo = recipes.findRecipeById(id);
		try {
			recipeChangeService.calculateRecipeNutrition(pojo);
		} catch (BadParameterException e) {
			log.error("Could not calculate nutrition values", e);
		}
		return pojo;
	}

	@RequestMapping("/ingredients")
	public List<RecipePOJO> getIngredients() {
		return recipes.findAll().subList(0, TITLE_LIMIT);
	}

	@RequestMapping("/muffins")
	public List<RecipePOJO> getMuffins() {
		return recipes.getMuffins();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/original")
	public List<RecipePOJO> getRecipesOriginal() {
		List<RecipePOJO> list = recipes.findUnmodifiedRecipes();

		list.sort(new Comparator<RecipePOJO>() {

			@Override
			public int compare(RecipePOJO o1, RecipePOJO o2) {
				int i = o1.getTitle().compareTo(o2.getTitle());
				return i;
			}

		});

		return list;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/muffins/parentId/{parentId}")
	public List<RecipePOJO> getMuffinsByParentId(@PathVariable("parentId") String parentId) {
		log.info("getRecipesByParentId   parent is " + parentId);
		return recipes.findByParentId(parentId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/muffins/{id}/ingredients")
	public List<Line> getIngredientsByRecipe(@PathVariable("id") String id) {
		RecipePOJO pojo = recipes.findRecipeById(id);
		List<Line> lines = (List<Line>) RecipePOJO.getIngredientLines(pojo);
		return lines;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/demo/titles")
	public List<RecipePOJO> getDemoTitles() {
		List<RecipePOJO> results = recipes.getMuffinTitles();
		return results;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/parent/{id}")
	public List<RecipePOJO> getRecipesByParentId(@PathVariable("id") String id) {
		log.info("Calling /recipes/parent/id with id of " + id);
		List<RecipePOJO> list = recipes.findByParentId(id);
		if (list == null || list.isEmpty()) {
			log.warn("No results returned from /recipes/parent/id");
		}
		return list;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/ingredient/{id}")
	public List<IngredientPOJO> getIngredientByNdb(@PathVariable("id") String id) {
		String search = String.format("{ _id : \"%s\"}", id);
		Query query = new BasicQuery(search);
		return mongoOperations.find(query, IngredientPOJO.class);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/ingredient/search/{word}")
	public List<IngredientPOJO> searchIngredient(@PathVariable("word") String word) {
		String search = String.format("{ $text : { $search : \"%s\" }}", word);
		Query query = new BasicQuery(search);
		return mongoOperations.find(query, IngredientPOJO.class);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/search/{word}")
	public List<RecipeTitle> searchRecipes(@PathVariable("word") String word) {
		Date start = new Date();
		log.info( "Search started for recipes with phrase '" + word + "'");

		TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(word);
		Query query = TextQuery.queryText(criteria).sortByScore().with(new PageRequest(0, TITLE_LIMIT*2));

		List<RecipePOJO> pojos = mongoOperations.find(query, RecipePOJO.class);
		//Set<String> ids = new HashSet<>();
		//Set<String> uniqueNames = new HashSet<>();
		Predicate<RecipePOJO> isValidRecipe = (a) -> a.getTitle() != null && a.getPhotos() != null && a.getUrn() != null ;
		List<RecipeTitle> result = null;
	
		result = pojos.stream()
				.filter ( isValidRecipe )
				.map    (a -> new RecipeTitle(a)).collect(Collectors.toList());
		Double elapsed = BigDecimal.valueOf( new Date().getTime() - start.getTime() ).divide( BigDecimal.valueOf(1000D), 3, BigDecimal.ROUND_DOWN ).doubleValue();
		String message = String.format( "search term: '%s'  time:  %03.3f seconds   recipes: %d,  recipe titles: %d",
				  word
				, elapsed
				, pojos.size() 
				, result.size() );
		log.info( message);
		return result;
	}

	/*
	 * check id and if present the parentId if either has already been added to
	 * results then skip
	 */
	private boolean isUniqueSearchResult(RecipePOJO x, Set<String> ids, Set<String> uniqueNames) {
		String id = x.getId();
		String parentid = x.getParentId();
		boolean result = ids.add(id);
		if (result && parentid.isEmpty() && (uniqueNames.add(x.getTitle()) || x.getTitle().equals("no title")))
			return true;
		return result && ids.add(parentid);

	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/changed/title")
	public List<RecipeTitle> getChangedTitles() {
		Set<String> unique = new HashSet<>();
		List<RecipeTitle> titleList = new ArrayList<>();
		PageRequest pageable = new PageRequest(1, TITLE_LIMIT, Direction.DESC, "categories");
		try (Stream<DemoRecipeSearch> stream = imageRepository.findSubstituteTitle(pageable)) {
			List<RecipeTitle> titleListPart2 = stream.filter(x -> unique.add(x.getTitle())).limit(900)
					.map(x -> new RecipeTitle(x.getRecipeId(), x.getTitle(), x.getUrl(), x.getSite(), x.getImageUrl()))
					.collect(Collectors.toList());
			titleList.addAll(titleListPart2);
		}
		return titleList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/substitute/{description}")
	public List<RecipeTitle> getRecipesBySubstitutionRule(@PathVariable("description") String description) {
		Date start = new Date();
		String[] searchValues = description.split("_");
		String sourceId = searchValues[0];
		String targetId = searchValues[1];
		log.info("Searching for substitutions from " + sourceId + "  to  " + targetId);
		List<RecipePOJO> pojos = findRecipesUsingSubstitute(sourceId, targetId);
		pojos.sort(new RecipeComparator());
		//Set<String> uniqueTitle = new HashSet<>();
		List<RecipeTitle> result = pojos.stream()
				.map(x -> new RecipeTitle(x.getId(), x.getTitle(), x.getUrn(), x.getWebsite(), x.getPhotos()))
				.collect(Collectors.toList());
		Date end = new Date();
		long millis = end.getTime() - start.getTime();
		log.info("/recipes/substitute/" + description + " results: " + pojos.size() + " time " + millis + " ms");
		return result;

	}

	private List<RecipePOJO> findRecipesUsingSubstitute(String sourceId, String targetId) {

		log.debug("findRecipesWithSubstitute  " + sourceId + "  " + targetId);
		List<String> recipeIdList = substitutionsRepository.findBySourceAndTarget(sourceId, targetId)
				.limit(TITLE_LIMIT)
				.map(x -> x.getRecipeId()).collect(Collectors.toList());

		//String ids = recipeIdList.stream().map(d -> d.toString()).collect(Collectors.joining(", "));
		//log.info("IDS: " + ids);

		// Query query = query( where("id" ).in(
		// recipeIdList)).limit(TITLE_LIMIT);
		// query.fields().include("id");
		// query.fields().include("title");
		// query.fields().include("urn");
		// query.fields().include("site");
		// query.fields().include("photos");
		//
		// log.info("QUERY: " + query.toString() );
		//
		// List<RecipePOJO> list = mongoOperations.find( query, RecipePOJO.class
		// );

		List<RecipePOJO> list = recipes.findRecipesById(recipeIdList, new Sort(Sort.Direction.DESC, "categories"));
		log.info("substitutions: " + recipeIdList.size() + "   recipes: " + list.size());

		return list;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/title/search/{phrase}")
	List<RecipeTitle> getRecipeTitlesBySearchPhase(@PathVariable("phrase") String phrase) {

		Date start = new Date();
		Set<String> recipeIdSet = recipes.findBySearchPhrase("*" + phrase, new PageRequest(0, 100)).map(a -> a.getId())
				.collect(Collectors.toSet());
		List<RecipeTitle> result = imageRepository.findAllTitles().filter(a -> recipeIdSet.contains(a.getRecipeId()))
				.map(a -> new RecipeTitle(a.getRecipeId(), a.getTitle(), a.getUrl(), a.getSite(), a.getImageUrl()))
				.collect(Collectors.toList());
		Date queryEnd = new Date();
		long milis = queryEnd.getTime() - start.getTime();
		log.info("/recipes/title/search/{phrase} Phrase: " + phrase + " duration " + milis);

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/title/autocomplete/{phrase}")
	List<RecipeTitle> getRecipeTitlesAutoComplete(@PathVariable("phrase") String chars) {

		Date start = new Date();
		Set<String> recipeIdSet = recipes.findTitleStartsWith(chars).map(a -> a.getId()).collect(Collectors.toSet());
		List<RecipeTitle> result = imageRepository.findAllTitles().filter(a -> recipeIdSet.contains(a.getRecipeId()))
				.map(a -> new RecipeTitle(a.getRecipeId(), a.getTitle(), a.getUrl(), a.getSite(), a.getImageUrl()))
				.collect(Collectors.toList());
		Date queryEnd = new Date();
		long milis = queryEnd.getTime() - start.getTime();
		log.info("/recipes/title/search/{phrase} Phrase: " + chars + " duration " + milis);

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/with-substitute/{id}")
	@ResponseBody
	String getRecipeWithSubstitute(@PathVariable("id") String id,
			@RequestParam(value = "optionUid", required = false) String optionId) {
		Date startTime = new Date();
		if (optionId == null)
			optionId = "NONE";
		if (optionId.equals("NONE")) {
			log.info("Start timer:  /recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id);
		} else {
			log.info("/recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id
					+ " and recipeSubOption id of " + optionId);
		}

		RecipePOJO parentRecipe = recipes.findRecipeById(id);
		if (parentRecipe == null) {
			log.warn("recipePOJO was not found for id " + id);
		} else {
			List<RecipePOJO> myrecipes = recipeSubstitutionService.getRecipeAndSubstitute(parentRecipe, optionId);
			if (myrecipes.isEmpty()) {
				log.warn("No results were obtained from the recipeSubstitutionService " + id);
			} else {
				RecipePOJO recipePojo = myrecipes.get(0);
				RecipeChange rc = recipePojo.getRecipeChange();
				RecipeChangeService.prepareForDisplay(rc);
				recipePojo.setRecipeChange(rc);
				XmlMapper map = new XmlMapper();
				ObjectWriter ow = map.writer();
				return serializeResult(ow, myrecipes);
			}
		}
		log.warn("Empty result set returned for recipePojo " + id);
		Date stopTime = new Date();
		long elapsed = stopTime.getTime() - startTime.getTime();
		log.info("End timer took " + Long.toString(elapsed)
				+ "ms:   /recipes/with-substitute  (getRecipeWithSubstitute) called with id " + id);
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/full-substitute/{id}")
	@ResponseBody
	RecipePOJO getRecipeFullSubstitute(@PathVariable("id") String id,
			@RequestParam(value = "sort", defaultValue = "NONE") String sort) {

		RecipePOJO parentRecipe = recipes.findRecipeById(id);

		List<RecipePOJO> myrecipes = recipeSubstitutionService.getRecipeAndSubstitute(parentRecipe, null);
		if (myrecipes.isEmpty()) {
			log.warn("No results were obtained from the recipeSubstitutionService " + id);
		} else {
			RecipePOJO subs = myrecipes.get(0);
			subs.getSubs().forEach(sub -> {
				sub.getOptions().forEach(option -> {
					option.setIngredient(ingredientPojoService.getByEntityMapping((option.getTargetId())));
				});

				if ("carbs".equals(sort)) {
					sub.getOptions().sort(new IngredientSorters.LowCarbsCompare());
				} else if ("gluten-free".equals(sort)) {
					sub.getOptions().sort(new IngredientSorters.GlutenFree());
				}

			});

			return subs;
		}

		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/changed/pair/{id}")
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
		List<SubstitutionResults> subResults = parentAndChildren.stream().filter(y -> y.getRecipeChange() != null)
				.map(y -> new SubstitutionResults(y.getId(), y.getRecipeChange())).collect(Collectors.toList());

		Optional<RecipePOJO> parentOpt = parentAndChildren.stream().filter(x -> x.getSubstitutionRule() == null)
				.findFirst();

		Map<String, String> substitutionMap = getSubstitutionMap(parentAndChildren);

		Optional<RecipePOJO> childOpt;
		if (childIndicator.equals("1")) {
			childOpt = parentAndChildren.stream()
					.filter(x -> (x.getRecipeChange() != null && x.getSubstitutionRule().equals(title))).findFirst();
		} else {
			Optional<Double> minOpt = parentAndChildren.stream().filter(x -> x.getRecipeChange() != null)
					.map(x -> x.getRecipeChange().getProbability()).min(Double::compare);

			if (minOpt.isPresent()) {
				Double min = minOpt.get();
				childOpt = parentAndChildren.stream()
						.filter(x -> (x.getRecipeChange() != null && x.getRecipeChange().getProbability() <= min))
						.findFirst();
			} else {
				childOpt = parentAndChildren.stream().filter(x -> (x.getRecipeChange() != null)).findFirst();
			}
		}

		if (parentOpt.isPresent() && childOpt.isPresent()) {
			RecipePOJO parentRecipe = parentOpt.get();
			RecipePOJO childRecipe = childOpt.get();
			childRecipe.setSubstitutionResult(subResults);
			myrecipes.add(childRecipe);
			RecipeChange rc = childOpt.get().getRecipeChange();
			RecipeChangeService.prepareForDisplay(rc);
			addSubstitutionMap(parentRecipe, substitutionMap);
			myrecipes.add(parentRecipe);
			return serializeResult(ow, myrecipes);
		}

		log.warn("INVALID  RESPONSE  recipeId: " + id);
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipes/changed/child/{id}")
	@ResponseBody
	String getRecipeChild(@PathVariable("id") String childId, @RequestParam("parentId") String parentId) {
		log.info("getRecipeChild with child id: " + childId);
		XmlMapper map = new XmlMapper();
		ObjectWriter ow = map.writer();
		List<RecipePOJO> myrecipes = new ArrayList<>();
		List<RecipePOJO> parentAndChildren = recipes.findParentAndChildren(parentId);
		Optional<RecipePOJO> parentOpt = parentAndChildren.stream().filter(x -> x.getId().equals(parentId)).findFirst();

		Map<String, String> substitutionMap = getSubstitutionMap(parentAndChildren);

		Optional<RecipePOJO> childOpt;

		childOpt = parentAndChildren.stream().filter(x -> x.getId().equals(childId)).findFirst();

		List<SubstitutionResults> subResults = parentAndChildren.stream().filter(y -> y.getRecipeChange() != null)
				.map(y -> new SubstitutionResults(y.getId(), y.getRecipeChange())).collect(Collectors.toList());

		if (parentOpt.isPresent() && childOpt.isPresent()) {
			RecipePOJO parentRecipe = parentOpt.get();
			RecipePOJO childRecipe = childOpt.get();
			childRecipe.setSubstitutionResult(subResults);
			myrecipes.add(childRecipe);
			RecipeChange rc = childRecipe.getRecipeChange();

			RecipeChangeService.prepareForDisplay(rc);
			addSubstitutionMap(parentRecipe, substitutionMap);
			myrecipes.add(parentRecipe);
			return serializeResult(ow, myrecipes);
		}

		log.warn("INVALID  RESPONSE  recipeId: " + childId);
		return null;
	}

	/*
	 * @Todo change for this to occur for every ingredient in the RecipeSubs
	 * array For the display add the subs to one ingredient
	 */
	private void addSubstitutionMap(final RecipePOJO parentRecipe, Map<String, String> substitutionMap) {
		Optional<Line> lineOpt = RecipePOJO.getIngredientLines(parentRecipe).stream()
				.filter(x -> x.getSubs().size() > 0).findFirst();

		if (lineOpt.isPresent()) {
			Line line = lineOpt.get();
			List<IngredientSubstitution> subs = line.getSubs();
			List<IngredientSubstitution> replaceSubs = new ArrayList<>();
			subs.forEach(s -> {
				String searchString = s.getSubstituteFor();
				Optional<String> match = substitutionMap.keySet().stream()
						.filter(key -> substitutionMap.get(key).contains(searchString)).findFirst();
				if (match.isPresent()) {
					String id = match.get();
					s.setId(id);
				}
				replaceSubs.add(s);
			});
			line.setSubs(replaceSubs);
		}
	}

	private Map<String, String> getSubstitutionMap(List<RecipePOJO> parentAndChildren) {
		return parentAndChildren.stream().filter(x -> x.getSubstitutionRule() != null)
				.collect(Collectors.toMap(RecipePOJO::getId, RecipePOJO::getSubstitutionRule));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipe/image/{id}")
	@ResponseBody
	public DemoRecipeSearch getRecipeImage(@PathVariable("id") String id) {
		DemoRecipeSearch obj = imageRepository.findByRecipeid(id);
		if (obj == null || obj.getImagePNG() == null) {
			log.warn("Null object in /recipe/image/id " + id);
			return null;
		}
		/*
		 * byte[] buf = obj.getImagePNG(); ByteArrayInputStream bis = new
		 * ByteArrayInputStream( buf ); try { BufferedImage img =
		 * ImageIO.read(bis ); ImageIO.write(img, "png", new File("image.png"));
		 * } catch (IOException e) { log.warn( e ); }
		 */
		log.info("returning image for id: " + id);
		return obj;
	}

	protected String serializeResult(ObjectWriter ow, List<RecipePOJO> myrecipes) {

		StringWriter sw = new StringWriter();
		try {
			ow.with(SerializationFeature.WRAP_ROOT_VALUE).writeValue(sw, myrecipes);
			String xmlString = sw.toString();
			/*
			 * try ( FileWriter fw = new FileWriter( "../tmp.xml")) { ow.with(
			 * SerializationFeature.WRAP_ROOT_VALUE).writeValue(fw, myrecipes);
			 * } catch (IOException e1) { log.error(e1); }
			 */
			return xmlString;
		} catch (JsonProcessingException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipe/old/substitutions")
	@ResponseBody
	public String getRecipeSubstitutionsOld() {
		Date start = new Date();

		HashSet<String> set = new HashSet<>();
		recipes.findRecipeSubstitutions().forEach(r -> {
			RecipePOJO.getIngredientLines(r).forEach(i -> {
				i.getSubs().forEach(s -> set.add(s.getDescription()));
			});
		});
		Date end = new Date();
		long millis = end.getTime() - start.getTime();
		log.info("/recipe/substitutions    time: " + millis + " set size: " + set.size());
		List<String> titles = set.stream().sorted().collect(Collectors.toList());
		SubstitutionsList subslist = new SubstitutionsList();
		subslist.setList(subslist.getList());
		mongoOperations.save(subslist);
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		String jsonString = gson.toJson(titles);

		return jsonString;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipe/substitutions")
	@ResponseBody
	public List<DropDownTitle> getRecipeSubstitutions() throws JsonParseException, JsonMappingException, IOException {
		Date start = new Date();
		log.info("call /recipe/substitutions");

		Optional<SubstitutionsList> savedList = subsListRepository.findAll().stream()
				.filter(x -> x.getList() != null && x.getList().size() > 0).findFirst();
		List<DropDownTitle> output = new ArrayList<>();
		savedList.ifPresent(a -> output.addAll(a.getList()));

		if (output.size() > 0)
			return output;

		output.addAll(createDropDownTitles());

		Long elapsed = new Date().getTime() - start.getTime();
		log.info("call /recipe/substitutions  took " + elapsed + " ms");
		return output;
	}

	/**
	 * @return
	 */
	protected List<DropDownTitle> createDropDownTitles() {
		List<DropDownTitle> titles = new ArrayList<>();

		mapReduce(titles);

		return titles;
	}

	/**
	 * @param titles
	 */
	protected void mapReduce(List<DropDownTitle> titles) {
		Date start = new Date();
		log.info("generating the dropdowntitles for the substitutions");
		BasicQuery query = new BasicQuery("{}");
		String substitutions = mongoOperations.getCollectionName(Substitutions.class);
		log.info("starting mapReduce using " + substitutions);
		DBCollection collect = mongoOperations.getCollection(substitutions);
		new MapReduceCommand(collect, mapfunction, reducefunction, "substitutionsListSource",
				MapReduceCommand.OutputType.REPLACE, query.getQueryObject());
		/*
		 * MapReduceResults<MapReduceValue> values =
		 * mongoOperations.mapReduce(query, substitutions , mapfunction ,
		 * reducefunction , new
		 * MapReduceOptions().outputCollection("substitutionsListSource").
		 * verbose(true) , MapReduceValue.class);
		 */
		Long elapsed = new Date().getTime() - start.getTime();
		log.info("building drop down titles finshed in " + elapsed + " ms");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipe/substitution/{id}")
	@ResponseBody
	public Substitutions findRecipeSubstitutions(@PathVariable("id") String id) {
		Substitutions substitutions = substitutionsRepository.findByRecipeId(id);
		if (substitutions == null) {
			log.error("'" + id + "'  does not have a subtitutions matching it");
			return null;
		}
		return substitutions;
	}

	/*
	 * Now using a javascript process
	 */
	@Deprecated
	private void processSubstitutions(Substitutions sub, Set<DropDownTitle> substitutionList) {
		sub.getSubs().forEach(recipeSub -> substitutionList.addAll(getSubstitutionDescriptions(recipeSub)));
	}

	List<DropDownTitle> getSubstitutionDescriptions(RecipeSub sub) {
		String source = sub.getSource();
		String sourceId = sub.getSourceId();
		return sub.getOptions().stream().map(a -> mapDropDownTitle(a.getTarget(), a.getTargetId(), source, sourceId))
				.collect(Collectors.toList());
	}

	DropDownTitle mapDropDownTitle(String target, String targetId, String source, String sourceId) {
		String description = String.format("%s for %s", source, target);
		return new DropDownTitle(sourceId, targetId, description);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/admin/prepare")
	public String calculateAllRecipes() {
		ArrayList<RecipePOJO> buffer = new ArrayList<>();
		recipes.streamAllRecipes().forEachOrdered(pojo -> {
			try {
				recipeChangeService.calculateRecipeNutrition(pojo);
				buffer.add(pojo);
				if (buffer.size() > 1000) {
					recipes.save(buffer);
					buffer.clear();
				}
			} catch (BadParameterException e) {
				log.error("Error in recipe " + pojo.getId(), e);
			}

		});
		recipes.save(buffer);
		return "DONE";
	}
	
	@RequestMapping( method = RequestMethod.PUT, value = "/admin/substitutions")
	public void processAllSubstitutions() {
		recipes.streamAllRecipes().forEachOrdered(pojo -> {
			
			log.info( pojo.getId() + "  " + pojo.getTitle() );
			recipeSubstitutionService.getRecipeAndSubstitute( pojo );
			
		});		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/recipe/nutrition/{id}")
	@ResponseBody
	public RecipePOJO calculateNutrition(@PathVariable("id") String id) {
		RecipePOJO pojo = recipes.findRecipeById(id);
		try {
			recipeChangeService.calculateRecipeNutrition(pojo);
		} catch (BadParameterException e) {
			log.error("recipe " + pojo.getId(), e);
		}
		return pojo;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/substitutions/recipe/{recipeId}")
	@ResponseBody
	public Substitutions findSubstitutionsByRecipeid(@PathVariable("recipeId") String recipeId) {
		return substitutionsRepository.findByRecipeId(recipeId);
	}

	Callable<Boolean> assign = () -> {
		// do { Thread.sleep( 100 ); }while( ingredientPojoService == null );
		this.recipeChangeService = new RecipeChangeService(ingredientPojoService);
		log.info("ingredientPojoService has been make active");
		return true;
	};

	private ExecutableMongoScript substitutionScript;

	Callable<Boolean> createSubstitutions = () -> {
		List<DropDownTitle> titles = createDropDownTitles();

		ClassPathResource resource = new ClassPathResource("data/substitutionsList.js");
		String script = readToString(resource.getInputStream());
		if (script.length() > 0) {
			log.info("Executing mongoscript 'substitutions'");
		} else {
			log.error("could not load script file 'substitutionsList.js' ");
		}
		substitutionScript = new ExecutableMongoScript(script);
		// Register script and call it later
		scriptOperations.execute(substitutionScript);
		// scriptOperations.register(substitutionScript);

		SubstitutionsList currentSubs = new SubstitutionsList(titles);
		log.info("Created the title drop downs and saving to database");
		subsListRepository.insert(currentSubs);

		return true;
	};

	private String mapfunction;

	private String reducefunction;

	Callable<Boolean> readInScripts = () -> {
		log.info("mapfunction");
		ClassPathResource r = new ClassPathResource("mapfunction.js");
		mapfunction = readToString(r.getInputStream());
		log.info(mapfunction);
		log.info("reducefunction");
		r = new ClassPathResource("reducefunction.js");
		reducefunction = readToString(r.getInputStream());
		log.info(reducefunction);
		List<DropDownTitle> titles = new ArrayList<>();
		mapReduce(titles);
		return true;
	};

	@PostConstruct
	public void postConstruct() throws InterruptedException, ExecutionException, TimeoutException {
		log.info("calling postConstruct");
		scriptOperations = mongoOperations.scriptOps();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(assign);

		log.info("readInScripts");
		ExecutorService executor3 = Executors.newSingleThreadExecutor();
		executor3.submit(readInScripts);

		log.info("createSubstitions");
		ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
		executor2.schedule(new FutureTask<Boolean>(createSubstitutions), 30, java.util.concurrent.TimeUnit.SECONDS);
		scriptOperations.getScriptNames().forEach(x -> log.info("script " + x + " is registered"));

	}

	protected String readToString(InputStream s) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(s))) {
			char[] cb = new char[1000];
			while (br.read(cb) > 0)
				sb.append(new String(cb));
		}
		return sb.toString();
	}

}
