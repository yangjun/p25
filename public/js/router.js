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
        var TOKEN = $window.localStorage.getItem('TOKEN');
        if (!TOKEN || TOKEN === 'null') {
            /*检查请求参数中是否有JWT_TOKEN信息*/
            if ($window.location.hash.indexOf('token=') <= -1) {
                $.toast('获取JWT_TOKEN…');
                $window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd1a9eba03a82e959&redirect_uri=http://luotaoyeah.iok.la/wx/callback&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect';
                return;
            } else {
                /*将请求参数中的JWT_TOKEN保存到本地存储中*/
                TOKEN = $window.location.hash.substring($window.location.hash.indexOf('token=') + 6);
                $window.localStorage.setItem('TOKEN', TOKEN);
            }
        }


        /*------------------------------ 获取并保存当前用户信息 ------------------------------*/
        if (!$rootScope.user) {
            var deferred = $q.defer();
            $http.get(CONTEXT.CRM_CTX + '/auth/userProfile').success(function (result) {
                $rootScope.user = result;
                deferred.resolve();
            }).error(function (data, status) {
                $.toast('获取用户信息失败…' + JSON.stringify(data));
                deferred.reject(data, status);
            });
            return deferred.promise;
        }
    };

    $stateProvider
        .state('crm', {
            abstract: true,
            url: '/crm',
            templateUrl: '../html/crm-template.html'
        });

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
        .state('crm.hospital', {
            abstract: true,
            url: '/hospital',
            template: '<div ui-view></div>'
        })
        /*所有医院*/
        .state('crm.hospital.list', {
            url: '/list',
            templateUrl: '../html/hospital/hospital-list.html',
            controller: 'HospitalListCtrl',
            resolve: {auth: auth}
        })
        /*医院信息*/
        .state('crm.hospital.info', {
            url: '/:id/info',
            templateUrl: '../html/hospital/hospital-info.html',
            controller: 'HospitalInfoCtrl',
            resolve: {auth: auth}
        })
        /*新建医院*/
        .state('crm.hospital.create', {
            url: '/create',
            templateUrl: '../html/hospital/hospital-create.html',
            controller: 'HospitalCreateCtrl',
            resolve: {auth: auth}
        })
        /*编辑医院*/
        .state('crm.hospital.edit', {
            url: '/:id/edit',
            templateUrl: '../html/hospital/hospital-edit.html',
            controller: 'HospitalEditCtrl',
            resolve: {auth: auth}
        })
        /*申请开发*/
        .state('crm.hospital.develop', {
            url: '/:id/develop',
            templateUrl: '../html/hospital/hospital-develop.html',
            controller: 'HospitalDevelopCtrl',
            resolve: {auth: auth}
        })
        /*记录开发进度*/
        .state('crm.hospital.resume', {
            url: '/:id/resume',
            templateUrl: '../html/hospital/hospital-resume.html',
            controller: 'HospitalResumeCtrl',
            resolve: {auth: auth}
        })
        /*归档，成为合作伙伴*/
        .state('crm.hospital.partner', {
            url: '/:id/partner',
            templateUrl: '../html/hospital/hospital-partner.html',
            controller: 'HospitalPartnerCtrl',
            resolve: {auth: auth}
        })
        /*编辑归档信息*/
        .state('crm.hospital.archive', {
            url: '/:id/archive',
            templateUrl: '../html/hospital/hospital-archive.html',
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
            templateUrl: '../html/hospital/doctor/hospital-doctor-list.html',
            controller: 'HospitalDoctorListCtrl',
            resolve: {auth: auth}
        })
        /*医生：添加医生*/
        .state('crm.hospital.doctor.create', {
            url: '/create',
            templateUrl: '../html/hospital/doctor/hospital-doctor-create.html',
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
            templateUrl: '../html/hospital/order/hospital-order-list.html',
            controller: 'HospitalOrderListCtrl',
            resolve: {auth: auth}
        })
        /*订单：添加订单*/
        .state('crm.hospital.order.create', {
            url: '/create',
            templateUrl: '../html/hospital/order/hospital-order-create.html',
            controller: 'HospitalOrderCreateCtrl',
            resolve: {auth: auth}
        })
        /*订单：取消订单*/
        .state('crm.hospital.order.remove', {
            url: '/:oid/remove',
            templateUrl: '../html/hospital/order/hospital-order-delete.html',
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
            templateUrl: '../html/order/order-list.html',
            controller: 'OrderListCtrl',
            resolve: {auth: auth}
        })
        /*订单信息*/
        .state('crm.order.info', {
            url: '/:oid/info',
            templateUrl: '../html/order/order-info.html',
            controller: 'OrderInfoCtrl',
            resolve: {auth: auth}
        })
        /*提交订单*/
        .state('crm.order.commit', {
            url: '/:oid/commit',
            templateUrl: '../html/order/order-commit.html',
            controller: 'OrderCommitCtrl',
            resolve: {auth: auth}
        })
        /*订单：出库清单*/
        .state('crm.order.goods', {
            url: '/:oid/goods',
            templateUrl: '../html/order/order-goods.html',
            controller: 'OrderGoodsCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 待办任务 ------------------------------*/
    $stateProvider
        .state('crm.task', {
            abstract: true,
            url: '/task',
            template: '<div ui-view></div>'
        })
        /*所有任务*/
        .state('crm.task.list', {
            url: '/list',
            templateUrl: '../html/task/task-list.html',
            controller: 'TaskListCtrl',
            resolve: {auth: auth}
        })
        /*任务信息*/
        .state('crm.task.info', {
            url: '/:tid/info',
            templateUrl: '../html/task/task-info.html',
            controller: 'TaskInfoCtrl',
            resolve: {auth: auth}
        })
        /*审核通过*/
        .state('crm.task.permit', {
            url: '/:tid/permit',
            templateUrl: '../html/task/task-permit.html',
            controller: 'TaskPermitCtrl',
            resolve: {auth: auth}
        })
        /*审核拒绝*/
        .state('crm.task.reject', {
            url: '/:tid/reject',
            templateUrl: '../html/task/task-reject.html',
            controller: 'TaskRejectCtrl',
            resolve: {auth: auth}
        })
        /*参与者列表*/
        .state('crm.task.holders', {
            url: '/:tid/holders',
            templateUrl: '../html/task/task-holders.html',
            controller: 'TaskHoldersCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 处方 ------------------------------*/
    $stateProvider
        .state('crm.presc', {
            abstract: true,
            url: '/presc',
            template: '<div ui-view></div>'
        })
        /*所有处方*/
        .state('crm.presc.list', {
            url: '/list',
            templateUrl: '../html/presc/presc-list.html',
            controller: 'PrescListCtrl',
            resolve: {auth: auth}
        })
        /*处方信息*/
        .state('crm.presc.info', {
            url: '/:pid/info',
            templateUrl: '../html/presc/presc-info.html',
            controller: 'PrescInfoCtrl',
            resolve: {auth: auth}
        })
        /*添加处方*/
        .state('crm.presc.create', {
            url: '/create',
            templateUrl: '../html/presc/presc-create.html',
            controller: 'PrescCreateCtrl',
            resolve: {auth: auth}
        })
        /*编辑处方*/
        .state('crm.presc.edit', {
            url: '/:pid/edit',
            templateUrl: '../html/presc/presc-edit.html',
            controller: 'PrescEditCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 问答 ------------------------------*/
    $stateProvider
        .state('crm.faq', {
            abstract: true,
            url: '/faq',
            template: '<div ui-view></div>'
        })
        /*所有问答*/
        .state('crm.faq.list', {
            url: '/list',
            templateUrl: '../html/faq/faq-list.html',
            controller: 'FaqListCtrl',
            resolve: {auth: auth}
        })
        /*问答信息*/
        .state('crm.faq.info', {
            url: '/:fid/info',
            templateUrl: '../html/faq/faq-info.html',
            controller: 'FaqInfoCtrl',
            resolve: {auth: auth}
        })
        /*添加问答*/
        .state('crm.faq.create', {
            url: '/create',
            templateUrl: '../html/faq/faq-create.html',
            controller: 'FaqCreateCtrl',
            resolve: {auth: auth}
        })
        /*编辑问答*/
        .state('crm.faq.edit', {
            url: '/:fid/edit',
            templateUrl: '../html/faq/faq-edit.html',
            controller: 'FaqEditCtrl',
            resolve: {auth: auth}
        })
        /*问答：回答*/
        .state('crm.faq.answer', {
            abstract: true,
            url: '/:fid/answer',
            template: '<ui-view></ui-view>'
        })
        /*问答：所有回答*/
        .state('crm.faq.answer.list', {
            abstract: true,
            url: '/:fid/answer/list',
            templateUrl: '../html/faq/answer/faq-answer-list.html',
            controller: 'FaqAnswerListCtrl',
            resolve: {auth: auth}
        })
        /*问答：添加回答*/
        .state('crm.faq.answer.create', {
            abstract: true,
            url: '/:fid/answer/create',
            templateUrl: '../html/faq/answer/faq-answer-create.html',
            controller: 'FaqAnswerCreateCtrl',
            resolve: {auth: auth}
        })
        /*问答：编辑回答*/
        .state('crm.faq.answer.edit', {
            abstract: true,
            url: '/:fid/answer/:aid/edit',
            templateUrl: '../html/faq/answer/faq-answer-edit.html',
            controller: 'FaqAnswerEditCtrl',
            resolve: {auth: auth}
        });

    /*------------------------------ 我的 ------------------------------*/
    $stateProvider
        .state('crm.self', {
            abstract: true,
            url: '/self',
            template: '<div ui-view></div>'
        })
        /*首页*/
        .state('crm.self.home', {
            url: '/home',
            templateUrl: '../html/self/self-home.html',
            controller: 'SelfHomeCtrl',
            resolve: {auth: auth}
        })
        /*订单列表*/
        .state('crm.self.orders', {
            url: '/orders',
            templateUrl: '../html/self/self-order-list.html',
            controller: 'SelfOrdersCtrl',
            resolve: {auth: auth}
        })
        /*任务列表*/
        .state('crm.self.tasks', {
            url: '/tasks',
            templateUrl: '../html/self/self-task-list.html',
            controller: 'SelfTasksCtrl',
            resolve: {auth: auth}
        });

    $urlRouterProvider.when('', '/home');
});

app.constant('CONTEXT', {
    CRM_CTX: '/crm/v1/sdk',
    WX_CTX: '/wx'
});