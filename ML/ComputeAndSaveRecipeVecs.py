#
# This program matches a vector for each recipe, and writes it in the recipe vector collection
# The method used here simply adds up the vectors corresponding to the ingredients in the recipe.
#

import sys
import logging
import numpy as np
import re
import random
import IOTools
from sets import Set
import json
import json
from optparse import OptionParser
from bson.objectid import ObjectId

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-m", "--min_ing", dest="min_ing", help="Min number of ingredients for output, default=5", default="5", metavar="MIN_ING")
  parser.add_option("-1", "--normalize1", dest="normalize1", help="Should the entity vectors be normalized before summing them up? (Default True)", default="True", metavar="NORMALIZE1")
  parser.add_option("-2", "--normalize2", dest="normalize2", help="Should the entity vectors be normalized after summing them up? (Default True)", default="True", metavar="NORMALIZE2")
  parser.add_option("-q", "--use_quantity", dest="use_quantity", help="Should the relative quantity of the ingredient be taken into account? (Default True)", default="True", metavar="USE_QUANTITY")
  parser.add_option("-v", "--vecname", dest="vecname", help="Name of vector", default="word2vec", metavar="VECNAME")
  parser.add_option("--do", "--debug_one", dest="debug_one", help="Id of single recipe for debugging.  Empty (default) for no debugging.", default="", metavar="DEBUG_ONE")
  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()

  if options.debug_one == '':
    rdb.resetRecipeVecs()
  total_written = 0

  use_quantity = eval(options.use_quantity)
  if options.debug_one == '':
    query = {}
  else:
    query = {'_id': ObjectId(options.debug_one)}
  for r in rdb.findRecipes(query = query):
    if options.debug_one != '':
      print 'recipe:', r
    if not r.isValid(): continue
    c = 0
    total = None
    ings = r.getIngs()
    tot_grams = 0.0
    for ing in ings:
      if use_quantity:
        if ing['gram'] is None or ing['gram']==0.0: continue
        coeff = float(ing['gram'])
      else:
        coeff = 1.0
      if options.debug_one: 
        print 'cannonical: ', ing['cannonical']
        print 'vecname=', options.vecname
      v = rdb.getEntityVec(ing['cannonical'], type_=options.vecname)
      if v is None:
        if options.debug_one != '': print 'Entity %s has no vec' % ing['cannonical']
        continue
      v = np.array(v)
      v = v if not eval(options.normalize1) else v/np.linalg.norm(v)
      if total is None:
        total = v * coeff
      else:
        total = total + v * coeff
      tot_grams = tot_grams + coeff
      if len(options.debug_one): print ing
      c = c + 1
    if options.debug_one != '':
      #print total
      print c
      print tot_grams
    if len(options.debug_one): print 'c=%d' %c
    if c < eval(options.min_ing): continue
    if len(options.debug_one): print 'Here'
    if total is None: continue
    if options.debug_one: print 'Here'
    if use_quantity:
      total = total / tot_grams
    else:
      total = total / c
    total = total if not eval(options.normalize2) else total/np.linalg.norm(total)
    if options.debug_one == '':
      rdb.addRecipeVec(rid = r.getId(), vecname = options.vecname, v = total.tolist())
    total_written = total_written + 1
    sys.stdout.write('\r%d' % total_written)

  if options.debug_one == '':
    sys.stdout.write('\nFlushing writes to databse\n')    
    rdb.flushRecipeVecs()
  print 'Total recipevecs written: %d' % total_written
