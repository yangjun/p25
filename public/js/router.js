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
            /*问题反馈*/
            .state('problem', {
                url: '/problem',
                templateUrl: '../html/feed/problem_feedback.html'
            })
            /*开发医院申请*/
            .state('application', {
                url: '/application',
                templateUrl: '../html/application.html',
                controller: 'ApplicationController'
            });
        $urlRouterProvider.when('', '/organization');
    });

app.constant('CTX', "/crm/v1/sdk");