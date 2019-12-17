var connect2Deploy = angular.module("connect2Deploy", ['ngRoute']);
connect2Deploy.config(function($routeProvider) {
    $routeProvider
        .when('/index', {
            templateUrl: './html/loginGithub.html'
        })
        .when('/apps/dashboard', {
            templateUrl: './html/dashboard.html'
        })
        .when('/apps/error', {
            templateUrl: './html/error.html'
        })
        .otherwise({
            redirectTo: '/index'
        });
});

connect2Deploy.controller('indexController', function ($scope, $http, $attrs, $location) {

    if($location) {
        let code = new URL($location.$$absUrl).searchParams.get('code');
        let state = new URL($location.$$absUrl).searchParams.get('state');

        if(code !== undefined && code !== null && code !== '' && state !== undefined && state !== null && state !== '') {
            $http.get("/gitAuth?code=" + code+"&state="+state).then(function (response) {
                let objResponse = JSON.parse(response.data);
                if(objResponse.access_token !== undefined && objResponse.access_token !== null && objResponse.access_token !== '') {
                    $location.path("/apps/dashboard");
                } else {
                    $location.path("/index");
                }
            }, function (error) {

            });
        }
    }
})