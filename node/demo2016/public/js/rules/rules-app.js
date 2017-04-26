agGrid.initialiseAgGridWithAngular1(angular);
var rulesApp = angular.module( "rulesApp", [ 'agGrid', 'ui.router', 'ui.grid', 'angularjs-dropdown-multiselect'] );

rulesApp.config(function($stateProvider){
    $stateProvider
        .state('new-rule', {
            views: {
                'workspace': {
                    url: '/new-rule',
                    templateUrl: 'new-rule.html'
                }
            }

        })
        .state('all-rules', {
            views:
            {
                'workspace':{
                    url: '/all-rules',
                    params: {
                        id: null,id2:null
                    },
                    templateUrl: 'all-rules.html'
                }

            }

        });
});

