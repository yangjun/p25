'use strict';
var app = angular.module('wxApp');

/**
 * 医院：首页
 */
app.controller('HospitalHomeCtrl', ['$rootScope', '$scope',
    function ($rootScope, $scope) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '医院'
            }
        };
    }]);

/**
 * 医院：所有医院
 */
app.controller('HospitalListCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有医院',
                hasback: true,
                backurl: '#/hospital/home'
            }
        };

        $scope.hasless = false;
        $scope.hasmore = true;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $scope.loading = true;
            $.showPreloader();
            $scope.filter.skip = skip;
            hospitalService.listHospital($scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.hospitals = result.data;
            }).finally(function () {
                $scope.loading = false;
                $.hidePreloader();
                $scope.hasless = $scope.filter.skip > 0;
                $scope.hasmore = $scope.hospitals.length == $scope.filter.limit;
            });
        };

        $scope.load(0);

    }]);

/**
 * 医院：医院信息
 */
app.controller('HospitalInfoCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            /*TODO 获取医院信息*/
            $scope.hospital = {
                id: '',
                name: '',
                area: '',
                address: '',
                loc: {
                    latitude: 0,
                    longitude: 0
                }
            };
            $rootScope.config = {
                title: {
                    hastitle: true,
                    title: '{{医院名}}',
                    hasback: true,
                    backurl: '#/hospital/list'
                }
            };
        };
        $scope.load();


        $scope.popupMenu = function () {
            var buttons1 = [
                {
                    text: '编辑',
                    onClick: function () {
                        $scope.$state.go('hospital.edit', {id: $scope.hospital.id});
                    }
                },
                {
                    text: '申请开发',
                    onClick: function () {
                        $scope.$state.go('hospital.develop', {id: $scope.hospital.id});
                    }
                }
            ];
            var buttons2 = [
                {
                    text: '取消',
                    color: 'danger'
                }
            ];
            var groups = [buttons1, buttons2];
            $.actions(groups);
        };

    }]);

/**
 * 医院：新建医院
 */
app.controller('HospitalCreateCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '新建医院',
                hasback: true,
                backurl: '#/hospital/home'
            }
        };

        $scope.hospital = {
            name: '',
            area: '',
            address: '',
            loc: {
                latitude: 0,
                longitude: 0
            }
        };

        $scope.createHospital = function () {
            hospitalService.createHospital({'hospital': $scope.hospital}).then(function (result) {
                $.toast("新建成功");
            });
        };
    }]);

/**
 * 医院：编辑医院
 */
app.controller('HospitalEditCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $scope.hospital = {
                name: '',
                area: '',
                address: '',
                loc: {
                    latitude: 0,
                    longitude: 0
                }
            };
            $rootScope.config = {
                title: {
                    hastitle: true,
                    title: '编辑医院',
                    hasback: true,
                    backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                }
            };
        };
        $scope.load();

        $scope.editHospital = function () {
            hospitalService.editHospital($scope.$state.params.id, {'hospital': $scope.hospital}).then(function (result) {
                $.toast("保存成功");
            });
        };
    }]);

/**
 * 医院：申请开发
 */
app.controller('HospitalDevelopCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $scope.hospital = {
                name: '{{医院名称}}',
                area: '',
                address: '',
                loc: {
                    latitude: 0,
                    longitude: 0
                }
            };
            $rootScope.config = {
                title: {
                    hastitle: true,
                    title: '开发医院',
                    hasback: true,
                    backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                }
            };
        };
        $scope.load();

    }]);
