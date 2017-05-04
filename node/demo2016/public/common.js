var port = window.port;

var hostname = window.hostname;



function bySubstitute(obj) {
    var original = 'Search for Substitutions';
    var description = obj.text;
    var target = obj.name;
    $('#dropdownLabel').html(description);
    setTimeout(function () {
        $('#dropdownLabel').html(original);
    }, 25000);
    $.ajax({
        url: "/api/recipes/substitute/" + target ,
        context: document.body,
        dataType: 'json'
    }).success(function (data, status, jqXHR) {
        if (data) {
            var title_obj = {'titles': data}
            //console.log( JSON.stringify( title_obj ));
            htm = Defiant.render('title-list', title_obj);
            console.log(htm)
            $("#side-menu").html(htm);
        }
    }).done(function () {
        $(this).addClass("done");
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
        url: url,
        context: document.body,
        dataType: 'json'
    }).success(function (data, status, jqXHR) {
        if (data) {
            var title_obj = {
                'titles': data
            }
            htm = Defiant.render('subDropDown', title_obj);
            console.log(htm)
            $("#subSearchDpdwn").html(htm);
        }
    });
}


function searchByPhrase(phrase) {
    console.log("searching by phrase: " + phrase);
    var url = '/api/recipes/title/search/' + escape(phrase) ;
    console.log(url);
    $.ajax({
        url: url,
        context: document.body,
        dataType: 'json'
    }).success(function (data, status, jqXHR) {
        if (data) {
            var title_obj = {'titles': data}
            if ( demoMode ) {
                var list  = [];
                data.forEach( function(a) { if ( a.dm ===1) list.push( a ); });
                title_obj = {'titles' : list };
            }
            console.log(JSON.stringify(title_obj));
            htm = Defiant.render('title-list', title_obj);
            console.log(htm)
            $("#side-menu").html(htm);
        }
    }).done(function () {
        $(this).addClass("done");
    });

}

/*  open the gui that will replace this demo */
function openGUI(obj) {
    var url = obj.href
    window.open(url);
}

function setDemoMode( checked ) {
    if ( checked) {
        console.log( "demo mode on");
        demoMode = 1;
    }
    else {
        demoMode = 0;
        console.log("demo mode off");
    }
}

function updateRecipePair(link, child) {
    var id = link.name;  // name holds the id field
    var parentId = link.id;
    var divId = "#div-" + parentId;
    $("#objectid").html(id);
    var href = "http://54.84.218.231:3000/#/recipe/" + id;
    $("#objectIdAnchor").attr('href', href);

    var title = link.text;
    link.style = "opacity:0.8";
    $("#list-parent").children().removeClass("mfe-group-item-selected");
    $(divId).toggleClass("mfe-group-item-selected");
    console.log("updateRecipePair: " + link.outerHTML);
    // var url = '/api/recipes/modified/';
    var url = '/api/recipes/with-substitute/';
    if (child) {
        url = url + parentId + escape("?child=1&title=" + title);
    } else {
        url = url + id;
    }

    processUrl(url, title);
    //updateDropdown( child, parentId, id );
}

function selectSubstitute(link) {
    var target = escape(link.getAttribute('optionUid'));
    var id = link.id;
    console.log('selectSubstitute  id = ' + id + ' with target =' + target);
    var url = '/api/recipes/with-substitute/' + id + "?target=" + target ;
    processUrl(url, "blanks");
    //updateDropdown(1, id, target);
}




function processUrl(url, selector) {
    var xmlfile = Saxon.requestXML(url);
    var xsl2 = Saxon.requestXML("display-recipe-pair.xsl")
    var proc2 = Saxon.newXSLT20Processor(xsl2);
    Saxon.setLogLevel('INFO');
    proc2.setParameter(null, 'title', selector)
    proc2.setSuccess(performAfterSuccess);

    // wrap in promise to ensure transform is all done async
    var promise = new Promise(function (resolve, reject) {
        proc2.transformToDocument(xmlfile);
        resolve("Transform completed");
    });

    promise.then(function (result) {
        console.log(result);
    }, function (err) {
        console.log(err);
    });
}


