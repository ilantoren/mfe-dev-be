var reducefunction = function( key, values) {
	print( key );
	var x = values.pop();
	printjson(x);
	return x;
}
	