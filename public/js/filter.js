'use strict';
var app = angular.module('wxApp');

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

