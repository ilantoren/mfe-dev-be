var port =  window.port;

var hostname = window.hostname;

function bySubstitute( obj ) {
	var original = 'Search for Substitutions';
	var description = obj.name;
	$('#dropdownLabel').html( description );
	setTimeout( function() { $('#dropdownLabel').html( original ); }, 25000);
	$.ajax({
  url: "/api/recipes/substitute/" + description,
  context: document.body,
  dataType: 'json' 
  }).success( function(data, status, jqXHR ) {
  	  if ( data ) {
  	  var title_obj = { 'titles' : data }
      //console.log( JSON.stringify( title_obj ));
      htm = Defiant.render('title-list', title_obj );
      console.log( htm )
      $("#side-menu").html( htm );
      }
}).done(function() {
  $( this ).addClass( "done" );
});
  	  	  
}

/*
 * <ul id="subSearchDpdwn" class="dropdown-menu"   aria-labelledby="dropdownMenu1">
     <li><a name="Wheatberries for Rice" onclick="bySubstitute(this)">Wheatberries for Rice</a></li>
  </ul>
 * 
 */

function populateSubstituteDropdown() {
	console.log("getting the list of substitutions for the dropdown1")
	var outputHtml = "";
	var url = '/api/recipe/substitutions';
	$.ajax({
		url : url,
		context : document.body,
		dataType : 'json'
	}).success(function(data, status, jqXHR) {
		if (data) {
			var title_obj = {
				'titles' : data
			}
			htm = Defiant.render('subDropDown', title_obj);
			console.log(htm)
			$("#subSearchDpdwn").html(htm);
		}
	});
}

    
function searchByPhrase( phrase ) {
	console.log( "searching by phrase: " + phrase);
	var url = '/api/recipes/title/search/' + escape(phrase);
	console.log( url );
	$.ajax({
			url: url,
			context: document.body,
			dataType: 'json'
	}).success( function( data, status, jqXHR ) {
	if ( data ) {
		var title_obj = { 'titles' : data }
		console.log( JSON.stringify( title_obj ));
		htm = Defiant.render('title-list', title_obj );
		console.log( htm )
		$("#side-menu").html( htm );
      }
    }).done(function() {
  $( this ).addClass( "done" );
});
  	  	  
}
 
function updateRecipePair( link, child ) {
     var id = link.name;  // name holds the id field
     var parentId = link.id;
     var divId = "#div-" + parentId;
     
     var title = link.text;
     link.style = "opacity:0.8";
     $("#list-parent").children().removeClass( "mfe-group-item-selected");
     $(divId).toggleClass( "mfe-group-item-selected");
     console.log( "updateRecipePair: " + link.outerHTML);
    // var url = '/api/recipes/modified/';
     var url = '/api/recipes/with-substitute/';
     if ( child) {
     	 url = url + parentId +  escape("?child=1&title=" + title);
     }
     else {
     	 url = url + id;
     }
     
     processUrl( url, title );   
     //updateDropdown( child, parentId, id );
}

function selectSubstitute( link ) {
	var target = escape(link.name);
	var id = link.id;
	console.log( 'selectSubstitute  id = ' + id + ' with target =' + target);
	var url = '/api/recipes/with-substitute/' + id + "?target=" + target;
	processUrl( url, "blanks");
	//updateDropdown(1, id, target);
}

function processUrl( url, selector) {
	    var xmlfile = Saxon.requestXML( url );
        var xsl2 = Saxon.requestXML("display-recipe-pair.xsl")
        var proc2 = Saxon.newXSLT20Processor(xsl2);
        Saxon.setLogLevel('INFO');
        proc2.setParameter(null, 'title', selector)
        proc2.setSuccess( performAfterSuccess);
        proc2.transformToDocument( xmlfile );
        
        
}


function performAfterSuccess( proc ) {
	 console.log( 'performAfterSuccess' );
	 var doc = proc.getResultDocument();
	 var d1 = Saxon.serializeXML(doc.firstElementChild.children[0]);
        var d2 = Saxon.serializeXML( doc.firstElementChild.children[1]);
        var d3 = Saxon.serializeXML( doc.firstElementChild.children[2] );
        var d4 = Saxon.serializeXML( doc.firstElementChild.children[3]);

        $("#tabs-child-2").html( d1 );
        $("#tabs-child-1").html( d2 );
        $("#tabs-orig-2").html( d3 );
        $("#tabs-orig-1").html( d4 );
}


function updateDropdown(child, parentId, id) {
	    var menuUrl;
        if ( child)
        	menuUrl = 'api/recipe/parent/' + parentId;
        else
        	menuUrl = 'api/recipe/parent/' + id;
        
        //$('#tabs-orig-2').prepend( ptitle );
        //$('#tabs-child-2').prepend(  ctitle );
        $.ajax({
            url: menuUrl,
            context: document.body,
            dataType: 'json'
        }).success( function( data, status,xxx)  {
            console.log("DROPDOWN " +  data)
            if( data[0] ) {
            	dropdownprocess( data );
            }
        });
 }

