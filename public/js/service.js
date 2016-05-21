'use strict';
var app = angular.module('wxApp');

app.factory('restClient', ['$http', '$q', function ($http, $q) {
    var reject = function (deferred, data, status) {
        /*TODO*/
        deferred.reject(data, status);
    };

    return {
        get: function (url) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.get(url).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        post: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.post(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        put: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.put(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        patch: function (url, data) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.patch(url, data).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        },
        delete: function (url) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
            var deferred = $q.defer();
            $http.delete(url).then(function (data, status, headers, config) {
                deferred.resolve(data);
            }, function (data, status, headers, config) {
                reject(deferred, data, status);
            });
            return deferred.promise;
        }
    };
}]);

/*医院*/
app.factory('hospitalService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询医院
             * @param name 医院名称
             * @param skip
             * @param limit
             */
            pageHospital: function (name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital?name=' + name + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 获取医院
             * @param id 医院ID
             */
            getHospital: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id);
            },
            /**
             * 添加医院
             * @param obj
             */
            createHospital: function (obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital', obj);
            },
            /**
             * 编辑医院
             * @param id 医院ID
             * @param obj
             */
            editHospital: function (id, obj) {
                return restClient.patch(CONTEXT.CRM_CTX + '/hospital/' + id, obj);
            },
            /**
             * 申请开发医院
             * @param id 医院ID
             * @param obj
             */
            developHospital: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/develop', obj);
            },
            /**
             * 记录开发过程；开发过程中填写开发履历
             * @param id 医院ID
             * @param obj
             */
            editDevelopResume: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/resume', obj);
            },
            /**
             * 归档
             * @param id 医院ID
             * @param obj
             */
            becomePartner: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/partner', obj);
            },
            /**
             * 编辑归档
             * @param id 医院ID
             * @param obj
             */
            editArchive: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/archive', obj);
            },
            /**
             * 添加医生
             * @param id 医院ID
             * @param obj
             * @returns {*}
             */
            createDoctor: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/doctor', obj);
            },
            /**
             * 分页查询医生
             * @param id 医院ID
             * @param name
             * @param skip
             * @param limit
             */
            pageDoctor: function (id, name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id + '/doctor?name=' + name + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 添加订单
             * @param id 医院ID
             * @param obj
             * @returns {*}
             */
            createOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/hospital/' + id + '/order', obj);
            },
            /**
             * 分页查询订单
             * @param id 医院ID
             * @param name
             * @param skip
             * @param limit
             */
            pageOrder: function (id, name, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/hospital/' + id + '/order?skip=' + skip + '&limit=' + limit);
            }
        }
    }]);

/*医生*/
app.factory('doctorService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 删除医生
             * @param id 医生ID
             * @returns {*}
             */
            deleteDoctor: function (id) {
                return restClient.delete(CONTEXT.CRM_CTX + '/doctor/' + id);
            }
        }
    }]);

/*订单*/
app.factory('orderService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询订单
             * @param no 订单编号
             * @param status 订单状态
             * @param skip
             * @param limit
             */
            pageOrder: function (no, status, skip, limit) {
                // return restClient.get(CONTEXT.CRM_CTX + '/order?no=' + no + '&status=' + status + '&skip=' + skip + '&limit=' + limit);
                return restClient.get(CONTEXT.CRM_CTX + '/order?skip=' + skip + '&limit=' + limit);
            },
            /**
             * 获取订单
             * @param id 订单ID
             */
            getOrder: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/order/' + id);
            },
            /**
             * 提交订单
             * @param id 订单ID
             * @param obj
             */
            commitOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/commit', obj);
            },
            /**
             * 取消订单
             * @param id 订单ID
             * @param obj
             */
            cancelOrder: function (id, obj) {
                return restClient.delete(CONTEXT.CRM_CTX + '/order/' + id, obj);
            },
            /**
             * 分页查询库存
             * @param id 订单ID
             * @param skip
             * @param limit
             */
            pageStock: function (id, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/order/' + id + '/goods?skip=' + skip + '&limit=' + limit);
            },
            /**
             * 创建库存
             * @param id 订单ID
             * @param obj
             */
            createStock: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/stock', obj);
            },
            /**
             * 出库
             * @param id 订单ID
             * @param obj
             */
            deliveryOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/delivery', obj);
            },
            /**
             * 确认订单：收货
             * @param id 订单ID
             * @param obj
             */
            confirmOrder: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/order/' + id + '/confirm', obj);
            }
        }
    }]);

