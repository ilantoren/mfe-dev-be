#-*- coding: utf-8 -*-

import json
import re
from pprint import pprint
from sets import Set
from optparse import OptionParser
from IOTools import *
import sys
import urllib3
import pprint
import os
from optparse import OptionParser
import RDF

ROOT_URL = 'http://www.foodsubs.com/'
CACHE_FOLDER = 'data/foodsubs_cache/'
FOODSUBS_ORIGIN = 'foodsubs'

CATEGORY_TAG = 'category:'

SKIP = ["FGEquip.html", "Sweeten.html", "Eggs.html", "Fatsoils.html", "Herbs.html"]

condition_dict = {
  'For general baking': {"cond": "recipe_tag_prob('is_general_baking')>0.5", "notes":""},
  'For yeast breads': {"cond":"recipe_tag_prob('is_yeast_bread')>0.5", "notes":""},
  'For coating fish and meat before frying': {"cond":"recipe_tag_prob('is_coated_meat_fish_chicken')>0.5", "notes":""},
  'if cooked': {"cond": "prob_served_cooked > 0.8", "notes":""},
  'especially if served raw': {"cond": "prob_served_raw > 0.8", "notes":""},
  "don't serve raw": {"cond": "prob_served_raw < 0.2", "notes":""},
  "for hummus": {"cond":"recipe_tag_prob('is_hummus')>0.8", "notes":""},
  "for sauces":  {"cond": "prob_part_of_sauce > 0.8", "notes":""},
  "This works well in tabouli, but the berries need to be cooked first": {"cond": "recipe_tag_prob('is_tabouli')>0.8", "notes":"Berries need to be cooked first"},
  "\s*especially\s+in\s+tabouli\s*": {"cond": "recipe_tag_prob('is_tabouli')>0.8", "notes":""},
  "for cold dishes and salads": {"cond": "recipe_tag_prob('is_salad')>0.8", "notes":""},  # not exactly, but close enough for now (feb 28 2017)
  "in salads": {"cond": "recipe_tag_prob('is_salad')>0.8", "notes":""},
  "for sauteing": {"cond": "recipe_tag_prob('is_sauteed')>0.8", "notes":""},
  "for greasing pans": {"cond": "prob_for_greasing_pans > 0.8", "notes":""},
  "(in making bread)|(bread recipe)" : {"cond": "recipe_tag_prob('is_bread')>0.8", "notes":""},
  "bread" :  {"cond": "recipe_tag_prob('is_bread')>0.8", "notes":""},
  "consider blanching first if using in a raw salad" : {"cond": "recipe_tag_prob('is_salad')>0.8", "notes":"Consider blanching first"}
}
# List of all ingredients in WHEAT_FLOUR_LIST, with each ingredient
# appearing in list of synonyms
WHEAT_FLOUR_LIST = [
  ['wheat flour'], ['durum wheat flour'], ['semolina flour'], ['whole wheat flour'],
  ['graham flour'], ['bread flour'], ['pastry flour'], ['cake flour']]
# This is a special case of wheat flour, but has a whole separate set of subs
# in Flour.html
ALL_PURPOSE_FLOUR_LIST = [
  ['all purpose flour', 'flour', 'plain flour']]

PARSE_PROB_FILE = open("data/foodsubs.bad.txt", "w")


def fixOilPage(p):
  print re.search('walnut or almond or hazelnut oil \(for cold dishes and salads\)', p)
  p = re.sub('walnut or almond or hazelnut oil \(for cold dishes and salads\)', 
            'walnut oil (for cold dishes and salads) or almond oil (for cold dishes and salads) or hazelnut oil (for cold dishes and salads)', p)
  p = re.sub('corn or\speanut oil \(for sauteing\)', 'corn oil (for sauteing) or peanut oil (for sauteing)', p)
  return p

# Bug in page: Asparagus entry is split in multiple <tr>'s, and this breaks the parser...
def fixStalkPage(p):
  p = re.sub('</p>\s*</td>\s*</tr>\s*<tr>\s*<td width="100%" colspan="2">\s*<p>There\'s a purple variety', 'There\'s a purple variety', p)
  return p


