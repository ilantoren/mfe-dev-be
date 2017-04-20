#-*- coding: utf-8 *-*
#
# This program collects basic statistics on ingredient, word and quantities distribution in the recipe database.
#

import IOTools
from sets import *
import numpy as np
import sys
import math
from optparse import OptionParser
from bson.objectid import ObjectId
import re
import pprint
import enchant # PyEnchant - used for checking that words are actually English words
	
EPS=0.0001
INFINITY = 9999999999

INGREDIENT_FREQUENCY = 'ingredient_frequency'
INGREDIENT_BIGRAM_FREQUENCY = 'ingredient_bigram_frequency'
INGREDIENT_AV_GRAM_RATIO = 'ingredient_av_gram_ratio'
INGREDIENT_VAR_GRAM_RATIO = 'ingredient_var_gram_ratio'
GENERAL_WORD_FREQUENCIES_TOP5000 = 'general_word_frequencies_top5000'
INSTRUCTIONS_WORD_FREQUENCIES = 'instructions_word_frequencies'

def dict_count(dic, key, value=1):
  if not key in dic:
    dic[key] = value
  else:
    dic[key] = dic[key]+value


def writeIngredientListStats(rdb):
  freqs = {}
  av_gram = {}
  var_gram = {}
  tot = {}
  recipes = rdb.find(query={})
  for r in recipes:
    ings, original_ings, grams = rdb.getCannonicalIngredientList(r, with_gram = True)
    tot_grams = sum(grams)
    if tot_grams < EPS: continue
    for i in range(len(ings)):
      ing = ings[i]
      gram_ratio = grams[i] / tot_grams
      dict_count(freqs, ing)
      dict_count(av_gram, ing, gram_ratio)
      dict_count(var_gram, ing, gram_ratio * gram_ratio)
  for ent in freqs:
    av_gram[ent] = av_gram[ent] / (1.0*freqs[ent]) # compute average
    var_gram[ent] = var_gram[ent] / (1.0*freqs[ent]) - av_gram[ent] * av_gram[ent]
  rdb.writeStats(
    name=INGREDIENT_FREQUENCY, 
    description='Frequency of (cannonical) ingredients in ingredient list', 
    table=freqs) 
  rdb.writeStats(
    name=INGREDIENT_AV_GRAM_RATIO,
    description='Average ratio of (cannonical) ingredients quantity in recipe, conditioned on ingredient in recipe', 
    table=av_gram) 
  rdb.writeStats(
    name=INGREDIENT_VAR_GRAM_RATIO, 
    description='Variance of ratio of (cannonical) ingredients quantity in recipe, conditioned on ingredient in recipe', 
    table=var_gram) 

def writeIngredientListBigram(rdb):
  freqs = {}
  recipes = rdb.findRecipes()
  for recipe in recipes:
    ings = recipe.getIngs()
    cache = Set([])
    for i in range(len(ings)):
      for j in range(i+1, len(ings)):
        ing1 = ings[i]
        ing2 = ings[j]
        cann1 = ing1["cannonical"]
        cann2 = ing2["cannonical"]
        if cann1 is None or cann2 is None: continue
        if (cann1, cann2) in cache or (cann2, cann1) in cache: continue
        dict_count(freqs, (cann1, cann2))
        cache.add((cann1, cann2))
  print 'Size of bigram document ', len(freqs.__str__()), ' bytes'
  rdb.writeStats(
    name=INGREDIENT_BIGRAM_FREQUENCY,
    description = 'Frequency of pairs of ingredients',
    table=freqs)

def writeGeneralWordFrequenciesTop5000(rdb, file_, denominator):
  data = open(file_, "r").read()
  matches = re.findall(
    "<td align=\"center\">(?P<index>[0-9]+)</td>\s+<td>&nbsp;&nbsp;&nbsp;(?P<word>[a-zA-Z'\-/]+)</td>" + \
    "\s+<td align=\"center\">[a-z]</td>\s+<td align=\"center\">(?P<frequency>[0-9]+)</td>", 
    data)
  indices = map(lambda x:int(x[0]), matches)
  assert indices == range(1,5001)
  table = dict(map(lambda x:(x[1].lower(), float(x[2])/denominator), matches))
  rdb.writeStats(
    name=GENERAL_WORD_FREQUENCIES_TOP5000,
    description='Frequency of top 500 words in English from general text',
    table=table) 

  
