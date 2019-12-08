var app = angular.module('forceCIApp', []);
app.controller('orderFromController', function ($scope, $http, $attrs) {
    //$http.get("/listRepository").then(listRepositoryCallback, listRepositoryErrorCallback);

    $http.get("/fetchUserName").then(function (response) {
        if (response.data !== undefined && response.data !== null) {
            $scope.userName = response.data.login;
            localStorage.setItem('githubOwner', response.data.login);
            const avatarSpanTag = '<span class="absolute flex items-center justify-center w2 h2 z-2 ' +
                'nudge-right--4 pe-none" style="top: -15px">\n' +
            '          <img src='+response.data.avatar_url+'>\n' +
            '        </span>';
            $(avatarSpanTag).insertAfter('#idSelectTab');
        }
    }, function (error) {

    });

    function listRepositoryCallback(response) {
        var foundRepository = [];
        if (response.data) {
            for (var index = 0; index < response.data.lstRepositories.length; ++index) {
                foundRepository.push(response.data.lstRepositories[index]);
            }
            $scope.lstRepositoryData = foundRepository;

        }
    }

    $scope.fetchRepo = function () {
        if ($scope.repoName) {
            $http.get("/fetchRepository"+"?repoName="+$scope.repoName+"&"+"repoUser="+localStorage.getItem('githubOwner')).then(function (response) {
                console.log(response);
                for (let i = 0; i < response.data.items.length; i++) {
                    const eachNewDiv = '<div class="bb b--light-silver pv2 flex-auto flex items-center">\n' +
                        '\n' +
                        '                <svg style="width: 16px; height: 16px;" data-test-target="malibu-icon" data-test-icon-name="repo-16" class="icon malibu-icon fill-near-black nudge-down--1 mr1">\n' +
                        '                    <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="#repo-16">\n' +
                        '                        <svg id="repo-16"\n' +
                        '                             viewBox="0 0 16 16">\n' +
                        '                            <path fill-rule="evenodd" d="M6 4v1h1V4H6zm0-2v1h1V2H6zM3 0c-.5 0-1 .5-1 1v12c0 .5.5 1 1 1h2v2l1.5-1.5L8 16v-2h5c.5 0 1-.5 1-1V1c0-.5-.5-1-1-1H3zm9.5 13H8v-1H5v1H3.5c-.266 0-.5-.266-.5-.5V11h10v1.5c0 .25-.234.5-.5.5zM5 10V1h8.016L13 10H5zm1-2v1h1V8H6zm0-2v1h1V6H6z">\n' +
                        '\n' +
                        '                            </path></svg>\n' +
                        '                        <title></title>\n' +
                        '                    </use>\n' +
                        '                </svg>\n' +
                        '\n' +
                        '                <span ng-model="repoNameToConnect">'+response.data.items[i].full_name+'</span>\n' +
                        '                <div class="flex-auto"></div>\n' +
                        '                <button id="ember88" class="async-button default hk-button-sm--secondary ember-view" type="button" onclick="callWebHook(event)">    Connect\n' +
                        '                </button>\n' +
                        '            </div>';
                    $('#repoDialog').append(eachNewDiv);
                }
                $('#repoDialog').removeClass('hidden');

            }, function (error) {

            });
        }
    };

    $scope.callWebHook = function () {
        console.log($scope.repoNameToConnect);
    };
    
    function callWebHook(event) {
        $(event.target).closest('span');
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
                }, true],
                ['<button>NO</button>', function (instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut',
                        onClosing: function (instance, toast, closedBy) {
                            iziToast.destroy();
                        }
                    }, toast, 'button');
                    eachData.active = !eachData.active;
                    $scope.$apply();
                }],]
        });
    };
});