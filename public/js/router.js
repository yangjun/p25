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
            /*编辑医院*/
            .state('hospital.edit', {
                url: '/:id/edit',
                templateUrl: '../html/hospital/hospital_edit.html',
                controller: 'HospitalEditCtrl'
            })
            /*申请开发*/
            .state('hospital.develop', {
                url: '/:id/develop',
                templateUrl: '../html/hospital/hospital_develop.html',
                controller: 'HospitalDevelopCtrl'
            })
            /*记录开发进度*/
            .state('hospital.resume', {
                url: '/:id/resume',
                templateUrl: '../html/hospital/hospital_resume.html',
                controller: 'HospitalResumeCtrl'
            })
            /*编辑归档信息*/
            .state('hospital.archive', {
                url: '/:id/archive',
                templateUrl: '../html/hospital/hospital_archive.html',
                controller: 'HospitalArchiveCtrl'
            })
            /*医生*/
            .state('hospital.doctor', {
                url: '/:id/doctor',
                template: '<div ui-view></div>'
            })
            /*医生：所有医生*/
            .state('hospital.doctor.list', {
                url: '/list',
                templateUrl: '../html/hospital/hospital_doctor_list.html',
                controller: 'HospitalDoctorListCtrl'
            })
            /*医生：添加医生*/
            .state('hospital.doctor.create', {
                url: '/create',
                templateUrl: '../html/hospital/hospital_doctor_create.html',
                controller: 'HospitalDoctorCreateCtrl'
            })
            /*订单*/
            .state('hospital.order', {
                url: '/:id/order',
                template: '<div ui-view></div>'
            })
            /*订单：所有订单*/
            .state('hospital.order.list', {
                url: '/list',
                templateUrl: '../html/hospital/hospital_order_list.html',
                controller: 'HospitalOrderListCtrl'
            })
            /*订单：添加订单*/
            .state('hospital.order.create', {
                url: '/create',
                templateUrl: '../html/hospital/hospital_order_create.html',
                controller: 'HospitalOrderCreateCtrl'
            })
            /*订单：删除订单*/
            .state('hospital.order.remove', {
                url: '/:oid/remove',
                templateUrl: '../html/hospital/hospital_order_remove.html',
                controller: 'HospitalOrderRemoveCtrl'
            });

        /*------------------------------订单------------------------------*/
        $stateProvider
            .state('order', {
                url: '/order',
                template: '<div ui-view></div>'
            })
            /*所有订单*/
            .state('order.list', {
                url: '/list',
                templateUrl: '../html/order/order_list.html',
                controller: 'OrderListCtrl'
            })
            /*订单信息*/
            .state('order.info', {
                url: '/:oid/info',
                templateUrl: '../html/order/order_info.html',
                controller: 'OrderInfoCtrl'
            })
            /*接受订单*/
            .state('order.permit', {
                url: '/:oid/permit',
                templateUrl: '../html/order/order_permit.html',
                controller: 'OrderPermitCtrl'
            });


        $urlRouterProvider.when('', '/home');
    });

app.constant('CTX', "/crm/v1/sdk");