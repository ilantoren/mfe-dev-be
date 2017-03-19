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
from WordVecTools import *
import math
from sklearn import tree
from sklearn import svm
import numpy as np

eps = 0.001

pos = [
  ("rice", "quinoa"),
  ("rice", "couscous"),
  ("rice", "noodle"),
  ("rice", "brown rice"),
  ("rice", "soba"),
  ("tomato", "cherry tomato"),
  ("pecan", "almond"),
  ("pecan", "sliced almond"),
  ("pecan", "pistachio"),
  ("pecan", "silvered almond"),
  ("pecan", "macadamia nut"),
  ("pecan", "toasted pecan"),
  ("pecan", "hazelnut"),
  ("pecan", "cashew"),
  ("chicken", "turkey"),
  ("chicken", "chicken wing"),
  ("chicken", "pork"),
  ("chicken", "pork chop"),
  ("chicken", "duck"),
  ("beef", "lamb"),
  ("beef", "steak"),
  ("beef", "rib"),
  ("beef", "venison"),
  ("beef", "fillets"),
  ("beef", "tenderloins"),
  ("beef", "lamb shank"),
  ("raisin", "dried cranberry"),
  ("raisin", "currant"),
  ("raisin", "dried fruit"),
  ("raisin", "sultanas"),
  ("raisin", "dried cherry"),
  ("raisin", "cranberry"),
  ("raisin", "craisin"),
  ("feta", "mozzarella cheese"),
  ("feta", "parmesan cheese"),
  ("arugula", "watercress"),
  ("arugula", "salad green"),
  ("arugula", "radicchio"),
  ("arugula", "red cabbage"),
  ("arugula", "romaine lettuce"),
  ("arugula", "lettuce"),
  ("arugula", "pea shoot"),
  ("arugula", "spinach"),
  ("arugula", "mustard greens"),
  ("arugula", "escarole")]

neg = [
  ("rice", "pasta"),
  ("rice", "lentil"),
  ("rice", "sauerkraut"),
  ("rice", "gnocchi"),
  ("rice", "green bean"),
  ("rice", "bean"),
  ("rice", "macaroni"),
  ("rice", "maccaroni"),
  ("rice", "potato"),
  ("rice", "linguine"),
  ("tomato", "caper"),
  ("tomato", "olives"),
  ("tomato", "jalapeno"),
  ("tomato", "cucumber"),
  ("tomato", "black bean"),
  ("tomato", "bok choy"),
  ("tomato", "red onion"),
  ("tomato", "onion"),
  ("tomato", "snow pea"),
  ("tomato", "chile"),
  ("tomato", "tomato sauce"),
  ("tomato", "chili pepper"),
  ("tomato", "shrimps"),
  ("tomato", "kidney bean"),
  ("tomato", "bean sprouts"),
  ("tomato", "beet green"),
  ("tomato", "cauliflower"),
  ("tomato", "anchovy"),
  ("tomato", "green pepper"),
  (" walnut", "coconut"),
  ("walnut", "raisin"),
  ("walnut", "dried cranberry"),
  ("walnut", "shredded coconut"),
  ("walnut", "oat"),
  ("walnut", "pumpkin seed"),
  ("walnut", "potato chip"),
  ("walnut", "date"),
  ("walnut", "craisin"),
  ("walnut", "dried cherry"),
  ("walnut", "corn flake"),
  ("walnut", "cornflake"),
  ("walnut", "cranberr"),
  ("walnut", "cinnamon chip"),
  ("chicken", "salmon"),
  ("chicken", "oxtail"),
  ("chicken", "scallop"),
  ("chicken", "tenderloins"),
  ("chicken", "cabbage"),
  ("chicken", "bok choy"),
  ("chicken", "ground meat"),
  ("raisin", "walnut"),
  ("raisin", "grated carrot"),
  ("raisin", "nut"),
  ("raisin", "oat"),
  ("raisin", "corn flake"),
  ("raisin", "apricot"),
  ("raisin", "coconut"),
  ("raisin", "pistachio"),
  ("raisin", "potato chip"),
  ("raisin", "cashew"),
  ("feta", "arugula"),
  ("feta", "pesto"),
  ("feta", "parsley"),
  ("feta", "romaine lettuce"),
  ("feta", "roasted vegetable"),
  ("feta", "radicchio"),
  ("feta", "smoked salmon"),
  ("feta", " salad green"),
  ("feta", "pea shoot"),
  ("feta", "caper"),
  ("feta", "smoked fish"),
  ("feta", "watercress"),
  ("feta", "pimientos"),
  ("arugula", "feta"),
  ("arugula", "red onion"),
  ("arugula", "scallion onion"),
  ("arugula", "parsley"),
  ("arugula", "cilantro"),
  ("arugula", "yellow pepper"),
  ("arugula", "bean sprout"),
  ("arugula", "coleslaw"),
  ("arugula", " chives"),
  ("arugula", "pimientos"),
  ("arugula", "green onion"),
  ("arugula", "caper"),
  ("arugula", "scallion")]

