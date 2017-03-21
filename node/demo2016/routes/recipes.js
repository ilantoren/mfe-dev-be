var express = require('express');
var router = express.Router();

var cfenv = require('cfenv');
var appEnv = cfenv.getAppEnv();
var assert = require('assert');
var Client = require('node-rest-client').Client;
//decode64 = require('base64').decode;
 
// create a custom timestamp format for log statements
var SimpleNodeLogger = require('simple-node-logger'),
	opts = {
		logFilePath:'mylogfile.log',
		timestampFormat:'YYYY-MM-DD HH:mm:ss.SSS'
	},
	
log = SimpleNodeLogger.createSimpleLogger( opts )
log.setLevel('info');

var client = new Client();
var host = appEnv.url;


var service = appEnv.getService("mfe-tech-rest")

if ( service == null) {
	var resthost = "localhost";
	var restport = ":8080";
}else {
	var resthost = "mfe-tech-rest.mybluemix.net";
	var restport = ""
}

client.registerMethod("recipesChangedChild",  "http://"+ resthost + restport + "/recipes/changed/child/${id}", "GET");
client.registerMethod("recipes",              "http://"+ resthost + restport + "/recipes", "GET");
client.registerMethod("recipe",               "http://"+ resthost + restport + "/recipe/${id}", "GET");
client.registerMethod( "originals" ,          "http://"+ resthost + restport +  "/recipes/original", "GET");
client.registerMethod( "changedTitle",        "http://"+ resthost + restport + "/recipes/changed/title", "GET");
client.registerMethod( "childAndParent",      "http://"+ resthost + restport + "/recipes/changed/pair/${id}", "GET");
client.registerMethod( "recipeByParent",      "http://"+ resthost + restport + "/recipes/parent/${id}", "GET");
client.registerMethod( "recipeBySubstitute",  "http://"+ resthost + restport + "/recipes/substitute/${description}", "GET");
client.registerMethod( "recipeBySearchPhrase","http://"+ resthost + restport + "/recipes/title/search/${phrase}", "GET");
client.registerMethod( "mongoImage"          ,"http://"+ resthost + restport + "/recipe/image/${id}", "GET");
client.registerMethod( "recipeWithSubstitute", "http://"+ resthost + restport + "/recipes/with-substitute/${id}", "GET");
client.registerMethod( "recipeSubstitutions", "http://" +resthost  + restport + "/recipe/substitutions", "GET" );



	

router.get( '/recipe/local/image/:id', function( req, res, next ) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    console.log( req.params );
    console.log('request image for ' +  req.params.id );
    var id = req.params.id;
		db.find( {recipeId : id } , function( err, doc) {
			   if ( err  ) {
			   	   console.log( 'error ' + err );  	   
			   }
			   if ( doc &&  doc.imagePNG) {
			   	   console.log( doc.toString() );
			   	   res.setHeader('Content-Type', 'image/png')
			   	   console.log('recipeId:' +  doc.recipeId );
			   	   console.log( doc.imagePNG );
			   	   res.send( (new Buffer(doc.imagePNG, 'base64')) );
			   }
			   else {
			   	   res.setHeader('Content-Type', 'image/svg+xml')
			   	   console.log( "default image");
			   	   var xml = '<svg width="250" height="200" viewBox="0 0 250 200"><rect x="0" y="0" height="200" width="250" style="fill:blue"/></svg>';
			   	   res.send(xml);
			   }
			});
	});

router.get( '/recipe/image/:id', function( req, res, next ) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    console.log( req.params );
    console.log('request remote image for ' +  req.params.id );
    var id = req.params.id;
    var args = { path: {'id': id}};
    client.methods.mongoImage( args, function(data, response ) {
    		if ( data && data.imagePNG ) {
    			//console.log( data );
    		    log.debug(data);
    			res.setHeader('Content-Type', 'image/png') 			
			   	 res.send( new Buffer( data.imagePNG, 'base64') );
    		}
    		else {
    			console.log( response );
    			res.setHeader('Content-Type', 'image/svg+xml')
			   	   console.log( "default image");
			   	   var xml = '<svg width="250" height="200" viewBox="0 0 250 200"><rect x="0" y="0" height="200" width="250" style="fill:blue"/></svg>';
			   	   res.send(xml);
    		}
    });
});


