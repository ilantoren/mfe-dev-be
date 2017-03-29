# This program is used for providing nearest neighbor tools for recipes
#

import sys
import logging
import numpy as np
import math
import re
import random
import pprint
import IOTools
from optparse import OptionParser
from bson.objectid import ObjectId
import Queue as Q
import RDF
from SubstitutionEngine import AutomaticRule, ListSinglePick, SimpleSubstitution
from sets import Set

EPS = 0.00001

def increment(d,x):
  if x in d:
     d[x] = d[x] + 1
  else:
     d[x] = 1

  
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
    
def myfloat(x):
  if x=='': return 0.0
  if x is None: return 0.0
  return float(x)
def none2str(x):
  if x is None: return ''
  return x

def match_recipes_munkres(r1, r2, graph):
  ings1 = r1.getIngs()
  ings2 = r2.getIngs()
  #print 'ings1=', map(lambda x:x['cannonical'], ings1)
  #print 'ings2=', map(lambda x:x['cannonical'], ings2)
  n = max(len(ings1), len(ings2))
  cost = []
  for i in range(n):
    cost.append([0]*n)
  for i in range(n):
    if i >= len(ings1): break
    for j in range(n):
      if j >= len(ings2): break
      node1 = graph.getNode(ings1[i]["cannonical"])
      node2 = graph.getNode(ings2[j]["cannonical"])
      if node1 is None or node2 is None: continue
      if len(node1.getNeighbors(RDF.TYPE_OF) & node2.getNeighbors(RDF.TYPE_OF)): #and len(node1.getNeighbors(RDF.TYPE_OF)) and len(node2.getNeighbors(RDF.TYPE_OF)):
        cost[i][j] = -1
      if (node2 in node1.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY)) or (node1 in node2.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY)) or node1 == node2:	
        cost[i][j] = -1
  val = 0
  for i in range(len(ings1)):
    val = val + sum(cost[i])
  return val/len(ings1)
  return value / len(ings1)

def match_recipes(r1, r2, graph, use_gram = False, gram_ratio_thresh = 10):
  ings1 = r1.getIngs()
  ings2 = r2.getIngs()
  total_match = 0.0
  total_match_gram = 0
  total_gram = 0.0
  match_info = []
  count = 0
  for ing in ings1:
    match = False
    node = graph.getNode(ing["cannonical"])
    total_gram = total_gram + myfloat(ing["gram"])
    for ing2 in ings2:
      # If the ratio of gram quantities is higher than a threshold, then no match for the ingredients
      if ing["gram"] is not None and ing2["gram"] is not None:
        if ing["gram"] < ing2["gram"]/gram_ratio_thresh or ing["gram"] > ing2["gram"]*gram_ratio_thresh:
          continue

      if ing["cannonical"] == ing2["cannonical"] and ing["cannonical"] is not None and ing2["cannonical"] is not None:
        total_match_gram = total_match_gram + myfloat(ing["gram"])
        total_match = total_match + 1
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break
      if node is None: continue
      node2 = graph.getNode(ing2["cannonical"])
      if node2 is None: continue
      if len(node.getNeighbors(RDF.TYPE_OF) & node2.getNeighbors(RDF.TYPE_OF)): # and len(node.getNeighbors(RDF.TYPE_OF)) and len(node2.getNeighbors(RDF.TYPE_OF)):
        total_match_gram = total_match_gram + myfloat(ing["gram"])
        total_match = total_match + 1
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))       
        break
      if node2 in node.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY):
        total_match_gram = total_match_gram + myfloat(ing["gram"])
        total_match = total_match + 1
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break
      if node in node2.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY):
        total_match_gram = total_match_gram + myfloat(ing["gram"])
        total_match = total_match + 1
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break

#  print 'munkres: ', match_recipes_munkres(r1, r2, graph)
  if use_gram:
    return total_match_gram/total_gram, match_info
  else:
    return ((total_match+.0)/len(ings1)), match_info

