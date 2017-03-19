#-*- coding: utf-8 -*-

from sets import Set
import IOTools # for testing
from optparse import OptionParser
import re
import sys

class Recipe:
  def __init__(self, dic): # currently, only from a dictionary
    self.dic = dic
    self.ings = []
    self.attrs = {}
    self.id = self.dic['_id'].__str__()
    self.title = self.dic['title'] if 'title' in self.dic else 'Untitled'
    self.tags = {}
    if 'tags' in dic:
      for t in dic['tags']:
        self.tags[t["name"]] = t

    if not self.isValid():
      print 'Warning: %s invalid' % self.id
      return

    for s in self.dic['steps']:
      for l in s['lines']: 
        if not 'food' in l: l['food'] = None
        if not 'cannonical' in l: l['cannonical'] = None
        if not 'gram' in l: l['gram'] = None
        if not 'quantity' in l: 
          l['quantity'] = None  
        if not 'original' in l: l['original'] = ''      
        self.ings.append(l)

  def isValid(self):
    for s in self.dic['steps']:
      if type(s) is not dict: return False
      if not '_class' in self.dic or self.dic['_class'] != "com.mfe.model.RecipePOJO": 
        return False
    return True

  def setAttr(self, name, value):
    self.attrs[name] = value
  
  def getAttr(self, name):
    return self.attrs[name] if name in self.attrs else None
  
  def getIngs(self):
    return self.ings

  def getCannonicalIngNames(self):
    return map(lambda x: x['cannonical'], self.ings)

  def getId(self):
    return self.id

  def getTitle(self):
    return self.title

  def getTags(self):
    return self.dic['tags'] if 'tags' in self.dic else []

  def getTags(self):
    return self.tags

  def getTagProb(self, t):
    if not t in self.tags: return 0.0
    tag_info = self.tags[t]
    if 'probability' in tag_info: return float(tag_info['probability'])
    return 1.0 if tag_info['value'].lower() == 'true' else 0.0

if __name__ == '__main__':
  parser = OptionParser()
  parser.add_option("-x", "--x", dest="x", help="Id of recipe", default="", metavar="X")
  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()

  recipe = rdb.findOneRecipe(options.x)
  print recipe.title
  for ing in recipe.ings:
    print '  %s (%s) gram=%s original=%s' % (ing['food'], ing['cannonical'], ing['gram'], ing['original'])



