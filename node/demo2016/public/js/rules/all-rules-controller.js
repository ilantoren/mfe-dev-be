rulesApp.controller( "AllRulesCtrl",  function($scope, $http, $state, $rootScope) {

    $scope.rulesGridOptions = {

        columnDefs: [
            //{headerName: "ID", field: "_id", width:20, visible:false, hidden:true},
            {headerName: "Name", field: "name", width:600},
            {headerName: "Origin", field: "origin", width:150},
            {headerName: "Condition", field: "cond",  width:750},
            {headerName: "Probability", field: "probability",  width:80}
        ],

        defaultColDef: {
            editable: false
        },

        enableColResize: true,
        rowSelection: 'single',
        onSelectionChanged: function() {
            var selectedRows = $scope.rulesGridOptions.api.getSelectedRows();
            if (selectedRows && selectedRows.length > 0){
                $rootScope.ruleId = selectedRows[0]._id;
                $state.go('new-rule');
                $scope.$emit('ruleChanged', $rootScope.ruleId);
            }
        },

        rowData: [

        ]

    };

    $scope.getRules = function(){
        $http({
            method: 'GET',
            url: 'controller/rules'
        }).then(function(response) {
            $scope.rulesGridOptions.api.setRowData(response.data);
            $scope.rulesGridOptions.api.refreshView();
        }, function errorCallback(response) {
            alert("Error: Failed to generate ID " + response.statusText);
        });
    };

    $scope.getRules();

});