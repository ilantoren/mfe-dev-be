#!/bin/bash

#mfe-front-end
# mongo ds151719-a0.mlab.com:51719/mfe-front-end -u mfe-front -p mfe-front --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
# mongorestore --host ds151719-a0.mlab.com:51719 -d mfe-front-end -u mfe-front -p mfe-front entityMapping.bson
# mongorestore --host ds151719-a0.mlab.com:51719 -d mfe-front-end -u mfe-front -p mfe-front ingredientPOJO.bson
 
 # myfavoreats-develop
 mongo ds145728-a0.mlab.com:45728/myfavoreats-develop -u java -p javadevelop --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
 mongorestore --host ds145728-a0.mlab.com:45728 -d myfavoreats-develop -u java -p javadevelop  entityMapping.bson
 mongorestore --host ds145728-a0.mlab.com:45728 -d myfavoreats-develop -u java -p javadevelop  ingredientPOJO.bson
 
 
 #java-demo
 mongo ds155718.mlab.com:55718/java-demo -u javademo -p javademo --eval " db.entityMapping.drop(); db.ingredientPOJO.drop() "
 mongorestore --host ds155718.mlab.com:55718 -d java-demo -u javademo -p javademo entityMapping.bson
 mongorestore --host ds155718.mlab.com:55718 -d java-demo -u javademo -p javademo ingredientPOJO.bson
 