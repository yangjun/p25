'use strict';
var app = angular.module('wxApp');

app.constant('ROUTER',
    function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            .state('home', {
                url: '/home',
                templateUrl: '../html/home.html',
                controller: 'HomeCtrl'
            });


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
            /*医院首页*/
            .state('hospital.home', {
                url: '/home',
                templateUrl: '../html/hospital/hospital_home.html',
                controller: 'HospitalHomeCtrl'
            })
            /*所有医院*/
            .state('hospital.list', {
                url: '/list',
                templateUrl: '../html/hospital/hospital_list.html',
                controller: 'HospitalListCtrl'
            })
            /*医院信息*/
            .state('hospital.info', {
                url: '/:id/info',
                templateUrl: '../html/hospital/hospital_info.html',
                controller: 'HospitalInfoCtrl'
            })
            /*新建医院*/
            .state('hospital.create', {
                url: '/create',
                templateUrl: '../html/hospital/hospital_create.html',
                controller: 'HospitalCreateCtrl'
            })
            /*申请开发*/
            .state('hospital.develop', {
                url: '/:id/develop',
                templateUrl: '../html/hospital/hospital_develop.html',
                controller: 'HospitalDevelopCtrl'
            })
            /*编辑医院*/
            .state('hospital.edit', {
                url: '/:id/edit',
                templateUrl: '../html/hospital/hospital_edit.html',
                controller: 'HospitalEditCtrl'
            });
        $urlRouterProvider.when('', '/home');
    });

app.constant('CTX', "/crm/v1/sdk");