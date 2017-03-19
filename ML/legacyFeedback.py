#-*- coding: utf-8 *-*
#######################################################################################
#
# This creates feedback records from a legacy csv file that Orly labelled
#
# This program should be executed once until the end of time, assuming the
# "feedback" collection is persistent (which it should be...).
#
#######################################################################################

import IOTools
from  Feedback import *
import sys
from optparse import OptionParser

PERSONID = "Orly"
RICE_SUB_TARGETS = ['brown rice', 'jasmine rice', 'wheat berries', 'grated cauliflower', 'bulgur', 'quinoa']
TYPE = 'substitution'
TASK_ID = 'legacy:rice substitutions'

RICE_FILE_CONTENTS = '''Id,Url,Class a,Class b,brown rice,jasmine rice,wheat berries,grated cauliflower,Bulgur,Quinoa,Note
5767979be4b0aa7a3941bdd8,http://www.yummly.com/recipe/Inside-Out-Stuffed-Peppers-Allrecipes,?,,,,,,,,
5763a904e4b0ad09acae7c89,http://www.foodnetwork.com/recipes/patrick-and-gina-neely/dirty-rice-stuffed-collards-recipe.html,? Dirty rice a new term,,,,,,,,
5763a60de4b0ad09acadf021,http://www.recipetips.com/recipe-cards/t--102307/inside-out-stuffed-peppers-2.asp,casserole,,,,,,,,
576627a0e4b089e19319cc3e,http://www.recipetips.com/recipe-cards/t--102307/inside-out-stuffed-peppers-2.asp,casserole,,,,,,,,
5763a423e4b0ad09acad9587,http://food52.com/recipes/7188-vegetabel-paella,paella,paella,1,1,1,1,0,0,cauliflower rice
5763ac2fe4b0ad09acaf145b,http://food52.com/recipes/7196-seafood-paella,paella,paella,1,1,1,1,0,0,cauliflower rice
5763a26ee4b0ad09acad4432,http://food52.com/recipes/7235-carol-s-seafood-paella,paella,paella,1,1,1,1,0,0,
5763aa4be4b0ad09acaeb9ba,http://food52.com/recipes/7244-the-sun-also-rises-paella,paella,paella,1,1,1,1,0,0,
5763a8c7e4b0ad09acae7144,http://girlinthelittleredkitchen.com/2012/05/risotto-paella/,paella,paella,1,1,1,1,0,0,
57679adce4b0aa7a39425849,http://www.foodandwine.com/recipes/seafood-paella-with-spinach-and-arugula,paella,paella,1,1,1,1,0,0,cauliflower rice
57661f3fe4b089e19318d3e9,http://allrecipes.com.au/recipe/17950/water-chestnut-rice-pilaf.aspx,pilaf,pilaf,1,1,0,0,0,0,
5763ae25e4b0ad09acaf7046,http://www.epicurious.com/recipes/food/views/brown-and-wild-rice-pilaf-with-porcini-and-parsley-10811,pilaf,pilaf,1,1,0,0,0,0,
57662858e4b089e19319e74a,http://www.epicurious.com/recipes/food/views/brown-and-wild-rice-pilaf-with-porcini-and-parsley-10811,pilaf,pilaf,1,1,0,0,0,0,
5763addde4b0ad09acaf631d,http://www.everydayhealth.com/recipes/cherry-rice-pilaf-1/,pilaf,pilaf,1,1,0,0,0,0,
5763a5dee4b0ad09acade766,http://www.seriouseats.com/recipes/2011/01/plov-uzbek-rice-pilaf-saffron-recipe.html,pilaf,pilaf,1,1,0,0,0,0,"wild rice, "
576620dde4b089e19319091b,https://recipeland.com/recipe/v/armenian-rice-pilaf-3369,pilaf,pilaf,1,1,0,0,0,0,"wild rice, jasmin rice, cherry rice"
5763a2e8e4b0ad09acad5a68,http://www.seriouseats.com/recipes/2012/06/phirni-indian-rice-pudding-recipe.html,,,,,,,,,
5763a3b7e4b0ad09acad80a7,http://allrecipes.co.uk/recipe/157/easy-rice-pudding.aspx,rice-pudding,pudding,1,1,0,0,0,0,"yasmin rice, brown rice"
5763a83de4b0ad09acae582a,http://allrecipes.co.uk/recipe/1681/honey-and-sultana-rice-pudding.aspx,rice-pudding,pudding,1,1,0,0,0,0,brown rice (instead of white rice)
5763add3e4b0ad09acaf614f,http://allrecipes.com/recipe/228914/old-fashioned-creamy-rice-pudding/,rice-pudding,pudding,1,1,0,0,0,0,Jasmine rice 
5763ab07e4b0ad09acaedce3,http://www.amazingmexicanrecipes.com/recipes/dessert-recipes/easy-mexican-dessert-recipes/mexican-rice-pudding-receta-de-arroz-con-leche/,rice-pudding,pudding,1,1,0,0,0,0,
576799d1e4b0aa7a394225fd,http://www.archanaskitchen.com/ven-pongal-south-indian-rice-and-lentil-pudding,rice-pudding,pudding,1,1,0,0,0,0,
5763a924e4b0ad09acae8231,http://www.bigoven.com/recipe/crock-pot-rice-pudding/157549,rice-pudding,pudding,1,1,0,0,0,0,"Jasmati rice, brown rice"
57662387e4b089e193195806,http://www.bigoven.com/recipe/dairy-free-rice-pudding/77505,rice-pudding,pudding,1,1,0,0,0,0,
5763a69fe4b0ad09acae0adc,http://www.chowhound.com/recipes/leftover-chinese-takeout-rice-pudding-11034,rice-pudding,pudding,1,1,0,0,0,0,?
5763a34be4b0ad09acad6c61,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a407e4b0ad09acad9042,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a470e4b0ad09acada479,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a625e4b0ad09acadf441,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a667e4b0ad09acae0067,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a767e4b0ad09acae3090,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,,
5763ab05e4b0ad09acaedcb2,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,,
5763aba0e4b0ad09acaef901,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,,
5763acaee4b0ad09acaf2bb2,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,,
5763adece4b0ad09acaf6593,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,,
576621dfe4b089e19319231a,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
576623f6e4b089e19319653d,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
57679aafe4b0aa7a39424fe0,http://www.closetcooking.com/2008/07/strawberry-rice-pudding-with-balsamic.html,rice-pudding,pudding,1,1,0,0,0,0,
5778dd8ee4b0592f499ac7ed,http://www.epicurious.com/recipes/food/views/rice-pudding-with-cranberry-walnut-sauce-10079,rice-pudding,pudding,1,1,0,0,0,0,
5763a84ee4b0ad09acae5b2a,http://www.foodnetwork.com/recipes/paula-deen/baked-rice-pudding-recipe2.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a859e4b0ad09acae5d70,http://www.kraftrecipes.com/recipes/chocolate-marshmallow-rice-pudding-70248.aspx,rice-pudding,pudding,1,1,0,0,0,0,
5763a6f2e4b0ad09acae1af9,http://www.recipe4living.com/recipes/real_rice_pudding.htm/,rice-pudding,pudding,1,1,0,0,0,0,
5763a86ce4b0ad09acae60fb,http://www.recipetips.com/recipe-cards/t--38114/creamy-rice-pudding.asp,rice-pudding,rice,1,1,0,0,0,0,
5766204ce4b089e19318fc36,http://www.recipetips.com/recipe-cards/t--54987/nanas-rice-pudding.asp,rice-pudding,pudding,1,1,0,0,0,0,
57679891e4b0aa7a3941ea83,http://www.rte.ie/lifestyle/food/recipes/2011/1010/745736-baked-coconut-rice-pudding/,rice-pudding,pudding,1,1,0,0,0,0,
5766282be4b089e19319e26d,http://www.seriouseats.com/recipes/2010/10/cook-the-book-miss-inas-down-home-rice-pudding-recipe.html,rice-pudding,pudding,1,1,0,0,0,0,
57662357e4b089e193194f4a,http://www.seriouseats.com/recipes/2010/10/pulut-hitam-black-rice-pudding-with-coconut-m.html,rice-pudding,pudding,1,1,0,0,0,0,
576797ace4b0aa7a3941c0d8,http://www.seriouseats.com/recipes/2010/10/simple-rice-pudding.html,rice-pudding,pudding,1,1,0,0,0,0,
5763a492e4b0ad09acadaa76,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
5763a4fee4b0ad09acadbdd4,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,soup,,,,,,,,
5763a812e4b0ad09acae505e,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,soup,,,,,,,,
5763a853e4b0ad09acae5c0e,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
5763aabde4b0ad09acaeceac,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
576797b0e4b0aa7a3941c197,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
57679aa3e4b0aa7a39424d7e,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
5778dd68e4b0592f499ac582,http://www.closetcooking.com/2010/02/sauerkraut-cabbage-roll-soup.html,Soup,,,,,,,,
5763acd8e4b0ad09acaf337f,http://www.justapinch.com/recipe/stuffed-pepper-soup-by-gail-herbest-gaillee,soup,,,,,,,,
5763ab10e4b0ad09acaedec9,http://www.livingafrugallife.com/frugal-recipe-friday-stuffed-pepper-soup/,soup,,,,,,,,
57679adbe4b0aa7a39425806,http://www.livingafrugallife.com/frugal-recipe-friday-stuffed-pepper-soup/,soup,,,,,,,,
5763a840e4b0ad09acae58b1,http://www.yummly.com/recipe/Cabbage-And-Ground-Beef-Soup-Recipezaar,soup,,,,,,,,
57661fffe4b089e19318ee4e,http://www.yummly.com/recipe/Unstuffed-Cabbage-Roll-Soup-Recipezaar,soup,,,,,,,,
576626d8e4b089e19319b1c8,http://allrecipes.com.au/recipe/14229/cabbage-rolls-with-yoghurt-sauce.aspx,Stuffed Cabbage,stuffed,0,0,0,1,1,1, grated cauliflower (for paleo)
5778e026e4b0592f499af43d,http://allrecipes.com/recipe/16323/stuffed-cabbage/,Stuffed Cabbage,stuffed,0,0,0,1,1,1,"bulgur , quinoa "
5763a360e4b0ad09acad7048,http://allrecipes.com/recipe/218234/bob-evans-stuffed-cabbage/,Stuffed Cabbage,stuffed,0,0,0,1,1,1,"bulgur , quinoa "
5763a48de4b0ad09acada99e,http://food52.com/recipes/11943-stuffed-cabbage-leaves,Stuffed Cabbage,stuffed,0,0,0,1,1,1,"quinoa, bulgur"
5763ae13e4b0ad09acaf6cc9,http://food52.com/recipes/25833-not-your-grandmother-s-cabbage-roll-soup,Stuffed Cabbage,stuffed,0,0,0,1,1,1,
5763a7b1e4b0ad09acae3e9a,http://rantsfrommycrazykitchen.com/2014/06/15/polish-coal-miner-piggies-stuffed-cabbage-rolls-golabki-sundaysupper/,Stuffed Cabbage,stuffed,0,0,0,1,1,1,"quinoa, bulgur"
5763a4fae4b0ad09acadbd32,http://whattocooktoday.com/cabbage-rolls-kaalikaaryleet.html,Stuffed Cabbage,stuffed,0,0,0,1,1,1,
5763a9d7e4b0ad09acaea3c6,http://www.foodnetwork.com/recipes/bobby-flay/cabbage-rolls-recipe.html,stuffed cabbage,stuffed,0,0,0,1,1,1,
57679a8ce4b0aa7a39424901,http://www.olgasflavorfactory.com/main-course/golubtsi-cabbage-rolls/,stuffed cabbage,stuffed,0,0,0,1,1,1,
5763a827e4b0ad09acae542c,http://www.panningtheglobe.com/2012/12/16/russian-stuffed-cabbage/,stuffed cabbage,stuffed,0,0,0,1,1,1,
57661f9de4b089e19318df70,http://www.recipetips.com/recipe-cards/t--160589/beef-stuffing-for-cabbage-leaves.asp,stuffed cabbage,stuffed,0,0,0,1,1,1,
5763a798e4b0ad09acae3a0e,http://www.recipetips.com/recipe-cards/t--3014/slow-cooked-cabbage-rolls.asp,stuffed cabbage,stuffed,0,0,0,1,1,1,
576798dce4b0aa7a3941f875,http://www.sanjeevkapoor.com/Recipe/Cabbage-and-Rice-Rolls-Sanjeev-Kapoor-Kitchen-FoodFood.html,stuffed cabbage,stuffed,0,0,0,1,1,1,
5763a425e4b0ad09acad95e8,http://www.womansday.com/recipefinder/stuffed-cabbage-cranberry-tomato-sauce-121956,stuffed cabbage,stuffed,0,0,0,1,1,1,
5763a7dde4b0ad09acae46b6,http://kosherscoop.com/2011/12/stuffed-chicken-capons/,stuffed chicken,stuffed,1,1,0,0,0,0,
5778de26e4b0592f499ad205,http://food52.com/recipes/11894-mom-s-stuffed-grape-leaves,stuffed leaves,stuffed,0,0,0,1,1,1,"quinoa, bulgur"
57679a91e4b0aa7a394249d4,http://www.simplecomfortfood.com/2012/11/28/rice-stuffed-meatballs/,stuffed meat,stuffed,0,1,1,0,0,0,
57679910e4b0aa7a394201c6,http://boysahoy.com/cheesy-veggie-stuffed-taco-peppers/,stuffed pepper,stuffed,0,0,0,1,1,1,quinoa
5778e145e4b0592f499b0659,http://boysahoy.com/cheesy-veggie-stuffed-taco-peppers/,stuffed pepper,stuffed,0,0,0,1,1,1,
57679909e4b0aa7a39420059,http://www.bettycrocker.com:10806/recipes/rice-and-kale-stuffed-peppers/2e737987-46ec-43d8-9eb4-2ac94dab2d3a?p=1,stuffed pepper,stuffed,0,0,0,1,1,1,
57662684e4b089e19319a590,http://www.epicurious.com/recipes/member/views/italian-sausage-stuffed-peppers-51992201,stuffed pepper,stuffed,1,1,1,0,0,0,
5766274de4b089e19319c007,http://www.foodchannel.com/recipes/recipe/easy-beef-stuffed-peppers/,stuffed pepper,stuffed,0,0,0,1,1,1,
5763a371e4b0ad09acad7387,http://www.mccormick.com/Gourmet/Recipes/Main-Dishes/Cuban-Stuffed-Peppers,stuffed pepper,stuffed,0,0,0,1,1,1,
5763a390e4b0ad09acad798f,http://www.recipetips.com/recipe-cards/t--109006/easy-crockpot-stuffed-bell-peppers.asp,stuffed pepper,stuffed,0,0,0,1,1,1,
5763ad5ae4b0ad09acaf4b41,http://www.recipetips.com/recipe-cards/t--152805/egg-cellent-stuffed-peppers.asp,stuffed pepper,stuffed,0,0,0,1,1,1,
5763a824e4b0ad09acae53ac,http://www.recipetips.com/recipe-cards/t--5667/stuffed-green-peppers.asp,stuffed pepper,stuffed,0,0,0,1,1,1,
5763a506e4b0ad09acadbf31,http://www.recipetips.com/recipe-cards/u--3638/stuffed-bell-peppers.asp,stuffed pepper,stuffed,0,0,0,1,1,1,
57679958e4b0aa7a39420f97,http://www.recipetips.com/recipe-cards/u--3638/stuffed-bell-peppers.asp,stuffed pepper,stuffed,0,0,0,1,1,1,
57679ab1e4b0aa7a3942505c,http://www.recipetips.com/recipe-cards/u--4374/stuffed-green-peppers.asp,stuffed pepper,stuffed,0,0,1,1,1,1,
576620f5e4b089e193190c4c,http://www.foodnetwork.com/recipes/stuffed-pork-shoulder-a-lo-caja-china-recipe.html,stuffed pork,stuffed,1,1,0,0,0,0,
5763ad2be4b0ad09acaf427c,http://www.foodily.com/r/FnLjDzTb74-poblano-and-cheddar-stuffed-portobello-mushrooms-by-food-wine,stuffed portabello,stuffed,1,1,0,0,0,0,
57679854e4b0aa7a3941df90,http://www.foodnetwork.com/recipes/stuffed-pepper-served-an-a-marinated-portobello-recipe.html,stuffed portabello,stuffed,1,1,0,0,0,0,
5763a5cde4b0ad09acade43e,http://www.everydayhealth.com/recipes/stuffed-sole-fillets/,stuffed sole,stuffed,1,1,0,0,0,0,
5763a558e4b0ad09acadce48,http://www.epicurious.com/recipes/food/views/stuffed-turkey-101738?print=true,stuffed turkey,stuffed,1,1,0,0,0,0,cauliflower rice
5778dce8e4b0592f499abc63,http://comfyinthekitchen.com/2010/11/grandmas-stuffed-peppers/,stuffed-peppers,stuffed,0,0,0,1,1,1,"quinoa, bulgur, califlower rice"'''

