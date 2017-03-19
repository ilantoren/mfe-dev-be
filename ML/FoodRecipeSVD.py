#
# This program reads the recipes from the database, and creates a sparse matrix from the ingredient-recipe incidences.
# It computs the svd for the matrix and writes the first k eigenvectors (both sides) to a file.
#

import json
import random
import sys
import re
import numpy as np
from scipy import linalg
from scipy.sparse import csr_matrix
from scipy.sparse.linalg import svds
from scipy.linalg import svd
from scipy.linalg  import norm
import matplotlib.pyplot as plt
from IOTools import *
import math

FILENAME = 'data/FoodRecipeSVD.json'
LIMIT = 100000
ROUND=6
K=100
EPS=0.0000001
def cosine(x,y):
  norm1 = np.sqrt(np.dot(x,x))
  norm2 = np.sqrt(np.dot(y,y))
  if norm1 == 0 or norm2 == 0:
   return 0
  return np.dot(x,y)/(norm1 * norm2)

class FoodRecipeSVD:
  def __init__(self, ):    
    print 'Connecting to MongoDB'
    self.rdb = RecipeDB()
    self.entitymap_dic = self.rdb.get_entitymap_dic()

  def computeForIngredientRecipe(self, limit = LIMIT, data_qry={}, k=K):
    print 'Loading recipe matching data_qry...'
    cursor = self.rdb.find(data_qry)
    num = 0
    rows = []
    cols = []
    self.recipe_ids = []
    for doc in cursor:
      id_ = '%s' % doc['_id']
      ingredients = []
      for s in doc["steps"]:
         ings = []
         for l in s["lines"]:
           if not "cannonical" in l:
             continue
           if l["quantity"] != "0": 
             cann = rdb.cannonicalize_entity(l["cannonical"])
             if not cann in self.entitymap_dic: continue
             ingredients.append(cann)

      food_indices = []
      for ingredient in ingredients:
        index = self.entitymap_dic[ingredient]["index"]
        food_indices.append(index)
      self.recipe_ids.append(id_)
      rows = rows + ([num] * len(food_indices))
      cols = cols + food_indices
      num = num + 1
      sys.stdout.write('\rsuccessful recipes: %d' % num)
      sys.stdout.flush()
      if (limit > 0 and num == limit):
        break
    data = np.array([1.] * len(rows))
    print "num=", num
    print "len entitymap_dic = ", len(self.entitymap_dic)
    mat = csr_matrix((data, (np.array(rows), np.array(cols))), shape=(num, len(self.entitymap_dic)))
    (self.u, self.s, self.vt) = svds(mat, k)  
    mat_norm = np.sqrt(len(rows))
    s_norm = norm(self.s)  
    self.sqr_error = (mat_norm * mat_norm - s_norm * s_norm)/(mat_norm * mat_norm)


  def computeForIngredientIngredient(self, limit=LIMIT, data_qry={}, k=K):
    cursor = self.rdb.find(data_qry)
    num = 0
    rows = []
    cols = []
    self.recipe_ids = []
    self.mat = np.zeros((len(self.entitymap_dic), len(self.entitymap_dic)))
    for doc in cursor:
      ingredients = []
      for s in doc["steps"]:
         ings = []
         for l in s["lines"]:
           if not "cannonical" in l:
             continue
           if l["quantity"] != "0": 
             cann = self.rdb.cannonicalize_entity(l["cannonical"])
             if not cann in self.entitymap_dic: continue
             ingredients.append(cann)

      food_indices = []
      for ingredient in ingredients:
        index = self.entitymap_dic[ingredient]["index"]
        food_indices.append(index)
      for i in range(len(food_indices)):
        for j in range(len(food_indices)):
          self.mat[food_indices[i],food_indices[j]] = self.mat[food_indices[i],food_indices[j]]+1
      num = num + 1
      sys.stdout.write('\rsuccessful recipes: %d' % num)
      sys.stdout.flush()
      if (limit > 0 and num == limit):
        break
    for i in range(len(self.entitymap_dic)):
      if self.mat[i,i] < EPS: continue
      for j in range(len(self.entitymap_dic)):
        if i==j: continue
        self.mat[i,j] = self.mat[i,j] / self.mat[i,i]
    for i in range(len(self.entitymap_dic)):
      self.mat[i,i] = 0
    #data = np.array([1.] * len(rows))
    #print "num=", num
    #print "len entitymap_dic = ", len(self.entitymap_dic)
    #(self.u, self.s, self.vt) = svd(self.mat)  
    #mat_norm = np.sqrt(len(rows))
    #s_norm = norm(self.s)  
    #self.sqr_error = (mat_norm * mat_norm - s_norm * s_norm)/(mat_norm * mat_norm)


  def writefile(self, filename=FILENAME):
    out_struct = {}
    #out_struct['singular_values'] = list(self.s)
    #out_struct['recipe_vectors'] = {}
    out_struct['food_vectors'] = {}
    for e in self.entitymap_dic:
      entity = self.entitymap_dic[e]
      word = entity["name"]
      out_struct['food_vectors'][word] = map(lambda x: round(x,ROUND), list(self.mat[entity["index"],:]))
    out_struct = json.dumps(out_struct, indent=2)
    f = file(filename, 'w')
    f.write(out_struct)
    f.close()


if __name__ == "__main__":
  if len(sys.argv) == 1:
    frsvd = FoodRecipeSVD()
    frsvd.computeForIngredientIngredient()
    print 'Writing to file..'
    frsvd.writefile()
  else: # use previous run to compute closest neighbors
    target = sys.argv[1]
    json_data=open(FILENAME, "r").read()
    data = json.loads(json_data)
    res = []
    for food in data["food_vectors"]:
      res.append((-cosine(data["food_vectors"][food], data["food_vectors"][target]), food))
    res.sort()
    print res[:30]

