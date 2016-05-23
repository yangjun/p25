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
            orderService.pageOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            orderService.pageOrder($scope.filter.no, $scope.filter.status, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = $scope.orders.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);

/**
 * 订单：订单信息
 */
app.controller('OrderInfoCtrl', ['$rootScope', '$scope', 'orderService', 'hospitalService',
    function ($rootScope, $scope, orderService, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            orderService.getOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '订单信息',
                        hasback: true,
                        backurl: '#/crm/order/list',
                        hasmenu: true,
                        menufunc: function () {
                            var actionButtons = [];

                            if ($scope.order.status === 'idle') {
                                actionButtons.push({
                                    text: '提交订单',
                                    onClick: function () {
                                        $scope.$state.go('crm.order.commit', {oid: $scope.$state.params.oid});
                                    }
                                });
                            }

                            if ($scope.order.status === 'idle'
                                || $scope.order.status === 'firstReview'
                                || $scope.order.status === 'review'
                                || $scope.order.status === 'stock'
                                || $scope.order.status === 'goodsReceipt'
                                || $scope.order.status === 'achieve') {
                                actionButtons.push({
                                    text: '拒绝订单',
                                    onClick: function () {
                                        $scope.$state.go('crm.order.reject', {oid: $scope.$state.params.oid});
                                    }
                                });
                            }

                            if ($scope.order.status === 'idle'
                                || $scope.order.status === 'firstReview'
                                || $scope.order.status === 'review'
                                || $scope.order.status === 'stock'
                                || $scope.order.status === 'goodsReceipt'
                                || $scope.order.status === 'achieve') {
                                actionButtons.push({
                                    text: '确认订单',
                                    onClick: function () {
                                        $scope.$state.go('crm.order.confirm', {oid: $scope.$state.params.oid});
                                    }
                                });
                            }

                            var stockButtons = [];

                            if ($scope.stockOrderId) {
                                stockButtons.push({
                                    text: '查看出库清单',
                                    onClick: function () {
                                        $scope.$state.go('crm.order.goods', {oid: $scope.$state.params.oid});
                                    }
                                });
                            }

                            var cancelButton = [
                                {
                                    text: '取消',
                                    color: 'danger'
                                }
                            ];

                            var actions = [];
                            if (actionButtons.length > 0) {
                                actions.push(actionButtons);
                            }
                            if (stockButtons.length > 0) {
                                actions.push(stockButtons);
                            }
                            actions.push(cancelButton);

                            $.actions(actions);
                        }
                    }
                };

                hospitalService.getHospital($scope.order.hospitalId).then(function (result) {
                    $scope.order.hospital = result.data;
                }).finally(function () {
                    $.hideIndicator($scope);
                });
            });
        };
        $scope.load();
    }]);

/**
 * 订单：提交订单
 */
app.controller('OrderCommitCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '提交订单',
                hasback: true,
                backurl: '#/crm/order/' + $scope.$state.params.oid + '/info'
            }
        };

        $scope.commitOrderRequest = {who: '', reason: ''};

        $scope.load = function () {
            $.showIndicator($scope);
            orderService.getOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*提交订单*/
        $scope.commitOrder = function (form) {
            if (form.$invalid) {
                if (form.who.$error.required) {
                    $.toast('订单接收人不能为空');
                    return;
                }
            }
            orderService.commitOrder($scope.$state.params.oid, $scope.commitOrderRequest).then(function (result) {
                $scope.$state.go('crm.order.info', {oid: $scope.$state.params.oid});
            });
        };

        $scope.load();

    }]);

/**
 * 订单：拒绝订单
 */
app.controller('OrderRejectCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '拒绝订单',
                hasback: true,
                backurl: '#/crm/order/' + $scope.$state.params.oid + '/info'
            }
        };

        $scope.rejectOrderReason = {reason: ''};

        $scope.load = function () {
            $.showIndicator($scope);
            orderService.getOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*拒绝订单*/
        $scope.rejectOrder = function () {
        };

        $scope.load();

    }]);

/**
 * 订单：确认订单
 */
app.controller('OrderConfirmCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '确认订单',
                hasback: true,
                backurl: '#/crm/order/' + $scope.$state.params.oid + '/info'
            }
        };

        $scope.confirmOrderReason = {reason: ''};

        $scope.load = function () {
            $.showIndicator($scope);
            orderService.getOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*确认订单*/
        $scope.confirmOrder = function () {
            $.showIndicator($scope);
            orderService.confirmOrder($scope.$state.params.oid, $scope.confirmOrderReason).then(function (result) {
                $scope.$state.go('crm.order.info', {oid: $scope.$state.params.oid});
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

    }]);

/**
 * 订单：出库清单
 */
app.controller('OrderGoodsCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '出库清单',
                hasback: true,
                backurl: '#/crm/order/' + $scope.$state.params.oid + '/info'
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
            orderService.pageStock($scope.$state.params.oid, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.stocks = result.data;
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*加载更多*/
        $scope.loadMore = function () {
            $.showIndicator($scope);
            $scope.filter.skip += $scope.filter.limit;
            orderService.pageStock($scope.$state.params.oid, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.stocks = $scope.stocks.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);

