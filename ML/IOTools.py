#-*- coding: utf-8 *-*

from pymongo import MongoClient
import ssl
import re
import numpy as np
import logging
from bson.objectid import ObjectId
from sets import Set
from StringIO import StringIO
from pymongo.errors import BulkWriteError
from pymongo import UpdateOne, TEXT
import sys
from pprint import pprint
from RDF import RDF, RDFGraph
from Recipe import Recipe
import json

# maximum size of chunk of table (for stats collection)
MAX_STATS_TABLE_DOC_SIZE = 10000

DEFAULT_INSTRUCTION_ANNOTATION_COLLECTION_NAME = 'instructionAnnotation'
DEFAULT_ENTITY_MAPPING_COLLECTION_NAME = 'entityMapping'
DEFAULT_RECIPE_COLLECTION_NAME = 'recipePOJO'
DEFAULT_INGREDIENT_COLLECTION_NAME = 'ingredientPOJO'
DEFAULT_MANUAL_SUB_RULES_COLLECTION_NAME = 'manualSubRules'
DEFAULT_SUB_COLLECTION_NAME = 'substitutions'
DEFAULT_FEEDBACK_COLLECTION_NAME = 'feedback'
DEFAULT_RECIPEVECS_COLLECTION_NAME = 'recipeVecs'
DEFAULT_ENTITYVECS_COLLECTION_NAME = 'entityVecs'
DEFAULT_STATS_COLLECTION_NAME = 'statsTables'
DEFAULT_RDF_COLLECTION_NAME = 'RDF'