def fixNutsPage(p):
  p = re.sub('pistachio nut', 'pistachio', p)
  return p

# convert eg "apple, dried" to "dried apple"
def fixDriedFruitPage(p):
  p = re.sub('<b>(?P<x>[a-z]+), dried', '<b>dried \g<x></b>', p)
  return p

def convertCond(c):
  for key in condition_dict:
    if re.search(key, c) is not None: return condition_dict[key]
  return {"cond":None, "notes":""}


def convert_CP1252_to_UTF8(s):
  ret = ''
  for c in s:
    if ord(c) < 0x80: ret = ret + c
    elif ord(c) < 0xC0: ret = ret + ('\xc2'+c) #sys.stdout.write('\xC2' + c)
    else: ret = ret + ('\xC3' + chr(ord(c)-64)) #sys.stdout.write('\xC3' + chr(ord(c) - 64))
  return ret

def remove_trailing(s):
  return re.sub("^\s+|\s+$", '', s)

class CrawlFoodSubs():
  def __init__(self):
    self.http = urllib3.PoolManager()
    self.total_leaves = 0

  def crawl(self, URI, parent, depth=0):
    if (URI in SKIP): return
    sys.stdout.write(' '*depth)
    sys.stdout.write('Crawling URI %s.. ' % URI)    

    # Fetch page, first check if it is already in cache
    if os.path.isfile(CACHE_FOLDER + URI):
      data = open(CACHE_FOLDER + URI, 'r').read()
      print URI
    else:
      print 'Fetching %s from internet' % URI
      page = self.http.request('GET', ROOT_URL  + URI)
      if page.status != 200:
        print 'Could not open %s, exising' % URI
        system.exit(-1)
      data = page.data
      open(CACHE_FOLDER + URI, 'w').write(data)

    data = convert_CP1252_to_UTF8(data)
    if URI == 'Oils.html': 
      data = fixOilPage(data)
    if URI == 'Stalk.html':
      data = fixStalkPage(data)
    if URI == 'Nuts.html':
      data = fixNutsPage(data)
    if URI == 'Fruitdry.html':
      data = fixDriedFruitPage(data)

    data = re.sub('\s', ' ', data)
    data = re.sub('&nbsp;', ' ', data)
    data = re.sub('&amp;', '&', data)
    data = re.sub(' +', ' ', data)

    if self.is_root(data):
      sys.stdout.write('Root page...')
      sys.stdout.write('\n')
      self.parse_root(data, parent, depth, URI)
    elif URI == 'Flour.html':
      self.parse_flour_page(data, parent, depth, URI)
    elif URI == 'Fruitvegies.html':  # an additional layer in the hierarchi
      self.parse_list_with_images(data, parent, depth, URI)
    elif self.is_list(data):
      sys.stdout.write('Category page...')
      sys.stdout.write('\n')
      self.parse_list(data, parent, depth, URI)
    else:
      sys.stdout.write('\n')
      self.parse_subs_page(data, parent, depth, URI)


  def is_root(self, data):
    if data.count('<li><b><a href') > 10: return True
    return False

  def parse_root(self, data, parent, depth, URI):
    while True:
      m = re.search("<li><b><a href=\"(?P<URI>[a-zA-Z]*.html)\">(?P<name>[a-zA-Z &]*)</a>", data)
      if m is None: break
      parent['children'].append({'name' : CATEGORY_TAG + m.group('name'), 'URL' : ROOT_URL + m.group('URI'), 'children' : []})
      self.crawl(m.group('URI'), parent['children'][-1], depth+2)
      data = data[m.end():]
      
  # A list of sub-categories (that is not the root)
  def is_list(self, data):
    if data.lower().count('category</title>') > 0:
      return True
    return False

  # A page like "FruitVegies.html" - A list of families, itemized by images.
  def parse_list_with_images(self, data, parent, depth, URI):
    # get family name
    m = re.search("<title>Cook's Thesaurus:\s+(?P<category>[a-zA-Z][a-zA-Z &]*[a-zA-Z])\s*</title>", data, re.IGNORECASE)
    assert(m is not None)
    category = m.group('category').lower()
    print " "*depth + 'Category = ', category
    
    # remove bold-face it is confusing
    data = re.sub('</?b>', '', data)
    while True:
      m = re.search("<td[^>]*><a href=\"(?P<URI>[a-zA-Z]*.html)\">(?P<name>([^<]|<(?!/td))*)</td>", data)
      if m is None: break
      data = data[m.end():]
      URI = m.group('URI')
      name = m.group('name')
      name = re.sub('<[^>]*>', '', name).lower()
      parent['children'].append({'name' : CATEGORY_TAG + name , 'URL' : ROOT_URL + URI, 'children':[]})
      self.crawl(URI, parent['children'][-1], depth+2)

  # A "category page" is a list of urls, each preceded by a gif-bullet
  def parse_list(self, data, parent, depth, URI):
    # The title tag contains the category name
    m = re.search("<title>Cook's Thesaurus:\s+(?P<category>[a-zA-Z][a-zA-Z &]*[a-zA-Z])\s+Category\s*</title>", data, re.IGNORECASE)
    assert (m is not None)
    category = m.group('category').lower()
    print " "*depth + 'Category = ', category
    while True:
      m = re.search("img SRC=\"bullet1.gif\" height=\"10\" width=\"10\">", data)
      if m is None: break
      data = data[m.end():]
      m = re.search("<a href=\"(?P<URI>[a-zA-Z]*.html)\">(?P<name>[a-zA-Z0-9\ \-&]*)</a>", data)
      if m is None:
        print 'Could not parse link and name in %s.' % data
        sys.exit(-1)
      parent['children'].append({'name' : CATEGORY_TAG + m.group('name'), 'URL' : ROOT_URL + m.group('URI'), 'children' : []})
      self.crawl(m.group('URI'), parent['children'][-1], depth+2)
      #print m.groups()
      data = data[m.end():]

  # A page containing lists of synonyms and subs
  def parse_subs_page(self, data, parent, depth, URI):

    # In case the page has a "varieties" section, the title is an ingredient,
    # the synonyms are written as a section and the substitutions too.  
    # then comes the varieties section which is like a "normal" page.
    # Oils.html, Dairtoth.html are exceptions

    if data.find('Varieties:') >= 0 and URI not in ["Oils.html", "Dairyoth.html", "Nutmeals.html", "MeatDried.html", "Fruittro.html"]:
      m = re.search('<font size=("\+3"|\+3)>([^V]|V(?!arieties))*(?=Varieties)', data, re.IGNORECASE)
      if m is None:
        PARSE_PROB_FILE.write('%s\n%s\n\n' % (URI, data))
      else:
        self.parse_page_preamble(m.group(), parent, depth, URI)
        data = data[:m.start()] + data[m.end():]

    self.parse_multiple_sub_parts(data, parent, depth, URI)
  
  #
  # A piece of html that contains multiple parts one after the other
  # each part is either enclosed in <td>..</td>, or <p>..</p>, with the
  # word "Substitutes" appearing inside.
  #
  def parse_multiple_sub_parts(self, data, parent, depth, URI):
    while True:
       # Most subs are within <td>...</td>
      m = re.search('<td(?P<element>([^<]|(<(?!/td>)))*)</td>', data)
      if m is not None:
        if m.group().find('Substitutes:') >= 0:
          self.parse_sub_part(m.group(), parent, depth, URI) 
        data = data[:m.start()] + data[m.end():]
        continue

      # some are within <p>...</p> or <p>....<p>  For some reasone the colon disappears sometimes from the "Substitutes"...
      m = re.search('<p[^>]*>(([^<])|(<(?!(/p>)|(p))))*Substitutes([^<]|<(?!(/p>)|(p)))*(?=(</p>)|(<p))', data)
      if m is not None:
        self.parse_sub_part(m.group(), parent, depth, URI)
        data = data[:m.start()] + data[m.end():]     
        continue
      break

    if data.find('Substitutes') >= 0:
      PARSE_PROB_FILE.write('%s\n%s\n\n' % (URI, data))

  def parse_sub_part(self, part, parent, depth, URI):
    if part.find('Substitutes') == -1: return

    # Get rid of <span ....> and </span>
    part = re.sub("<span[^>]*>", "", part)
    part = re.sub("</span>", "", part)
    m = re.search("<[b][^>]*>(?P<names>(([^<])|(<(?!/b)))*)</[b]>", part) # the ingredient names are in bold, at the beginning
    if m is None:
      PARSE_PROB_FILE.write('%s\n%s\n\n' % (URI, part))
      return
    names = m.group(1)
    
    # Remove 'notes' etc
    names = re.sub('<i>([^<]|<(?!/i>))*</i>', '', names) # remove comments (eg notes)
    names = re.sub('<a[^>]*>', '', names) # remove anchores that may appear in the names
    names = re.sub('</a>', '', names) 
    names=names.split('=')

    # get the substitutes clause
    m = re.search("Substitutes[^:]*:(?P<subs>.*)", part)
    if m is None:
      print part
      sys.exit(-1)
    subs = m.group('subs')
    subs_save_debug = subs

    # Remove all tags
    subs = re.sub('<[^>]*>', '', subs)

    # sometimes "notes"  or "Links" are added at end of subs, these must be removed
    subs = re.sub('Notes.*$', '', subs)
    subs = re.sub('Links.*$', '', subs)

    subs = re.split('[^a-zA-Z][oO][rR][^a-zA-Z]', subs) # fancy version of subs.split('OR')

    subs = map(remove_trailing, subs)
    node = {'sources' : [], 'targets' : [], 'URL' : ROOT_URL + URI}
    self.total_leaves = self.total_leaves+1
    parent['children'].append(node)
    for name in names:
      node['sources'].append(remove_trailing(name.lower()))
    for sub in subs:
      # remove notes in parentheses
      notes = ''
      m = re.search('\([^)]*\)', sub)
      if m is not None: notes = m.group() # save notes
      sub = re.sub('\([^)]*\)', '', sub) # remove notes
      sub = remove_trailing(sub)
      sub = sub.lower()
      notes = remove_trailing(notes)
      notes = re.sub('\(|\)','', notes) # remove parentheses from notes
      node['targets'].append({'target' : sub, 'notes' : notes})
      if sub =='corn' and 'olive oil' in node['sources']:
        print '***********'
        print subs_save_debug

  def parse_page_preamble(self, preamble, parent, depth, URI):
    if preamble.find('Substitutes') == -1:
      PARSE_PROB_FILE.write('%s\n%s\n\n' % (URI, preamble))
      return
    m = re.search('<(font size|FONT SIZE)[^>]*>(?P<name>[^<]*)(</font>|</FONT>).*Substitutes.*<ul>(?P<subs>.*)</ul>', preamble)
    if m is None:
      m = re.search('<(font size|FONT SIZE)[^>]*>(?P<name>[^<]*)(</font>|</FONT>).*Substitutes:(?P<subs>([^<]|<(?!/p))*)', preamble)
    name = m.group('name')
    subs = m.group('subs')
    node = {'sources' : [], 'targets' : [], 'URL' : ROOT_URL + URI}
    self.total_leaves = self.total_leaves+1
    name = remove_trailing(name)
    subs = re.sub('<[^>]*>', '', subs)
    subs = re.split('[^a-zA-Z][oO][rR][^a-zA-Z]', subs)
    subs = map(remove_trailing, subs)
    node['sources'].append(name)
    for sub in subs:
      notes = ''
      m = re.search('\((?P<notes>[^)]*)\)', sub)
      if m is not None:
        notes = m.group('notes')
        sub = sub[:m.start()] + sub[m.end():]
        sub = remove_trailing(sub)
      node['targets'].append({'target' : sub, 'notes': notes})
    parent['children'].append(node)
    #print node


  def parse_flour_page(self, data, parent, depth, URI):
    # collect substitutes for "wheat flour" up to "varieties"
    m = re.search("<p><b><i>Substitutes:</i></b>(?P<part1>.*)<p align=\"left\"><i><b>Varieties:</b></i>", data)
    part1 = m.group('part1')
    self.parse_list_of_conditions_with_itemized_subs(WHEAT_FLOUR_LIST, part1, parent, depth, URI)
    data = data[:m.start()] + data[m.end():]

    # collect substitutes for "all-purpose-flour" part
    m = re.search("<p><b><i>Substitutes:</i></b>(?P<part2>.*)<i><b>Notes:\s+</b>See also flour in the", data)
    part2 = m.group('part2') 
    self.parse_list_of_conditions_with_itemized_subs(ALL_PURPOSE_FLOUR_LIST, part2, parent, depth, URI)
    data = data[:m.start()] + data[m.end():]
    # From here, the usual parse_sub_part should be enough
    self.parse_multiple_sub_parts(data, parent, depth, URI)

  # This is for page parts with a list of conditions, each condition in bold and
  # italicized, followed by a sublist of subs inside <ul>...</ul>
  def parse_list_of_conditions_with_itemized_subs(self, sources, data, parent, depth, URI):
    targets = []
    while True:
      m = re.search("<b><i>(?P<cond>[^<]*)</i></b>[^<]*<ul>(?P<subs>([^<]|<(?!/ul>))*)</ul>", data)
      if m is None: break
      cond = m.group('cond')
      #print 'cond=',cond
      data = data[:m.start()] + data[m.end():]
      subs = m.group('subs')
      while True:
        m = re.search('<li>(?P<sub>([^<]|<(?!/li))*)</li>', subs)
        if m is None: break
        subs = subs[:m.start()] + subs[m.end():]
        sub = m.group('sub')
        sub = re.sub('<[^>]*>', '', sub)
        sub = re.sub('OR', '', sub)
        notes = ''
        m = re.search('\((?P<notes>[^)]*)\)', sub)
        if m is not None: notes = m.group('notes')
        sub = re.sub('\(([^)]*)\)', '', sub)
        #print '  sub=', sub
        #print '  notes=', notes
        targets.append({'target' : remove_trailing(sub), 'notes' : notes, 'cond' : cond})
    
      for source in sources:
        node = {'sources' : source, 'targets' : targets, 'URL' : ROOT_URL + URI, 'cond' : cond}
        self.total_leaves = self.total_leaves+1
        parent['children'].append(node)


