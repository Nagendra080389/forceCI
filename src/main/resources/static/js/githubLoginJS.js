var connect2Deploy = angular.module("connect2Deploy", ['ngRoute']);
connect2Deploy.config(function($routeProvider) {
    $routeProvider
        .when('/index', {
            templateUrl: './html/loginGithub.html'
        })
        .when('/apps/dashboard', {
            templateUrl: './html/dashboard.html'
        })
        .otherwise({
            redirectTo: '/index'
        });
});