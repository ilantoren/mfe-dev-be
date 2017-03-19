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
from Word2VecDistance import *
import RDF

EPS = 0.00001

def increment(d,x):
  if x in d:
     d[x] = d[x] + 1
  else:
     d[x] = 1

  
def myfloat(x):
  if x=='': return 0.0
  if x is None: return 0.0
  return float(x)
def none2str(x):
  if x is None: return ''
  return x
def match_recipes(r1, r2, graph):
  ings1 = r1.getIngs()
  ings2 = r2.getIngs()
  total_match = 0.0
  total_gram = 0.0
  match_info = []

  for ing in ings1:
    match = False
    node = graph.getNode(ing["cannonical"])
    total_gram = total_gram + myfloat(ing["gram"])
    for ing2 in ings2:
      if ing["cannonical"] == ing2["cannonical"] and ing["cannonical"] is not None and ing2["cannonical"] is not None:
        total_match = total_match + myfloat(ing["gram"])
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break
      if node is None: continue
      node2 = graph.getNode(ing2["cannonical"])
      if node2 is None: continue
      if node.getNeighbors(RDF.TYPE_OF) == node2.getNeighbors(RDF.TYPE_OF) and len(node.getNeighbors(RDF.TYPE_OF)) and len(node2.getNeighbors(RDF.TYPE_OF)):
        total_match = total_match + myfloat(ing["gram"])
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))       
        break
      if node2 in node.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY):
        total_match = total_match + myfloat(ing["gram"])
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break
      if node in node2.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY):
        total_match = total_match + myfloat(ing["gram"])
        match_info.append(none2str(ing["food"]) + '->' + none2str(ing2['food']))
        break

  return total_match/total_gram, match_info

def find_subs(rdb, graph, x, neighbor_ids, t=0.35, source_ing = '', match_thresh=0.70):
  recipe = rdb.findOneRecipe(x)
  ings = recipe.getIngs() 
  neighbor_recipes = list(rdb.findRecipes(query={'_id' : {'$in' : map(lambda x: ObjectId(x),neighbor_ids)}}))

  clean_neighbor_recipes = []
  for neighbor_recipe in neighbor_recipes:
    match, match_info = match_recipes(recipe, neighbor_recipe, graph)
    if match < match_thresh: continue
    match, match_info = match_recipes(neighbor_recipe, recipe, graph)
    if match < match_thresh: continue
    clean_neighbor_recipes.append(neighbor_recipe)
    print match, match_info
    print '  %s %s: %s' % (neighbor_recipe.getId(), neighbor_recipe.getTitle(), neighbor_recipe.getCannonicalIngNames())
  neighbor_recipes = clean_neighbor_recipes

  print 'num neighbors = %d' % len(neighbor_recipes)
  w2vd = Word2VecDistance(rdb=rdb)
  for ing in ings:
    if ing is None: continue
    if source_ing != '' and ing['cannonical'] != source_ing: continue
    #if myfloat(ing['gram'] == 0.0): continue
    gram = myfloat(ing['gram'])
    print none2str(ing['cannonical']) + '--->'
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
    
    for ing_ in ing_counter:
      print '  ', ing_, ing_counter[ing_]


if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-x", "--x", dest="x", help="Id of recipe", default="", metavar="X")
  parser.add_option("--ing", "--ing", dest="ing", help="Name of ingredient that is source of substitution, leave empty (default) for all ingredients", default="", metavar="TARGET_ING")
  parser.add_option("-v", "--vecname", dest="vecname", help="Name of vector", default="word2vec", metavar="VECNAME")
  parser.add_option("-k", "--k", dest="k", help="Number of nearest neighbors", default="10", metavar="K")
  parser.add_option("-t", "--threshold", dest="t", help="Ingredient Similarity threshold", default="0.4", metavar="THRESHOLD")
  parser.add_option("--mt", "--match_thresh", dest="match_thresh", help="Threshold for match algorithm, default 0.7", default="0.7", metavar="MATCH_THRESH")

  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()

  graph = rdb.getRDFGraph(['foodsubs', 'manual rules', 'entityMapping'])
  q = Q.PriorityQueue(maxsize=eval(options.k))
  
  recipe_vecs = rdb.readAllRecipeVecs(options.vecname)


  if options.x != 'random' :
    if not options.x in recipe_vecs:
      print 'Recipe %s doesnt have a vec of type %s.' % (options.x, options.vecname)
      sys.exit(-1)
  else:
    options.x = random.choice(recipe_vecs.keys())

  xvec = np.array(recipe_vecs[options.x])

  closest = None
  closest_sim = 0.0
  for rv in recipe_vecs:
    if rv == options.x: continue
    yvec = np.array(recipe_vecs[rv])
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
    r = rdb.get(id_)
    t = r['title'] if 'title' in r else ''
    ings, original_ings = rdb.getCannonicalIngredientList(r)
    res.append((sim, id_, t, ings.__str__()))
  neighbor_ids = map(lambda x: x[1], res)
  print 'Worst similarity = ', res[0][0]

  find_subs(rdb, graph, options.x, neighbor_ids, t=eval(options.t), source_ing = options.ing, match_thresh = eval(options.match_thresh))


