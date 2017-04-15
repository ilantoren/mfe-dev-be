var log = require('../log');

class MappedEntityDao{

    constructor(){
    }

    searchAlts(text, limit) {
        let meCol = this.app.locals.db.collection("entityMapping");
        return meCol.find({$text: {$search: text}}).limit(limit).toArray();
    }
}


module.exports = new MappedEntityDao();
