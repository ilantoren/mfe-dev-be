var express = require('express');
var router = express.Router();
var mappedEntityDao = require('../dao/mapped-entity-dao');
var recipeDao = require('../dao/recipe-dao');
ObjectID = require('mongodb').ObjectID;

router.get('/ingredients/search/:text', function( req, res, next) {
    console.log( '/ingredients/search/' + req.params );
    let text = req.params.text;
    mappedEntityDao.searchAlts(text, 100).then(function(data){
        let alts = [];
        data.forEach(function(doc){
           doc.altName.forEach(function(alt){
               if (alt.alt.indexOf(text) > -1){
                   alts.push(alt.alt);
               }
           });
        });
       res.send(alts);
    }).catch(function(err){
        res.status(500).send(err);
    });
});


router.get('/recipes/find/:text', function( req, res, next) {
    console.log( '/recipes/find/' + req.params );
    let text = req.params.text;
    recipeDao.searchByTitle(text).then(function(data){
        res.send(data);
    }).catch(function(err){
        res.status(500).send(err);
    });
});

router.get('/recipes/autocomplete/:text', function( req, res, next) {
    console.log( '/recipes/autocomplete/' + req.params );
    let text = req.params.text;
    recipeDao.autoComplete(text).then(function(data){
        res.send(data);
    }).catch(function(err){
        res.status(500).send(err);
    });
});

router.get('/recipes/recipe/:id', function( req, res, next) {
    console.log( '/recipes/recipe/' + req.params );
    let id = req.params.id;
    recipeDao.getRecipe(id).then(function(data){
        res.send(data);
    }).catch(function(err){
        res.status(500).send(err);
    });
});

router.get('/recipes/recipe-with-subs/:id', function( req, res, next) {
    console.log( '/recipes/recipe/' + req.params );
    let id = req.params.id;
    recipeDao.adjustRecipeForGluten(id).then(function(data){
        res.send(data);
    }).catch(function(err){
        res.status(500).send(err);
    });
});

module.exports = router;