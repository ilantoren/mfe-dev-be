#
# Provides various techniques for computing similarity between recipes.
#

import sys
from IOTools import *
import re
import json
import math
from bson.objectid import ObjectId
from sets import Set

ForbiddenWords = Set(['are', 'the', 'then', 'a', 'to', 'is', 'do', 'and', 'an', 'EOR', 'EOI', 'of', 'on', 'in', 'with'])

def CosineSim(x,y):
  if np.dot(x,x)<0.000001 or np.dot(y,y) < 0.000001: return 0
  return np.dot(x,y)/math.sqrt(np.dot(x,x) * np.dot(y,y))

class RecipeSimilarity():
   
  def InitWord2VecSum(self, params):
    f = open(params['wv_filename'], 'rt')
    self.pivot = ''
    self.entities_only = 0
    if 'pivot' in params:
      self.pivot = params['pivot']
      self.window = params['window']
      self.base = params['base']
    if 'entities_only' in params:
      self.entities_only = params['entities_only']

    self.wv, self.dim = ReadWord2VecOutputFile(f)
    f.close()
    print self.dim

  def __init__(self, args = {}):
    self.method = args['method']
    if self.method == 'word2vec_sum':
      self.InitWord2VecSum(args['params'])
      return
    assert 0==1

  def SumEntityVecs(self, ev, mask):
    out = np.array([0] * self.dim)
    i = 0
    for e in ev:
      if e in self.wv and (not e in ForbiddenWords) and ((not self.entities_only) or e[0] == '|'):
        out = out + mask[i] * self.wv[e]
      i = i + 1
    return out

  # given a word vector (from a text), compute a mask that assigns a higher weight to words
  # that are close to the pivot word
  def ComputeMask(self, word_vec):    
    mask = [0.0] * len(word_vec)
    for i in range(len(word_vec)):
      if word_vec[i] == self.pivot:
        for j in range(max(i-self.window,0), min(i+self.window+1, len(word_vec))):
          mask[j] = max(mask[j], math.pow(self.base, abs(j-i)))
          if j==i: mask[j] = 0.0  # ignore the pivot word, of course
    return mask
 
  def ComputeSim(self, txt1, txt2):
    if self.method == 'word2vec_sum':
      ev1 = StringToEntityVec(txt1)
      ev2 = StringToEntityVec(txt2)
      mask1 = [1.0] * len(ev1)
      mask2 = [1.0] * len(ev2)
      if self.pivot != '':
        mask1 = self.ComputeMask(ev1)
        mask2  =self.ComputeMask(ev2)
      rep1 = self.SumEntityVecs(ev1, mask = mask1)
      rep2 = self.SumEntityVecs(ev2, mask = mask2)
      return CosineSim(rep1, rep2) 


if __name__ == "__main__":
  rs = RecipeSimilarity({
    'method' : 'word2vec_sum', 
    'params' : {
      'wv_filename' : sys.argv[1], 'pivot' : sys.argv[3], 'window' : int(sys.argv[4]), 'base' : float(sys.argv[5]), 'entities_only' : int(sys.argv[6])}})

  outf = file(sys.argv[7], 'wt')
  rdb = RecipeDB(collection = 'instructionAnnotation')
  x = rdb.get(sys.argv[2])['replacedText']
  pivot = re.sub('\|', '\\\|', sys.argv[3])
  regx = re.compile(pivot)	
  Y = rdb.find({'replacedText' : regx})
  sims = []
  for y in Y:
    sims.append((rs.ComputeSim(x, y['replacedText']), y['replacedText']))
  sims.sort()
  for s in sims:
    outf.write('%f\n%s\n' % (s[0], s[1]))
  outf.close()
  


