'use strict';
var app = angular.module('wxApp');

/**
 * 我的：首页
 */
app.controller('MeHomeCtrl', ['$rootScope', '$scope', 'selfService',
    function ($rootScope, $scope, selfService) {
    }]);

/**
 * 我的：订单列表
 */
app.controller('MeOrdersCtrl', ['$rootScope', '$scope', 'selfService',
    function ($rootScope, $scope, selfService) {
        $scope.hasmore = false;
        $scope.filter = {
            no: '',
            status: '',
            skip: 0,
            limit: 10
        };

        $rootScope.config = {
            title: {
                title: '我的订单',
                back: '#/me/home',
                menu: function () {
                    var actionButtons = [
                        {
                            text: '按订单状态过滤',
                            label: true
                        },
                        {
                            text: '申请中',
                            color: $scope.filter.status == 'idle' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'idle';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '初审中',
                            color: $scope.filter.status == 'firstReview' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'firstReview';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '审核中',
                            color: $scope.filter.status == 'review' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'review';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '出库中',
                            color: $scope.filter.status == 'stock' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'stock';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '收货中',
                            color: $scope.filter.status == 'goodsReceipt' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'goodsReceipt';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '已完成',
                            color: $scope.filter.status == 'achieve' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'achieve';
                                $scope.load(0);
                            }
                        },
                        {
                            text: '已取消',
                            color: $scope.filter.status == 'cancel' ? 'danger' : '',
                            onClick: function () {
                                $scope.filter.status = 'cancel';
                                $scope.load(0);
                            }
                        }
                    ];
                    var cancelButton = [
                        {
                            text: '取消',
                            color: 'danger'
                        }
                    ];

                    $.actions([actionButtons, cancelButton]);
                }
            }
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            selfService.pageOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            selfService.pageOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = $scope.orders.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);
    }]);

/**
 * 我的：任务列表
 */
app.controller('MeTasksCtrl', ['$rootScope', '$scope', 'selfService',
    function ($rootScope, $scope, selfService) {
        $scope.hasmore = false;
        $scope.filter = {
            status: '',
            action: '',
            skip: 0,
            limit: 10
        };

        $rootScope.config = {
            title: {
                title: '我的任务',
                back: '#/me/home',
                menu: function () {
                    var actionButtons = [];
                    var cancelButton = [
                        {
                            text: '取消',
                            color: 'danger'
                        }
                    ];

                    $.actions([cancelButton]);
                }
            }
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            selfService.pageTask($scope.filter.status, $scope.filter.action, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.tasks = result.data;
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
            selfService.pageTask($scope.filter.status, $scope.filter.action, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.tasks = $scope.tasks.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);

