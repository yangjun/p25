'use strict';
var app = angular.module('wxApp');

/**
 * 医院：所有医院
 */
app.controller('HospitalListCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有医院',
                hasmenu: true,
                menufunc: function () {
                    var actionButtons = [
                        {
                            text: '添加医院',
                            onClick: function () {
                                $scope.$state.go('hospital.create');
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

        $scope.hasmore = false;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.listHospital($scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.hospitals = result.data;
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
            hospitalService.listHospital($scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.hospitals = $scope.hospitals.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
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
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = result.data;
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '医院信息',
                        hasback: true,
                        backurl: '#/hospital/list',
                        hasmenu: true,
                        menufunc: function () {
                            var actionButtons = [
                                {
                                    text: '编辑',
                                    onClick: function () {
                                        $scope.$state.go('hospital.edit', {id: $scope.hospital.id});
                                    }
                                }
                            ];
                            var doctorButtons = [
                                {
                                    text: '查看医生',
                                    onClick: function () {
                                        $scope.$state.go('hospital.doctor.list', {id: $scope.hospital.id});
                                    }
                                }
                            ];
                            var orderButtons = [
                                {
                                    text: '查看订单',
                                    onClick: function () {
                                        $scope.$state.go('hospital.order.list', {id: $scope.hospital.id});
                                    }
                                }
                            ];
                            var cancelButton = [
                                {
                                    text: '取消',
                                    color: 'danger'
                                }
                            ];

                            /*申请开发菜单*/
                            if ($scope.hospital.status === 'idle') {
                                var developButton = {
                                    text: '申请开发',
                                    onClick: function () {
                                        $scope.$state.go('hospital.develop', {id: $scope.hospital.id});
                                    }
                                };
                                actionButtons.push(developButton);
                            }

                            /*记录开发过程菜单*/
                            if ($scope.hospital.status === 'developing') {
                                var resumeButton = {
                                    text: '记录开发过程',
                                    onClick: function () {
                                        $scope.$state.go('hospital.resume', {id: $scope.hospital.id});
                                    }
                                };
                                actionButtons.push(resumeButton);
                            }

                            /*编辑归档信息菜单*/
                            if ($scope.hospital.status === 'partner') {
                                var archiveButton = {
                                    text: '编辑归档信息',
                                    onClick: function () {
                                        $scope.$state.go('hospital.archive', {id: $scope.hospital.id});
                                    }
                                };
                                actionButtons.push(archiveButton);
                            }

                            $.actions([actionButtons, doctorButtons, orderButtons, cancelButton]);
                        }
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

    }]);

/**
 * 医院：新建医院
 */
app.controller('HospitalCreateCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '添加医院',
                hasback: true,
                backurl: '#/hospital/list'
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

        $scope.createHospital = function (form) {
            if (form.$invalid) {
                if (form.name.$error.required) {
                    $.toast('医院名称不能为空');
                    return;
                }
            }
            hospitalService.createHospital({'hospital': $scope.hospital}).then(function (result) {
                $.toast('添加成功');
                $scope.$state.go('hospital.list');
            });
        };
    }]);

/**
 * 医院：编辑医院
 */
app.controller('HospitalEditCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    id: result.data.id,
                    name: result.data.base.name,
                    area: result.data.base.area,
                    address: result.data.base.address,
                    loc: {
                        latitude: result.data.base.loc.latitude,
                        longitude: result.data.base.loc.longitude
                    }
                };
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '编辑医院信息',
                        hasback: true,
                        backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.editHospital = function () {
            hospitalService.editHospital($scope.$state.params.id, {'hospital': $scope.hospital}).then(function (result) {
                $scope.$state.go('hospital.info', {id: $scope.$state.params.id});
            });
        };
    }]);

/**
 * 医院：申请开发
 */
