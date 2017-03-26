
import IOTools
from sets import *
import numpy as np
import sys
from sklearn import svm
import math


# Given a numpy vector, extend it to index+1 coordinates if it has fewer, otherwise do nothing
def possiblyExtendVector(v, index):
  if len(v) < index+1:
    v.extend([0.0] * (index+1-len(v)))


def getTotalError(clf, X, Y):
  Y_ = clf.predict(X)
  return math.pow(np.linalg.norm(Y-Y_), 2)

def getFold(X,Y, fold, num_folds):
  n = len(Y)
  fold_size = (n*1.0)/num_folds
  X_train = []
  Y_train = []
  for i in range(num_folds):
    if i==fold: continue
    X_train = X_train + X[int(fold_size * i) : min(int(fold_size*(i+1)), n)]
    Y_train = Y_train + Y[int(fold_size * i) : min(int(fold_size*(i+1)), n)]
  X_val = X[int(fold_size * fold) : min(int(fold_size*(fold+1)), n)]
  Y_val = Y[int(fold_size * fold) : min(int(fold_size*(fold+1)), n)]
  return X_train, Y_train, X_val, Y_val

def randomPermuteData(X,Y):
  n = len(X)
  X_ = []
  Y_ = []
  rp = np.random.permutation(n)
  for i in range(n):
    X_.append(X[rp[i]])
    Y_.append(Y[rp[i]])
  return X_, Y_

def splitTrainValidate(X, Y, validation_ratio):
  n = len(X)
  rp = np.random.permutation(n)
  n_train = int((1-validation_ratio) * n)
  X_train = []
  Y_train = []
  X_val   = []
  Y_val   = []
  for i in range(n_train):
    X_train.append(X[rp[i]])
    Y_train.append(Y[rp[i]])
  for i in range(n_train, n):
    X_val.append(X[rp[i]])
    Y_val.append(Y[rp[i]])
  return X_train, Y_train, X_val, Y_val

#######################################################################
# Computes a feature vector for a recipe.
# The feature vector consists of two parts:
# - A word2vec-like vector, which is averaged over all ingredients.
# - A tag-based vector
########################################################################
def getFeatureVector(rdb, recipe, entityVecType = 'word2vec'):
  ings, original_ings = IOTools.getCannonicalIngredientList(recipe)
  if not len(ings): return None
  first = True
  count = 0
  res = None
  for ing in ings:
    v = rdb.getEntityVec(ing, entityVecType)
    if v is None: continue
    if (first):   
      res = np.array(v)
      count = 1
      first = False
    else:       
      res = res + np.array(v)
      count = count + 1
  if res is None: return None
  res = res / count
  return res

def getTagVector(recipe, tag2index):
  res = []
  if not 'tags' in recipe: return res
  for tag in recipe['tags']:
    tagname = tag['name']
    if tagname in tag2index:
      index = tag2index[tagname]
    else:
      index = len(tag2index)
      tag2index[tagname] = index
    possiblyExtendVector(res, index)
    if 'probability' in tag: 
      res[index] = float(tag['probability'])
    else:
      res[index] = 1.0 if tag['value'].lower() == 'true' else 0.0
  return res

class SupervisedML:

  # Initializing this class requires labeled examples

  def __init__(self, rdb, examples, labels, classifiers, num_folds=5):
    self.examples = examples
    self.labels = labels
    self.rdb = rdb
    self.tag2index = {}
    X = []
    Y = []
    max_dim = 0
    for i in range(len(examples)):
      r = examples[i] 
      l = labels[i]
      v = getFeatureVector(rdb, r) # as np.array
      vtag = getTagVector(r, self.tag2index)   # as list
      if v is None: continue
      max_dim = max(max_dim, len(vtag)+len(v))
      X.append(v.tolist() + vtag)
      Y.append(labels[i])
    for v in X:
      possiblyExtendVector(v, max_dim-1)
    self.dim = max_dim
    min_err = 1.5
    best_classifier = None
    X_, Y_ = randomPermuteData(X,Y)
    for classifier in classifiers:
      val_err = 0
      for fold in range(num_folds):
        X_train, Y_train, X_val, Y_val = getFold(X, Y, fold, num_folds)
        classifier.fit(X_train, Y_train)
        val_err = val_err + ((getTotalError(classifier, X_val, Y_val)*1.0) / len(Y_val))
      val_err = val_err / num_folds
      if val_err < min_err:
        min_err = val_err
        self.best_classifier = classifier
    
    self.best_classifier.fit(X, Y)
    print 'Validation error: %f' % (min_err)
    print 'Training error: %f' % ((getTotalError(self.best_classifier, X_val, Y_val)*1.0)/len(X))
    

  def predict(self, recipe):
    v = getFeatureVector(self.rdb, recipe)
    vtag = getTagVector(recipe, self.tag2index)
    v = np.append(v, vtag)[:self.dim]
    return self.best_classifier.predict_proba([v])[0][1]  # return probability of class "1"



# Debug main:

def isRice(ing):
  return ing.lower() in ['rice', 'white rice', 'brown rice', 'jasmine rice']

if __name__ == '__main__':
  rdb = IOTools.RecipeDB(argv = sys.argv)

  recipes = rdb.findFeedback(query={'taskId' : 'legacy:rice substitutions'})
  ids = []
  labels = []
  for r in recipes:
    recipeId = r['recipeId']
    for f in r['feedback']:
      if f['info']['target'] == 'grated cauliflower':
        labels.append((f['info']['polarity']+1)/2)
        ids.append(recipeId)
  recipes = rdb.find(query={'_id' : {'$in' : ids}})
  recipe_by_id = {}
  for r in recipes:
    recipe_by_id[r['_id']] = r
  examples = map(lambda x: recipe_by_id[x], ids)
  classifiers = [svm.SVC(C=0.1, probability = True), svm.SVC(C=1, probability = True), svm.SVC(C=5, probability = True), svm.SVC(C=10, probability = True), svm.SVC(C=20, probability = True), svm.SVC(C=50, probability = True)]
  sml = SupervisedML(rdb, examples, labels, classifiers)
  recipes = rdb.find(query={})
  for r in recipes:
    ings, original_ings = IOTools.getCannonicalIngredientList(r)
    found = False
    for ing in ings:
      if isRice(ing.lower()):
        found = True
        break
    if not found: continue
    print r['_id'], sml.predict(r), r['title'] if 'title' in r else ''

#  print data


    