function buildMenu() {
    var titles = [
    	{ "id" : "58c063e4c782fc0490755747", "title" : "Spaghetti with sauce", "urn" : "https://plus.strauss-group.co.il/recipe/245", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/mini_spaghetti.jpg", "site" : "Strauss" }
    	,{ "id" : "58c063e4c782fc049075573e", "title" : "leek patties", "urn" : "https://plus.strauss-group.co.il/recipe/33", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/leek_cutlets.jpg", "site" : "Strauss" }
    	,{ "id" : "58c063e4c782fc0490755744", "title" : "mediterranean feta and vegetable quiche", "urn" : "https://plus.strauss-group.co.il/recipe/191", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/roasted_vegetables_fetta_quiche.jpg", "site" : "Strauss" }
    	,{ "id" : "58c063e4c782fc0490755741", "title" : "Mediteranean bulgur salad garnished with tahina and doritos", "urn" : "https://plus.strauss-group.co.il/recipe/7", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/wheat_groats_tahini_salad.jpg", "site" : "Strauss" }
    	,{ "id" : "58c063e4c782fc049075573a", "title" : "baby kale and leek quiche", "urn" : "https://plus.strauss-group.co.il/recipe/19", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/baby_kale_leek_quiche.jpg", "site" : "Strauss" }
    	,{ "id" : "58c063e4c782fc0490755739", "title" : "Cubed chicken breast on honeyed kadaif", "urn" : "https://plus.strauss-group.co.il/recipe/16", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/honey_glazed_chicken_breast_on_kadaif.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc049075573c", "title" : "chicken and quinoa patties", "urn" : "https://plus.strauss-group.co.il/recipe/24", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/chicken_quinoa_cutlets.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755748", "title" : "zucchini cutlets", "urn" : "https://plus.strauss-group.co.il/recipe/342", "imageUrl" : "https://cc.strauss-group.com/Uploads//Recipes/Zucchini-_potato_pancake2.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755749", "title" : "Stuffed shells", "urn" : "https://plus.strauss-group.co.il/recipe/372", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/cheese_filled_pasta.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc049075573b", "title" : "vegetable muffin", "urn" : "https://plus.strauss-group.co.il/recipe/22", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/vegetables_muffins.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc049075573d", "title" : "sweet potato cigars", "urn" : "https://plus.strauss-group.co.il/recipe/29", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/sweet_potato_cigar_pastry.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc049075573f", "title" : "eggplant casserole", "urn" : "https://plus.strauss-group.co.il/recipe/50", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/eggplants_yogurt_pie.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755740", "title" : "vegetable pancake", "urn" : "https://plus.strauss-group.co.il/recipe/66", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/vegetables_pancake.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755742", "title" : "mango bulgur salad", "urn" : "https://plus.strauss-group.co.il/recipe/81", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/mango_and_wheat_groats_salad.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755743", "title" : "liver filled phyllo", "urn" : "https://plus.strauss-group.co.il/recipe/90", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/liver_stuffed_filo.jpg", "site" : "Strauss" }	
		,{ "id" : "58c063e4c782fc0490755745", "title" : "spinach lasagna", "urn" : "https://plus.strauss-group.co.il/recipe/194", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/spinace_ricotta_lasagna.jpg", "site" : "Strauss" }
		,{ "id" : "58c063e4c782fc0490755746", "title" : "kale quiche", "urn" : "https://plus.strauss-group.co.il/recipe/230", "imageUrl" : "https://cc.strauss-group.com/Uploads/Recipes/kale_cheese_quiche.jpg", "site" : "Strauss" }
	];
    $.ajax({
  url: "/api/recipes/changed/title",
  context: document.body,
  dataType: 'json' 
  }).success( function(data, status, jqXHR ) {
      console.log( "success" );
      var mydata = data.slice(1);
      console.log( JSON.stringify(mydata[0]) )
      for (var key in mydata[0]) {
        console.log(key);
       }
       console.log('title' +  mydata[0].title )
       for( i= 0; i< mydata.length; i++ ) {
          var recipe = mydata[i];
          //console.log( recipe.title )
          var recipeTitle = recipe.title;
          var lastTitle = '' ;
          var imageUrl = recipe.imageUrl;
          if ( recipeTitle == lastTitle) {
           console.log( recipe.id + ' ' + recipe.parentId );
          }
          else {
          	   titles.push (  {'id': recipe.id,  'title':recipeTitle, 'urn': recipe.urn, 'site': recipe.site, 'imageUrl': imageUrl  }  )
          	   lastTitle = recipeTitle;
          }
      }
      var title_obj = { 'titles' : titles }
      console.log( JSON.stringify( title_obj ));
      htm = Defiant.render('title-list', title_obj );
      console.log( htm )
      $("#side-menu").html( htm );
}).done(function() {
  console.log( )
  $( this ).addClass( "done" );
});
}

var popupBlockerChecker = {
        check: function(popup_window){
            var _scope = this;
            if (popup_window) {
                if(/chrome/.test(navigator.userAgent.toLowerCase())){
                    setTimeout(function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                     },200);
                }else{
                    popup_window.onload = function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                    };
                }
            }else{
                _scope._displayError();
            }
        },
        _is_popup_blocked: function(scope, popup_window){
            if ((popup_window.innerHeight > 0)==false){ scope._displayError(); }
        },
        _displayError: function(){
            alert("Popup Blocker is enabled! Please add this site to your exception list if you wish to see the original recipes text.");
        }
    };


	function dropdownprocess(recipe ) {
                htm = Defiant.render('dropdown', recipe)
                console.log( htm )
		 $("#dropdowndynamic").html( htm );
           }
           
           
 