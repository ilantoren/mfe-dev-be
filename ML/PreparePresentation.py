import sys
from IOTools import *
import re
from sets import Set
import random
LIMIT = 9999999
import numpy as np
import scipy.linalg
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import math

for_display = Set(['tomato', 'cucumber', 'roma tomato', 'beef', 'turkey', 'chicken', 'pork', 'rice', 'burgul', 'pasta', 'orzo', 'risotto', 'wine', 'vinegar', 'olive oil', 'butter', 
'lemon', 'lime', 'lettuce', 'romaine lettue', 'provolone cheese', 'onion', 'fennel', 'salt', 'pepper', 'lentil', 'cauliflower', 'whiskey', 'vodka', 'oil', 'vinegar', 'sugar', 'peach', 'plum', 'potato', 'apple', 'peach', 'orange', 'grape', 'sweet potato', 'pasrsnip', 'walnut', 'pistachio', 'almond', 'pea', 'carrot', 'cabbage', 'swiss cheese', 'mozzarella cheese', 'bell pepper'])


def cosine(x,y):
  return np.dot(x,y)/(math.sqrt(np.dot(x,x)) * math.sqrt(np.dot(y,y)))

def get_nearest_cosine(x_ind, mat, k):
  n = (mat.shape)[0]
  cosines = []
  for i in range(n): cosines.append((-cosine(mat[x_ind,:], mat[i,:]), i))
  cosines.sort()
  return cosines[:k]

def visualize2d(mat, names, display_ind):
  names = np.array(names)
  U,S,Vh = scipy.linalg.svd(mat)
  fig = plt.figure()
  ax = fig.add_subplot(111, projection='3d')

  U1 = U[:,0]
  U2 = U[:,1]
  U3 = U[:,2]
  #ax.plot(U1[display_ind], U2[display_ind], 'ro')
  ax.scatter(U1[display_ind], U2[display_ind], U3[display_ind], 'ro')
  for i in range(len(display_ind)):
    ax.text(U1[display_ind[i]], U2[display_ind[i]], U3[display_ind[i]], names[display_ind[i]])
    #ax.annotate(names[display_ind[i]], xy=(U1[display_ind[i]], U2[display_ind[i]]))
  plt.show()

def PreparePresentation(fn = 'data/word2vec.ing.permute.out.txt', limit_recipe=200000, recipe_display_num=100, k=10):
  rdb = RecipeDB()
  entity_dic = rdb.get_entitymap_dic()
  entity_alt_dic = rdb.get_entitymap_alt_dic()

  all_entities = []
  all_vectors = []
  display_indices = []
  ing2ind = {}
  f = open(fn, 'rt')
  ings = []
  i = 0	
  for line in f.readlines():
    tokens = line.split(' ')
    if tokens[-1] == '\n': tokens = tokens[:-1]
    if len(tokens) == 2:
      dim = int(tokens[1])
      print 'Dim = %d'%dim
      continue
    ing = tokens[0]
    ing = undecorate_entity(ing)
    if ing in entity_alt_dic: 
      ing = entity_alt_dic[ing]
    if not ing in entity_dic: 
      print ing + ' not found in entity dic, ' + ing
      continue
    all_vectors.append(map(lambda x: float(x), tokens[1:]))
    all_entities.append(ing)
    ing2ind[ing] = i
    #if  'origin' in entity_dic[ing] and entity_dic[ing]['origin'] == 'MANUAL':
    if ing in for_display:
      display_indices.append(i)
    i = i + 1
    #if not ing in for_display: continue
  f.close()

  
  mat = np.array(all_vectors)
  num_ings = len(all_vectors)
  for i in range(num_ings):
    mat[i,:] = mat[i,:] / math.sqrt(np.dot(mat[i,:], mat[i,:]))

  visualize2d(mat, all_entities, display_indices)
  asdfsadf()
  for disp_ind in display_indices:
    closest = get_nearest_cosine(disp_ind, mat, k)
    print map(lambda x: (all_entities[x[1]], -int(x[0]*100)), closest)
  
  #asdfdsaf()
  i = 0
  all_recipes = []
  all_recipe_vecs = []
  for r in rdb.find():
    if not 'title' in r: continue
    ings, gram = getCannonicalIngredientList(r, with_gram = True)
    recipevec = np.array([0] * dim)
    tot_gram = 0
    for j in range(len(ings)):
      ing = ings[j]
      if not ing in ing2ind: continue
      ind = ing2ind[ing]
      recipevec = recipevec +  gram[j] * np.array(all_vectors[ind])
      tot_gram = tot_gram + gram[j]
    if tot_gram < 0.00001: continue
    all_recipe_vecs.append(recipevec / tot_gram)
    all_recipes.append(r['title'])
    i = i + 1
    if limit_recipe and i == limit_recipe: break
  to_display = []
  for k in range(recipe_display_num): to_display.append(random.randint(0, i-1))
  mat = np.array(all_recipe_vecs)
  #visualize2d(np.array(all_recipe_vecs), all_recipes, to_display)
  for disp_ind in to_display:
    closest = get_nearest_cosine(disp_ind, mat, k)
    print map(lambda x: (all_recipes[x[1]], -int(x[0]*100)), closest)

if __name__ == "__main__":
  PreparePresentation(fn=sys.argv[1] if len(sys.argv) > 1 else 'data/word2vec.ing.permute.out.txt')
