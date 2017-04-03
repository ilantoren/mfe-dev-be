var log = require('../log');

class RecipeDao{

    constructor(){
        this.limit = 100;
    }

    searchByTitle(text){
        let recipeCol = this.app.locals.db.collection("recipePOJO");
        return recipeCol.find({$text: {$search: text}}).limit(this.limit).toArray();
    }

    autoComplete(text){
        let recipeCol = this.app.locals.db.collection("recipePOJO");
        return recipeCol.find({title: {$regex:new RegExp(text,"g"), $options: "si"}}, {title:1, _id:1}).limit(this.limit).toArray();
    }

    getRecipe(id){
        let recipeCol = this.app.locals.db.collection("recipePOJO");
        return recipeCol.findOne({_id: ObjectID.createFromHexString(id)});
    }

    getSubstitute(id){
        let subCol = this.app.locals.db.collection("substitutions");
        return subCol.findOne({_id: ObjectID.createFromHexString(id)});
    }

    adjustRecipeForGluten(id){
        let That = this;
        return new Promise(function (resolve, reject) {
            if (!id){
                reject("no id passed");
            }
            That.getSubstitute(id).then(function(recipe){
               if (!recipe){
                   reject("no recipe found for id" + id);
               }
               let subs = recipe.subs;

               let optionIds = [];
               if (!subs || subs.length == 0){
                   reject("no subs for this recipe");
               }
               subs.forEach(function(sub){
                   let options = sub.options;
                   if (options){
                       options.forEach(function(option){
                           optionIds.push(ObjectID.createFromHexString(option.targetId));
                       });
                   }
               });

                That.getMappings(optionIds).then(function(data){
                    if (!data){
                        reject('No results from entity mapping');
                    }
                    let ndb_nos = [];
                    data.forEach(function(mapping){
                        ndb_nos.push('' + mapping.ndb_no);
                    });

                    That.getIngredients(ndb_nos).then(function(ingredients){
                       resolve(ingredients);
                    });
               }).catch(function(err){
                    reject(err);
                });
           });
        });
    }

    getMappings(ids){
        let mapping = this.app.locals.db.collection("entityMapping");
        return mapping.find({_id:{$in:ids}}).toArray();
    }

    getIngredients(ids){
        let mapping = this.app.locals.db.collection("ingredientPOJO");
        return mapping.find({_id:{$in:ids}}).toArray();
    }
}


module.exports = new RecipeDao();
