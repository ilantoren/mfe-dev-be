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

    getSubsWithIngredients(id){
        let that = this;
        return new Promise(function (resolve, reject) {
            if (!id){
                log.error("no id passed");
                reject("no id passed");
            }
            that.getSubstitute(id).then(function(recipe){
                log.info("Found recipe: " + id);
               if (!recipe){
                   log.error("no recipe found for id" + id);
                   reject("no recipe found for id" + id);
               }
               let subs = recipe.subs;

               let optionIds = [];
               if (!subs || subs.length == 0){
                   log.error("no subs for this recipe");
                   reject("no subs for this recipe");
               }
                log.info("recipe: " + id + " has " + subs.length + " subs");
               subs.forEach(function(sub){
                   let options = sub.options;
                   if (options){
                       log.info("sub " + sub._id + " has " + options.length + " options");
                       options.forEach(function(option){
                           optionIds.push(ObjectID.createFromHexString(option.targetId));
                       });
                   }
               });

                that.getMappings(optionIds).then(function(data){
                    if (!data || data.length == 0){
                        log.error('No results from entity mapping');
                        reject('No results from entity mapping');
                    }
                    let ndb_nos = [];
                    log.info("Found " + data.length + " mappings");
                    data.forEach(function(mapping){
                        ndb_nos.push('' + mapping.ndb_no);
                    });

                    log.info("searching for " + ndb_nos.length + " ingredients");
                    log.dir(ndb_nos);

                    that.getIngredientsByUid(ndb_nos).then(function(ingredients){
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

    getIngredientsByUid(ids){
        let mapping = this.app.locals.db.collection("ingredientPOJO");
        return mapping.find({uid:{$in:ids}}).toArray();
    }
}


module.exports = new RecipeDao();