#
# Write RDF information to DB, as triplets
#
def getRDFs(rdb, crawl_info, parent, rdfs):
  if 'name' in crawl_info and parent is not None and 'name' in parent:
    rdfs.append(RDF.RDF(x=crawl_info['name'].lower(), y=parent['name'].lower(), relation=RDF.TYPE_OF, origin=FOODSUBS_ORIGIN))

  if 'sources' in crawl_info and parent is not None and 'name' in parent:
    sources = crawl_info['sources']
    targets = crawl_info['targets']
    for ing in sources:
      rdfs.append(RDF.RDF(x=ing.lower(), y=parent['name'].lower(), relation=RDF.TYPE_OF, origin = FOODSUBS_ORIGIN))
    for i in range(len(sources)):
      for j in range(i+1, len(sources)):
        rdfs.append(RDF.RDF(x=sources[i].lower(), y=sources[j].lower(), relation=RDF.EQUIVALENT_TO, origin = FOODSUBS_ORIGIN))
    for ing in sources:
      for ing_ in targets:
        rdfs.append(RDF.RDF(x=ing_['target'], y=ing.lower(), relation=RDF.POSSIBLY_SUBSTITUTE_OF, origin=FOODSUBS_ORIGIN))
  
  if 'children' in crawl_info:
    for child in crawl_info['children']:
      getRDFs(rdb, child, crawl_info, rdfs)