function performAfterSuccess(proc) {
    console.log('performAfterSuccess');
    var doc = proc.getResultDocument();
    var d1 = Saxon.serializeXML(doc.firstElementChild.children[0]);
    var d2 = Saxon.serializeXML(doc.firstElementChild.children[1]);
    var d3 = Saxon.serializeXML(doc.firstElementChild.children[2]);
    var d4 = Saxon.serializeXML(doc.firstElementChild.children[3]);

    $("#tabs-child-2").html(d1);
    $("#tabs-child-1").html(d2);
    $("#tabs-orig-2").html(d3);
    $("#tabs-orig-1").html(d4);
}


function updateDropdown(child, parentId, id) {
    var menuUrl;
    if (child)
        menuUrl = 'api/recipe/parent/' + parentId;
    else
        menuUrl = 'api/recipe/parent/' + id;

    //$('#tabs-orig-2').prepend( ptitle );
    //$('#tabs-child-2').prepend(  ctitle );
    $.ajax({
        url: menuUrl,
        context: document.body,
        dataType: 'json'
    }).success(function (data, status, xxx) {
        console.log("DROPDOWN " + data);
        if (data[0]) {
            dropdownprocess(data);
        }
    });
}

function buildMenu() {
    var titles = [];
    $.ajax({
        url: "/api/recipes/changed/title",
        context: document.body,
        dataType: 'json'
    }).success(function (data, status, jqXHR) {
        console.log("success");
        var mydata = data.slice(1);
        console.log(JSON.stringify(mydata[0]))
        for (var key in mydata[0]) {
            console.log(key);
        }
        console.log('title' + mydata[0].title)
        for (i = 0; i < mydata.length; i++) {
            var recipe = mydata[i];
            //console.log( recipe.title )
            var recipeTitle = recipe.title;
            var lastTitle = '';
            var imageUrl = recipe.imageUrl;
            var dm = recipe.dm;
            if (recipeTitle == lastTitle) {
                console.log(recipe.id + ' ' + recipe.parentId);
            } else if( demoMode ) {
                if( dm ===1 ) {
                    titles.push({'id': recipe.id, 'title': recipeTitle, 'urn': recipe.urn, 'site': recipe.site, 'imageUrl': imageUrl, 'dm': dm });
                    lastTitle = recipeTitle;
                }
            }else {
               titles.push({'id': recipe.id, 'title': recipeTitle, 'urn': recipe.urn, 'site': recipe.site, 'imageUrl': imageUrl, 'dm': dm});
                    lastTitle = recipeTitle;  
            }
        }
        var title_obj = {'titles': titles}
        console.log(JSON.stringify(title_obj));
        htm = Defiant.render('title-list', title_obj);
        console.log(htm)
        $("#side-menu").html(htm);
    }).done(function () {
        console.log( )
        $(this).addClass("done");
    });
}

var popupBlockerChecker = {
    check: function (popup_window) {
        var _scope = this;
        if (popup_window) {
            if (/chrome/.test(navigator.userAgent.toLowerCase())) {
                setTimeout(function () {
                    _scope._is_popup_blocked(_scope, popup_window);
                }, 200);
            } else {
                popup_window.onload = function () {
                    _scope._is_popup_blocked(_scope, popup_window);
                };
            }
        } else {
            _scope._displayError();
        }
    },
    _is_popup_blocked: function (scope, popup_window) {
        if ((popup_window.innerHeight > 0) == false) {
            scope._displayError();
        }
    },
    _displayError: function () {
        alert("Popup Blocker is enabled! Please add this site to your exception list if you wish to see the original recipes text.");
    }
};


function dropdownprocess(recipe) {
    htm = Defiant.render('dropdown', recipe)
    console.log(htm)
    $("#dropdowndynamic").html(htm);
}


 