def find_subs(rdb1, rdb2, graph, x, neighbor_ids, t=0.35, source_ing = '', match_thresh=0.70):
  recipe = rdb1.findOneRecipe(x)
  ings = recipe.getIngs() 
  neighbor_recipes = list(rdb2.findRecipes(query={'_id' : {'$in' : map(lambda x: ObjectId(x),neighbor_ids)}}))

  clean_neighbor_recipes = []
  for neighbor_recipe in neighbor_recipes:
    match, match_info = match_recipes(recipe, neighbor_recipe, graph)
    match_, match_info_ = match_recipes(neighbor_recipe, recipe, graph)
    if match < match_thresh or match_ < match_thresh: continue
    clean_neighbor_recipes.append(neighbor_recipe)
    #print match, match_info
    #print '  %s %s: %s' % (neighbor_recipe.getId(), neighbor_recipe.getTitle(), neighbor_recipe.getCannonicalIngNames())
  neighbor_recipes = clean_neighbor_recipes

  print 'num neighbors = %d' % len(neighbor_recipes)
  #w2vd = Word2VecDistance(rdb=rdb)
  for ing in ings:
    if ing is None: continue
    if source_ing != '' and ing['cannonical'] != source_ing: continue
    #if myfloat(ing['gram'] == 0.0): continue
    gram = myfloat(ing['gram'])
    #print none2str(ing['cannonical']) + '--->'
    #if not w2vd.hasVector(ing): continue
    ing_counter = {}
    for neighbor_recipe in neighbor_recipes:
      ings_ = neighbor_recipe.getIngs() #, ings_original_ = rdb.getCannonicalIngredientList(neighbor_recipe)
      for ing_ in ings_:
        if not ing_['cannonical'] in graph.nodes: continue
        node_ = graph.nodes[ing_['cannonical']]
        gram_ = myfloat(ing_['gram'])
        #if gram_ == 0.0: continue
        #if gram_ < gram/5 or gram < gram_/5: continue
        sources = map(lambda x: x.name, node_.getNeighbors(RDF.POSSIBLY_SUBSTITUTE_OF))
        if not ing['cannonical'] in sources: continue
        increment(ing_counter, ing_['cannonical'])
    
    #for ing_ in ing_counter:
    #  print '  ', ing_, ing_counter[ing_]


