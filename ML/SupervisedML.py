
import IOTools
from sets import *
import numpy as np
import sys
from sklearn import svm
import math
from optparse import OptionParser
from bson.objectid import ObjectId

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


class SupervisedML:

  # Initializing this class requires labeled examples

  def __init__(self, examples, labels, classifiers, num_folds=5):
    self.examples = examples
    self.labels = labels
    self.tag2index = {}
    X = []
    Y = []
    max_dim = 0
    for i in range(len(examples)):
      v = examples[i]
      if v is None: continue
      X.append(v)
      Y.append(labels[i])
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
    

  def predict(self, v):
    return self.best_classifier.predict_proba([v])[0][1]  # return probability of class "1"



# Debug main:

def isRice(ing):
  return ing.lower() in ['rice', 'white rice', 'brown rice', 'jasmine rice']

if __name__ == '__main__':
  option_parser = OptionParser()
  option_parser.add_option("--config", "--config", dest="config", help="Configure location of mongo", default="local.config")
  (options, args) = option_parser.parse_args()
  rdb = IOTools.RecipeDB(config_file = options.config)

  feedbacks = rdb.findFeedback(query={'taskId' : 'legacy:rice substitutions'})
  ids = []
  labels = []
  for feedback in feedbacks:
    recipe = list(rdb.findRecipes(query={'urn' : feedback['urn']}))
    if len(recipe)==0:
      print 'Couldnt find recipe for urn %s, skipping' % feedback['urn']
      continue
    if len(recipe) > 0:
      print 'Warning, multiple recipes with urn %s, taking 1st' % feedback['urn']
    recipe = recipe[0]
    for f in feedback['feedback']:
      if f['info']['target'] == 'grated cauliflower':
        labels.append((f['info']['polarity']+1)/2)
        ids.append(recipe.getId())
  
  recipe_vecs = rdb.getRecipeVecs('word2vec', {'recipeId' : {"$in" : ids}})
  print recipe_vecs

  examples = map(lambda x: recipe_vecs[x] if x in recipe_vecs else None, ids)
  zipped = zip(examples, labels)
  zipped = filter(lambda x: x[0] is not None, zipped)
  unzipped = zip(*zipped)
  examples = unzipped[0]
  labels = unzipped[1]

  print examples
  print labels
  print 'Found %d examples with recipevecs ' % len(examples)
  

  classifiers = [
    svm.SVC(C=0.1, probability = True), 
    svm.SVC(C=1, probability = True), 
    svm.SVC(C=5, probability = True), 
    svm.SVC(C=10, probability = True), 
    svm.SVC(C=20, probability = True), 
    svm.SVC(C=50, probability = True)]
  sml = SupervisedML(examples, labels, classifiers)
  recipes = rdb.findRecipes(query={})
  for r in recipes:
    ings = r.getIngs()
    canns = map(lambda x: x['cannonical'], ings)
    found = False
    for cann in canns:
      if cann is not None and isRice(cann.lower()):
        found = True
        break
    if not found: continue
    vec = rdb.getRecipeVecs('word2vec', {'recipeId' : r.getId()})
    if not len(vec): continue
    vec = vec[r.getId()]
    print r.getId(), sml.predict(vec), r.getTitle()

#  print data


    
