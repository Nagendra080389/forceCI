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

connect2Deploy.controller('indexController', function ($scope, $http, $attrs, $location) {

    if($location){
        debugger;
    }
    $http.get("/gitAuth").then(function (response) {

    })
})