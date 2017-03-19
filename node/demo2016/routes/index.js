var express = require('express');
var router = express.Router();
var path = require('path');
var Client = require('node-rest-client').Client;
var client = new Client();

fs = require('fs');

router.get('/main', function(req, res, next) {
		console.log( "Attempting to get the main.js");
		fs.readFile(path.join(__dirname, '/build/main.js'), 'utf8', function (err,data) {
				if ( err ) {
					console.log(err)
				}
					res.send(data);
		});
});

router.get('/defiant', function(req, res, next) {
		console.log( "Attempting to get the defiant.js");
		fs.readFile(path.join(__dirname, '/defiant.js'), 'utf8', function (err,data) {
				if ( err ) {
					console.log(err)
				}
					res.send(data);
		});
});

/* GET home page. */
router.get('/', function(req, res, next) {
    res.render('index.html');
});

 
module.exports = router;

