'use strict';
var app = angular.module('wxApp');

/**
 * 待办任务：所有任务
 */
app.controller('TaskListCtrl', ['$rootScope', '$scope', 'taskService',
    function ($rootScope, $scope, taskService) {
        $scope.hasmore = false;
        $scope.filter = {
            status: '',
            action: '',
            skip: 0,
            limit: 10
        };

        $rootScope.config = {
            title: {
                title: '待办任务',
                back: '#/home',
                backtext: '首页'
            }
        };

        $scope.load = function (skip) {
            $.showIndicator($scope);
            $scope.filter.skip = skip;
            taskService.pageTask($scope.filter.status, $scope.filter.action, $scope.filter.skip, $scope.filter.limit).then(function (result) {
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
            taskService.pageTask($scope.filter.status, $scope.filter.action, $scope.filter.skip, $scope.filter.limit).then(function (result) {
                $scope.tasks = $scope.tasks.concat(result.data);
                $scope.hasmore = result.data && result.data.length > 0;
            }).finally(function () {
                $.hideIndicator($scope);
            });
        };

        $scope.load(0);

    }]);

/**
 * 待办任务：任务信息
 */
app.controller('TaskInfoCtrl', ['$rootScope', '$scope', 'taskService',
    function ($rootScope, $scope, taskService) {
        $scope.load = function () {
            $.showIndicator($scope);
            taskService.getTask($scope.$state.params.tid).then(function (result) {
                $scope.task = result.data;
                $rootScope.config = {
                    title: {
                        title: '任务信息',
                        back: '#/crm/task/list',
                        menu: function () {
                            var actionButtons = [];

                            actionButtons.push({
                                text: '接收任务',
                                onClick: function () {
                                }
                            });

                            actionButtons.push({
                                text: '同意',
                                onClick: function () {
                                }
                            });

                            actionButtons.push({
                                text: '拒绝',
                                onClick: function () {
                                }
                            });


                            var cancelButton = [
                                {
                                    text: '取消',
                                    color: 'danger'
                                }
                            ];

                            var stakeholderButtons = [];

                            stakeholderButtons.push({
                                text: '查看参与者',
                                onClick: function () {
                                }
                            });

                            var actions = [];
                            if (actionButtons.length > 0) {
                                actions.push(actionButtons);
                            }
                            if (stakeholderButtons.length > 0) {
                                actions.push(stakeholderButtons);
                            }
                            actions.push(cancelButton);

                            $.actions(actions);
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
 * 待办任务：审核通过
 */
app.controller('TaskPermitCtrl', ['$rootScope', '$scope', 'taskService',
    function ($rootScope, $scope, taskService) {
    }]);

/**
 * 待办任务：审核拒绝
 */
app.controller('TaskRejectCtrl', ['$rootScope', '$scope', 'taskService',
    function ($rootScope, $scope, taskService) {
    }]);


/**
 * 待办任务：参与者列表
 */
app.controller('TaskHoldersCtrl', ['$rootScope', '$scope', 'taskService',
    function ($rootScope, $scope, taskService) {
    }]);

