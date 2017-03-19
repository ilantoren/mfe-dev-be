#
# This program reads a word-vector file (in text format), takes a word (which must appear in the word-vector file) as input,
# ranks the other words by cosine-similarity, and outputs
# the top k
#

import sys
from IOTools import *
from math import sqrt
from sets import Set
import RecipeClassification as rc
K = 30

def dp(v,u):
  res = 0.0
  for i in range(len(v)):
    res = res + v[i] * u[i]
  return res
  
def cosine_sim(v,u):
  return dp(v,u)/sqrt(dp(v,v) * dp(u,u))

class WordVecTools:
  def __init__(self, vec_filename, normalize = True):
    f  = open(vec_filename, 'r')
    line = f.readline().split(' ')
    vocab_size = int(line[0])
    self.vec_size = int(line[1])
   
    self.vecs = {}
    for line in f.readlines():
      words = line.split(' ')
      word = words[0]
      self.vecs[word] = []
      for i in range(self.vec_size):
        self.vecs[word].append(float(words[i+1]))
      nrm = sqrt(dp(self.vecs[word], self.vecs[word]))
      if nrm < 0.000001:
        del self.vecs[word]
        continue
      if normalize:
        for i in range(self.vec_size):
          self.vecs[word][i] = self.vecs[word][i] / nrm
  
    f.close()
    self.keys = Set(self.vecs.keys())

    
  def GetVec(self,word):
    return self.vecs[word] if (word in self.vecs) else None
  
  def GetVocabulary(self):
    return self.keys

  def GetVecSize(self):
    return self.vec_size

  def RankCosineSims(self, word, top):
    cosine_sims = []
    for w in self.vecs:
      cosine_sims.append((-cosine_sim(self.vecs[word], self.vecs[w]), w))

    cosine_sims.sort()
    return cosine_sims[:top]