/* GET All Recipe */
router.get('/recipes', function(req, res, next) {
   res.header("Access-Control-Allow-Origin", host);
   res.header("Access-Control-Allow-Methods", "GET, POST");
   client.methods.recipes( function( data, response ) {
   		  log.debug( response );
   		  return data;
   });
   
});
/* list of all substitutions for the recipes */
router.get("/recipe/substitutions", function(req, res, next) {
	res.header("Access-Control-Allow-Origin", host);
	res.header("Access-Control-Allow-Methods", "GET, POST");
	console.log("Getting list of recipe substitutions from server");
	var args = { path: { }};
	client.methods.recipeSubstitutions(args, function(data, response) {
		if (data) {
			res.send(data);
		} else {
			log.debug(response);
			res.send( {"error": "see log"} );
		}
	});
	
});
 
/* GET One Recipe with the provided ID */
router.get('/recipe/:id', function(req, res, next) {
res.header("Access-Control-Allow-Origin", host);
res.header("Access-Control-Allow-Methods", "GET, POST");
console.log( req.params.id );
var id = req.params.id;
var args = { path: {'id': id }};
	client.methods.recipe( args, function( data, response) {
		if ( data ) {
			res.send( data );
		}
		else {
			log.debug( response );
		}
	});
});

router.get('/recipes/changed/title', function( req, res, next ) {
    res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    var myset = [];
	client.methods.changedTitle( function ( data, response ) {
		log.debug("changedTitle\n" +  response );
		if (data )
			res.send( data);
	});
});

router.get('/recipes/title/search/:phrase', function( req, res, next ) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");	
    var searchphrase = req.params.phrase;
    console.log( '/recipes/title/search/:phrase  PHRASE: ' + searchphrase);
    var phrase = escape( searchphrase );
    var args = { path : { "phrase": phrase}};
    client.methods.recipeBySearchPhrase( args, function( data, response ) {
    		if ( data )
    			res.send(data);
    		else 
    			console.log( response );
    });
});

router.get('/recipes/original', function( req, res, next ) {
    res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
	client.methods.originals( function( data, response) {
			console.log( response );
			if (data)
				return data;
	});
});

router.get('/recipes/substitute/:description' , function( req, res, next ) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
   
    var description = req.params.description;
    
    console.log( "Calling /recipes/substitute on web side with " + description );	
    var args = { path: { "description": escape(description)}};
    client.methods.recipeBySubstitute( args, function( data, response ) {
    		if (data) {
    			res.send( data );
    		}
    		else {
    			console.log(response);
    		}
    });
});

router.get('/recipe/parent/:id' , function( req, res, next) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    console.log( "Calling /recipe/parent/id on web side with " + req.params.id );
    var pid = req.params.id;
    var args = {
    	path: { "id" : pid }
    }
    client.methods.recipeByParent( args, function( data, response ) {
    		//console.log( response ); 
    		//console.log( data );
    		if ( data )
    			res.send( data );
    });
}); 

router.get('/recipes/modified/:id', function (req, res, next) {
    res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    res.setHeader('Content-Type', 'text/xml; charset=utf-8')
    console.log( req.params );
    var child = req.query.child
    var id = req.params.id
    var title = req.query.title;
    var args = {
    	path: { "id" : id },
    	parameters: { "child": child, "title": title }
    }
    console.log( "childAndParent: " + id + " " + child + " " + title);
    client.methods.childAndParent( args, function( data, response ) {
    		//console.log( response ); 
    		//console.log( data );
    		if ( data) 
    			res.send( data );
    });
}); 

router.get('/recipes/with-substitute/:id', function (req, res, next) {
    res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    res.setHeader('Content-Type', 'text/xml; charset=utf-8')
    console.log("/recipes/with-substitute/:id  " );
    console.log(  req );
   
    var id =     req.params.id
    var target = req.query.target;
 
    var args
	   if ( target ) {
		args = {
			path : {
				"id" : id },
			parameters: { "target": target }
		}
	} else {
		args = {
			path : {
				"id" : id
			}
		};
	}
    console.log( args );
    console.log( "/recipes/with-substitute/: " + id + " target is " + target);
    var regexp = new RegExp(/>(\d|\.)+\s?[gm|kcal]+</, 'ig');
    client.methods.recipeWithSubstitute( args, function( data, response ) {
    		if ( data && typeof myVar === 'string' ) { 
    			data = data.replace(regexp, ">$1<");
    			res.send( data );
    		}
    });
}); 

router.get('/recipes/changed/child/:id', function( req, res, next) {
	res.header("Access-Control-Allow-Origin", host);
    res.header("Access-Control-Allow-Methods", "GET, POST");
    res.setHeader('Content-Type', 'text/xml; charset=utf-8');
    console.log( '/recipes/changed/child/' + req.params );
    var id = req.query.id
    var parentId = req.query.parentId;
    var args = {
    	path: {"id" : id },
    	parameters: {"parentId": parentId}
    };
    client.methods.recipesChangedChild( args, function( data, response ) {
    		if ( data) 
    			res.send( data );
    });
    
});

module.exports = router;
