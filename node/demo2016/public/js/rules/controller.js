rulesApp.controller( "RulesController",  function($scope, $http, $rootScope, $state) {

    $scope.sources = [];
    $scope.targets = [];
    $scope.functions = ['recipe_tag_prob'];

    $scope.tags = [
        {id: 1, label: "is_rice"},
        {id: 2, label: "is_potato"},
        {id: 3, label: "is_grated_potato"},
        {id: 4, label: "is_mashed_potatoes"},
        {id: 5, label: "is_soup"},
        {id: 6, label: "is_salad"}
    ];

    $scope.operations = ['=','<','>', '<>'];

    $scope.tagsSettings = {
        enableSearch: true,
        smartButtonMaxItems: 2,
        smartButtonTextConverter: function(itemText, originalItem) {
            return itemText;
        }
    };

    $scope.sourcesGridOptions = {
        columnDefs: [
       //     {headerName: "ID", field: "id", editable: false,  width:20},
            {headerName: "Name", field: "name", enableCellEditOnFocus: true,
                cellTemplate:'<div><input type="text" ng-model="row.entity.name" placeholder="sources" uib-typeahead="ing for ing in {{grid.appScope.getIngredients(row.entity.name)}}" typeahead-loading="loadingLocations" typeahead-no-results="noResults" class="form-control"></div>'},
            {headerName: "Quantity", field: "qty", width: 80}
        ], rowHeight:22, data: $scope.sources, multiSelect:false, enableHorizontalScrollbar:1, onRegisterApi : function(gridApi){
            $scope.sourcesGridOptions.gridApi = gridApi;
    }};

    $scope.getIngredients = function(text){
        return text;
    };


    $scope.targetGridOptions = {
        columnDefs: [
           // {headerName: "ID", field: "id", editable: false,  width:20},
            {headerName: "Name", field: "name", enableCellEditOnFocus: true, width:200, editableCellTemplate:''},
            {headerName: "Quantity", field: "qty", width: 80},
            {headerName: "Rule (optional)", field: "rule", width: 800}
        ], rowHeight:22, data: $scope.targets, multiSelect:false, enableHorizontalScrollbar:1, onRegisterApi : function(gridApi){
            $scope.targetGridOptions.gridApi = gridApi;
        }};

    $scope.addSource = function(){
        $scope.sources.push({});
    };

    $scope.addTarget = function(){
        $scope.targets.push({});
    };

    $scope.getRule = function(id){
        $http({
            method: 'GET',
            url: 'controller/rules/' + id
        }).then(function(response) {
            $scope.rule = response.data;
            if ($scope.rule.sources){
                for (var i=0; i<$scope.rule.sources.length; i++){
                    $scope.sources.push($scope.rule.sources[i]);
                }

                for (i=0; i<$scope.rule.targets.length; i++){
                    $scope.targets.push($scope.rule.targets[i]);
                }
            }

        }, function errorCallback(response) {
            alert("Error: Failed to generate ID " + response.statusText);
        });
    };

    $scope.rule = {rules:[{tags:[], func:'recipe_tag_prob'}]};

    if ($rootScope.ruleId){
      $scope.getRule($rootScope.ruleId);
    }

    $scope.$on('ruleChanged', function(event, ruleId) {
        $scope.getRule(ruleId);
    });

    $scope.addRuleRow = function(){
        $scope.rule.rules.push({tags:[], func:'recipe_tag_prob'});
    };

    $scope.removeRuleRow = function(row){
       if ($scope.rule.rules.length > 1){
           for (var i=0; i<$scope.rule.rules.length; i++){
               if ($scope.rule.rules[i] == row){
                   $scope.rule.rules.splice(i, 1);
                   break;
               }
           }
       }

    };

    $scope.rules = function(){
        $scope.rule = null;
        $state.go('all-rules');
    };

    $scope.newRule = function(){
        $scope.rule = {rules:[{tags:[], func:'recipe_tag_prob'}]};
        $state.go('new-rule');
    };

});