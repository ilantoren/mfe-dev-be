/*eslint-env node*/
"use strict"
//------------------------------------------------------------------------------
// node.js starter application for Bluemix
//------------------------------------------------------------------------------

// This application uses express as its web server
// for more info, see: http://expressjs.com
var application_root = __dirname,
    express = require("express"),
    path = require("path");

// cfenv provides access to your Cloud Foundry environment
// for more info, see: https://www.npmjs.com/package/cfenv
var cfenv = require('cfenv');
// get cloud foundry appEnv
var AppEnv = cfenv.getAppEnv()
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var index = require('./routes/index');
var recipes = require('./routes/recipes');
var controller = require('./routes/controller');
var Promise = require('bluebird');
var config = require('./config');
var log = require('./log');


// create a new express server
var app = express();

var mappedEntityDao = require('./dao/mapped-entity-dao');
var recipeDao = require('./dao/recipe-dao');
var rulesDao = require('./dao/rules-dao');
mappedEntityDao.app = app;
recipeDao.app = app;
rulesDao.app = app;


var mongoCon = require('mongodb').MongoClient;


//view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');
app.engine('html', require('ejs').renderFile);
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: false
}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', index );
app.use('/api', recipes);
app.use('/controller', controller);

// get the app environment from Cloud Foundry
var appEnv = cfenv.getAppEnv();
log.info(JSON.stringify(appEnv ) );
log.info( getServiceInfo() );
var mongoInstance = appEnv.getService("mongo-instance");
var mongoUrl = JSON.stringify( mongoInstance.credentials.credential );
// start server on the specified port and binding host
app.listen(appEnv.port, '0.0.0.0', function() {
   if ( appEnv ) {
       log.info( mongoUrl );
   }
   var mongoCfg = config.mongo;
    mongoCon.connect('mongodb://' + mongoCfg.url + '/' + mongoCfg.db)
        .then(function(db) {
            log.info("Created promised Mongo Connection");
            app.locals.db  = db;
        });
    log.info("server starting on " + appEnv.url);
});

function getServiceInfo() {
  var serviceA = AppEnv.getService("ServiceA");
  var serviceB = AppEnv.getService("ServiceB");
  var serviceX = AppEnv.getService(/Service.*/) || {name: "(not bound)"};
  var services = AppEnv.getServices();

  var output = [];
  output.push("  isLocal?        - " + yesNo(AppEnv.isLocal));
  output.push("  ServiceA bound? - " + yesNo(serviceA));
  output.push("  ServiceB bound? - " + yesNo(serviceB));
  output.push("  /Service.*/     - " + serviceX.name);

  output.push("bound services:");

  var someServices = false;
  for (var serviceName in services) {
    var service = services[serviceName];
    var creds   = JSON.stringify(service.credentials);

    output.push("  " + service.label + ": " + service.name + ": " + creds);

    someServices = true;
  }

  if (!someServices) {
    output.push("  (none)");
  }
  return output.join("\n");
}

//------------------------------------------------------------------------------
function yesNo(bool) {
  return bool ? "yes" : "no";
}

//-----------------------------------------------------------------------------
