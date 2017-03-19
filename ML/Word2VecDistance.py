#
# This program reads a (non binary) output of word2vec, and then given a word outputs the words closest to it
# The class also allows construction from a recipeDB 
#

import sys
from IOTools import *
import re
import numpy as np
import math
import IOTools

LIMIT = 9999999
EPS = 0.00001

def dp(x,y):
  return np.dot(x,y)
def dp_normalized(x,y):
  return np.dot(x,y) / math.sqrt(np.dot(x,x) * np.dot(y,y))

class Word2VecDistance:
  def __init__(self, filename=None, filename_cv='', rdb=None, vecname='word2vec', cvecname=None):
    assert filename is None or rdb is None # Cannot read vectors from two sources.
    if filename is not None:
      f = open(filename, 'rt')
      f_cv = open(filename_cv, 'rt')
      self.dic, self.dim = ReadWord2VecOutputFile(f)
      self.dic_cv, self.dim_cv = ReadWord2VecOutputFile(f_cv)
      assert self.dim == self.dim_cv
      f.close()
      f_cv.close()
    if rdb is not None:
      self.dic = {}
      self.dic_cv = {}
      for ing in rdb.get_entitymap_dic():
        if not ing in rdb.get_entityvecs_dic(): 
          continue
        ing_data = rdb.get_entityvecs_dic()[ing]
        if vecname in ing_data:
          self.dic[ing] = np.array(ing_data[vecname])
        if cvecname in ing_data:
          self.dic_cv[ing] = np.array(ing_data[cvecname])

  def hasVector(self, x):
    return x is not None and x in self.dic

  def CosineSimilarity(self,x,y):
    return dp_normalized(self.dic[x], self.dic[y])

  def SimilarityAsymmetric(self, x, y, normalized=True):
    if not x in self.dic_cv or not y in self.dic: return 0.0
    if normalized: return dp_normalized(self.dic_cv[x], self.dic[y])
    return dp(self.dic_cv[x], self.dic[y])

  def Dist(self, x,y):
    return np.linalg.norm(self.dic[x] - self.dic[y])

  def Dot(self, x,y):
    return np.dot(self.dic[x], self.dic[y])

  def Dot_CV(self, x,y):
    return np.dot(self.dic_cv[x], self.dic[y])

  def SortCosine(self, w):
    v = map(lambda x: (-self.CosineSimilarity(w, x), x), self.dic)
    v.sort()
    return v

  def SortDP(self, w):
    v = map(lambda x: (-self.Dot(w, x), x), self.dic)
    v.sort()
    return v

  def SortDist(self, w):
    v = map(lambda x: (self.Dist(w, x), x), self.dic)
    v.sort()
    return v

  # For all entities sort all other entities by cosine sim, then use p as cutoff similarity for the returned vector
  # Currently this is done super naively.  
  def SortCosineAll(self, p):
    res = {}
    logging.info('Running naive nearest neighbors for all ingredients using word2vec...')
    for x in self.dic:
      tmp = self.SortCosine(x)
      # Find cutoff - super naive (should do binary search)
      for j in range(len(tmp)):
        if tmp[j][0] > -p: break
        j = j + 1   
      if j>1: res[x] = tmp[1:j]    
    logging.info('Done.')
    return res


  # Given a list of ingredients, a target ingredients and a substitution for the target, compute
  # the similarity of the substitute vector to the average of the ingredients in the context ingredient list
  # (excluding the target from the context)
  def ContextSimilarity(self, context, target, sub, normalized = True):
    if not sub in self.dic: return 0.0
    context = filter(lambda x: x in self.dic_cv and x != target, context)
    if not len(context): return 0.0
    context_vec = sum(map(lambda x: self.dic_cv[x], context)) / (1.0*len(context))
    if np.dot(context_vec, context_vec) < EPS: return 0.0
    if normalized: return dp_normalized(context_vec, self.dic[sub])
    return dp(context_vec, self.dic[sub])

      

  def SortTripleDist(self, context, word, alpha, beta, gamma, delta):
    v = []
    for k in self.dic:
      context_score = 0.0
      for c in context: context_score = context_score + self.Dot_CV(c, k)
      cosine_score = self.CosineSimilarity(word, k)
      negative_context_score = self.Dot_CV(word, k)
      negative_cosine_score = 0.0
      for c in context: negative_cosine_score = negative_cosine_score + self.Dot(c, k)

      v.append((
        -alpha*cosine_score - beta * context_score + gamma * negative_context_score + delta * negative_cosine_score, k))
    v.sort()
    return v

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("--vn", "--vecname", dest="vecname", help="Name of vectors, default-'word2vec'", default = 'word2vec', metavar="VECNAME")
  parser.add_option("--cvn", "--cvecnamename", dest="cvecname", help="Name of context vectors, default='word2vec'", default = 'word2vec', metavar="CVECNAME")
  parser.add_option("-w", "--word", dest="word", help="Target word", default='', metavar="WORD")
  parser.add_option("-k", "--k", dest="k", help="How many words to show in sorted list", default='10', metavar="k")
  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()
  w2vd = Word2VecDistance(rdb=rdb, vecname=options.vecname, cvecname=options.cvecname)

  word = options.word
  if not word in options.word:
    print 'Word %s not found.' % word
    sys.exit(-1)

  print 'sqr-norm = ', w2vd.Dot(word, word)
  v = w2vd.SortCosine(word)
  for i in range(eval(options.k)):
    print v[i]


