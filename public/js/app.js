'use strict';
var app = angular.module('wxApp', ['ui.router', 'angular-jwt']);

app.config(['$stateProvider', '$urlRouterProvider', '$httpProvider', '$windowProvider', 'jwtInterceptorProvider', 'ROUTER',
    function ($stateProvider, $urlRouterProvider, $httpProvider, $windowProvider, jwtInterceptorProvider, ROUTER) {
        ROUTER($stateProvider, $urlRouterProvider);
        var $window = $windowProvider.$get();

        /**
         * 配置 JWT 拦截器，为每一个 $http 请求添加请求头：Authorization: Bearer [JWT_TOKEN]。
         * 可支持 refresh token。
         */
        jwtInterceptorProvider.tokenGetter = ['jwtHelper', '$http', function (jwtHelper, $http) {
            var TOKEN = $window.localStorage.getItem('TOKEN');
            return TOKEN;

            /* TODO REFRESH TOKEN
            var refreshToken = $window.localStorage.getItem('refresh_token');
            if (jwtHelper.isTokenExpired(JWT_TOKEN)) {
                return $http({
                    url: '/delegation',
                    skipAuthorization: true, /!*This makes it so that this request doesn't send the JWT*!/
                    method: 'POST',
                    data: {
                        grant_type: 'refresh_token',
                        refresh_token: refreshToken
                    }
                }).then(function (result) {
                    var token = result.data.id_token;
                    $window.localStorage.setItem('JWT_TOKEN', token);
                    return token;
                });
            } else {
                return JWT_TOKEN;
            }
            */
        }];

        $httpProvider.interceptors.push('jwtInterceptor');
    }]);

app.run(['$rootScope', '$state', function ($rootScope, $state) {
    $rootScope.$state = $state;

    /*页面相关设置*/
    $rootScope.config = {
        title: {
            title: false, /*标题*/
            back: false, /*返回按钮地址*/
            menu: false /*菜单回调方法*/
        }
    };

    /*设置菜单*/
    $rootScope.menu = function () {
        if (!!$rootScope.config.title.menu && (typeof $rootScope.config.title.menu === 'function')) {
            $rootScope.config.title.menu();
        }
    };

}]);