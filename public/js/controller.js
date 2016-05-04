'use strict';
var app = angular.module('wxApp');

/**
 * 开发医院申请
 */
app.controller('ApplicationController', ['$scope', 'commonService',
    function ($scope, commonService) {
        commonService.test().then(function (result) {
            $scope.ok = result;
        });
    }]);