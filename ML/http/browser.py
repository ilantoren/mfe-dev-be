from flask import Flask, request
import jinja2
import sys


app = Flask(__name__)
sys.path.append("../")
import IOTools


PAGE_HTML = '''
<html>
<body>
<div id="form">
<form action="/myfavoreats/browser" method="get" id="theform">
Enter a recipe Id: <input type="text" name="id" value="{{id}}"> </input>
 <input type="checkbox" name="filter_gluten">Show gluten subs only</input>
<br>
Database:
 <select name="db" form="theform">
  <option value="developdb">Development database</option>
  <option value="glutendb">Gluten database </option>
 </select>
<input type="hidden" name="formfilled" value="1"></input>
<input type="submit" value="Submit"> </input>
</form>
</div>

<div id="recipe">
{{recipe}}
</div>
<div id="substitutions">
{{substitutions}}
</div>
</body>
</html>
'''


RECIPE_HTML = '''
<h1> {{title}}</h1>
<h2> id={{id}}</h2>
<h2> Link: <a href="{{link}}"> {{link}} </a> <h2>
<img src="{{img}}" style="max-width:500px;"> </img>
<h2> Ingredients:</h2>
<table>
<tr>
<td> Food </td>
<td> Quantity </td>
<td> Measure </td>
</tr>
{% for ing in ingredients %}
<tr>
<td> {{ing.food}} </td> 
<td> {{ing.quantity}} </td>
<td> {{ing.measure}} </td>
</tr>
{% endfor %}
</table>
'''

SUBSTITUTIONS_HTML = '''
<h2> Substitutions: </h2>
prob_threshold = {{prob_threshold}} <br>
<table>
<tr>
<td> Source </td>
<td> Target </td>
<td> Ratio </td>
<td> Notes </td>
{% if debug_mode %}
  <td> Probability </td>
  <td> Algorithm </td>
{% endif %}
</tr>
{% for sub in substitutions %}
  {% for option in sub.options %}
    <tr>
      <td> {{sub.source}} </td>
      <td> {{option.target}} </td>
      <td> {{option.ratio}} </td>
      <td> {{option.moreinfo}} </td>
      {% if debug_mode %}
        <td> {{option.probability}} </td>
        <td> {{sub.origin}}, version {{sub.version}} </td>
      {% endif %}
    </tr>
  {% endfor %}
  {% if sub.target %}
    <tr>
      <td> {{sub.source}} </td>
      <td> {{sub.target}} </td>
      <td> {{sub.ratio}} </td>
      <td> {{sub.moreinfo}} </td>
      {% if debug_mode %}
        <td> {{sub.probability}} </td>
        <td> {{sub.origin}} </td>
      {% endif %}
    </tr>
  {% endif %}
{% endfor %}
</table>
'''

# Fliter only substitutions that take a glutenous ingredient to a gluten-free one
def filter_glutenfree(subs, rdb):
  ret = []

  ing_list = []
  for s in subs:
    ing_list.append(s['source'])
    for o in s['options']:
      ing_list.append(o['target'])
  entities, ingredients = rdb.fetchEntitiesWithUsdaInfo(ing_list)

  for sub in subs:
    source = sub['source']
    if not source in entities: continue
    if not 'ndb_no' in entities[source]: continue
    ndb = entities[source]['ndb_no']
    if not ndb in ingredients: continue
    if not 'factors' in ingredients[ndb]: continue
    for factor in ingredients[ndb]['factors']:
      if not 'value' in factor: continue
      if factor['value'] in [ u'CONTAINS GLUTEN', u'MAY CONTAIN GLUTEN']:
        ret.append(sub)
        new_options = []
        for o in sub["options"]:
          target = o['target']
          if not target in entities: continue
          if not 'ndb_no' in entities[target]: continue
          ndb = entities[target]['ndb_no']
          if not ndb in ingredients: 
            new_options.append(o)
            continue
          if not 'factors' in ingredients[ndb]:
            new_options.append(o)
            continue
          has_gluten = False
          for factor in ingredients[ndb]['factors']:
            if not 'value' in factor: continue
            if factor['value'] in [ u'CONTAINS GLUTEN', u'MAY CONTAIN GLUTEN']:
              hase_gluten = True
              break
          if not has_gluten: new_options.append(o)
        sub['options'] = new_options
        break
  return ret