class NearNeighborSubstitutionRule(AutomaticRule):
  def __init__(self, rdb, sim_vecname = 'word2vec', sim_threshold = 0.6, match_threshold = 0.6, source_ing = '', debug=False, bigram_table_name = None, limit_sim=100, gram_ratio_threshold=10):
    self.rdb = rdb
    self.recipe_vecs = rdb.getRecipeVecs(sim_vecname)
    self.sim_threshold = sim_threshold
    self.match_threshold = match_threshold
    self.source_ing = source_ing # This is just used for debugging, in case you want to debug substitution of a single ingredient
    self.origin = 'NearNeighbor'
    self.version = '1'
    self.name = ''
    self.rdf_graph = rdb.getRDFGraph(['foodsubs', 'manual rules', 'entityMapping'])
    self.debug = debug
    self.gram_ratio_threshold = gram_ratio_threshold
    if bigram_table_name is not None:
      self.bigram_table = self.rdb.getStats(bigram_table_name)
    else: self.bigram_table = None
    self.limit_sim = limit_sim

  def apply(self, recipe):
    _id = recipe.getId()
    res = []
    if not _id in self.recipe_vecs: return []
    xvec = np.array(self.recipe_vecs[_id])
    near_ids = []
    for other_recipe in self.recipe_vecs:
      if other_recipe == _id: continue
      yvec = np.array(self.recipe_vecs[other_recipe])
      sim = np.dot(xvec, yvec)  / math.sqrt(np.dot(xvec, xvec) * np.dot(yvec, yvec))
      if sim >= self.sim_threshold:
        near_ids.append(other_recipe)
        if self.limit_sim and len(near_ids) == self.limit_sim: break

    if self.debug:
      print 'Number of near recipes: %d' % len(near_ids)

    # Another match based filter
    near_recipes = list(self.rdb.findRecipes(query={'_id' : {'$in' : map(lambda x: ObjectId(x), near_ids)}}))
    near_recipes2 = []

    for near_recipe in near_recipes:
      match1, match_info1 = match_recipes(recipe, near_recipe, self.rdf_graph, False, self.gram_ratio_threshold)
      match2, match_info2 = match_recipes(recipe, near_recipe, self.rdf_graph, False, self.gram_ratio_threshold)
      if match1 < self.match_threshold: continue
      if match2 < self.match_threshold: continue
      near_recipes2.append(near_recipe)
    near_recipes = near_recipes2

    if self.debug:
      print "Number of matching recipes: ", len(near_recipes)

    ings = recipe.getIngs()
    if ings is None: return []
    for ing in ings:
      cann = ing['cannonical']
      if self.source_ing != '' and cann != self.source_ing: continue
      gram = myfloat(ing['gram'])
      if cann is None: 
        print 'Warning, no cannonical for ' + ing['food']
        continue

      ing_counter = {}
      lsp = ListSinglePick(
        source = ing["food"],  # the original (un-cannonicalized) ingredient 
        sourceId = self.rdb.getEntityIdByCannonical(cann),
        instanceId = ing["uid"], 
        origin = self.origin, 
        version = self.version,  
        infolink = '',
        moreinfo = '',
        name = self.name)
      options_quickref = Set([])
      for near_recipe in near_recipes:
        ings_ = near_recipe.getIngs()
        for ing_ in ings_:
          cann_ = ing_['cannonical'] 
          if cann_ == cann: continue
          if cann_ in options_quickref: continue
          if not cann_ in self.rdf_graph.nodes: continue
          node_ = self.rdf_graph.nodes[cann_]
          gram_ = myfloat(ing_['gram'])
          sources = map(lambda x: x.name, node_.getNeighbors(RDF.POSSIBLY_SUBSTITUTE_OF))
          if not cann in sources: continue 
          if self.violation_by_bigram(cann, cann_, recipe): continue
          options_quickref.add(cann_)         
          lsp.addOption(SimpleSubstitution(
            target = cann_,
            targetId = self.rdb.getEntityIdByCannonical(cann_),
            quantityRatio = 1.0,
            probability = 0.8,
            moreinfo = ''))
      if not len(lsp.options): continue
      res.append(lsp.getDict())
    if self.debug: print 'Result of nearest neighbor substitution:', res
    return res 
            

  def violation_by_bigram(self, source_cann, target_cann, recipe):
    for other_ing in recipe.getIngs():
      other_cann = other_ing["cannonical"]
      if other_cann is None: continue
      if other_cann == source_cann: continue
      if (not (target_cann, other_cann) in self.bigram_table) and (not (other_cann, target_cann) in self.bigram_table): return True
    return False

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-x", "--x", dest="x", help="Id of recipe", default="", metavar="X")
  parser.add_option("--ing", "--ing", dest="ing", help="Name of ingredient that is source of substitution, leave empty (default) for all ingredients", default="", metavar="TARGET_ING")
  parser.add_option("-v", "--vecname", dest="vecname", help="Name of vector", default="word2vec", metavar="VECNAME")
  parser.add_option("-k", "--k", dest="k", help="Number of nearest neighbors", default="10", metavar="K")
  parser.add_option("-t", "--threshold", dest="t", help="Ingredient Similarity threshold", default="0.4", metavar="THRESHOLD")
  parser.add_option("--mt", "--match_thresh", dest="match_thresh", help="Threshold for match algorithm, default 0.7", default="0.7", metavar="MATCH_THRESH")
  parser.add_option("--c1", "--config1", dest="config1", help="Mongo configuration for recipes on which we want to find subs", default="local.config", metavar="CONFIG1")
  parser.add_option("--c2", "--config2", dest="config2", help="Mongo configuration for corpus in which we find matches for the recipes in config1", default="local.config", metavar="CONFIG2")
  (options, args) = parser.parse_args()

  rdb1 = IOTools.RecipeDB(config_file = options.config1)
  rdb2 = IOTools.RecipeDB(config_file = options.config2)

  graph = rdb2.getRDFGraph(['foodsubs', 'manual rules', 'entityMapping'])
  q = Q.PriorityQueue(maxsize=eval(options.k))
  
  recipe_vecs1 = rdb1.GetRecipeVecs(options.vecname)
  recipe_vecs2 = rdb2.GetRecipeVecs(options.vecname)


  if options.x != 'random' :
    if not options.x in recipe_vecs1:
      print 'Recipe %s doesnt have a vec of type %s.' % (options.x, options.vecname)
      sys.exit(-1)
  else:
    options.x = random.choice(recipe_vecs1.keys())

  xvec = np.array(recipe_vecs1[options.x])

  closest = None
  closest_sim = 0.0
  for rv in recipe_vecs2:
    if rv == options.x: continue
    yvec = np.array(recipe_vecs2[rv])
    if np.linalg.norm(xvec-yvec) < EPS: continue
    sim = np.dot(xvec, yvec)  / math.sqrt(np.dot(xvec, xvec) * np.dot(yvec, yvec))
    if q.full():
      o = q.get()
      if sim > o[0]:
        q.put((sim, rv))
      else:
        q.put(o)
    else:
      q.put((sim, rv)) 

  res = []

  while not q.empty():
    o = q.get()
    id_ = o[1]
    sim = o[0]
    r = rdb2.get(id_)
    t = r['title'] if 'title' in r else ''
    ings, original_ings = rdb2.getCannonicalIngredientList(r)
    res.append((sim, id_, t, ings.__str__()))
  neighbor_ids = map(lambda x: x[1], res)
  print 'Worst similarity = ', res[0][0]

  find_subs(rdb1, rdb2, graph, options.x, neighbor_ids, t=eval(options.t), source_ing = options.ing, match_thresh = eval(options.match_thresh))


