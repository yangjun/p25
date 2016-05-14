'use strict';
var app = angular.module('wxApp');

/**
 * 订单：所有订单
 */
app.controller('OrderListCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $scope.hasmore = false;
        $scope.filter = {
            no: '',
            status: '',
            skip: 0,
            limit: 10
        };

        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有订单',
                hasmenu: true,
                menufunc: function () {
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
