'use strict';
var app = angular.module('wxApp');

app.constant('ROUTER',
    function ($stateProvider, $urlRouterProvider) {
        $stateProvider
        /*系统组织机构*/
            .state('organization', {
                url: '/organization',
                templateUrl: '../html/org/org.html'
            })
            /*开发医院申请*/
            .state('application', {
                url: '/application',
                templateUrl: '../html/application.html',
                controller: 'ApplicationController'
            });
        $urlRouterProvider.when('', '/organization');
    });