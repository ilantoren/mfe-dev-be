var mapfunction = function() {
	var subs = this.subs;
	subs.forEach( function(b) {
		var source = b.source;
		var sourceId = b.sourceId;
		var opts = b.options;
		opts.forEach( function(c) {
				var target = c.target;
				var targetId = c.targetId;
				var key = sourceId + "_" + targetId;
				var description = source + " for " + target;
				var value =  {
					"sourceId" : sourceId,
					"targetId" : targetId,
					"description" : description
				};
				emit( key, value );
			});
	});
}