def writeInstructionAnnotationWordFrequencies(rdb, min_freq=4, field="sourceText"):
  recipes = rdb.findRecipeAnnotation({})
  d = enchant.Dict("en_US")
  freqs = {}
  i = 0
  tot = 0.0
  for r in recipes:
    if not field in r: continue
    text = IOTools.tokenizeInstructionAnnotation(r[field])
    for token in text:
      if not d.check(token): continue # Not an English word
      token = token.lower()
      if re.search('[^a-z]', token) is not None: 
        if len(token) < 4: continue
        if token[0]!='{' or token[1]!='%' or token[-1]!='}' or token[-2]!='%':
          continue
      if not token in freqs:
        freqs[token] = 1
      else:
        freqs[token] = freqs[token] + 1
      tot = tot + 1.0
    i = i + 1
    sys.stdout.write('\r%d'% i)
  sys.stdout.write('\n')
  to_del = []
  for key in freqs:
    if freqs[key] < min_freq:
      to_del.append(key)
  for key in to_del: del freqs[key]
  tot = sum(freqs.values())+.0
  print 'tot = ', tot
  rdb.writeStats(
    name=INSTRUCTIONS_WORD_FREQUENCIES,
    description='Word frequency in recipe instructions',
    table=freqs) 
  

def sanityCheck(rdb):
  pp = pprint.PrettyPrinter()

  gen = rdb.getStats(GENERAL_WORD_FREQUENCIES_TOP5000)
  inst = rdb.getStats(INSTRUCTIONS_WORD_FREQUENCIES)
  output = []

  tot_inst = sum(inst.values())+.0

  for word in inst:
    if not word in gen:
      output.append((-INFINITY, inst[word], 0.0, word))
      continue
    output.append((-inst[word]/(tot_inst*gen[word]), inst[word], gen[word], word))
  output.sort()
  for i in range(len(output)):   
    print ("%d %f %s %f" % (i, output[i][1], IOTools.clean_str(output[i][3]), -output[i][0]))



if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("--wff", "--word_frequency_file", dest="wff", help="Location of word frequency file", default="data/word_frequency_5000.html", metavar="WORD_FREQUENCY_FILE")
  parser.add_option("--denom", "--denominator", dest="denominator", help="Denominator for converting frequencies to probabilities in file", default="450000000.0", metavar="DENOMINATOR")
  parser.add_option("--inst", "--instructions", dest="instructions", help="Obtain instructions annotation stats", default="True", metavar="INSTRUCTIONS")
  parser.add_option("--instmin", "--instructions_min", dest="instructions_min", help="Minimum number of times a word has to appear in corpus in order to be included in instructions stats", default="4", metavar="INSTRUCTIONS_MIN")
  parser.add_option("--instfield", "--instructions_field", dest="instructions_field", help="Which field in the instructions records to use for stats? Default: sourceText", default="sourceText", metavar="INSTRUCTIONS_FIELD")
  parser.add_option("--st", "--sanity_test", dest="sanity_test", help="Sanity test (after execution - will disable all other tasks)", default="False", metavar="SANITY_TEST")
  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()

  if eval(options.sanity_test):
    sanityCheck(rdb)
    sys.exit(0)

  if eval(options.instructions):
    print "Getting word statistics from instructions..."
    writeInstructionAnnotationWordFrequencies(rdb, min_freq = eval(options.instructions_min), field=options.instructions_field)
  print "Collecting ingredient statistics..."
  writeIngredientListStats(rdb)
  print "Collecting ingredient bigram statistics..."
  writeIngredientListBigram(rdb)
  print "Getting word statistics from general text (from file)..."
  writeGeneralWordFrequenciesTop5000(rdb, file_=options.wff, denominator = float(options.denominator))

  writeIngredientListBigram(rdb)
  


