'use strict';
var app = angular.module('wxApp');

app.constant('ROUTER', function ($stateProvider, $urlRouterProvider) {
    /**
     * 权限检查
     * @param $rootScope
     * @param $q
     * @param $http
     * @param $window
     * @param CONTEXT
     * @returns {*}
     */
    var auth = function ($rootScope, $q, $http, $window, CONTEXT) {
        /*------------------------------ 检查并设置JWT_TOKEN信息 ------------------------------*/
        /*检查本地存储中是否有JWT_TOKEN信息*/
        var JWT_TOKEN = $window.localStorage.getItem('JWT_TOKEN');
        if (!JWT_TOKEN || JWT_TOKEN === 'null') {
            /*检查请求参数中是否有JWT_TOKEN信息*/
            if ($window.location.hash.indexOf('token=') <= -1) {
                console.log('获取JWT_TOKEN…');
                $window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd1a9eba03a82e959&redirect_uri=http://luotaoyeah.iok.la/wx/callback&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect';
            } else {
                /*将请求参数中的JWT_TOKEN保存到本地存储中*/
                JWT_TOKEN = $window.location.hash.substring($window.location.hash.indexOf('token=') + 6);
                $window.localStorage.setItem('JWT_TOKEN', JWT_TOKEN);
            }
        }

        /*------------------------------ 获取并保存当前用户信息 ------------------------------*/
        if (!$rootScope.user) {
            console.log('获取用户信息…');
            var deferred = $q.defer();
            $http.get(CONTEXT.CRM_CTX + '/auth/userProfile?_=' + new Date().getTime()).success(function (result) {
                $rootScope.user = result;
                deferred.resolve();
            }).error(function (data, status) {
                deferred.reject(data, status);
            });
            return deferred.promise;
        }
    };

    $stateProvider
        .state('crm', {
            abstract: true,
            url: '/crm',
            templateUrl: '../html/crm_template.html'
        });

    /*------------------------------ 首页 ------------------------------*/
    $stateProvider
        .state('crm.home', {
            url: '/home',
            templateUrl: '../html/home.html',
            controller: 'HomeCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 医院 ------------------------------*/
    $stateProvider
        .state('crm.hospital', {
            abstract: true,
            url: '/hospital',
            template: '<div ui-view></div>'
        })
        /*所有医院*/
        .state('crm.hospital.list', {
            url: '/list',
            templateUrl: '../html/hospital/hospital_list.html',
            controller: 'HospitalListCtrl',
            resolve: {auth: auth}
        })
        /*医院信息*/
        .state('crm.hospital.info', {
            url: '/:id/info',
            templateUrl: '../html/hospital/hospital_info.html',
            controller: 'HospitalInfoCtrl',
            resolve: {auth: auth}
        })
        /*新建医院*/
        .state('crm.hospital.create', {
            url: '/create',
            templateUrl: '../html/hospital/hospital_create.html',
            controller: 'HospitalCreateCtrl',
            resolve: {auth: auth}
        })
        /*编辑医院*/
        .state('crm.hospital.edit', {
            url: '/:id/edit',
            templateUrl: '../html/hospital/hospital_edit.html',
            controller: 'HospitalEditCtrl',
            resolve: {auth: auth}
        })
        /*申请开发*/
        .state('crm.hospital.develop', {
            url: '/:id/develop',
            templateUrl: '../html/hospital/hospital_develop.html',
            controller: 'HospitalDevelopCtrl',
            resolve: {auth: auth}
        })
        /*记录开发进度*/
        .state('crm.hospital.resume', {
            url: '/:id/resume',
            templateUrl: '../html/hospital/hospital_resume.html',
            controller: 'HospitalResumeCtrl',
            resolve: {auth: auth}
        })
        /*归档，成为合作伙伴*/
        .state('crm.hospital.partner', {
            url: '/:id/partner',
            templateUrl: '../html/hospital/hospital_partner.html',
            controller: 'HospitalPartnerCtrl',
            resolve: {auth: auth}
        })
        /*编辑归档信息*/
        .state('crm.hospital.archive', {
            url: '/:id/archive',
            templateUrl: '../html/hospital/hospital_archive.html',
            controller: 'HospitalArchiveCtrl',
            resolve: {auth: auth}
        })
        /*医生*/
        .state('crm.hospital.doctor', {
            abstract: true,
            url: '/:id/doctor',
            template: '<div ui-view></div>'
        })
        /*医生：所有医生*/
        .state('crm.hospital.doctor.list', {
            url: '/list',
            templateUrl: '../html/hospital/hospital_doctor_list.html',
            controller: 'HospitalDoctorListCtrl',
            resolve: {auth: auth}
        })
        /*医生：添加医生*/
        .state('crm.hospital.doctor.create', {
            url: '/create',
            templateUrl: '../html/hospital/hospital_doctor_create.html',
            controller: 'HospitalDoctorCreateCtrl',
            resolve: {auth: auth}
        })
        /*订单*/
        .state('crm.hospital.order', {
            abstract: true,
            url: '/:id/order',
            template: '<div ui-view></div>'
        })
        /*订单：所有订单*/
        .state('crm.hospital.order.list', {
            url: '/list',
            templateUrl: '../html/hospital/hospital_order_list.html',
            controller: 'HospitalOrderListCtrl',
            resolve: {auth: auth}
        })
        /*订单：添加订单*/
        .state('crm.hospital.order.create', {
            url: '/create',
            templateUrl: '../html/hospital/hospital_order_create.html',
            controller: 'HospitalOrderCreateCtrl',
            resolve: {auth: auth}
        })
        /*订单：删除订单*/
        .state('crm.hospital.order.remove', {
            url: '/:oid/remove',
            templateUrl: '../html/hospital/hospital_order_remove.html',
            controller: 'HospitalOrderRemoveCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 订单 ------------------------------*/
    $stateProvider
        .state('crm.order', {
            abstract: true,
            url: '/order',
            template: '<div ui-view></div>'
        })
        /*所有订单*/
        .state('crm.order.list', {
            url: '/list',
            templateUrl: '../html/order/order_list.html',
            controller: 'OrderListCtrl',
            resolve: {auth: auth}
        })
        /*订单信息*/
        .state('crm.order.info', {
            url: '/:oid/info',
            templateUrl: '../html/order/order_info.html',
            controller: 'OrderInfoCtrl',
            resolve: {auth: auth}
        })
        /*接受订单*/
        .state('crm.order.permit', {
            url: '/:oid/permit',
            templateUrl: '../html/order/order_permit.html',
            controller: 'OrderPermitCtrl',
            resolve: {auth: auth}
        })
        /*拒绝订单*/
        .state('crm.order.reject', {
            url: '/:oid/reject',
            templateUrl: '../html/order/order_reject.html',
            controller: 'OrderRejectCtrl',
            resolve: {auth: auth}
        })
        /*确认订单*/
        .state('crm.order.confirm', {
            url: '/:oid/confirm',
            templateUrl: '../html/order/order_confirm.html',
            controller: 'OrderConfirmCtrl',
            resolve: {auth: auth}
        })
        /*订单：出库清单*/
        .state('crm.order.goods', {
            url: '/:oid/goods',
            templateUrl: '../html/order/order_goods.html',
            controller: 'OrderGoodsCtrl',
            resolve: {auth: auth}
        });

    $urlRouterProvider.when('', '/crm/home');
});

app.constant('CONTEXT', {
    CRM_CTX: '/crm/v1/sdk',
    WX_CTX: '/wx'
});