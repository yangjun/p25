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

/**
 * 医院：新建医院
 */
app.controller('HospitalCreateCtrl', ['$scope',
    function ($scope) {
    }]);