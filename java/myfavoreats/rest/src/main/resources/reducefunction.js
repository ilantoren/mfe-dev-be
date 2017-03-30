var reducefunction = function( key, values) {
	var x = values.pop();
	printjson(x);
	return x;
}
	