var reducefunction = function( key, values) {
	if ( values.length > 20) {
		var x = values.pop();
		return x;
	}
}
	