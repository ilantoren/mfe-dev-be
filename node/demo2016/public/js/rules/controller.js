rulesApp.controller( "RulesController",  function($scope, $http, $rootScope) {

    $scope.test = 'abc';

    $scope.sourcesGridOptions = {

        columnDefs: [
            {headerName: "ID", field: "id", editable: false},
            {headerName: "Name", field: "name"},
            {headerName: "Quantity", field: "qty", width: 80}
        ],

        defaultColDef: {
            editable: true,
            filter: 'text'
        },

        rowData: [
            //{
            //    "id": "57b936a9a413460ad9a27bdd",
            //    "name": "pearl barley",
            //    "qty": 0.7
            //},
            //{
            //    "id": "57b9368ea413460ad9a27a4b",
            //    "name": "millet",
            //    "qty": 0.688
            //},
            //{
            //    "id": "57b93688a413460ad9a2792c",
            //    "name": "quinoa",
            //    "qty": 1
            //}
        ]
    };


    $scope.targetGridOptions = {

        columnDefs: [
            {headerName: "ID", field: "id", editable: false},
            {headerName: "Name", field: "name"},
            {headerName: "Quantity", field: "qty", width: 80}
        ],

        defaultColDef: {
            editable: true,
            filter: 'text'
        },

        rowData: [
            //{
            //    "id": "57b936a9a413460ad9a27bdd",
            //    "name": "pearl barley",
            //    "qty": 0.7
            //},
            //{
            //    "id": "57b9368ea413460ad9a27a4b",
            //    "name": "millet",
            //    "qty": 0.688
            //},
            //{
            //    "id": "57b93688a413460ad9a2792c",
            //    "name": "quinoa",
            //    "qty": 1
            //}
        ]
    };

    $scope.addSource = function(){
        $scope.sourcesGridOptions.api.addItems([{id:0, name:'', qty:1}]);
    };

    $scope.addTarget = function(){
        $scope.targetGridOptions.api.addItems([{id:0, name:'', qty:1}]);
    };

    $scope.getRule = function(id){
        $http({
            method: 'GET',
            url: 'controller/rules/' + id
        }).then(function(response) {
            $scope.rule = response.data;

            $scope.sourcesGridOptions.api.setRowData($scope.rule.sources);
            $scope.sourcesGridOptions.api.refreshView();

            $scope.targetGridOptions.api.setRowData($scope.rule.targets);
            $scope.targetGridOptions.api.refreshView();
        }, function errorCallback(response) {
            alert("Error: Failed to generate ID " + response.statusText);
        });
    };

    $scope.rule = {};

    if ($rootScope.ruleId){
      $scope.getRule($rootScope.ruleId);
    }

    $scope.$on('ruleChanged', function(event, ruleId) {
        $scope.getRule(ruleId);
    });


});