app.controller('HospitalDevelopCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    day: '',
                    salesman: ''
                };
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '申请开发医院',
                        hasback: true,
                        backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.developHospital = function (form) {
            if (form.$invalid) {
                if (form.day.$error.required) {
                    $.toast('开发天数不能为空');
                    return;
                }
            }

            hospitalService.developHospital($scope.$state.params.id, $scope.hospital).then(function (result) {
                $.toast('申请成功');
                $scope.$state.go('hospital.info', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：记录开发进度
 */
app.controller('HospitalResumeCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    hospitalDescription: result.data.lastDevelopResume.hospitalDescription,
                    scheduleDescription: result.data.lastDevelopResume.scheduleDescription
                };
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '记录开发进度',
                        hasback: true,
                        backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.resumeHospital = function (form) {
            hospitalService.EditDevelopResume($scope.$state.params.id, $scope.hospital).then(function (result) {
                $scope.$state.go('hospital.info', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：编辑归档信息
 */
app.controller('HospitalArchiveCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    hospitalDescription: result.data.lastDevelopResume.hospitalDescription,
                    scheduleDescription: result.data.lastDevelopResume.scheduleDescription
                };
                $rootScope.config = {
                    title: {
                        hastitle: true,
                        title: '编辑归档信息',
                        hasback: true,
                        backurl: '#/hospital/' + $scope.$state.params.id + '/info'
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.resumeHospital = function (form) {
            hospitalService.EditDevelopResume($scope.$state.params.id, $scope.hospital).then(function (result) {
                $scope.$state.go('hospital.info', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：医生：所有医生
 */
app.controller('HospitalDoctorListCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有医生',
                hasback: true,
                backurl: '#/hospital/' + $scope.$state.params.id + '/info',
                hasmenu: true,
                menufunc: function () {
                    var actionButtons = [
                        {
                            text: '添加医生',
                            onClick: function () {
                                $scope.$state.go('hospital.doctor.create', {id: $scope.$state.params.id});
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

        $scope.hasmore = true;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.listDoctor($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.doctors = result.data;
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
            hospitalService.listDoctor($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.doctors = $scope.doctors.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*点击医生时，弹出菜单*/
        $scope.popupMenu = function (doctor) {
            var actionButtons = [
                {
                    text: '删除',
                    onClick: function () {
                        $.confirm('确认删除 ？',
                            function () {
                                hospitalService.removeDoctor(doctor.id).then(function (result) {
                                    $scope.load(0);
                                });
                            },
                            function () {
                            }
                        );
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
        };

        $scope.load(0);

    }]);

/**
 * 医院：医生：添加医生
 */
app.controller('HospitalDoctorCreateCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '添加医生',
                hasback: true,
                backurl: '#/hospital/' + $scope.$state.params.id + '/doctor/list'
            }
        };

        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.doctor = {
                    name: '',
                    email: '',
                    mobile: '',
                    job: '',
                    hospital: result.data.base.name
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.createDoctor = function (form) {
            if (form.$invalid) {
                if (form.name.$error.required) {
                    $.toast('姓名不能为空');
                    return;
                }
                if (form.email.$error.pattern) {
                    $.toast('邮箱格式错误');
                    return;
                }
                if (form.mobile.$error.pattern) {
                    $.toast('手机格式错误');
                    return;
                }
            }
            hospitalService.createDoctor($scope.$state.params.id, $scope.doctor).then(function (result) {
                $.toast('添加成功');
                $scope.$state.go('hospital.doctor.list', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：订单：所有订单
 */
app.controller('HospitalOrderListCtrl', ['$rootScope', '$scope', 'hospitalService', 'orderService',
    function ($rootScope, $scope, hospitalService, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '所有订单',
                hasback: true,
                backurl: '#/hospital/' + $scope.$state.params.id + '/info',
                hasmenu: true,
                menufunc: function () {
                    var actionButtons = [
                        {
                            text: '添加订单',
                            onClick: function () {
                                $scope.$state.go('hospital.order.create', {id: $scope.$state.params.id});
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

        $scope.hasmore = true;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.listHospitalOrder($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            hospitalService.listHospitalOrder($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = $scope.orders.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*点击订单时，弹出菜单*/
        $scope.popupMenu = function (order) {
            var actionButtons = [
                {
                    text: '删除',
                    onClick: function () {
                        /*点击‘删除’菜单，跳转‘删除订单’页面*/
                        $scope.$state.go('hospital.order.remove', {id: $scope.$state.params.id, oid: order.id});
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
        };

        $scope.load(0);

    }]);

/**
 * 医院：订单：添加订单
 */
app.controller('HospitalOrderCreateCtrl', ['$rootScope', '$scope', 'hospitalService', 'orderService',
    function ($rootScope, $scope, hospitalService, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '添加订单',
                hasback: true,
                backurl: '#/hospital/' + $scope.$state.params.id + '/order/list'
            }
        };

        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.queryHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = result.data;
                $scope.order = {
                    hospitalId: $scope.$state.params.id,
                    items: [],
                    proposer: '',
                    notes: ''
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        /*添加订单项*/
        $scope.addOrderItem = function () {
            var item = {
                no: $scope.order.items.length + 1,
                goodsName: '',
                specification: '',
                unit: '',
                quantity: 0,
                notes: ''
            };
            $scope.order.items.push(item);
        };

        /*创建订单*/
        $scope.createOrder = function (form) {
            if (form.$invalid) {
                if (form.no.$error.required) {
                    $.toast('序号不能为空');
                    return;
                }
                if (form.goodsName.$error.required) {
                    $.toast('药品名称不能为空');
                    return;
                }
                if (form.specification.$error.required) {
                    $.toast('规格型号不能为空');
                    return;
                }
                if (form.unit.$error.required) {
                    $.toast('单位不能为空');
                    return;
                }
                if (form.quantity.$error.required) {
                    $.toast('数量不能为空');
                    return;
                }
            }
            if (!$scope.order.items || $scope.order.items.length <= 0) {
                $.toast('订单项不能为空');
                return;
            }
            orderService.createOrder($scope.$state.params.id, $scope.order).then(function (result) {
                $scope.$state.go('hospital.order.list', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：订单：删除订单
 */
app.controller('HospitalOrderRemoveCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                hastitle: true,
                title: '删除订单',
                hasback: true,
                backurl: '#/hospital/' + $scope.$state.params.id + '/order/list'
            }
        };

        $scope.cancelOrderReason = {reason: ''};

        $scope.load = function () {
            $.showIndicator($scope);
            orderService.queryOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*删除订单*/
        $scope.cancelOrder = function () {
            $.showIndicator($scope);
            orderService.cancelOrder($scope.order.id, $scope.cancelOrderReason).then(function (result) {
                $scope.$state.go('hospital.order.list', {id: $scope.$state.params.id});
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };
        $scope.load();

    }]);