parser = OptionParser()
rdb = IOTools.RecipeDB(option_parser = parser)

def convertCSVToFeedbackArray(file_string, source_ing, target_ing):
  res = []
  missing_urns = []
  lines = file_string.split('\n')
  col_names = lines[0].split(',')
  col_i = 0
  for cn in col_names:
    if cn.lower() == target_ing:
      break
    col_i = col_i + 1
    if col_i == len(col_names):
      print 'Couldnt find %s in %s' % (target_ing, lines[0])
      sys.exit(0)
  print 'col_i = %d' % col_i

  for line in lines[1:]:
    cols = line.split(',')
    urn = cols[1]

    if cols[col_i] in ['0', '1']:
      # First, identify the "rice" ingredient  in recipe
      recipe = rdb.find_one(query = {'urn' : urn})
      if recipe is None:
        print 'Couldnt find recipe with urn %s, skipping' % urn
        missing_urns.append(urn)
        continue
      ings = IOTools.getCannonicalIngredientList(recipe)
      source_in_recipe = None
      for ing in ings:
        if ing.lower().find(source_ing) >= 0:
          source_in_recipe = ing
          break
      if source_in_recipe is None:
        print 'Couldnt find rice in recipe %s.  Ingredients are: %s' % (recipe['_id'].__str__(), ings.__str__())
        continue

      polarity = int(cols[col_i]) * 2 - 1  # convert to -1/1
      f = Feedback(
        personId = PERSONID,
        recipeId = recipe['_id'], # the new Id's don't match, we have to match by urn
        urn = urn,
        DBVersion = 'legacy',
        timestamp = '',
        taskId = TASK_ID,
        feedback = [FeedbackData(
          type_ = TYPE,
          ref = source_in_recipe,
          info = {'polarity' : polarity, 'target' : target_ing},
          comments = 'Substitution of %s' % source_ing)])            
      res.append(f)
  return res, missing_urns


