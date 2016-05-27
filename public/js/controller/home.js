'use strict';
var app = angular.module('wxApp');

/**
 * 首页
 */
app.controller('HomeCtrl', ['$rootScope', '$scope',
    function ($rootScope, $scope) {
        $rootScope.config = {
            title: {
                title: false,
                menu: false
            }
        };
    }]);