/*任务*/
app.factory('taskService', ["restClient", "CONTEXT",
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询任务
             * @param status 状态
             * @param action 动作
             * @param skip
             * @param limit
             * @returns {*}
             */
            pageTask: function (status, action, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/task?status=' + status + '&action=' + action + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 获取任务
             * @param id 任务ID
             * @returns {*}
             */
            getTask: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/task/' + id);
            },
            /**
             * 接受任务
             * @param id 任务ID
             * @returns {*}
             */
            acceptTask: function (id) {
                return restClient.post(CONTEXT.CRM_CTX + '/task/' + id + '/accept');
            },
            /**
             * 同意任务
             * @param id 任务ID
             * @param obj
             * @returns {*}
             */
            permitTask: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/task/' + id + '/permit', obj);
            },
            /**
             * 拒绝任务
             * @param id 任务ID
             * @param obj
             * @returns {*}
             */
            rejectTask: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/task/' + id + '/reject', obj);
            },
            /**
             * 分页查询参与者
             * @param id 任务ID
             * @param skip
             * @param limit
             * @returns {*}
             */
            pageStakeholder: function (id, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/task/' + id + '/stakeholders?skip=' + skip + '&limit=' + limit);
            }
        }
    }]);

/*个人*/
app.factory('selfService', ["restClient", "CONTEXT",
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询订单
             * @param no 编号
             * @param status 状态
             * @param skip
             * @param limit
             * @returns {*}
             */
            pageOrder: function (no, status, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/my/order?no=' + no + '&status' + status + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 分页查询任务
             * @param status 状态
             * @param action 动作
             * @param skip
             * @param limit
             * @returns {*}
             */
            pageTask: function (status, action, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/my/task?status=' + status + '&action' + action + '&skip=' + skip + '&limit=' + limit);
            }
        }
    }
]);


/*权限*/
app.factory('authService', ['restClient', 'CONTEXT',
    function (restClient, CONTEXT) {
        return {
            /**
             * 获取个人信息
             * @returns {*}
             */
            getProfile: function () {
                return restClient.get(CONTEXT.CRM_CTX + '/auth/userProfile');
            },
            /**
             * 关键字查找用户
             * @param key 关键字
             * @returns {*}
             */
            getUser: function (key) {
                return restClient.get(CONTEXT.CRM_CTX + '/auth/user?name=' + key);
            },
            /**
             * 编辑用户
             * @param id 用户ID
             * @param obj
             * @returns {*}
             */
            updateUser: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/auth/user/' + id, obj);
            },
            /**
             * 获取所有角色
             * @returns {*}
             */
            listRole: function () {
                return restClient.get(CONTEXT.CRM_CTX + '/auth/roles');
            },
            /**
             * 给用户分配角色
             * @param id 用户ID
             * @param role 角色名
             * @returns {*}
             */
            allocateRole: function (id, role) {
                return restClient.post(CONTEXT.CRM_CTX + '/auth/user/' + id + '/role?role=' + role);
            },
            /**
             * 给用户移除角色
             * @param id 用户ID
             * @param role 角色名
             * @returns {*}
             */
            removeRole: function (id, role) {
                return restClient.delete(CONTEXT.CRM_CTX + '/auth/user/' + id + '/role?role=' + role);
            }
        }
    }]);

