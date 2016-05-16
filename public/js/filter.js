'use strict';
var app = angular.module('wxApp');

/*回车事件*/
app.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if (event.which === 13) {
                scope.$apply(function () {
                    scope.$eval(attrs.ngEnter, {'event': event});
                });
                event.preventDefault();
            }
        });
    };
});

/*医院状态*/
app.filter('hospitalStatus', function () {
    return function (decision) {
        switch (decision) {
            case 'idle':
                return '待开发';
            case 'developing':
                return '开发中';
            case 'partner':
                return '合作伙伴';
        }
    }
});

/*订单状态*/
app.filter('orderStatus', function () {
    return function (decision) {
        switch (decision) {
            case 'idle':
                return '申请阶段';
            case 'firstReview':
                return '初审阶段';
            case 'review':
                return '审核阶段';
            case 'stock':
                return '出库阶段';
            case 'goodsReceipt':
                return '收货阶段';
            case 'achieve':
                return '已完成';
            case 'cancel':
                return '已取消';
        }
    }
});

