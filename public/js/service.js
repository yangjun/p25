'use strict';
var app = angular.module('wxApp');

app.factory('restClient', ['$http', '$q', function ($http, $q) {
    var reject = function (deferred, data, status) {
        /*TODO*/
        deferred.reject(data, status);
    };

    return {
        get: function (url) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.get(url).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        post: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.post(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        put: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.put(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        patch: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.patch(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        delete: function (url) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.delete(url).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        }
    };
}]);

/*认证服务*/
app.factory('authService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            auth: function () {
                return restClient.get("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd1a9eba03a82e959&redirect_uri=http://www.demo.com/wx/callback&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect");
            },
            getUserProfile: function () {
                return restClient.get(CONTEXT.CRM_CTX + '/crm/v1/sdk/auth/userProfile');
            }
        }
    }]);

/*医院*/
app.factory('hospitalService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 新建医院
             * @param obj 医院信息
             */
            createHospital: function (obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital', obj);
            },
            /**
             * 查询医院列表
             * @param name 医院名称
             * @param skip
             * @param limit
             */
            listHospital: function (name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital?name=' + name + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 查询医院信息
             * @param id 医院ID
             */
            queryHospital: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id);
            },
            /**
             * 编辑医院信息
             * @param id 医院ID
             * @param obj
             */
            editHospital: function (id, obj) {
                return restClient.patch(CONTEXT.CRM_CTX + '/hospital/' + id, obj);
            },
            /**
             * 申请开发医院
             * @param id 医院ID
             * @param obj
             */
            developHospital: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/develop', obj);
            },
            /**
             * 记录开发过程；开发过程中填写开发履历
             * @param id 医院ID
             * @param obj
             */
            editDevelopResume: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/resume', obj);
            },
            /**
             * 归档，成为合作伙伴
             * @param id 医院ID
             * @param obj
             */
            becomePartner: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/partner', obj);
            },
            /**
             * 查询医生列表
             * @param id
             * @param name
             * @param skip
             * @param limit
             */
            listDoctor: function (id, name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id + '/doctor?name=' + name + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 添加医生
             * @param id
             * @param obj
             * @returns {*}
             */
            createDoctor: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/doctor', obj);
            },
            /**
             * 删除医生
             * @param id
             * @returns {*}
             */
            removeDoctor: function (id) {
                return restClient.delete(CONTEXT.CRM_CTX + '/doctor/' + id);
            },
            /**
             * 查询订单列表：根据医院ID
             * @param id
             * @param name
             * @param skip
             * @param limit
             */
            listHospitalOrder: function (id, name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id + '/order?skip=' + skip + '&limit=' + limit);
            }
        }
    }]);

/*订单*/
app.factory('orderService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 查询订单列表
             * @param no 订单编号
             * @param status 订单状态
             * @param skip
             * @param limit
             */
            listOrder: function (no, status, skip, limit) {
                // return restClient.get(CONTEXT.CRM_CTX + '/order?no=' + no + '&status=' + status + '&skip=' + skip + '&limit=' + limit);
                return restClient.get(CONTEXT.CRM_CTX + '/order?skip=' + skip + '&limit=' + limit);
            },
            /**
             * 创建订单
             * @param id 医院ID
             * @param obj
             */
            createOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/order', obj);
            },
            /**
             * 查询订单信息
             * @param id 订单ID
             */
            queryOrder: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/order/' + id);
            },
            /**
             * 取消订单
             * @param id 订单ID
             * @param obj
             */
            cancelOrder: function (id, obj) {
                return restClient.delete(CONTEXT.CRM_CTX + '/order/' + id, obj);
            },
            /**
             * 接受订单
             * @param id 订单ID
             * @param obj
             */
            permitOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/permit', obj);
            },
            /**
             * 拒绝订单
             * @param id 订单ID
             * @param obj
             */
            rejectOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/reject', obj);
            },
            /**
             * 确认订单
             * @param id 订单ID
             * @param obj
             */
            confirmOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/confirm', obj);
            },
            /**
             * 根据订单查询出库清单
             * @param id 订单ID
             * @param skip
             * @param limit
             */
            queryStockItemByOrder: function (id, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/order/' + id + '/goods?skip=' + skip + '&limit=' + limit);
            }
        }
    }]);