#
# Write the crawl output to DB, as manual rules, RDF data(?) etc.
#
def WriteManualRulesToDB(rdb, crawl_info, unavailable_entities_file):
  if 'sources' in crawl_info: 
    record = {
      "origin" : "foodsubs.com",
      "version" : "1",
      "probability":1.0,
      "name":"Substitute for %s" % crawl_info['sources'].__str__(),
      "infolink":crawl_info['URL'], 
      "moreinfo":"", 
      "type":"ListSinglePick",
      "sources":[], #resolveEntities({"potato":{"qty":1}}),
      "targets":[] #resolveEntities({"sweet potato":{"qty":1}, "celery root":{"qty":1, 'infolink':"http://low-carb-support.com/low-carb-potato-substitute/"}})
      }

    record['cond'] = 'True'
    if 'cond' in crawl_info:
      converted = convertCond(crawl_info['cond'])
      if converted['cond'] is not None:
        record['cond'] = converted['cond']
      
    for ing in crawl_info['sources']:
      if ing is None or ing=='': continue
      can_ing = rdb.cannonicalize_entity(ing)
      if can_ing is None: 
        unavailable_entities_file.write(('%s: %s appears as source, possibly synonymous with %s\n' % (crawl_info['URL'], ing, crawl_info['sources'].__str__())))
        continue
      record["sources"].append({
        "name":can_ing,
        "qty":1,
        "id" : rdb.get_entitymap_dic()[can_ing]['_id'].__str__()})
    for target in crawl_info['targets']:
      notes = target['notes']
      target = target['target']
      converted_notes = convertCond(notes)
      cond = converted_notes["cond"]
      if cond is not None:        
        notes = converted_notes["notes"]
      
      can_target = rdb.cannonicalize_entity(target)
      if can_target is None: 
        unavailable_entities_file.write(('%s: %s appears as target \n' % (crawl_info['URL'], target)))
        continue
      record["targets"].append({
        "name":can_target,
        "qty":1, 
        "moreinfo" : notes,
        "id" : rdb.get_entitymap_dic()[can_target]['_id'].__str__()})
      if cond is not None: record["targets"][-1]["cond"] = cond

    if len(record["sources"]) and len(record["targets"]):
      rdb.InsertManualSubRule(record)
  if 'children' in crawl_info:
    for child in crawl_info['children']:
      WriteManualRulesToDB(rdb, child, unavailable_entities_file)