feedback = []
for t in RICE_SUB_TARGETS:
  res, missing_urns = convertCSVToFeedbackArray(RICE_FILE_CONTENTS, 'rice', t)
  print 'There are %d bits of feedback for rice->%s' % (len(res), t) 
  feedback = feedback + res
  print 'Number of missing urns: %d' % len(missing_urns)

  
# Consolidate multiple feedbacks for the same urn into one feedback
feedback_by_urn = {}
for f in feedback:
  if not f.urn in feedback_by_urn:
    feedback_by_urn[f.urn] = f
  else:
    feedback_by_urn[f.urn].feedback.append(f.feedback[0])

for urn in feedback_by_urn:
  print urn, feedback_by_urn[urn].getStruct()

feedback = feedback_by_urn.values()

print 'Number of recipes with feedback: %d' % len(feedback_by_urn)

# Write to db
print 'Writing records to feedback collection.'
for f in feedback:
  existing_record = rdb.get_feedback_coll().find_one({'urn' : f.urn, 'taskId' : TASK_ID})
  if existing_record:
    print 'Feedback for %s already exists in record id %s' % (f.urn, existing_record['_id'].__str__())
    rdb.get_feedback_coll().update({'_id' : existing_record['_id']}, {"$set" : f.getStruct()}, upsert=False)  #{'feedback' : f.getStruct()['feedback'], 'recipeId' : f.recipeId}}, upsert=False)
  else:
    print 'Feedback for %s doesnt exist, creating new record' % f.urn
    rdb.get_feedback_coll().insert_one(f.getStruct())


print 'Removing urns that are missing from recipe collection from feedback collection...'
for mu in missing_urns:
  rdb.get_feedback_coll().remove({'urn' : mu, 'taskId' : TASK_ID})
print 'Done.'
