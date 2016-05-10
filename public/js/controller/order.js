'use strict';
var app = angular.module('wxApp');

/**
 * 订单：所有订单
 */
app.controller('OrderListCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有订单'
            }
        };

        $scope.hasmore = false;
        $scope.filter = {
            no: '',
            status: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            orderService.listOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = result.data;
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*搜索框失去焦点后立即加载新数据*/
        $(document).on("blur", ".searchbar input", function (b) {
            $scope.load(0);
        });

        /*加载更多*/
        $scope.loadMore = function () {
            $.showIndicator($scope);
            $scope.filter.skip += $scope.filter.limit;
            orderService.listOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = $scope.orders.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);
