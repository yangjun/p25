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
app.factory('commonService', ['$window', '$timeout',
    function ($window, $timeout) {
        return {
            /**
             * 设置页面标题
             * @param title 标题
             */
            title: function (title) {
                var $body = $('body');
                $window.document.title = title;
                /*HACK：在微信等 webview 中无法修改 document.title*/
                var $iframe = $('<iframe style="display: none;"></iframe>').on('load', function () {
                    $timeout(function () {
                        $iframe.off('load').remove()
                    }, 0);
                }).appendTo($body);
            }
        }
    }]);