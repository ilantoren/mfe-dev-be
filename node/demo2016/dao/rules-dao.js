"use strict"
var log = require('../log');

class RulesDao{

    constructor(){
        this.limit = 100;
    }

    getAllRules(){
        let subCol = this.app.locals.db.collection("manualSubRules");
        return subCol.find({}).toArray();
    }

    getRule(id){
        let subCol = this.app.locals.db.collection("manualSubRules");
        return subCol.findOne({_id:ObjectID.createFromHexString(id)});
    }

}


module.exports = new RulesDao();