def debugPrint(crawl_info, indent):
  sys.stdout.write(" "*indent)
  sys.stdout.write((crawl_info["URL"] if 'URL' in crawl_info else '?') + ' ')
  if 'name' in crawl_info:
    sys.stdout.write('name=%s '% crawl_info['name'])
  if 'sources' in crawl_info:
    sys.stdout.write('%s==>%s ' % (crawl_info['sources'].__str__(), map(lambda x: x['target'], crawl_info['targets']).__str__()))
  sys.stdout.write('\n')
  if 'children' in crawl_info:
    for child in crawl_info['children']:
      debugPrint(child, indent+2)

parser = OptionParser()
rdb = RecipeDB(option_parser = parser)
cfs = CrawlFoodSubs()
crawl_info = {'name':'foodsubs root', 'children' : []}
cfs.crawl('index.html', crawl_info)

debugPrint(crawl_info, 0)
unavailable_entities_file = open('data/unavailable_entities_foodsubs.txt', 'w')
WriteManualRulesToDB(rdb, crawl_info, unavailable_entities_file)
rdfs = []
getRDFs(rdb, crawl_info, None, rdfs)
rdb.removeRDFsFromOrigin(FOODSUBS_ORIGIN)
rdb.writeRDFs(rdfs)

for rdf in rdfs:
  print rdf.serialize()
unavailable_entities_file.close()
print 'Total number of leaves: %d' % cfs.total_leaves
PARSE_PROB_FILE.close()

