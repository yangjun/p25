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
            });

        /*------------------------------医院------------------------------*/
        $stateProvider
            .state('hospital', {
                url: '/hospital',
                template: '<div ui-view></div>'
            })
            /*新建医院*/
            .state('hospital.create', {
                url: '/create',
                templateUrl: '../html/hospital/hospital_create.html',
                controller: 'HospitalCreateCtrl'
            });
        $urlRouterProvider.when('', '/organization');
    });

app.constant('CTX', "/crm/v1/sdk");