def remove_pipe_dash(x):
  x = re.sub('\|', '', x)
  x = re.sub('-', ' ', x)
  return x


# Compute pairwise frequencies of a ground set of entities, over set of recipes.
def compute_pairwise_frequencies(rdb, entity_ground_set, recipe_id_filter_set=None):
  entity_ground_set = map(lambda x: rdb.cannonicalize_entity(x), entity_ground_set)
  n = len(entity_ground_set)
  res = np.zeros([n,n])
  entity_to_index = {}
  for i in range(n):  entity_to_index[entity_ground_set[i]] = i
  cursor = rdb.recipe_coll.find() 
  print 'number of recipes matching query: ', cursor.count()
  entity_ground_set = Set(entity_ground_set)
  count = 0
  for r in cursor:
    if recipe_id_filter_set and not r['_id'] in recipe_id_set: continue
    ings = []
    for s in r["steps"]:
      for l in s["lines"]:
        if not "cannonical" in l: 
          # print "No cannonical for " + l["original"]
          continue
        try:
          quantity = float(l["quantity"])
        except:
          continue
        if quantity == 0.0: # probably a word found in the instructions, not an actual ingredient
          continue
        if rdb.cannonicalize_entity(l["cannonical"]) in entity_ground_set: 
          ings.append(rdb.cannonicalize_entity(l["cannonical"]))
    m = len(ings)
    for i in range(m):
      for j in range(m):
        res[entity_to_index[ings[i]]][entity_to_index[ings[j]]] = res[entity_to_index[ings[i]]][entity_to_index[ings[j]]] + 1
    count = count + 1
  return res, count

def pair_feature_vector(ent1, usda1, ent2, usda2, wvt, food_groups, num_food_groups, entity_vec, pairwise_freq, pairwise_freq_total):
  group_vec1 = [0]*num_food_groups
  group_vec1[food_groups[usda1.attrib["foodGroup"]]['id']] = 1
  group_vec2 = [0]*num_food_groups
  group_vec2[food_groups[usda2.attrib["foodGroup"]]['id']] = 1
  res = group_vec1 + group_vec2
  res.append(int(food_groups[usda1.attrib["foodGroup"]] == food_groups[usda2.attrib["foodGroup"]]))
  
  v1 = wvt.GetVec(IOTools.decorate_entity(ent1))
  v2 = wvt.GetVec(IOTools.decorate_entity(ent2))
  res.append(cosine_sim(v1,v2))
  nutr1 = USDA.getNutrients(usda1)
  nutr2 = USDA.getNutrients(usda2)
  res.append(abs(nutr1['Water']- nutr2['Water']))
  res.append(((nutr1['Water']+eps)/(nutr2['Water']+eps)))
  res.append(abs(nutr1['Alcohol, ethyl'] - nutr2['Alcohol, ethyl']))
  res.append(((nutr1['Alcohol, ethyl']+eps) / (nutr2['Alcohol, ethyl']+eps)))
  res.append(abs(nutr1['Protein'] - nutr2['Protein']))
  res.append(((nutr1['Protein']+eps)/(nutr2['Protein']+eps)))

  # get pairwise frequency related features
  entity_to_index = {}
  i = 0
  for e in entity_vec:
    entity_to_index[e] = i
    i = i + 1
  index1 = entity_to_index[ent1]
  index2 = entity_to_index[ent2]
  p = (1.0 * pairwise_freq[index1][index1] + 1.0) / (pairwise_freq_total + 1.0);
  q = (pairwise_freq[index1][index2] * 1.0 + 1.0) / (pairwise_freq[index1][index1] + 1.0);
  res.append(p/q);
  res.append(math.log(p/q))
  return res