@app.route('/')
def get():
  id_ = request.args['id'] if 'id' in request.args else ''
  prob_threshold = 0.4
  if ('prob_threshold' in request.args and request.args['prob_threshold']!= ''):
    prob_threshold = float(request.args['prob_threshold'])
  # this means we got to the browser without submitting, just return
  if 'formfilled' in request.args and request.args['formfilled'] != '1': 
    return(jinja2.Template(PAGE_HTML).render({}))

  dont_show_sugar = True
  dont_show_salt = True
  dont_show_pepper = True
  dont_show_water = True

  rand = False
  if id_ is None or not len(id_):
    rand = True
      
      
  rdb = IOTools.RecipeDB(config_file="../local.config")

  if rand:
    recipe = rdb.get_random_recipe()
    id_ = recipe["_id"].__str__()
  else:
    recipe = rdb.get(id_)
  ingredients = []
  for step in recipe["steps"]:
    for line in step["lines"]:
      ingredients.append(line)

  recipe_html = jinja2.Template(RECIPE_HTML).render({
    'title' : recipe['title'] if 'title' in recipe else 'Unknown Title',
    'id' : recipe['_id'].__str__(),
    'img' : recipe['photos'] if 'photos' in recipe else '',      
    'ingredients' : ingredients,
    'link' : recipe['urn'] if 'urn' in recipe else ''})


  subs = rdb.getSubByRecipeId(id_)
  deduper = {}

  # weed out subs that appear with too low probability
  subs_for_view = []
  if subs is not None:
    for s in subs["subs"]:
      if dont_show_sugar:
        if s["source"].lower().find("sugar") >= 0: continue
      if dont_show_salt:
        if s["source"].lower().find("salt") >= 0: continue
      if dont_show_pepper:
        if s["source"].lower().find("pepper") >= 0: continue
      if dont_show_water:
        if s["source"].lower().find("water") >= 0: continue

      sub = {"source" : s["source"], "origin" : s["origin"], "options" : []}
      subs_for_view.append(sub)
      for o in s["options"]:
        if "probability" in o: 
          o["probability"] = '%.2f' % float(o["probability"])
        else: o["probability"] = "1.0"
        if 'qty' in subs and 'qty' in o:
          o['ratio'] = '1:%.1f' % (float(o['qty'])/float(subs['qty']))
        else:
          o['ratio'] = '1:1'
        if 'moreinfo' in o:
          if len(o['moreinfo']) < 3: o['moreinfo'] = ''
        else: o['moreinfo'] = ''
        if (float(o["probability"]) >= prob_threshold):
          for_deduper = sub["source"]  + o["target"] + o['ratio'] + o['moreinfo'] + sub["origin"]  + o['probability']
          if not for_deduper in deduper:
            sub["options"].append(o)
            deduper[for_deduper] = 1
  print(subs_for_view)
 
  if not subs_for_view:
    substitutions_html = jinja2.Template(SUBSTITUTIONS_HTML).render({})  
  else:
    if filter_gluten: subs_for_view = filter_glutenfree(subs_for_view, rdb)
    substitutions_html = jinja2.Template(SUBSTITUTIONS_HTML).render({
      "substitutions" : subs_for_view, "debug_mode":False, "prob_threshold" : '%f' % prob_threshold
    })

  return jinja2.Template(PAGE_HTML).render({'recipe' : recipe_html, 'id' : id_, 'substitutions': substitutions_html})



