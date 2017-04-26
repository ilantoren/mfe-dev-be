#
# Recipe tagging

#

import sys
import logging
import numpy as np
import re
import random
import IOTools
from sets import Set
from optparse import OptionParser
from bson.objectid import ObjectId


class RecipeTagger_V1:
  
  def __init__(self, recipe, debug=False):
    self.r = recipe
    self.debug = debug

  # Mainly noodle dish
  def is_noodle(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    noodle_pos = r.find('noodle')
    if noodle_pos < 0: return False
    with_pos = max(r.find('with'), r.find('w/'), r.find('over'))
    if with_pos >= 0 and with_pos < noodle_pos: #don't want "xxx with noodles"
      return False
    stir_fry_pos = re.search('stir?fr', r, re.IGNORECASE)
    if stir_fry_pos: return True
    pad_thai_pos = re.search('pad?thai', r, re.IGNORECASE)
    if pad_thai_pos: return True
    lo_mein_pos = re.search('lo?mein', r, re.IGNORECASE)
    if lo_mein_pos: return True
    forbidden = re.search('(salad)|(cake)|(soup)|(popover)|(stuff)|(pancake)|(bake)|(tart)', r, re.IGNORECASE)
    if forbidden: return False
    return True

  # Mainly rice dish
  def is_rice(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    rice_pos = r.find('rice')
    if rice_pos < 0: return False
    with_pos = max(r.find('with'), r.find('w/'), r.find('over'))
    if with_pos >= 0 and with_pos < rice_pos: #don't want "xxx with rice"
      return False 
    stir_fry_pos = re.search('stir?fr', r, re.IGNORECASE)
    if stir_fry_pos: return True
    fried_rice_pos = re.search('(fried)|(fry) rice', r, re.IGNORECASE)
    if fried_rice_pos: return True
    forbidden = re.search('(pudding)|(sweets)|(krispie)|(krispy)|(dessert)|(crispie)|(crispy)|(salad)|(soup)|(chili)|(balls)|(cake)|(rice stuffed)|(rice noodle)|(tart)', r, re.IGNORECASE)
    if forbidden: return False
    return True

  # a pasta dish
  def is_pasta(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    pasta_pos = re.search('(pasta)|(macaroni)|(maccaroni)|(farfalle)|(fettucine)|(fusilli)|(gemelli)|(linguine)|(orzo)|(penne)|(rigatoni)|(spaghetti)|(tagliatelle)|(tortellini)|(vermicelli)|(ziti)', r)
    if pasta_pos: return True
    return False

  def is_quinoa(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    quinoa_pos = re.search('(quinoa)', r)
    if quinoa_pos: return True
    return False

  def is_couscous(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    couscous_pos = re.search('(couscous)', r)
    if couscous_pos: return True
    return False

  def is_burgul(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    burgul_pos = re.search('(burgul)', r)
    if burgul_pos: return True
    return False


    
  def is_gratin(self):
    return False

  def is_french_fries(self):
    return False
  
  def is_grated_potatoes(self):
    return False

  def is_gratin(self):
    return False
  # Mainly potato dish, not mashed/quiche etc
  def is_potato(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    potato_pos = r.find('potato')
    if potato_pos < 0: return False
    with_pos = max(r.find('with'), r.find('w/'))
    if with_pos >= 0 and with_pos < potato_pos: #don't want "xxx with potato"
      return False 
    forbidden = re.search('(soup)|(pudding)|(salad)|(skin)|(mash)|(chip)|(pizza)|(gratin)|(quiche)|(muffin)|(pie)|(scramble)|(hash)|(cake)|(potato stuffed)|(and?potato)|(tart)|(fritatta)|(puree)|(roll)', r, re.IGNORECASE)
    if forbidden: return False
    return True
 
  def is_carb_side(self):
    return self.is_noodle() | self.is_rice() | self.is_potato() | self.is_pasta() | self.is_quinoa() | self.is_couscous() | self.is_burgul()

  def is_cornbread(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    pos = r.find('cornbread')
    if pos < 0: return False
    return True

  def is_gingerbread(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    pos = r.find('gingerbread')
    if pos < 0: return False
    return True

  def is_shortbread(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    pos = r.find('shortbread')
    if pos < 0: return False
    return True

  def is_flatbread(self):
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    pos = r.find('flatbread')
    if pos < 0: return False
    return True

  def is_bread(self):
    if self.is_cornbread() or self.is_gingerbread() or self.is_shortbread() or self.is_flatbread(): return False
    if not 'title' in self.r: return False
    r = self.r['title'].lower()
    if r.find('bread') < 0: return False
    if r.find('pudding')>=0: return False
    return True

  def is_muffin(self):
    if not 'title' in self.r: return False
    t = self.r['title'].lower()
    return t.find('muffin') >= 0

  def is_cake(self):
    if not 'title' in self.r: return False
    t = self.r['title'].lower()
    return re.search('\scake', t) is not None

  def is_general_baking(self):
    return self.is_cake() or self.is_muffin() or self.is_bread() or self.is_flatbread() or self.is_shortbread() or self.is_cornbread() or self.is_gingerbread()


  def is_yeast_bread(self):
    return False # currently only few recipes have "yeast" in title, so probably not important yet

  def is_coated_meat_fish_chicken(self):
    return False # not sure how to discover this yet

  def is_meatballs(self):
    if not 'title' in self.r: return False
    t = self.r['title'].lower()
    return re.search('meatballs', t) is not None

  def is_soup(self):
    if not 'title' in self.r: return False
    t = self.r['title'].lower()
    if self.debug: print '  title.lower()=%s'% t
    return re.search('soup', t) is not None

  def is_salad(self):
    if not 'title' in self.r: return False
    # no sandwich, and salad must appear without 'dressing'
    t = r['title'].lower()
    t = re.sub('salad\s+dressing', '', t)
    if re.search('sandwich', t) is not None: return False
    if re.search('(salad)|(tabb?o?ull?e)', t) is not None: return True
    return False
 
  def is_frittata(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('fritt?att?a', t) is not None: return True
    return False

  def is_quiche(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if t.find('quiche')>=0: return True
    return False

  def is_latkes(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if t.find('latkes')>=0: return True
    return False


  def is_tabouli(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('tabo?ull?[ie]', t) is not None: return True
    if re.search('bo?u[lr]gh?(u|a|ou)[rl]\s+salad', t) is not None: return True
    return False

  def is_sauteed(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('saute', t) is not None: return True
    return False

  def is_cooked(self):
    inst = rdb.getInstructionsString(self.r).lower()
    if re.search('(cook|preheat|heat|boil|broil|grill|saute|bake|fry|simmer)', inst) is not None: return True
    return False
    
  def is_vegetable_fritter(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('fritter', t) is not None and re.search('(vegetable)|(zucchini)|(corn)|(potato)', t) is not None:
      return True
    return False

  def is_patties(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('patty', t) is not None or re.search('patties', t) is not None:
      return True
    return False

  def is_casserole(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('casserole', t) is not None:
      return True
    return False

  def is_pancake(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('pancake', t) is not None:
      return True
    return False

  def is_cutlet(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('cutlet', t) is not None:
      return True
    return False

  def is_smoothie(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('smoothie', t) is not None:
      return True
   return False

  def is_dip(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('(^|\s)dips?(\W|$)', t) is not None:
      return True
    return False

  def is_mashed(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('mashed', t) is not None:
      return True
    return False

  def is_lasagna(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('lasagna', t) is not None:
      return True
    return False

  def is_burger(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('burger', t) is not None:
      return True
    return False

  def is_meatloaf(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('meatloaf', t) is not None:
      return True
    return False


  def is_pudding(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('pudding', t) is not None:
      return True
    return False

  def is_custard(self):
    if not 'title' in self.r: return False
    t = r['title'].lower()
    if re.search('custard', t) is not None:
      return True
    return False


  def getAllTags(self):
    res = []
    if not 'title' in self.r: return res
    tags = [ 
      ('is_noodle', self.is_noodle),
      ('is_rice', self.is_rice),
      ('is_pasta', self.is_pasta),
      ('is_quinoa', self.is_quinoa),
      ('is_couscous', self.is_couscous),
      ('is_burgul', self.is_burgul),
      ('is_potato', self.is_potato),
      ('is_gratin', self.is_gratin),
      ('is_mashed_potatoes', self.is_mashed_potatoes),
      ('is_gratin', self.is_gratin),
      ('is_bread', self.is_bread),
      ('is_cornbread', self.is_cornbread),
      ('is_shortbread', self.is_shortbread),
      ('is_gingerbread', self.is_gingerbread),
      ('is_flatbread', self.is_flatbread),
      ('is_muffin', self.is_muffin),
      ('is_cake', self.is_cake),
      ('is_general_baking', self.is_general_baking),
      ('is_yeast_bread', self.is_yeast_bread),
      ('is_coated_meat_fish_chicken', self.is_coated_meat_fish_chicken),
      ('is_meatballs', self.is_meatballs),
      ('is_soup', self.is_soup),
      ('is_salad', self.is_salad),
      ('is_frittata', self.is_frittata),
      ('is_quiche', self.is_quiche),
      ('is_latkes', self.is_latkes),
      ('is_tabouli', self.is_tabouli),
      ('is_sauteed', self.is_sauteed),
      ('is_cooked', self.is_cooked),
      ('is_vegetable_fritter', self.is_vegetable_fritter),
      ('is_patties', self.is_patties),
      ('is_casserole', self.is_casserole),
      ('is_pancake', self.is_pancake),
      ('is_cutlet', self.is_cutlet),
      ('is_smoothie', self.is_smoothie),
      ('is_dip', self.is_dip),
      ('is_mashed', self.is_mashed),
      ('is_lasagna', self.is_lasagna), 
      ('is_burger', self.is_burger),
      ('is_meatloaf', self.is_meatloaf),
      ('is_pudding', self.is_pudding),
      ('is_custard', self.is_custard)]

    for t in tags:
      if self.debug: print 'Tag=%s'% t[0]
      if t[1]():
        if self.debug: print '  Yes'
        res.append(t[0])
      else:
        if self.debug: print '  No'
    return res

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("--id", "--recipe_id", dest="recipe_id", help="If you want to run on only one recipe.  Default is empty string, which means run on all.", default="", metavar="RECIPE_ID")
  parser.add_option("--debug", "--debug", dest="debug", help="Debug mode, default False", default="False", metavar="DEBUG")
  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()

  query = {}
  if options.recipe_id != '': query = {"_id" : ObjectId(options.recipe_id)}
  recipes = rdb.find(query=query, projection={'title':1})
  c = 0
  for r in recipes:
    if eval(options.debug): print '**************************',r
    rt = RecipeTagger_V1(r, debug=eval(options.debug))
    tags = rt.getAllTags()
    tags_json = []
    for t in tags:
      tags_json.append({"name" : t, "probability" : "1.0", "origin" : "AlgorithmDec2016", "version" : "1.1"}) 
    rdb.bulkUpdateOne(r["_id"], 
        {"tags" : tags_json})
    sys.stdout.write('\r%d' %c)
    c = c + 1
  rdb.bulkFlush()
  print
