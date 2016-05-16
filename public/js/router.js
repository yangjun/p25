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
                $window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd1a9eba03a82e959&redirect_uri=http://luotaoyeah.iok.la/wx/callback&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect';
            } else {
                /*将请求参数中的JWT_TOKEN保存到本地存储中*/
                JWT_TOKEN = $window.location.hash.substring($window.location.hash.indexOf('token=') + 6);
                $window.localStorage.setItem('JWT_TOKEN', JWT_TOKEN);
            }
        }

        /*------------------------------ 获取并保存当前用户信息 ------------------------------*/
        var deferred = $q.defer();
        $http.get(CONTEXT.CRM_CTX + '/auth/userProfile?_=' + new Date().getTime()).success(function (result) {
            console.log(result);
            deferred.resolve();
        }).error(function (data, status) {
            console.log(data);
            deferred.reject(data, status);
        });
        return deferred.promise;
    };

    /*------------------------------ 首页 ------------------------------*/
        $stateProvider
            .state('home', {
                url: '/home',
                templateUrl: '../html/home.html',
                controller: 'HomeCtrl',
                resolve: {auth: auth}
            });

    /*------------------------------ 医院 ------------------------------*/
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

    /*------------------------------ 订单 ------------------------------*/
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
            })
            /*拒绝订单*/
            .state('order.reject', {
                url: '/:oid/reject',
                templateUrl: '../html/order/order_reject.html',
                controller: 'OrderRejectCtrl'
            })
            /*确认订单*/
            .state('order.confirm', {
                url: '/:oid/confirm',
                templateUrl: '../html/order/order_confirm.html',
                controller: 'OrderConfirmCtrl'
            })
            /*订单：出库清单*/
            .state('order.goods', {
                url: '/:oid/goods',
                templateUrl: '../html/order/order_goods.html',
                controller: 'OrderGoodsCtrl'
            });

        $urlRouterProvider.when('', '/home');
    });

app.constant('CONTEXT', {
    CRM_CTX: '/crm/v1/sdk',
    WX_CTX: '/wx'
});