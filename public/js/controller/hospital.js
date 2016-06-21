'use strict';
var app = angular.module('wxApp');

/**
 * 医院：所有医院
 */
app.controller('HospitalListCtrl', ['$scope', 'hospitalService',
    function ($scope, hospitalService) {
        $scope.hasmore = false;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.pageHospital($scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.hospitals = result.data;
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*菜单*/
        $scope.menu = function () {
            var actionButtons = [
                {
                    text: '添加医院',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.create');
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

        /*搜索框失去焦点后立即加载新数据*/
        $(document).on("blur", ".searchbar input", function (b) {
            $scope.load(0);
        });

        /*加载更多*/
        $scope.loadMore = function () {
            $.showIndicator($scope);
            $scope.filter.skip += $scope.filter.limit;
            hospitalService.pageHospital($scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.menu = function () {
            var actionButtons = [
                {
                    text: '编辑',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.edit', {id: $scope.hospital.id});
                    }
                }
            ];
            var doctorButtons = [
                {
                    text: '查看医生',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.doctor.list', {id: $scope.hospital.id});
                    }
                }
            ];
            var orderButtons = [
                {
                    text: '查看订单',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.order.list', {id: $scope.hospital.id});
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
                        $scope.$state.go('crm.hospital.develop', {id: $scope.hospital.id});
                    }
                };
                actionButtons.push(developButton);
            }

            if ($scope.hospital.status === 'developing') {
                /*记录开发进度菜单*/
                var resumeButton = {
                    text: '记录开发进度',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.resume', {id: $scope.hospital.id});
                    }
                };
                actionButtons.push(resumeButton);

                /*归档，成为合作伙伴菜单*/
                var partnerButton = {
                    text: '归档',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.partner', {id: $scope.hospital.id});
                    }
                };
                actionButtons.push(partnerButton);
            }

            /*编辑归档信息菜单*/
            if ($scope.hospital.status === 'partner') {
                var archiveButton = {
                    text: '编辑归档信息',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.archive', {id: $scope.hospital.id});
                    }
                };
                actionButtons.push(archiveButton);
            }

            $.actions([actionButtons, doctorButtons, orderButtons, cancelButton]);
        }
    }]);

/**
 * 医院：新建医院
 */
