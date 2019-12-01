var app = angular.module('forceCIApp', []);
app.controller('orderFromController', function ($scope, $http, $attrs) {
    $http.get("/listRepository").then(listRepositoryCallback, listRepositoryErrorCallback);

    function listRepositoryCallback(response) {
        var foundRepository = [];
        if (response.data) {
            for (var index = 0; index < response.data.lstRepositories.length; ++index) {
                foundRepository.push(response.data.lstRepositories[index]);
            }
            $scope.lstRepositoryData = foundRepository;
            localStorage.setItem('githubOwner', response.data.ownerId);
        }
    }

    function listRepositoryErrorCallback(error) {
    }

    $scope.change = function (eachData) {
        var popMessage = '';
        if (eachData.active) {
            popMessage = 'Enabling this will add a WEBHOOK to this repository. Do you want to continue?'
        } else {
            popMessage = 'Disabling this will delete the WEBHOOK from this repository. Do you want to continue?'
        }
        iziToast.question({
            timeout: false,
            pauseOnHover: true,
            close: false,
            overlay: true,
            toastOnce: true,
            backgroundColor: '#009edb',
            id: 'question',
            zindex: 999,
            title: 'Hey',
            message: popMessage,
            position: 'center',
            buttons: [
                ['<button><b>YES</b></button>', function (instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                    var data = {
                        active: eachData.active,
                        repositoryName: eachData.repositoryName,
                        owner: localStorage.getItem('githubOwner')
                    };
                    $http.post("/modifyRepository", data).then(function (response) {
                        if (response.status === 200) {
                            $http.post("/createWebHook", response.data).then(function (response) {
                                    eachData.webHookId = response.data.id;
                                    eachData.webHookUrl = response.data.url;
                                }, function (error) {
                                    console.log(error);
                                }
                            );
                        }
                    }, function (error) {
                    });
                },
                    true],
                ['<button>NO</button>', function (instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                    eachData.active = !eachData.active;
                    $scope.$apply();
                }],]
        });
    };
});