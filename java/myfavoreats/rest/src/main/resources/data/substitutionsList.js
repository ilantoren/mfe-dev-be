
var list = [];
var createSubstitutions = function() {
   print ("process start");
   db.dropDownTitle.drop();
   db.substitutionsListSource.find().sort( { 'value.description': 1}).forEach( function(a) {db.dropDownTitle.insert( a.value )});
   db.dropDownTitle.find({}).forEach( function(a) {  var item = { "sourceId": a.sourceId, "targetId": a.targetId, "description": a.description}; list.push( item  ); });
   db.substitutionsList.insert( { created: new Date(),  list: list, _class: "com.mfe.SubstitutionsList" });
   db.substitutionsList.createIndex( { "created": 1 }, { expireAfterSeconds: 3600 } );
   print( "process done")
};

createSubstitutions();
