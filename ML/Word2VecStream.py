#-*- coding: utf-8 *-*
	
#
# This program reads the recipes from the database, and generates a stream of words in preparation for word2vec.
# Word (or combinations) that are found in the entity dataset are replaced by a unique entity identifier.
#

import sys
from IOTools import *
import re
from sets import Set
import random
from optparse import OptionParser
import RDF
LIMIT = 9999999
EPS = 0.0001



# draw a random index from given probability distribution
# distribution does not have to be normalized
def draw_from_dist(probs):
  s = sum(probs)
  r = random.random() * s#(0, int(s))
  running = 0
  for i in range(len(probs)):
    if r < running + probs[i]: return i
    running = running + probs[i]
  return len(probs)-1

def my_random_shuffle(ings, gram):
  tmp = zip(ings, gram)
  random.shuffle(tmp)
  ings = map(lambda x: x[0], tmp)
  gram = map(lambda x: x[1], tmp)

def Word2VecStreamFromIngredients(
  rdb, limit=0, min_num_ings=5, data_qry={}, random_permute=True, outfn='data/out.txt', draw_by_quantity=False, num_draws=10, average_grams_table_name = '', 
  mark_extreme_quantity=False, substitute=False, substitute_number=4, substitute_graph=None): 
  if substitute: assert (substitute_graph is not None)

  entity_alt_dic = rdb.get_entitymap_alt_dic()
  entity_dic = rdb.get_entitymap_dic()
  cursor = rdb.find(query=data_qry)
  av_gram = rdb.getStats(average_grams_table_name)
  outf = open(outfn, 'w')
  i = 0
  for r in cursor:
    ings, original_ings, gram = rdb.getCannonicalIngredientList(r, with_gram = True)
    if len(ings) < min_num_ings: continue
    for k in range(len(ings)):      
      ing = ings[k]
      if ing is None: continue
      if not ing in entity_dic and ing in entity_alt_dic: 
        ing2 = entity_alt_dic[ing]
        ings[k] = ing2
    
    tot_grams = sum(gram)
    if sum(gram) < EPS: continue
    for j in range(len(ings)):   gram[j] = gram[j]*1.0/ tot_grams
    if not draw_by_quantity:
      if len(ings) < min_num_ings: continue

      for sub_iter in range(1 if not substitute else (substitute_number+1)):
        if random_permute: my_random_shuffle(ings, gram)              
        for j in range(len(ings)):
          ing = ings[j]
          if ing is None: continue
          if sub_iter>0:  # first iteration standard, following one use subs
            node = substitute_graph.getNode(ing)
            if node is not None:
              alts = node.getNeighbors(RDF.POSSIBLY_SUBSTITUTED_BY)
              if len(alts): 
               alt = (random.choice(list(alts)).name)
               alt = rdb.cannonicalize_entity(alt, default=ing)
               ing = alt             
          double = False
          if mark_extreme_quantity:
            if ing in av_gram: 
              if gram[j] > av_gram[ing]*3.0/2.0:
                double = True
          outf.write('%s ' % (decorate_entity(ing).encode('utf-8')))
          if double: outf.write('%s ' % (decorate_entity(ing).encode('utf-8')))
        outf.write('</s>\n')

    else:
      if len(ings) == 0: continue
      tot_gram = sum(gram)
      if tot_gram < EPS: continue
      for j in range(len(ings)): gram[j] = gram[j] / tot_gram
      for j in range(len(ings)):
        if gram[j] == 0 and ings[j] in av_gram: gram[j] = av_gram[ings[j]]
      for j in range(len(ings)): 
        gram[j] = gram[j] / av_gram[ings[j]] if (ings[j] in av_gram and av_gram[ings[j]] > EPS) else 1.0
      for j in range(num_draws if num_draws else len(ings)):
        index = draw_from_dist(gram)
        outf.write('%s ' % decorate_entity(ings[index]))
      outf.write('</s>\n')

    sys.stdout.write('\r%d' % i)
    sys.stdout.flush()
    i = i + 1
    if limit and i == limit:  break
  outf.close()


def Word2VecStreamFromInstructionAnnotation(rdb, limit, data_qry, outfn):
  recipes = rdb.findRecipeAnnotation(query=data_qry, limit=limit)
  f = open(outfn, 'wb')
  for r in recipes:
    if not 'replacedText' in r: continue
    tokens = tokenizeInstructionAnnotation(r["replacedText"])
    for t in tokens: f.write('%s ' % t.encode('utf-8').lower())
    f.write('</s>\n')
  f.close()

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-f", "--file", dest="filename", help="Output FILE", default='data/word2vecstream_inst.txt', metavar="FILE")
  parser.add_option("-m", "--method", dest="method", help="Which METHOD to use (ingredients|text)", default='ingredients', metavar="METHOD")
  parser.add_option("-l", "--limit", dest="limit", help="Recipe number LIMIT, 0 for no limit", default=0, metavar="LIMIT")
  parser.add_option("-q", "--draw_by_quantity", dest="draw_by_quantity", help="Draw by quantity (only in case of ingredients method), default False", default="False")
  parser.add_option("-r", "--random_permute",  dest="random_permute", help="Random permute ingredients (only in case of ingredients method), default True)", default="True")
  parser.add_option("-n", "--min_num_ings", dest="min_num_ings", help="Minimum number of ingredients to require from recipe (only in case of ingredients method), default 3", default="3")
  parser.add_option("-a", "--average_grams_table", dest="average_grams_table", help="Table with average grams of ingredient in recipes", default="ingredient_av_gram_ratio")
  parser.add_option("-d", "--num_draws", dest="num_draws", help="Number of draws (in case of ingredients method with draw by quantity)", default="20")
  parser.add_option("-x", "--mark_extreme", dest="mark_extreme", help="Mark ingredients if quantity is extreme, generating different types of ingredients for each ingredient", default="False")
  parser.add_option("-s", "--substitute", dest="substitute", help="Randomly substitute ingredients using RDF info", default="False", metavar="SUBSTITUTE")
  parser.add_option("--sn", "--substitute_number", dest="substitute_number", help="Number of substituted recipe to generate", default="4", metavar="SUBSTITUTE_NUMBER")
  rdb = RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()
  graph = rdb.getRDFGraph(['foodsubs', 'manual rules', 'entityMapping'])

  print 'method =', options.method
  if options.method == 'ingredients':
    Word2VecStreamFromIngredients(
      rdb=rdb, limit = int(options.limit), min_num_ings = int(options.min_num_ings), data_qry = {}, 
      outfn= options.filename, random_permute = eval(options.random_permute), draw_by_quantity = eval(options.draw_by_quantity),
      average_grams_table_name = options.average_grams_table, num_draws = eval(options.num_draws), mark_extreme_quantity=eval(options.mark_extreme),
      substitute=eval(options.substitute), substitute_number=eval(options.substitute_number), substitute_graph=graph)
  elif options.method == 'text':
    Word2VecStreamFromInstructionAnnotation(rdb, limit = int(options.limit), data_qry={}, outfn=options.filename, mark_extreme_quantity=eval(options.mark_extreme))
  else:
    print("Method must be either 'ingredients' or 'text'")
    sys.exit(-1)