app.controller('HospitalCreateCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
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
                $scope.$state.go('crm.hospital.list');
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
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
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
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.editHospital = function () {
            hospitalService.editHospital($scope.$state.params.id, {'hospital': $scope.hospital}).then(function (result) {
                $scope.$state.go('crm.hospital.info', {id: $scope.$state.params.id});
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
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    day: '',
                    salesman: ''
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
                if (form.day.$error.number) {
                    $.toast('开发天数必须为正整数');
                    return;
                }
            }

            hospitalService.developHospital($scope.$state.params.id, $scope.hospital).then(function (result) {
                $.toast('申请成功');
                $scope.$state.go('crm.hospital.info', {id: $scope.$state.params.id});
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
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    hospitalDescription: result.data.lastDevelopResume.hospitalDescription,
                    scheduleDescription: result.data.lastDevelopResume.scheduleDescription
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.resumeHospital = function (form) {
            hospitalService.editDevelopResume($scope.$state.params.id, $scope.hospital).then(function (result) {
                $scope.$state.go('crm.hospital.info', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：归档，成为合作伙伴
 */
app.controller('HospitalPartnerCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            /*加载医院信息*/
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    id: result.data.id,
                    name: result.data.base.name
                };

                $scope.partner = {
                    id: result.data.id,
                    principal: {
                        name: '',
                        job: '',
                        mobile: ''
                    },
                    principalDoctor: {
                        name: '',
                        job: '',
                        mobile: ''
                    }
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        /*归档*/
        $scope.becomePartner = function (form) {
            if (form.$invalid) {
                if (form.principal_name.$error.required) {
                    $.toast('负责人姓名不能为空');
                    return;
                }
                if (form.principal_job.$error.required) {
                    $.toast('负责人职位不能为空');
                    return;
                }
                if (form.principal_mobile.$error.required) {
                    $.toast('负责人手机不能为空');
                    return;
                }
                if (form.principal_mobile.$error.pattern) {
                    $.toast('负责人手机格式错误');
                    return;
                }

                if (form.doctor_name.$error.required) {
                    $.toast('用药人姓名不能为空');
                    return;
                }
                if (form.doctor_job.$error.required) {
                    $.toast('用药人职位不能为空');
                    return;
                }
                if (form.doctor_mobile.$error.required) {
                    $.toast('用药人手机不能为空');
                    return;
                }
                if (form.doctor_mobile.$error.pattern) {
                    $.toast('用药人手机格式错误');
                    return;
                }
            }
            hospitalService.becomePartner($scope.$state.params.id, $scope.partner).then(function (result) {
                $scope.$state.go('crm.hospital.info', {id: $scope.$state.params.id});
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
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
                $scope.hospital = {
                    hospitalId: result.data.id,
                    name: result.data.base.name,
                    hospitalDescription: result.data.lastDevelopResume.hospitalDescription,
                    scheduleDescription: result.data.lastDevelopResume.scheduleDescription
                };
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

        $scope.resumeHospital = function (form) {
            hospitalService.editDevelopResume($scope.$state.params.id, $scope.hospital).then(function (result) {
                $scope.$state.go('crm.hospital.info', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：医生：所有医生
 */
app.controller('HospitalDoctorListCtrl', ['$rootScope', '$scope', 'hospitalService', 'doctorService',
    function ($rootScope, $scope, hospitalService, doctorService) {
        $rootScope.menu = function () {
            var actionButtons = [
                {
                    text: '添加医生',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.doctor.create', {id: $scope.$state.params.id});
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

        $scope.hasmore = true;
        $scope.filter = {
            name: '',
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.pageDoctor($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            hospitalService.pageDoctor($scope.$state.params.id, $scope.filter.name, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
                        $.confirm('确认删除 ?',
                            function () {
                                doctorService.deleteDoctor(doctor.id).then(function (result) {
                                    $scope.load(0);
                                });
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
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
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
                $scope.$state.go('crm.hospital.doctor.list');
            });
        };

    }]);

/**
 * 医院：订单：所有订单
 */
app.controller('HospitalOrderListCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $rootScope.menu = function () {
            var actionButtons = [
                {
                    text: '添加订单',
                    onClick: function () {
                        $scope.$state.go('crm.hospital.order.create', {id: $scope.$state.params.id});
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

        $scope.hasmore = true;
        $scope.filter = {
            skip: 0,
            limit: 10
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            hospitalService.pageOrder($scope.$state.params.id, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = result.data;
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*加载更多*/
        $scope.loadMore = function () {
            $.showIndicator($scope);
            $scope.filter.skip += $scope.filter.limit;
            hospitalService.pageOrder($scope.$state.params.id, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.orders = $scope.orders.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);

/**
 * 医院：订单：添加订单
 */
app.controller('HospitalOrderCreateCtrl', ['$rootScope', '$scope', 'hospitalService',
    function ($rootScope, $scope, hospitalService) {
        $scope.load = function () {
            $.showIndicator($scope);
            hospitalService.getHospital($scope.$state.params.id).then(function (result) {
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

        /*移除订单项*/
        $scope.removeOrderItem = function (item) {
            $scope.order.items.splice($scope.order.items.indexOf(item), 1);
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
                if (form.quantity.$error.number) {
                    $.toast('数量必须为正整数');
                    return;
                }
            }
            if (!$scope.order.items || $scope.order.items.length <= 0) {
                $.toast('订单项不能为空');
                return;
            }
            hospitalService.createOrder($scope.$state.params.id, $scope.order).then(function (result) {
                $scope.$state.go('crm.hospital.order.list', {id: $scope.$state.params.id});
            });
        };

    }]);

/**
 * 医院：订单：取消订单
 */
app.controller('HospitalOrderRemoveCtrl', ['$rootScope', '$scope', 'orderService',
    function ($rootScope, $scope, orderService) {
        $rootScope.config = {
            title: {
                title: '取消订单',
                back: '#/crm/hospital/' + $scope.$state.params.id + '/order/list'
            }
        };

        $scope.cancelOrderRequest = {reason: ''};

        $scope.load = function () {
            $.showIndicator($scope);
            orderService.getOrder($scope.$state.params.oid).then(function (result) {
                $scope.order = result.data;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        /*取消订单*/
        $scope.cancelOrder = function () {
            $.showIndicator($scope);
            orderService.cancelOrder($scope.$state.params.oid, $scope.cancelOrderRequest).then(function (result) {
                $scope.$state.go('crm.hospital.order.list', {id: $scope.$state.params.id});
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load();

    }]);
