'use strict';
var app = angular.module('wxApp');

app.factory('restClient', ['$http', '$q', function ($http, $q) {
    var reject = function (deferred, data, status) {
        /*TODO*/
        deferred.reject(data, status);
    };

    return {
        get: function (url) {
            var deferred = $q.defer();
            $http.get(url).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        post: function (url, data) {
            var deferred = $q.defer();
            $http.post(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        put: function (url, data) {
            var deferred = $q.defer();
            $http.put(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        patch: function (url, data) {
            var deferred = $q.defer();
            $http.patch(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        delete: function (url) {
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

/*通用服务*/
app.factory('commonService', ['restClient', 'CTX',
    function (restClient, CTX) {
        return {
            test: function () {
                return restClient.get(CTX);
            }
        }
    }]);

/*医院*/
app.factory('hospitalService', ['restClient', 'CTX',
    function (restClient, CTX) {
        return {
            /**
             * 新建医院
             * @param obj 医院基本信息
             */
            createHospital: function (obj) {
                return restClient.post(CTX + '/hospital', obj);
            },
            /**
             * 查询医院列表
             * @param name 医院名称
             * @param skip
             * @param limit
             */
            listHospital: function (name, skip, limit) {
                //return restClient.get(CTX + '/hospital?name=' + name + '&skip=' + skip + '&limit=' + limit);
                return restClient.get(CTX + '/hospital?skip=' + skip + '&limit=' + limit);
            },
            /**
             * 查询医院信息
             * @param id 医院ID
             */
            queryHospital: function (id) {
                return restClient.get(CTX + '/hospital/' + id);
            },
            /**
             * 编辑医院信息
             * @param id 医院ID
             * @param obj
             */
            editHospital: function (id, obj) {
                return restClient.patch(CTX + '/hospital/' + id, obj);
            },
            /**
             * 申请开发医院
             * @param id
             * @param obj
             */
            developHospital: function (id, obj) {
                return restClient.post(CTX + '/hospital/' + id + '/develop', obj);
            },
            /**
             * 开发过程中填写开发履历
             * @param id
             * @param obj
             */
            EditDevelopResume: function (id, obj) {
                return restClient.post(CTX + '/hospital/' + id + '/resume', obj);
            },
            /**
             * 查询医生列表
             * @param id
             * @param name
             * @param skip
             * @param limit
             */
            listDoctor: function (id, name, skip, limit) {
                return restClient.get(CTX + '/hospital/' + id + '/doctor?skip=' + skip + '&limit=' + limit);
            },
            /**
             * 添加医生
             * @param id
             * @param obj
             * @returns {*}
             */
            createDoctor: function (id, obj) {
                return restClient.post(CTX + '/hospital/' + id + '/doctor', obj);
            },
            /**
             * 删除医生
             * @param id
             * @returns {*}
             */
            removeDoctor: function (id) {
                return restClient.delete(CTX + '/doctor/' + id);
            }
        }
    }]);