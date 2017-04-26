#-*- coding: utf-8 *-*
#
# This program writes manual substitution rules to the database.  It is meant for initial use, before we have a "front-end" friendly solution
# for this task.
#

import sys
import IOTools
from optparse import OptionParser
import RDF

MANUAL_RDF_ORIGIN = 'manual rules'

GRATED_CAULIFLOWER_INSTRUCTIONS = '''Remove the outer leaves from the cauliflower, cut it into quarters and remove most of the thick core, then cut each quarter into two or three chunks. You don’t want to overload the blender, or it will struggle to blitz the cauliflower, instead work with about half the cauliflower at a time. Blend for 30 seconds or so, until the cauliflower resembles fine rice, or couscous.'''
GRATED_CAULIFLOWER_COOKED_INSTRUCTIONS = '''Remove the outer leaves from the cauliflower, cut it into quarters and remove most of the thick core, then cut each quarter into two or three chunks. You don’t want to overload the blender, or it will struggle to blitz the cauliflower, instead work with about half the cauliflower at a time. Blend for 30 seconds or so, until the cauliflower resembles fine rice, or couscous.  Warm a tablespoon of olive oil or butter in a large skillet over medium heat. Stir in the cauliflower and sprinkle with a little salt. Cover the skillet and cook for 5 to 8 minutes.'''

COOKED_LENTILS_IN_SALAD_INSTRUCTIONS = '''Put the lentils in a sieve and rinse them thoroughly in cold water. The water will run clear when the lentils are clean.'''

ZUCCHINI_PASTA_INSTRUCTIONS = '''1. Cut lengthwise slices from zucchini using a vegetable peeler, stopping when the seeds are reached. Turn zucchini over and continue 'peeling' until all the zucchini is in long strips; discard seeds. Slice the zucchini into thinner strips resembling spaghetti.
2. Heat olive oil in a skillet over medium heat; cook and stir zucchini in the hot oil for 1 minute. Add water and cook until zucchini is softened, 5 to 7 minutes. Season with salt and pepper.'''

CHICKPEA_FLOUR_IN_PANCAKE_NOTE = '''Equal combination chickpea flour and almong flour recommended.'''
ALMOND_FLOUR_IN_QUICHE_INSTRUCTIONS = '''Recommended to adjust butter amount to 40 grams per cup of almond flour, and oil amount to 1/8 cup per cup of almond flour.'''
SWEET_POTATO_IN_SMOOTHIE_MORE_INFO = '''Bake sweet potato in a preheated oven or microwave until tender.  Peel and cool. '''
YOGURT_FOR_BANANA_IN_SMOOTHIE_MORE_INFO = '''Cube and freeze.'''
MILK_FOR_CREME_FRAICHE_MASHED_POTATO_MOREINFO = '''One cup creme fraiche per 3 pounds of potatoes'''
MILK_FOR_WATER_MASHED_POTATO_MOREINFO = '''Use the water potatoes were cooked in'''
EGG_FOR_ARROWROOT_PUDDING_CUSTARD_MOREINFO = '''Mix 1tbps arrowroot and 1 tablespoon oil and 1/4 cup water'''
EGG_FOR_POTATO_FLOUR_PATTY_MOREINFO = '''Combine potato flour with the recipe's liquid. Add oatmeal to the recipe's dry ingredients.'''

parser = OptionParser()
rdb = IOTools.RecipeDB(option_parser = parser)
#coll = rdb.getManualSubRulesColl()

# Cannonicalize entities, add id to a dictionary of entities
def resolveEntities(d):
  res = []
  for key in d:
    key_ = rdb.cannonicalize_entity(key)
    if key_ is None: continue
    if not key_ in rdb.get_entitymap_dic(): continue  
    res.append(d[key])
    res[-1]['name'] = key_
    res[-1]['id'] = rdb.get_entitymap_dic()[key_]['_id'].__str__()
  return res