# Avoid bad unicode characters
def clean_str(s):
  if s is None: return 'None'
  good = map(lambda x: ord(x), 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ -')
  ret = ''
  for c in s:
    if ord(c) in good: 
      ret = ret + c
    else:
      ret = ret + '?' 
  return ret

def tokenizeInstructionAnnotation(s):

  s = re.sub(r'[,;:!(\-)/\"\'*+#$&=]', ' ', s)
  s = re.sub(r'<[a-zA-Z/]+>?', ' ', s) # this shouldnt happen, but due to parsing problems I saw it!
  s = re.sub(r'[<>]', ' ', s)
  s = re.sub(r'\.', ' </s> ', s)
  s = re.sub(r'{%', ' {%', s)
  s = re.sub(r'%}', ' %} ', s)
  s = re.sub('\s+', ' ', s)  

  # replace spaces within {%...%} with dashes
  def repl(matchobj):
    x = matchobj.group(0)
    x = re.sub('{%\s+', '{%', x)
    x = re.sub('\s+%}', '%}', x)
    x_ = re.sub('\s+', '-', x)
    return x_
  s = re.sub('{%[^%]*%}', repl, s)
  return s.split()

def decorate_entity(x):
  x = re.sub(' ', '-', x)
  return '|'+x+'|'
def undecorate_entity(x):
  x = re.sub('-', ' ', x)
  if x[0] == '|': x=x[1:]
  if x[-1]== '|': x=x[:-1]    # |...| style
  if x[0] == '{': x = x[2:-2] #  {%... %} style
  return x # remove pipe symbols
def is_decorated_entity(x):
  return x[0] == '|' or x[0] == '{'

def clean_line(line):
  line = re.sub('\W', ' ', line).lower()
  return line.split()
 
def word_combination(wordvec, start, length):
  out = wordvec[start]
  for i in range(1, length):
    if (start + i) < len(wordvec):
      out = out + ' ' + wordvec[start + i]
  return out

def strip_newlines(x):
  while len(x) > 0 and (x[-1]=='\r' or x[-1]=='\n'):
    x = x[:-1]
  return x


def tuple_or_identity(x):
  if type(x) == str or type(x) == unicode: return x
  try:
    return tuple(x)
  except:
    return x

def getTags(r):
  ret = {}
  if not 'tags' in r:
    return []
  for t in r["tags"]:
    if t["value"].lower() == 'true':
      ret[t["name"]] = t["probability"]
  return ret

#
# Database of recipes, ingredients, substitutions....
#
class RecipeDB:
  def __init__(self, 
               recipe_collection_name = DEFAULT_RECIPE_COLLECTION_NAME,
               entity_mapping_collection_name = DEFAULT_ENTITY_MAPPING_COLLECTION_NAME,
               instruction_annotation_collection_name = DEFAULT_INSTRUCTION_ANNOTATION_COLLECTION_NAME,
               manual_sub_rules_collection_name = DEFAULT_MANUAL_SUB_RULES_COLLECTION_NAME,
               sub_collection_name = DEFAULT_SUB_COLLECTION_NAME,
               feedback_collection_name = DEFAULT_FEEDBACK_COLLECTION_NAME,
               entityvecs_collection_name = DEFAULT_ENTITYVECS_COLLECTION_NAME,
               recipevecs_collection_name = DEFAULT_RECIPEVECS_COLLECTION_NAME,
               ingredient_collection_name = DEFAULT_INGREDIENT_COLLECTION_NAME,
               stats_collection_name = DEFAULT_STATS_COLLECTION_NAME,
               rdf_collection_name = DEFAULT_RDF_COLLECTION_NAME,
               bulk_batch=100, sub_bulk_batch=100,
               option_parser = None,
               config_file = None):  

    if option_parser is not None:
      assert config_file is None
      option_parser.add_option("--config", "--config", dest="config", help="Configure location of mongo", default="local.config")
      (options, args) = option_parser.parse_args()
      config_file = options.config
      
    config_json = json.loads(open(config_file).read())

    DB = config_json["db"]
    mongoURL = config_json["host"] + "/" + config_json["db"]

    print('mongodb url=' + mongoURL)
    self.client = MongoClient(
      mongoURL,
      ssl_cert_reqs=ssl.CERT_NONE)
    
    self.db = self.client[DB]
    self.recipe_coll = self.db[recipe_collection_name]
    self.entitymap_coll = self.db[entity_mapping_collection_name]
    self.manualSubRules_coll = self.db[manual_sub_rules_collection_name]
    self.instannot_coll = self.db[instruction_annotation_collection_name]
    self.sub_coll = self.db[sub_collection_name]
    self.feedback_coll = self.db[feedback_collection_name]
    self.ingredient_coll = self.db[ingredient_collection_name]
    self.entityvecs_coll = self.db[entityvecs_collection_name]
    self.recipevecs_coll = self.db[recipevecs_collection_name]
    self.stats_coll = self.db[stats_collection_name]
    self.rdf_coll = self.db[rdf_collection_name]
    self.entitymap_dic = {}
    self.entitymap_alt_dic = {}
    self.entityvecs_dic = {}
    self.rdf_graphs = {}
    self.recipevecs_query_cache = {}

    cursor = self.entitymap_coll.find({})
    i = 0
    for e in cursor:
      if not "name" in e: continue
      if e["name"] == '':
        print("Warning: empty name in entity ", e)
        continue
      #if "origin" in e and e["origin"] == "REMOVE": continue
      
      if not e["name"] in self.entitymap_dic:
        self.entitymap_dic[e["name"]] = e
        self.entitymap_dic[e["name"]]["index"] = i    
        i = i + 1

      if 'altName' in e and e['altName'] is not None:
        for a in e['altName']: 
          if a['alt'] == '':
            print('Error: empty altname for ', e)
            continue
          self.entitymap_alt_dic[a['alt']] = e["name"]
    self.bulk = self.recipe_coll.initialize_unordered_bulk_op()
    self.bulk_counter = 0
    self.bulk_batch = bulk_batch
    cursor = self.entityvecs_coll.find({})
    for e in cursor:
      if not "name" in e: 	
        continue
      self.entityvecs_dic[self.cannonicalize_entity(e["name"])] = e

    self.ingredients_dic = {}
    self.food_group_set = Set([])
    cursor = self.ingredient_coll.find({})
    for ing in cursor:
      if ing['source'] in ['USDA SR28', 'SR27']:
        self.ingredients_dic[int(ing['uid'])] = ing
        self.food_group_set.add(ing['foodGroup'])

  # Get number of foodgroups in the ingredient collection
  def getNumFoodGroups(self):
    return len(self.food_group_set)

  # Cleanup before running full substitution script
  def dropAllSubstitutionCollections(self):
    self.manualSubRules_coll.drop()
    self.sub_coll.drop()
    self.entityvecs_coll.drop()
    self.recipevecs_coll.drop()
    self.rdf_coll.drop()
    self.stats_coll.drop()
  def getEntityUSDAFamily(self, cann):
    if not cann in self.entitymap_dic: return None
    ent = self.entitymap_dic[cann]
    if not 'ndb_no' in ent: return None
    ent_uid = int(ent['ndb_no'])
    if not ent_uid in self.ingredients_dic: return None
    return self.ingredients_dic[ent_uid]['foodGroup']

  def get_random_recipe(self, with_sub=True):
    if with_sub:
      sub_doc = list(self.sub_coll.aggregate([{"$sample": {'size':1}}]))[0]
      id_ = sub_doc['recipeId']
      return self.recipe_coll.find_one({"_id" : ObjectId(id_)})
    else:
      return list(self.recipe_coll.aggregate( [{"$sample": {'size': 1}}]))[0]


  def getSubByRecipeId(self, id_):   
    cursor = self.sub_coll.find({'recipeId' : id_})
    l = list(cursor)
    if not l: return None
    return l[0]

  def getEntityIdByCannonical(self, cannonical):
    return self.entitymap_dic[cannonical]['_id']

  def __del__(self): 
    logging.info('Deleting RecipeDB object')
  def get_feedback_coll(self): return self.feedback_coll

  def getEntityVecTypes(self):
    return entityvec_types

  def getEntityVec(self, entity, type_ = 'word2vec', normalize = True):
    if not entity in self.entityvecs_dic: 
      return None
    if not type_ in self.entityvecs_dic[entity]: 
      return None
    res = np.array(self.entityvecs_dic[entity][type_])
    if normalize: res = res / np.linalg.norm(res)
    return res

  def get_entityvecs_dic(self):
    return self.entityvecs_dic

  def get_entitymap_dic(self):
    return self.entitymap_dic
  def get_entitymap_alt_dic(self):
    return self.entitymap_alt_dic


  def cannonicalize_entities(self, recipe):
    for ing in recipe.ings:
      if ing['cannonical'] is None: 
        ing['cannonical'] = self.cannonicalize_entity(ing['food'])
        continue
      ing['cannonical'] = self.cannonicalize_entity(ing['cannonical'])

  def findRecipes(self, query = {}, limit=0):
    for obj in self.recipe_coll.find(filter=query, limit=limit):
      recipe = Recipe(obj)
      self.cannonicalize_entities(recipe)
      yield recipe
  def findOneRecipe(self, _id):
    obj = self.recipe_coll.find_one({'_id':ObjectId(_id)})
    recipe = Recipe(obj)
    self.cannonicalize_entities(recipe)
    return recipe
  

  def find(self, query={}, projection=None, limit=0,  coll=None):
    if coll is None: coll = self.recipe_coll
    return coll.find(filter=query, projection=projection, limit=limit)

  def findRecipeAnnotation(self, query={}, projection=None, limit=0):
    return self.instannot_coll.find(filter=query, projection=projection, limit = limit)

  def find_one(self, coll=None, query={}):
    if coll is None: coll = self.recipe_coll
    return coll.find_one(query)
    
  def get(self, id_, coll=None):
    if not coll: coll = self.recipe_coll
    cursor = coll.find({'_id' : ObjectId(id_)})
    return cursor.next()
  
  def update(self, id_, d):
    self.recipe_coll.update({"_id":ObjectId(id_)}, {"$set" : d}, upsert = False)

  def cannonicalize_entity(self, e, default=None):
    if not ((e in self.entitymap_dic) or (e in self.entitymap_alt_dic)): return default
    if e in self.entitymap_alt_dic:
      return self.entitymap_alt_dic[e]
    return e

  def count_entity_freqs(self, usda, limit = 0, outfilename_map2usda = None, outfilename_no_cannonical = None, outfilename_no_entitymap = None):
    for e in self.entitymap_dic: 
      self.entitymap_dic[e]["count"] = 0
    cursor = self.recipe_coll.find(limit = limit)  
    no_cannonical = Set()
    no_entitymap = Set()
    for r in cursor:    
      for s in r["steps"]:
        for l in s["lines"]:
          if not "cannonical" in l:
            print("No canonical for " + l["original"])
            no_cannonical.add(l["original"])
            continue
          ing = l["cannonical"]
          if ing in self.entitymap_alt_dic:
            ing = self.entitymap_alt_dic[ing]
          if ing in self.entitymap_dic:
            entity = self.entitymap_dic[ing]
          else:
            no_entitymap.add(ing)
            print('No entry in entitymap for: %s' % ing)
            continue
          entity["count"] = entity["count"] + 1
    res = []
    for e in self.entitymap_dic:
      res.append((-self.entitymap_dic[e]["count"], e))
    res.sort()
    res = map(lambda x: x[1], res)
    if outfilename_map2usda:
      f = open(outfilename_map2usda, "w")
      for e in res:
        f.write(e.encode("utf-8") + ' (appeared %d times): ' % rdb.entitymap_dic[e]["count"])
        if not "ndb_no" in rdb.entitymap_dic[e]:
          f.write("No mapping for this ingredient\n")
        else:
          if not rdb.entitymap_dic[e]["ndb_no"] in usda.dic:
            f.write('error: ndb number %s does not appear in USDA data\n' % rdb.entitymap_dic[e]["ndb_no"])
          else:
            f.write('%s\n'% usda.dic[rdb.entitymap_dic[e]["ndb_no"]].attrib["desc"])
      f.close()
    if outfilename_no_cannonical:
      f = open(outfilename_no_cannonical, 'w')
      for e in no_cannonical: f.write('%s\n' % e)
      f.close()
    if outfilename_no_entitymap:
      f = open(outfilename_no_entitymap, 'w')
      for e in no_entitymap: f.write('%s\n' % e)
      f.close()
    
  def bulkUpdateOne(self, _id, post):
    self.bulk.find({'_id':_id}).update({"$set": post})
    self.bulk_counter = self.bulk_counter + 1
    if self.bulk_counter == self.bulk_batch:
      self.bulkFlush()
      self.bulk = self.recipe_coll.initialize_unordered_bulk_op()
      self.bulk_counter = 0

  def bulkFlush(self):
    try:
      self.bulk.execute()
    except BulkWriteError as bwe:
      logging.info("Could not perform bulk operation.")
      pprint(bwe.details)
      sys.exit(-1)
    except:
      # This is typicaly because the bulk buffer is empty
      pass
      

  def updateOne(self, _id, post):
    return self.recipe_coll.update({'_id':_id}, {"$set": post}, upsert=False)

  # Takes a textual output of word2vec, and writes the vectors to a collection on the database.
  def writeWord2VecToDB(self, filename, vecname='word2vec'):
    logging.info('Writing word2vec vectors from file to DB')
    f = open(filename, 'rt')
    requests = []
    i = 0
    for l in f.readlines():
      if l[-1] == '\n': l = l[:-1]
      # first line has only two tokens, and the first is the dimensionality
      tokens = l.split(' ')
      if tokens[-1] == '': tokens = tokens[:-1]
      if len(tokens) <= 5:  # first line includes dimension and number of objects          
        continue
      ing = tokens[0]
      logging.info(ing)
      if ing[0] == '|': ing = undecorate_entity(ing)
      if ing[0] == '{': ing = undecorate_entity(ing)

      vec = map(lambda x: float(x), tokens[1:])
      update_instruction = {'$set' : {'name' : ing, vecname : vec}}
      if ing in self.entitymap_dic:
        update_instruction['$set']['entityMapId'] = self.entitymap_dic[ing]['_id']
      requests.append(UpdateOne({'name' : ing}, update_instruction, upsert = True))
      i = i + 1	
    logging.info('Preparing to write %d entity vecs to database' % i)
    try: 
      self.entityvecs_coll.bulk_write(requests)
    except BulkWriteError as bwe:
      logging.info("Could not perform bulk operation.")
      pprint(bwe.details)
      sys.exit(-1)
    logging.info('Done')
    f.close()

  # Next three used for updating reicpe vectors - in bulk
  def resetRecipeVecs(self):
    self.recipevecs_coll.drop()
    self.recipe_vecs_buffer = []
  def addRecipeVec(self, rid, vecname, v):
    self.recipe_vecs_buffer.append({'recipeId':rid, vecname: v})
  def flushRecipeVecs(self):
    self.recipevecs_coll.insert_many(self.recipe_vecs_buffer)

  def getRecipeVecs(self, vecname, query = {}):
    if query.__str__() in self.recipevecs_query_cache:
      return self.recipevecs_query_cache[query.__str__()]
    cursor = self.recipevecs_coll.find(query)
    ret = {}
    for c in cursor:
      if vecname in c:
        ret[c['recipeId'].__str__()] = c[vecname]
    self.recipevecs_query_cache[query.__str__()] = ret
    return ret

  def getManualSubRulesColl(self):
    return self.manualSubRules_coll


  def resetSubstitutions(self):
    self.sub_coll.drop()

  def bulkInsertSubstitutions(self, lst):
    self.sub_coll.insert_many(lst)


  def findFeedback(self, query={}):
    return self.feedback_coll.find(query)


  def recipeInvalid(self, r):
    if type(r['steps']) is not dict: return False
    return True

  def getIngredientsDic(self):
    return self.ingredients_dic

  def getUSDAIngredientByNDB(self, ndb_no):
    if int(ndb_no) in self.ingredients_dic: return self.ingredients_dic[int(ndb_no)]
    logging.warning('ndb_no=%s not found in USDA data' % ndb_no)
    return None

  def InsertManualSubRule(self, record):
    if self.manualSubRules_coll.find(record).count() > 0: return
    self.manualSubRules_coll.insert(record)
 
  def deleteSubstitutionsCollection(self):
    self.sub_coll.drop()
    
  def writeStats(self, name, description, table):
    self.stats_coll.remove({'name':name})
    n = len(table)
    table_items = table.items()
    ipart = 0
    while len(table_items):      
      chunk_size = min(len(table_items), MAX_STATS_TABLE_DOC_SIZE)
      part_table_items = table_items[:chunk_size]
      table_items = table_items[chunk_size:]
      record = {'name': name, 'part' : ipart, 'description':description, 'table': part_table_items}
      self.stats_coll.insert_one(record)
      ipart = ipart + 1

  def getStats(self, name):
    record = self.stats_coll.find({'name':name})
    table_items = []
    for r in record:
      table_items = table_items + r['table']
    for item in table_items:
      item[0] = tuple_or_identity(item[0])
    return dict(table_items)

  # with_gram: return a pair of lists, the first is the list of ingredients, the second is the corresponding grams
  # with_uid: return a pair of lists, the first is the list of ingredients, the second is the corresponding uids
  # (cannot do both with_gram and with_uid)
  # The recipe object below also uses this as a method, acting on a recipe.
  def getCannonicalIngredientList(self, r, with_gram=False, with_uid = False):
    assert (not (with_gram and with_uid))
    ret = []
    original = []  # original food
    quants = []
    uids = []     
    for s in r['steps']:
      if type(s) is not dict:
        print('Warning: recipe %s has bad "steps" form.  Skipping.' % r['_id'])
        continue
      for l in s['lines']:
        if not 'cannonical' in l: continue
        if with_gram and not 'gram' in l: continue
        cannonical = l['cannonical']
        cannonical = self.cannonicalize_entity(cannonical) # theoretically, we don't need this, but as of feb 23 2017 there is some problem
        ret.append(cannonical)
        original.append(l['food'] if 'food' in l else None)
        uids.append(l['uid'] if 'uid' in l else '')
        if 'gram' in l: quants.append(l['gram'])
    if with_gram: return ret, original, quants
    if with_uid: return ret, original, uids
    return ret, original

 
  def getInstructionsString(self, r):
    ret = ''
    if not 'steps' in r: return ret
    for s in r['steps']:
      if 'instruction' in s:
        ret = ret + s['instruction']
    return ret

  def removeRDFsFromOrigin(self, origin):
    self.rdf_coll.remove({'origin' : origin})

  def writeRDFs(self, rdfs):
    self.rdf_coll.insert_many(map(lambda x: x.serialize(), rdfs))

  def readRDFs(self, origins):
    print('origins = ', origins)
    curs = self.rdf_coll.find({'origin': {"$in": origins}}) 
    ret = []
    for rdf_json in curs:
      ret.append(RDF(json = rdf_json))
    return ret  

  # 
  # Read RDF info of given origin from DB, generate the associated graph and return it
  # Caching is done to avoid reading from DB more than once.
  #
  def getRDFGraph(self, origins):
    if origins.__str__() in self.rdf_graphs: return self.rdf_graphs[origins.__str__()] #cache
    rdfs = self.readRDFs(origins)
    graph = RDFGraph(rdfs = rdfs, entity_cannonicalizer = self.cannonicalize_entity)
    self.rdf_graphs[origins.__str__()] = graph
    return graph
def entity2USDAndb(rdb, usda, x):
  entitymap_dic = rdb.get_entitymap_dic()
  if not x in entitymap_dic:
    return None, None
  if not 'ndb_no' in entitymap_dic[x]:
    return None, None
  if entitymap_dic[x]['ndb_no'] == '00000':
    return None, None
  if not entitymap_dic[x]['ndb_no'] in usda.dic:
    return None, None
  return usda.dic[entitymap_dic[x]['ndb_no']], entitymap_dic[x]['origin']



#
# Take a recipe text, extract all words in a vector
# Treat entities (eg |olive oil|) as single tokens, replace their spaces with '-'
#


def StringToEntityVec(txt, mark_end_of_instruction=True, mark_end_of_recipe = True, forbidden_words = Set([])):
  out = []
  recipe = txt.lower()
  recipe = re.sub('^[0-9a-z,\.\|]', '', recipe)
  recipe  = re.sub('\.', ' . ', recipe)
  recipe = re.sub('-', ' - ', recipe)
  recipe  = re.sub('[,\?;\(\):/]', '', recipe)
  recipe = re.sub('\|s ', '| s ', recipe)
  recipe = re.sub(' d\|', ' d |', recipe)
  recipe = re.sub('\|or ', '| or ', recipe)
  recipe = re.sub(' into\|', ' into |', recipe)
  recipe = re.sub('\|stirring ', '| stirring ', recipe)
  recipe = recipe.split(' ')
  inside_pipes = False  # are we inside a pair of |...|  (an entity)
  word_inside_pipes = ''
  for w in recipe:
    if w == '': continue
    if w == '.':
      if inside_pipes:
        break
      if mark_end_of_instruction:
        out.append('</s>')
    elif w[0] == '|':
      if inside_pipes:
        break
      if w[-1] == '|':
        out.append(w)
      else:              
        inside_pipes = True
        word_inside_pipes = w
    elif '|' in w and w[0] != '|' :
      if not inside_pipes:
        break
      word_inside_pipes = word_inside_pipes + '-' + w
      out.append(word_inside_pipes)
      inside_pipes = False
      word_inside_pipes = ''
    else:
      if inside_pipes:
        word_inside_pipes = '\n' + word_inside_pipes + '-' + w
      else:
        if not w in forbidden_words:
          out.append(w) 
  if mark_end_of_recipe:
    out.append('</s>')
  return out




#
# Read word2vec output file in text format
# first row: number of entities and dimension, following rows are entities (words) and corresponding vectors
#
def ReadWord2VecOutputFile(f):
  dim = 0
  dic = dict()
  for line in f:
    if not dim:
      dim = int(line.split()[1]) # vector dimension
      continue
    line = line.split()
    dic[line[0]] = np.array(map(lambda x: float(x), line[1:]))
  return dic, dim


#
# This is a demo recipe DB that was used for a demo.  Instead of recomputing the subs, we use the previously computed results.
#
class LegacyRecipes:
  def __init__(self):
    self.client = MongoClient(
      "mongodb://testuser:testuser@ds041516.mlab.com:41516/demo-recipes",
      ssl_cert_reqs=ssl.CERT_NONE)
    self.db = self.client["demo-recipes"] 

  def getAll(self):
    return self.db["recipePOJO"].find({})

