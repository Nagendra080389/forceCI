var app = angular.module('forceCIApp', []);
app.controller('orderFromController', function($scope, $http) {
    $http.get("/listRepository").then(listRepositoryCallback, listRepositoryErrorCallback);

    function listRepositoryCallback(response) {
        var foundRepository = [];
        if (response.data) {
            for (var index = 0; index < response.data.length; ++index) {
                foundRepository.push(response.data[index]);
            }
            $scope.lstRepositoryData = foundRepository;
        }
    }

    function listRepositoryErrorCallback(error) {}
    $scope.change = function(enabled, repositoryName) {

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
            message: 'Enabling this will add a WEBHOOK to this repository. Do you want to continue?',
            position: 'center',
            buttons: [
                ['<button><b>YES</b></button>', function(instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                    $http.post("/modifyRepository", enabled, repositoryName).then(modifyRepositoryCallback, modifyRepositoryErrorCallback);
                },
                true],
                ['<button>NO</button>', function(instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                }], ],
            onClosing: function(instance, toast, closedBy) {
                console.info('Closing | closedBy: ' + closedBy);
            },
            onClosed: function(instance, toast, closedBy) {
                console.info('Closed | closedBy: ' + closedBy);
            }
        });
    }

    function modifyRepositoryCallback(response) {}

    function modifyRepositoryErrorCallback(error) {}
});