rules = [
  {
    "cond" : "recipe_tag_prob('is_rice')>0.5",
    "origin" : "Book",
    "version" : "1",
    "probability":1.0,
    "name":"rice_sub",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"rice":{"qty":1}, "white rice":{"qty":1}, "brown rice":{"qty":1}, "instant rice":{"qty":1}}),
    "targets":resolveEntities({"rice":{"qty":1}, "white rice":{"qty":1}, "brown rice":{"qty":1}, "instant rice":{"qty":1}, "wild rice":{"qty":1}, "couscous":{"qty":1.2}, "quinoa":{"qty":1}, "millet":{"qty":0.688}, "pearl barley":{"qty":0.7}, "bulgur":{"qty":1}, "wheatberries":{"qty":1.3}, "rye berries":{"qty":1.2}, "groats":{"qty":1}, "spelt":{"qty":1.3}, "buckwheat":{"qty":1.5}}  )
  },
  {
    "cond" : "recipe_tag_prob('is_pasta')>0.5",
    "origin" : "Book",
    "version" : "1",
    "probability":1.0,
    "name":"pasta_sub",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"linguine":{"qty":1}, "pasta":{"qty":1}, "spaghetti":{"qty":1}, "whole grain spaghetti":{"qty":1},"angel hair pasta":{"qty":1},"capellini":{"qty":1},"bucatini":{"qty":1},"perciatelli":{"qty":1},"vermicelli":{"qty":1},"spaghetti":{"qty":1}}),
    "targets":resolveEntities({"pasta":{"qty":1}, "spaghetti":{"qty":1}, "whole grain spaghetti":{"qty":1}, "angel hair pasta":{"qty":1}, "capellini":{"qty":1}, "bucatini":{"qty":1}, "perciatelli":{"qty":1}, "vermicelli":{"qty":1}, "chinese wheat noodles":{"qty":1}, "egg noodles":{"qty":1}}  )
  },
  {
    "cond" : "recipe_tag_prob('is_potato')>0.5",  #"lambda x: 'tags' in x and 'is_potato' in map(lambda y: y['name'], x['tags'])",
    "origin" : "Book",
    "version" : "1",
    "probability":1.0,
    "name":"potato_sub",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"potatoes":{"qty":1}}),
    "targets":resolveEntities({"cassava":{"qty":1}, "malanga":{"qty":1}, "yautia":{"qty":1}, "taro":{"qty":1}, "sweet potato":{"qty":1}, "parsnip":{"qty":1}, "jerusalem artichoke":{"qty":1}, "jicama":{"qty":1}, "plantain":{"qty":1}}  )
  },
  {
    "cond" : "recipe_tag_prob('is_grated_potato')>0.5 and recipe_tag_prob('is_hashbrown')>0.5", 
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Grated potato in hash brown substitution",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"potato":{"qty":1}}),
    "targets":resolveEntities({"jicama":{"qty":1}, "summer squash":{"qty":1, "infolink":"http://healthyrecipesblogs.com/2014/07/23/yellow-squash-fritters/"}})
  },  
  {
    "cond" : "recipe_tag_prob('is_mashed_potatoes')>0.5", 
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Mashed potatoes substitution",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"potatoes":{"qty":1}}),
    "targets":resolveEntities({"cauliflower":{"qty":1, "infolink":"http://allrecipes.com/recipe/230816/garlic-mashed-cauliflower/"}, 'turnips' : {'qty':1, "infolink":"http://allrecipes.com/recipe/230816/garlic-mashed-cauliflower/"}})
  },  
  {
    "cond" : "recipe_tag_prob('is_gratin')>0.5", 
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Gratin potato substitution",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"potatoes":{"qty":1}}),
    "targets":resolveEntities({"tempeh":{"qty":1,"moreinfo":"1.Saute a couple of cups of thinly diced tempeh with garlic and onions. 2. pour a cheese sauce (sharper is better) over the tempeh cubes 3.bake for half an hour."}})
  },  
  {
    "cond" : "recipe_tag_prob('is_potato_chips')>0.5 or recipe_tag_prob('is_french_fries')>0.5", 
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Potato chips or french fries substitution",
    "infolink":"", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"potatoes":{"qty":1}}),
    "targets":resolveEntities({"sweet potato":{"qty":1}, "celery root":{"qty":1, 'infolink':"http://low-carb-support.com/low-carb-potato-substitute/"}})
  },
  {
    "cond" : "recipe_tag_prob('is_soup')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Avoiding gluten in soups and creamy soups",
    "infolink":"http://damndelicious.net/2015/01/10/creamy-chicken-mushroom-soup/", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour" : {"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"cornflour":{"qty":1}, "arrowroot flour":{"qty":1}, "rice flour" : {"qty":1}}) 
  },
  {
    "cond" : "recipe_tag_prob('is_soup')>0.8 and ingredient_absolute_quantity_grams < 100",
    "origin" : "MFE",
    "version" : "1",
    "probability":1.0,
    "name":"Avoiding gluten in soups and creamy soups when flour is in small quantities",
    "infolink":"http://damndelicious.net/2015/01/10/creamy-chicken-mushroom-soup/", 
    "moreinfo":"", 
    "type":"ListSinglePick",
    "sources":resolveEntities({"wheat flour":{"qty":1}, "all purpose flour" : {"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"ground cashews":{"qty":1}, "coconut flour" : {"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_salad')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Burgul to grated cauliflower for no gluten/less carbs",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"bulgur":{"qty":1}}),
    "targets":resolveEntities({"cauliflower":{"qty":1, "moreinfo":GRATED_CAULIFLOWER_INSTRUCTIONS}})
  },
  {
    "cond" : "True",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Breadcrumbs to almond flour/meal for no gluten/less carbs",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"breadcrumb":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "almond meal":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_cutlet') or recipe_tag_prob('is_patties')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Flour or breadcrumb to almond flour/almond meal for cutlet/patty",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"flour":{"qty":1}, "all purpose flour":{"qty":1}, "whole wheat flour":{"qty":1}, "breadcrumb":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "almond meal":{"qty":1}, "chickpea flour" : {"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_meatballs')>0.8 or recipe_tag_prob('is_patties')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Breadcrumbs in meatballs to almond flour or potato flour for reduced gluten",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"breadcrumb":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "potato flour":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_meatballs')>0.8 or recipe_tag_prob('is_frittata') > 0.8 or recipe_tag_prob('is_latkes')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "White/all-purpose flour to almond flour in meatballs, frittata, latkes for no gluten or low carb",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour":{"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}})
  },
  {
    "cond" : "True",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Reduce gluten by substituting lasagna sheets",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"lasagna noodle":{"qty":1}}),
    "targets":resolveEntities({"vietnamese rice paper":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_pancake')>0.8 and recipe_has_vegetable",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "White/all-purpose flour to almond flour in vegetable pancake for no gluten or low carb",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour":{"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "almond meal":{"qty":1}, "gluten free flour":{"qty":1}, "chickpea flour":{"qty":1, "moreinfo":CHICKPEA_FLOUR_IN_PANCAKE_NOTE}})
  },
  {
    "cond" : "recipe_tag_prob('is_meatballs')>0.8 and (recipe_has_ingredient('quinoa') or recipe_has_ingredient('turkey'))",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "White/all-purpose/whole-wheat in quinoa/turkey meatballs to potato starch for no gluten or low carb",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour":{"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"potato starch":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_quiche')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "White/all-purpose flour to almond flour quiche crust for no gluten or low carb",
    "infolink":"",
    "moreinfo":"",  # TBD by MFE
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour":{"qty":1}, "whole wheat flour":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1.5, "moreinfo":ALMOND_FLOUR_IN_QUICHE_INSTRUCTIONS}})
  },
  {
    "cond" : "True",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Angel-hair pasta/spaghetti/vermicelli to zucchini pasta for gluten free or carb reduction",
    "infolink":"",
    "moreinfo":"",  # TBD by MFE
    "type":"ListSinglePick",
    "sources":resolveEntities({"vermicelli":{"qty":1}, "spaghetti pasta":{"qty":1}, "angel-hair pasta" :{"qty":1}}),
    "targets":resolveEntities({"zucchini":{"qty":1,"moreinfo":ZUCCHINI_PASTA_INSTRUCTIONS}, "soy vermicelli":{"qty":1},"rice vermicelli":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_salad')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Bulgur to lentils in cold salads for gluten free or carb reduction",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"bulgur":{"qty":1}}),
    "targets":resolveEntities({"beluga black lentil":{"qty":1,"moreinfo":COOKED_LENTILS_IN_SALAD_INSTRUCTIONS}})
  },
  {
    "cond" : "recipe_tag_prob('is_couscous')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Couscous to cauliflower for gluten free or carb reduction",
    "infolink":"",
    "moreinfo":COOKED_LENTILS_IN_SALAD_INSTRUCTIONS,
    "type":"ListSinglePick",
    "sources":resolveEntities({"couscous":{"qty":1}}),
    "targets":resolveEntities({"cauliflower":{"qty":1, "moreinfo":GRATED_CAULIFLOWER_INSTRUCTIONS}})
  },
  {
    "cond" : "recipe_tag_prob('is_cooked')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Couscous or bulgur to cauliflower in cooked recipes for gluten free or carb reduction",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"couscous":{"qty":1},"bulgur":{"qty":1}}),
    "targets":resolveEntities({"cauliflower":{"qty":1, "moreinfo":GRATED_CAULIFLOWER_COOKED_INSTRUCTIONS}})
  },
  {
    "cond" : "recipe_tag_prob('is_quiche')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "General rules for eggplant->zucchini/squash",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"eggplant":{"qty":1}}),
    "targets":resolveEntities({"zucchini":{"qty":1}, "squash":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_vegetable_fritter')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Removing glutten from flour in vegetable fritters",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "gluten free flour":{"qty":1}, "potato flour":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_vegetable_fritter')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Removing glutten from breadcrumbs in vegetable fritters",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"breadcrumbs":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "cooked quinoa":{"qty":1}, "potato flour":{"qty":1}})
  },
  {
    "cond" : "True",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacing filo dough",
    "infolink":"http://www.livestrong.com/article/557064-how-to-bake-with-rice-paper/",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"phyllo pastry sheet":{"qty":1}}),
    "targets":resolveEntities({"rice sheet":{"qty":1}, "vietnamese rice paper":{"qty":1}})
  },
  {
    "cond" : "True",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace Kadaif noodles with rice noodles",
    "infolink":"",
    "moreinfo":"Deep fry rice vermicelli. Optionally: Soak vermicelli in hot water for few minutes until soft and drain before frying. Tip: Use very thin (fine) rice vermicelli.",
    "type":"ListSinglePick",
    "sources":resolveEntities({"kadaif noodle":{"qty":1}}),
    "targets":resolveEntities({"rice noodle":{"qty":1}, "vermicelli":{"qty":1}, "soy vermicelli":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_casserole')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace Grisini crumbs with breadcrumbs(non-gluten)",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"grissini":{"qty":1}, "bread stick":{"qty":1}}),
    "targets":resolveEntities({"breadcrumb":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_muffin') > 0.8 and recipe_has_vegetable",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace flour with gluten free versions in vegetable muffins",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"white flour":{"qty":1}, "all purpose flour":{"qty":1}, "all wheat flour": {"qty":1}, "baking flour":{"qty":1}}),
    "targets":resolveEntities({"almond flour":{"qty":1}, "gluten free flour":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace milk in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"milk":{"qty":1}}),
    "targets":resolveEntities({"coconut milk":{"qty":1}, "greek yogurt":{"qty":1}, "soy milk":{"qty":1}, "almond milk":{"qty":1}, "non fat milk" : {"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace sugar in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"sugar":{"qty":1}}),
    "targets":resolveEntities({"brown sugar":{"qty":1}, "honey":{"qty":1}, "stevia":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replace blueberries in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"blueberry":{"qty":1}}),
    "targets":resolveEntities({"blackberry":{"qty":1}, "strawberry":{"qty":1}})
  },  
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.0,
    "name" : "Non-replacements of lemon juice in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"lemon juice":{"qty":1}}),
    "targets":resolveEntities({"white wine":{"qty":1}, "red wine vinegar":{"qty":1}, "other vinegar":{"qty":1}, "fruit vinegar":{"qty":1}, "cider vinegar":{"qty":1}})
  },  
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of lemon juice in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"lemon juice":{"qty":1}}),
    "targets":resolveEntities({"lime juice":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of vanilla yogurt in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"vanilla yogurt":{"qty":1}}),
    "targets":resolveEntities({"greek yogurt":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of soy milk in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"soy milk":{"qty":1}}),
    "targets":resolveEntities({"coconut milk":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of banana in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"banana":{"qty":1}}),
    "targets":resolveEntities({"canned pumpkin":{"qty":1}, "coconut milk":{"qty":1}, "sweet potato":{"qty":1, "moreinfo":SWEET_POTATO_IN_SMOOTHIE_MORE_INFO}, "mango":{"qty":1}, "avocado":{"qty":1}, "frozen spinach":{"qty":1}, "yogurt":{"qty":1, "moreinfo":YOGURT_FOR_BANANA_IN_SMOOTHIE_MORE_INFO}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of honey in smoothie",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"honey":{"qty":1}}),
    "targets":resolveEntities({"stevia":{"qty":1}}), 
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of honey in smoothie",
    "infolink":"",
    "moreinfo":"Increase protein",
    "type":"RecipeAddition",
    "targets":resolveEntities({"kale":{}, "pumpkin seed":{}, "oat":{}, "quinoa":{}, "spinach":{}, "almond butter":{}, "chia seed":{}})
  },
  {
    "cond" : "recipe_tag_prob('is_smothie') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of orange juice",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"orange juice":{"qty":1}}),
    "targets":resolveEntities({"carrot juice":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_patties') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of chickpea in patty",
    "infolink":"http://allrecipes.com/recipe/13997/garbanzo-bean-patties/",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"chick-pea":{"qty":1}}),
    "targets":resolveEntities({"quinoa":{"qty":1}, "red lentils":{"qty":1}, "green lentils":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_patties') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of chickpea in patty",
    "infolink":"http://allrecipes.com/recipe/128968/italian-vegetarian-patties/",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"red lentils":{"qty":1}}),
    "targets":resolveEntities({"green lentils":{"qty":1}, "brown lentils":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_dip') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of cottage cheese in dip",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"cottage cheese":{"qty":1}}),
    "targets":resolveEntities({"yogurt":{"qty":1}, "egg white":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_mashed') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of turnips in mashed turnips",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"turnip":{"qty":1}}),
    "targets":resolveEntities({"cauliflower":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_mashed') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of milk in mashed turnip/potato/cauliflower",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"milk":{"qty":1}}),
    "targets":resolveEntities({"rice milk":{"qty":1}, "soy milk":{"qty":1}, "almond milk":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_mashed') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of milk in mashed turnip/potato/cauliflower",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"butter":{"qty":1}}),
    "targets":resolveEntities({"rice milk":{"qty":1}, "soy milk":{"qty":1}, "almond milk":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_mashed') > 0.8 and recipe_has_ingredient('potato')",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of milk in mashed potato",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"milk":{"qty":1}}),
    "targets":resolveEntities({"half and half":{"qty":1}, "half-and-half":{"qty":1}, "half & half":{"qty":1}, "light cream":{"qty":1}, "heavy cream":{"qty":1}, "sour cream":{"qty":0.5}, "creme fraiche":{"qty":1, "moreinfo":MILK_FOR_CREME_FRAICHE_MASHED_POTATO_MOREINFO}, "water":{"qty":1, "moreinfo":MILK_FOR_WATER_MASHED_POTATO_MOREINFO}, "chicken broth":{"qty":1}, "vegetable stock":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_lasagna') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of mozzarella in Lasagna",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"mozzarella cheese":{"qty":1}}),
    "targets":resolveEntities({"ricotta cheese":{"qty":1}, "provolone cheese":{"qty":1}, "fontina cheese":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_patties') > 0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of mozzarella in patty",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"mozzarella cheese":{"qty":1}}),
    "targets":resolveEntities({"cheddar cheese":{"qty":1, "infolink":"http://www.food.com/recipe/fried-potato-patties-375608"}})
  },
  {
    "cond" : "recipe_tag_prob('is_burger') > 0.8 and (recipe_has_ingredient('ground lamb') or recipe_has_ingredient('lamb'))",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of mozzarella in lamb burger",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"mozzarella cheese":{"qty":1}}),
    "targets":resolveEntities({"buffalo mozzarella cheese":{"qty":1}})
  },
  {
    "cond" : "recipe_tag_prob('is_meatloaf')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of egg in meatloaf",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"egg":{"qty":1}}),
    "targets":resolveEntities({"cheddar cheese":{"qty":1, "moreinfo":"Shredded"}})
  },
  {
    "cond" : "recipe_tag_prob('is_pudding')>0.8 or recipe_tag_prob('is_custard')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of egg in pudding/custard",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"egg":{"qty":1}}),
    "targets":resolveEntities({"arrowroot":{"qty":1, "moreinfo":EGG_FOR_ARROWROOT_PUDDING_CUSTARD_MOREINFO}})
  },
  {
    "cond" : "recipe_tag_prob('is_patty')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of egg in patty",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"egg":{"qty":1}}),
    "targets":resolveEntities({"potato flour":{"qty":1, "moreinfo":EGG_FOR_POTATO_FLOUR_PATTY_MOREINFO}})
  },
  {
    "cond" : "recipe_tag_prob('is_quiche')>0.8",
    "origin" : "MFE",
    "version" : "1",
    "probability" : 0.8,
    "name" : "Replacement of spinache in quiche",
    "infolink":"",
    "moreinfo":"",
    "type":"ListSinglePick",
    "sources":resolveEntities({"spinache":{"qty":1}}),
    "targets":resolveEntities({"kale":{"qty":1}, "bok choy":{"qty":1}, "collards":{"qty":1}, "arugula":{"qty":1}, "swiss chard":{"qty":1}})
  }]


print rules

rdfs = []
for rule in rules:
  print rule
  # Insert rule verbatim
  rdb.InsertManualSubRule(rule)

  # Also deduce RDF info about the source/target ingredients in the rule
  if rule['type'] == 'ListSinglePick':
    for s in rule['sources']:
      for t in rule['targets']:
        rdfs.append(RDF.RDF(origin=MANUAL_RDF_ORIGIN, x=t['name'], relation = RDF.POSSIBLY_SUBSTITUTE_OF, y = s['name']))

rdb.removeRDFsFromOrigin(MANUAL_RDF_ORIGIN)  
rdb.writeRDFs(rdfs)

#  coll.insert_one(rule)

