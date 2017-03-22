#!/bin/bash

#mfe-front-end
 mongo ds151719-a0.mlab.com:51719/mfe-front-end -u mfe-front -p mfe-front --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
 mongo ds151719-a0.mlab.com:51719/mfe-front-end -u mfe-front -p mfe-front --eval " db.recipePOJO.remove( { website: 'Strauss'}) "
 mongorestore --host ds151719-a0.mlab.com:51719 -d mfe-front-end -u mfe-front -p mfe-front entityMapping.bson
 mongorestore --host ds151719-a0.mlab.com:51719 -d mfe-front-end -u mfe-front -p mfe-front ingredientPOJO.bson
  mongorestore --host ds151719-a0.mlab.com:51719 -d mfe-front-end -u mfe-front -p mfe-front -c recipePOJO StraussRecipes.bson

 
 # myfavoreats-develop
 mongo ds145728-a0.mlab.com:45728/myfavoreats-develop -u java -p javadevelop --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
  mongo ds145728-a0.mlab.com:45728/myfavoreats-develop -u java -p javadevelop --eval " db.recipePOJO.remove( { website: 'Strauss'}) "
 mongorestore --host ds145728-a0.mlab.com:45728 -d myfavoreats-develop -u java -p javadevelop  entityMapping.bson
 mongorestore --host ds145728-a0.mlab.com:45728 -d myfavoreats-develop -u java -p javadevelop  ingredientPOJO.bson
  mongorestore --host ds145728-a0.mlab.com:45728 -d myfavoreats-develop -u java -p javadevelop   -c recipePOJO StraussRecipes.bson

 
 #java-demo
 mongo ds155718.mlab.com:55718/java-demo -u javademo -p javademo --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
 mongo ds155718.mlab.com:55718/java-demo -u javademo -p javademo --eval " db.recipePOJO.remove( { website: 'Strauss'}) "
 mongorestore --host ds155718.mlab.com:55718 -d java-demo -u javademo -p javademo entityMapping.bson
 mongorestore --host ds155718.mlab.com:55718 -d java-demo -u javademo -p javademo ingredientPOJO.bson
 mongorestore --host ds155718.mlab.com:55718 -d java-demo -u javademo -p javademo  -c recipePOJO StraussRecipes.bson
