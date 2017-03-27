/*
   use map-reduce to produce the list of substitutions at start
*/
db.substitutionsListSource.drop();
db.dropDownTitle.drop();
db.substitutionsListSource.find().sort( { 'value.description': 1}).forEach( function(a) { db.dropDownTitle.insert( a.value ); });
db.substitutionsList.insert( { created: new Date() });
db.substitutionsList.createIndex( { "created": 1 }, { expireAfterSeconds: 3600 } )