/*处方*/
app.factory('prescriptionService', ["restClient", "CONTEXT",
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询处方
             * @param tag 标签
             * @param skip
             * @param limit
             * @returns {*}
             */
            pagePrescription: function (tag, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/prescription?tag=' + tag + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 获取处方
             * @param id 处方ID
             * @returns {*}
             */
            getPrescription: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/prescription/' + id);
            },
            /**
             * 创建处方
             * @param obj
             * @returns {*}
             */
            createPrescription: function (obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/prescription', obj);
            },
            /**
             * 编辑处方
             * @param id 处方ID
             * @param obj
             * @returns {*}
             */
            editPrescription: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/prescription/' + id, obj);
            },
            /**
             * 删除处方
             * @param id 处方ID
             * @returns {*}
             */
            deletePrescription: function (id) {
                return restClient.delete(CONTEXT.CRM_CTX + '/prescription/' + id);
            },
            /**
             * 赞同处方
             * @param id 处方ID
             * @returns {*}
             */
            approvePrescription: function (id) {
                return restClient.post(CONTEXT.CRM_CTX + '/prescription/' + id + '/praise');
            },
            /**
             * 反对处方
             * @param id 处方ID
             * @returns {*}
             */
            disapprovePrescription: function (id) {
                return restClient.post(CONTEXT.CRM_CTX + '/prescription/' + id + '/disagree');
            },
            /**
             * 切换处方是否热门
             * @param id 处方ID
             * @returns {*}
             */
            swapPrescription: function (id) {
                return restClient.post(CONTEXT.CRM_CTX + '/prescription/' + id + '/swap');
            }
        }
    }
]);

/*库存*/
app.factory('stockService', ["restClient", "CONTEXT",
    function (restClient, CONTEXT) {
        return {
            /**
             * 添加库存
             * @param id 出库单ID
             * @param obj
             */
            addStock: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/stock/' + id, obj);
            },
            /**
             * 移除库存
             * @param id 出库单ID
             * @param obj
             */
            removeStock: function (id, obj) {
                return restClient.delete(CONTEXT.CRM_CTX + '/stock/' + id, obj);
            },
            /**
             * 分页查询库存
             * @param id 出库单ID
             */
            pageStock: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/stock/' + id);
            }
        }
    }]);

/*问题*/
app.factory('questionService', ["restClient", "CONTEXT",
    function (restClient, CONTEXT) {
        return {
            /**
             * 分页查询问题
             * @param key 关键字
             * @param skip
             * @param limit
             */
            pageQuestion: function (key, skip, limit) {
                return restClient.get(CONTEXT.CRM_CTX + '/faq?key=' + key + '&skip=' + skip + '&limit=' + limit);
            },
            /**
             * 获取问题
             * @param id 问题ID
             */
            getQuestion: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/faq/' + id);
            },
            /**
             * 创建问题
             * @param obj
             */
            createQuestion: function (obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/faq', obj);
            },
            /**
             * 编辑问题
             * @param id 问题ID
             * @param obj
             */
            editQuestion: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/faq/' + id, obj);
            },
            /**
             * 删除问题
             * @param id 问题ID
             */
            deleteQuestion: function (id) {
                return restClient.delete(CONTEXT.CRM_CTX + '/faq/' + id);
            },
            /**
             * 分页查询回答
             * @param id 问题ID
             */
            pageAnswer: function (id) {
                return restClient.get(CONTEXT.CRM_CTX + '/faq/' + id + '/answer');
            },
            /**
             * 添加回答
             * @param id 问题ID
             * @param obj
             */
            createAnswer: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/faq/' + id + '/answer', obj);
            },
            /**
             * 编辑回答
             * @param id 回答ID
             * @param obj
             */
            editAnswer: function (id, obj) {
                return restClient.post(CONTEXT.CRM_CTX + '/faq/answer/' + id, obj);
            }
        }
    }]);
