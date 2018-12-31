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
    $('#activeRepositorySlider').change(function() {
        console.log($(this).prop('checked'));
    });
    $('#inactiveRepositorySlider').change(function() {
        console.log($(this).prop('checked'));
    });
});