if __name__ == "__newmain__":
  print 'Loading USDA'
  usda = USDA()
  print 'Getting food group stats'
  foodGroupDict, numFoodGroups = usda.getFoodGroupStats()
  print 'Num groups = ', numFoodGroups
  print 'Connecting to recipe db'
  rdb = RecipeDB()
  wvt = WordVecTools(sys.argv[1])
  good_entity_set = dict()
  # Get list of entities on pos and neg examples
  for pair in pos+neg:
    for entity in pair:
      if entity in good_entity_set: continue
      usda_ent, origin = entity2USDAndb(rdb, usda, entity)
      if not usda_ent or origin == "AUTO": continue
      if not wvt.GetVec(IOTools.decorate_entity(entity)): continue
      good_entity_set[entity] = usda_ent

  print 'Computing pairwise frequencies'
  good_entity_vec = good_entity_set.keys()
  pairwise_freq, pairwise_freq_total = compute_pairwise_frequencies(rdb, good_entity_vec, None)

  pos = filter(lambda x: x[0] in good_entity_set and x[1] in good_entity_set, pos)
  neg = filter(lambda x: x[0] in good_entity_set and x[1] in good_entity_set, neg)
  print len(pos), len(neg)

  pos_vecs = [] 
  neg_vecs = []
  for p in pos:
    pos_vecs.append(pair_feature_vector(p[0], good_entity_set[p[0]], p[1], good_entity_set[p[1]], wvt, foodGroupDict, numFoodGroups, good_entity_vec, pairwise_freq, pairwise_freq_total))
  for n in neg:
    neg_vecs.append(pair_feature_vector(n[0], good_entity_set[n[0]], n[1], good_entity_set[n[1]], wvt, foodGroupDict, numFoodGroups, good_entity_vec, pairwise_freq, pairwise_freq_total))
    #print USDA.getNutrients(good_entity_set[p[0]]
  print pos_vecs
  print neg_vecs
  np.random.shuffle(pos_vecs)
  np.random.shuffle(neg_vecs)
  X = pos_vecs[:-10] + neg_vecs[:-10]
  Y = [1] * (len(pos_vecs)-10) + [0] * (len(neg_vecs) - 10)
  clf = svm.SVC(C=1, kernel = 'poly', degree=2)
  clf =  clf.fit(X, Y)
  print clf
#  print clf.coef_
  print len(pos_vecs), len(neg_vecs)
  print clf.predict(X)
  hatY  = clf.predict(pos_vecs[-10:]+ neg_vecs[-10:])
  print hatY
#######################################################################################################
####################################################################################################
if __name__ == "__main__":
  usda = USDA()
  rdb = RecipeDB()


  w = WordVecTools(sys.argv[1])
  ranked = w.RankCosineSims(sys.argv[2], K)
  for i in range(len(ranked)):
    ranked[i] = (-ranked[i][0], remove_pipe_dash(ranked[i][1]))
  entity_ground_set = []
  for e in ranked:
    entity_ground_set.append(e[1])

  recipes = rdb.find()
  #print 'Computing is_carb_side...'
  #carb_sides = Set()
  #for r in recipes:
  #  rt = rc.RecipeTagger(r)
  #  if rt.is_carb_side():
  #    carb_sides.add(r['_id'])
  #print 'Done.'
  #print 'Size of matching recipe set: ', len(carb_sides)

  freq, count = compute_pairwise_frequencies(rdb, entity_ground_set)
  print 'total count=', count
  j = 0
  for e in ranked:
    usda_item, origin = entity2USDAndb(rdb, usda, e[1])
    p = (freq[j][j])/(count+0.1)  # probability of entity
    q = (freq[0][j])/(0.01+freq[0][0]) # probability of entity given anchor

    # adjust statistical significance
    #p = (freq[j][j] +1- sqrt(count*p))/(count+.0)
    #q = (freq[0][j] +1+ sqrt(freq[0][0]*q))/(freq[0][0])
    sys.stdout.write('%s %f,%f (%d,%d)' %  (e, p, q, int(freq[j][j]), int(freq[0][j])))
    #print (e,  '%d/%d=%f  %d/%d=%f' % (freq[0][j], freq[0][0],  (freq[0][j])/(0.0+freq[0][0]), freq[j][j], count, (freq[j][j]+0.0)/(count+0.0)))
    if not usda_item:
      sys.stdout.write( 'No mapping to usda\n')
    else:
      sys.stdout.write('%s, %s, %s\n' % (origin, usda_item.attrib['desc'], usda_item.attrib['foodGroup'])) 
    j = j + 1
      


