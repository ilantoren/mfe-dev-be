# -*- encoding utf-8 -*-

#
# This program reads recipes and generates word-context pairs in preparation for word2vecf.
# it also takes a regular expression array, used for initial clustering of the recipes, each cluster defined by one regexp
#

import json
import random
import sys
import re
import numpy as np
from IOTools import *
import math
import struct

LIMIT = 1000
MIN_FREQ = 5

def mylog(x):
  if x<0.000001:
    return math.log(0.000001)
  else:
    return math.log(x)

def score(ev, dic, dic_cv, window = 5):
  score = 0.0
  n = len(ev)
  c = 0
  for i in range(n-window-1):
    if not ev[i] in dic:
      continue
    for j in range(window):
      if not ev[i+j+1] in dic_cv:
        continue
      dp = np.dot(dic[ev[i]], dic_cv[ev[i+j+1]])
      score = score + mylog(math.exp(dp)/(math.exp(dp)+1))
      c = c + 1
  return score / (c+.0) if c>0 else -999999999

def Prepare_word2vecf(limit = LIMIT, data_qry={}, outfilename="data/prepare_word2vecf.out", vocabfilename="data/vocab.txt", cvocabfilename="data/cvocab.txt",  min_freq = MIN_FREQ):

    print 'Connecting to MongoDB'
    rdb = RecipeDB()
    entitymap_dict = rdb.get_entitymap_dic()
    outfile = open(outfilename, 'wt')
    vocabfile = open(vocabfilename, 'wt')
    cvocabfile = open(cvocabfilename, 'wt')

    print 'Creating entity index'

    print 'Working...'
    cursor = rdb.find()
    num = 0


    sorted_entities = []
    for k in entitymap_dict: 
      sorted_entities.append(k)
      entitymap_dict[k]["count"] = 0
    sorted_entities.sort(key = lambda x: entitymap_dict[x]["index"])

    pp = 0
    for doc in cursor:
      sys.stdout.write('%d\n' % pp)
      sys.stdout.flush()
      pp = pp + 1
      ings = []
      for s in doc["steps"]:
        ings = []
        for l in s["lines"]:
          if not "cannonical" in l:
            continue
          if l["quantity"] != "0": 
            cann = rdb.cannonicalize_entity(l["cannonical"])
            if not cann in entitymap_dict: continue
            ings.append(cann)
            entitymap_dict[cann]["count"] = entitymap_dict[cann]["count"] + 1
      for i in range(len(ings)):
        for j in range(len(ings)):
          if i==j: continue
          outfile.write(decorate_entity(ings[i]).encode('utf-8') + ' ' + decorate_entity(ings[j]).encode('utf-8') + '\n')

    print 'Writing vocabulary file'
    for k in sorted_entities:
      vocabfile.write('%s %d\n' % (decorate_entity(k).encode('utf-8'), entitymap_dict[k]["count"]))
      cvocabfile.write('%s %d\n' % (decorate_entity(k).encode('utf-8'), entitymap_dict[k]["count"]))

    cvocabfile.close()
    vocabfile.close()
    outfile.close()

if __name__ == "__main__":

  Prepare_word2vecf(limit=9999999)

