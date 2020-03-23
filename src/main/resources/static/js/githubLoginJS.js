var connect2Deploy = angular.module("connect2Deploy", ['ngRoute', 'angularjs-dropdown-multiselect', 'ngSanitize', 'angularUtils.directives.dirPagination', 'ngMaterial']);

connect2Deploy.filter('decodeURIComponent', function () {
    return window.decodeURIComponent;
});

let sse;
connect2Deploy.config(function ($routeProvider, $locationProvider) {
    $routeProvider
        .when('/index', {
            templateUrl: './html/loginGithub.html',
            controller: 'indexController',
        })
        .when('/apps/dashboard', {
            templateUrl: './html/dashboard.html',
            controller: 'dashBoardController',
        })
        .when('/apps/forgotPassword', {
            templateUrl: './html/forgotPassword.html',
            controller: 'forgotPasswordController',
        })
        .when('/apps/dashboard/success', {
            templateUrl: './html/verificationEmailSent.html'
        })
        .when('/apps/dashboard/token/:token', {
            templateUrl: './html/dashboard.html',
            controller: 'dashBoardController',
        })
        .when('/apps/register', {
            templateUrl: './html/register.html',
            controller: 'registerController',
        })
        .when('/apps/dashboard/app/:repoName/:repoId', {
            templateUrl: './html/repoDetails.html',
            controller: 'repoController',
        })
        .when('/apps/dashboard/app/:repoName/:repoId/:branchConnectedTo', {
            templateUrl: './html/deploymentDetails.html',
            controller: 'deploymentController',
        })
        .when('/apps/dashboard/createApp', {
            templateUrl: './html/addApp.html',
            controller: 'appPageRepoController',
        })
        .when('/apps/dashboard/scheduledDeployments', {
            templateUrl: './html/scheduledDeployments.html',
            controller: 'scheduledDeploymentController',
        })
        .when('/apps/error', {
            templateUrl: './html/error.html'
        })
        .otherwise({
            redirectTo: '/index'
        });

});

function validateConnect2DeployToken(strToken, $http) {
    return new Promise(function (resolve, reject) {
        $http.get("/validateConnect2DeployToken?strToken=" + strToken).then(function (response) {
            resolve(response.data);
        }, function (error) {
            reject(false);
        })
    });

}

function checkToken($location) {
    let accessToken = $.cookie("CONNECT2DEPLOY_TOKEN");
    if (accessToken !== undefined && accessToken !== null && accessToken !== '') {
        $location.path("/apps/dashboard");
    } else {
        $location.path("/index");
    }
}

function logoutFunctionCaller($location) {
    $.removeCookie("CONNECT2DEPLOY_TOKEN", {path: '/'});
    localStorage.removeItem('userEmail');
    $location.path("/index");
    setTimeout(function () {
        $('.modal-backdrop').removeClass('show');
        $('.modal-backdrop').remove();
    }, 500);
}

connect2Deploy.controller('indexController', function ($scope, $http, $location, $mdDialog) {
    let cookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    validateConnect2DeployToken(cookie, $http).then(function (objResult) {
        if(objResult) {
            $location.path("/apps/dashboard");
        } else {
            $location.path("/index");
        }
    }).catch(function (objError) {

    });
    $scope.login = function (userEntity) {
        $http.post("/loginConnect", userEntity).then(function (response) {
            if(response.data !== undefined && response.data !== null && response.data === 'No User Found'){
                iziToast.error({
                    title: 'Error',
                    message: response.data,
                    position: 'topRight'
                });
                $scope.user.emailId = '';
                $scope.user.password = '';
            } else if(response.data !== undefined && response.data !== null && response.data === 'Email Not Verified'){
                iziToast.error({
                    title: 'Error',
                    message: response.data + '! Please verify your email and login try again.',
                    position: 'topRight'
                });
            } else {
                localStorage.setItem('userEmail', response.data);
                $location.path("/apps/dashboard");
            }
        }, function (error) {
            console.log(error);
            iziToast.error({
                title: 'Error',
                message: 'SFDC connection failed, Please retry. ' + error.data.message,
                position: 'topRight'
            });
        })
    };
    $scope.redirectJS = function () {
        window.open('https://github.com/login/oauth/authorize?client_id=0b5a2cb25fa55a0d2b76&redirect_uri=https://forceci.herokuapp.com/gitAuth&scope=repo,user:email&state=Mv4nodgDGEKInu6j2vYBTLoaIVNSXhb4NWuUE8V2', '_self');
    };

    function DialogController($scope, $mdDialog) {
        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.answer = function (answer) {
            $mdDialog.hide(answer);
        };
    }

    $scope.redirectEnterpriseJS = function ($event) {
        const parentEl = angular.element(document.body);
        $mdDialog.show({
            controller: DialogController,
            template: '<md-dialog aria-label="Enterprise Github Configuration">\n' +
                '        <form>\n' +
                '            <md-toolbar>\n' +
                '                <div class="md-toolbar-tools">\n' +
                '                    <h2>Enterprise Github Configuration</h2>\n' +
                '                    <span flex></span>\n' +
                '                    <md-button class="md-icon-button" ng-click="cancel()">\n' +
                '                        <md-icon md-svg-src="img/icons/ic_close_24px.svg" aria-label="Close dialog"></md-icon>\n' +
                '                    </md-button>\n' +
                '                </div>\n' +
                '            </md-toolbar>\n' +
                '            <md-content layout-padding="">\n' +
                '                <div>\n' +
                '                    <form name="githubForm">\n' +
                '                        <md-input-container class="md-block" flex-gt-sm="">\n' +
                '                            <label>Server Name</label>\n' +
                '                            <input ng-model="githubEnterprise.ServerName">\n' +
                '                        </md-input-container>\n' +
                '                        <md-input-container class="md-block">\n' +
                '                            <label>Client Id</label>\n' +
                '                            <input ng-model="githubEnterprise.ClientId">\n' +
                '                        </md-input-container>\n' +
                '                        <md-input-container class="md-block">\n' +
                '                            <label>Client Secret</label>\n' +
                '                            <input ng-model="githubEnterprise.ClientSecret">\n' +
                '                        </md-input-container>\n' +
                '                    </form>\n' +
                '                </div>\n' +
                '            </md-content>\n' +
                '\n' +
                '            <md-dialog-actions layout="row">\n' +
                '                <span flex></span>\n' +
                '                <md-button ng-click="answer(githubEnterprise)">\n' +
                '                    Connect\n' +
                '                </md-button>\n' +
                '                <md-button ng-click="answer(\'Cancelled\')" style="margin-right:20px;">\n' +
                '                    Cancel\n' +
                '                </md-button>\n' +
                '            </md-dialog-actions>\n' +
                '        </form>\n' +
                '    </md-dialog>',
            parent: parentEl,
            targetEvent: $event,
            clickOutsideToClose: true
        });

        //window.open('https://github.com/login/oauth/authorize?client_id=a1568a2b609baf9a3634&redirect_uri=https://forceci.herokuapp.com/gitAuth&scope=repo,user:email&state=Mv4nodgDGEKInu6j2vYBTLoaIVNSXhb4NWuUE8V2', '_self');
    };

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
});

connect2Deploy.controller('dashBoardController', function ($scope, $http, $location, $route, $routeParams) {
    $scope.connect2DeployToken = $routeParams.token;
    let cookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;

    validateConnect2DeployToken(cookie, $http).then(function (objResult) {
        if(!objResult) {
            $location.path("/index");
        }
        if($scope.connect2DeployToken !== undefined && $scope.connect2DeployToken !== null && $scope.connect2DeployToken !== '') {
            $http.get("/validateToken?token=" + $scope.connect2DeployToken).then(function (response) {
                if (response !== undefined && response !== null && response.data !== undefined && response.data !== null) {
                    if (response.data === 'Email Verified') {
                        iziToast.success({
                            timeout: 5000,
                            icon: 'fa fa-chrome',
                            title: 'OK',
                            message: response.data
                        });
                    } else if (response.data === 'Email Already Verified') {
                        iziToast.success({
                            timeout: 5000,
                            icon: 'fa fa-chrome',
                            title: 'OK',
                            message: response.data
                        });
                    } else {
                        $location.path("/index");
                    }
                }
            }, function (error) {
                iziToast.error({
                    title: 'Error',
                    message: error.data.message,
                    position: 'topRight'
                });
            });
        }

        if(cookie !== undefined && cookie !== null){

        }

    }).catch(function (objError) {

    });
    $scope.lstRepositoryData = [];

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };
    /*$http.get("/api/fetchUserName").then(function (response) {
        if (response.data !== undefined && response.data !== null) {
            $scope.userName = response.data.login;
            $scope.avatar_url = response.data.avatar_url;
            localStorage.setItem('githubOwner', response.data.login);
            localStorage.setItem('avatar_url', response.data.avatar_url);
            $http.get("/api/fetchRepositoryInDB?gitHubUser=" + response.data.login).then(function (response) {
                $scope.lstRepositoryData = [];
                if (response.data.length > 0) {
                    for (let i = 0; i < response.data.length; i++) {
                        $scope.lstRepositoryData.push(response.data[i].repository);
                    }
                }
            }, function (error) {
                $location.path("/index");
            });

        }
    }, function (error) {
        $.removeCookie('ACCESS_TOKEN', {path: '/'});
        $.removeCookie('TOKEN_TYPE', {path: '/'});
        iziToast.error({
            title: 'Error',
            message: error.data.message,
            position: 'topRight'
        });
        $location.path("/index");
    });*/


    $scope.disconnectAndDelete = function (eachData) {

        iziToast.question({
            timeout: false,
            pauseOnHover: true,
            close: false,
            overlay: true,
            toastOnce: true,
            backgroundColor: 'fff',
            id: 'question',
            zindex: 999,
            title: 'Confirm',
            message: 'Are you sure you want to delete?',
            position: 'center',
            buttons: [
                ['<button><b>YES</b></button>', function (instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut',
                        onClosing: function (instance, toast, closedBy) {
                            iziToast.destroy();
                        }
                    }, toast, 'button');
                    if (eachData.repositoryId) {
                        $http.delete("/api/deleteWebHook?repositoryName=" + eachData.repositoryName + "&repositoryId=" + eachData.repositoryId + "&repositoryOwner=" +
                            eachData.owner + "&webHookId=" + eachData.webHook.id).then(function (response) {
                            console.log(response);
                            if (response.status === 200 && response.data === 204) {
                                iziToast.success({
                                    timeout: 5000,
                                    icon: 'fa fa-chrome',
                                    title: 'OK',
                                    message: 'App deleted successfully'
                                });
                                let gitHubOwner = localStorage.getItem('githubOwner');
                                $http.get("/api/fetchRepositoryInDB?gitHubUser=" + gitHubOwner).then(function (response) {
                                    $scope.lstRepositoryData = [];
                                    if (response.data.length > 0) {
                                        for (let i = 0; i < response.data.length; i++) {
                                            $scope.lstRepositoryData.push(response.data[i].repository);
                                        }
                                    }
                                }, function (error) {

                                });
                            } else {
                                iziToast.error({
                                    title: 'Error',
                                    message: 'Not able to delete WebHook, Please retry.',
                                    position: 'topRight'
                                });
                            }
                        }, function (error) {
                            console.log(error);
                            iziToast.error({
                                title: 'Error',
                                message: 'Not able to delete WebHook, Please retry.',
                                position: 'topRight'
                            });
                        })
                    }
                }, true],
                ['<button>NO</button>', function (instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut',
                        onClosing: function (instance, toast, closedBy) {
                            iziToast.destroy();
                        }
                    }, toast, 'button');
                }],]
        });


    };

    function checkIfInValid(objData) {
        return objData === undefined || objData === null || objData === '';
    }

});

connect2Deploy.controller('repoController', function ($scope, $http, $location, $routeParams, $mdDialog) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.lstSFDCConnectionDetails = [];
    let objWindow;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $scope.sfdcOrg = {
        orgName: '',
        testLevel: 'NoTestRun',
        environment: '0',
        userName: '',
        instanceURL: '',
        authorize: 'Authorize',
        save: 'Save',
        testConnection: 'Test Connection',
        delete: 'Delete',
        oauthSuccess: 'false',
        oauthFailed: 'false',
        oauthSaved: 'false',
        disabledForm: 'false',
    };

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

    $scope.availableTags = [];
    let sfdcAccessTokenFromExternalPage;
    let sfdcUserNameFromExternalPage;
    let sfdcInstanceFromExternalPage;
    let sfdcRefreshTokenFromExternalPage;


    window.addEventListener('message', function (objEvent) {
        if (objEvent !== undefined && objEvent !== null &&
            objEvent.data !== undefined && objEvent.data !== null &&
            objEvent.data.strDestinationId !== undefined && objEvent.data.strDestinationId !== null) {
            if (objEvent.data.strDestinationId === 'OauthPayload') {
                sfdcAccessTokenFromExternalPage = objEvent.data.sfdcAccessToken;
                sfdcUserNameFromExternalPage = objEvent.data.sfdcUserName;
                sfdcInstanceFromExternalPage = objEvent.data.sfdcInstanceURL;
                sfdcRefreshTokenFromExternalPage = objEvent.data.sfdcRefreshToken;
                if (objWindow !== undefined && objWindow !== null) {
                    objWindow.close();
                }
                $scope.sfdcOrg.oauthSuccess = 'true';
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: 'SFDC connection successful.'
                });
            }

            if (objEvent.data.strDestinationId === 'OauthPayloadFailed') {
                sfdcAccessTokenFromExternalPage = '';
                sfdcUserNameFromExternalPage = '';
                sfdcInstanceFromExternalPage = '';
                sfdcRefreshTokenFromExternalPage = '';
                if (objWindow !== undefined && objWindow !== null) {
                    objWindow.close();
                }
                $scope.sfdcOrg.oauthSuccess = 'false';
                iziToast.error({title: 'Error', message: 'Not able to create SFDC connection.', position: 'topRight'});
            }
        }
        $scope.$apply();

    });


    $http.get("/api/showSfdcConnectionDetails?gitRepoId=" + $scope.repoId).then(function (response) {
        $scope.lstSFDCConnectionDetails = response.data;
    }, function (error) {
        console.log(error);
    });

    $http.get("/api/getAllBranches?strRepoId=" + $scope.repoId).then(function (response) {
        $scope.availableTags = response.data;
    }, function (error) {
        console.log(error);
    });
    $scope.complete = function () {
        $("#branchNamel3").autocomplete({
            source: $scope.availableTags
        });
    };

    $scope.createNewConnection = function () {
        $.removeCookie('SFDC_ACCESS_TOKEN', {path: '/'});
        $.removeCookie('SFDC_USER_NAME', {path: '/'});
        $.removeCookie('SFDC_INSTANCE_URL', {path: '/'});
        $.removeCookie('SFDC_REFRESH_TOKEN', {path: '/'});
        $scope.sfdcOrg = {
            id: '',
            orgName: '',
            testLevel: 'NoTestRun',
            environment: '0',
            userName: '',
            instanceURL: '',
            authorize: 'Authorize',
            save: 'Save',
            testConnection: 'Test Connection',
            delete: 'Delete',
            oauthSuccess: 'false',
            oauthFailed: 'false',
            oauthSaved: 'false',
            disabledForm: 'false',
            branchConnectedTo: '',
            boolActive: false,
        };
        sfdcAccessTokenFromExternalPage = '';
        sfdcUserNameFromExternalPage = '';
        sfdcInstanceFromExternalPage = '';
        sfdcRefreshTokenFromExternalPage = '';
    };

    $scope.createSnapshot = function (targetBranch, repoId, event) {
        // Appending dialog to document.body to cover sidenav in docs app
        const confirm = $mdDialog.prompt()
            .title('Create Snapshot')
            .textContent('Take a snapshot of current branch.')
            .placeholder('Branch Name')
            .targetEvent(event)
            .required(true)
            .ok('Create')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function (result) {
            // If confirmed run this code
            $http.get("/api/createBranch?repoId=" + repoId +
                "&targetBranch=" + targetBranch + "&userName=" + $scope.userName + "&newBranchName=" + result).then(function (response) {
                const responseResult = response.data;
                if (responseResult === 'Success') {
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: result + ' created successfully'
                    });
                } else {
                    iziToast.error({title: 'Error', message: 'Not able to create branch.', position: 'topRight'});
                }
            }, function (error) {
                console.log(error);
            });
        }, function () {
            // If cancelled Do nothing
        });
    };

    $scope.authorize = function (sfdcOrg) {
        let url = '';
        if (sfdcOrg.orgName === undefined || sfdcOrg.orgName === null || sfdcOrg.orgName === ''
            || sfdcOrg.userName === undefined || sfdcOrg.userName === null || sfdcOrg.userName === '' || (sfdcOrg.environment === '2'
                && (sfdcOrg.instanceURL === undefined || sfdcOrg.instanceURL === null || sfdcOrg.instanceURL === ''))) {
            iziToast.warning({title: 'Caution', message: 'Please fill in required fields.', position: 'center'});
            return;
        }
        if (sfdcOrg) {
            if (sfdcOrg.environment === '0') {
                url = 'https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + sfdcOrg.environment;
            } else if (sfdcOrg.environment === '1') {
                url = 'https://test.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + sfdcOrg.environment;
            } else {
                url = sfdcOrg.instanceURL + '/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + sfdcOrg.instanceURL;
            }
            const newWindow = objWindow = window.open(url, 'ConnectWithOAuth', 'height=600,width=450,left=100,top=100');
            if (window.focus) {
                newWindow.focus();
            }
        }
    };

    $scope.showDataOnForm = function (sfdcOrg) {
        $scope.sfdcOrg = {
            id: sfdcOrg.id,
            orgName: sfdcOrg.orgName,
            environment: sfdcOrg.environment,
            testLevel: sfdcOrg.testLevel,
            userName: sfdcOrg.userName,
            instanceURL: sfdcOrg.instanceURL,
            authorize: sfdcOrg.authorize,
            save: sfdcOrg.save,
            testConnection: sfdcOrg.testConnection,
            delete: sfdcOrg.delete,
            oauthSuccess: sfdcOrg.oauthSuccess,
            oauthFailed: sfdcOrg.oauthFailed,
            oauthSaved: sfdcOrg.oauthSaved,
            oauthToken: sfdcOrg.oauthToken,
            refreshToken: sfdcOrg.refreshToken,
            disabledForm: 'true',
            gitRepoId: sfdcOrg.gitRepoId,
            branchConnectedTo: sfdcOrg.branchConnectedTo,
            boolActive: sfdcOrg.boolActive,
        };
    };


    $scope.saveConnection = function (sfdcOrg) {
        const sfdcDetails = {
            id: sfdcOrg.id,
            orgName: sfdcOrg.orgName,
            environment: sfdcOrg.environment,
            testLevel: sfdcOrg.testLevel,
            userName: sfdcOrg.userName,
            instanceURL: checkIfInValid(sfdcOrg.instanceURL) ? sfdcInstanceFromExternalPage : sfdcOrg.instanceURL,
            refreshToken: checkIfInValid(sfdcOrg.refreshToken) ? sfdcRefreshTokenFromExternalPage : sfdcOrg.refreshToken,
            authorize: sfdcOrg.authorize,
            save: sfdcOrg.save,
            testConnection: sfdcOrg.testConnection,
            delete: sfdcOrg.delete,
            oauthSuccess: 'true',
            oauthFailed: sfdcOrg.oauthFailed,
            oauthSaved: sfdcOrg.oauthSaved,
            oauthToken: checkIfInValid(sfdcOrg.oauthToken) ? sfdcAccessTokenFromExternalPage : sfdcOrg.oauthToken,
            gitRepoId: $scope.repoId,
            branchConnectedTo: sfdcOrg.branchConnectedTo,
            boolActive: sfdcOrg.boolActive,
            repoName: $scope.repoName,
        };
        if (sfdcOrg.orgName === undefined || sfdcOrg.orgName === null || sfdcOrg.orgName === '' ||
            sfdcOrg.userName === undefined || sfdcOrg.userName === null || sfdcOrg.userName === '') {
            iziToast.error({
                title: 'Error',
                message: 'Please fill in all fields.',
                position: 'topRight'
            });
            return;
        }

        $http.post("/api/saveSfdcConnectionDetails", sfdcDetails).then(function (response) {
                $.removeCookie('SFDC_ACCESS_TOKEN', {path: '/'});
                $.removeCookie('SFDC_USER_NAME', {path: '/'});
                $.removeCookie('SFDC_INSTANCE_URL', {path: '/'});
                $.removeCookie('SFDC_REFRESH_TOKEN', {path: '/'});
                $scope.lstSFDCConnectionDetails = [];
                const gitRepoId = response.data.gitRepoId;
                fetchDetailsFromDB(gitRepoId);
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: 'SFDC connection created successfully'
                });

            }, function (error) {
                console.log(error);
                iziToast.error({
                    title: 'Error',
                    message: 'SFDC connection failed, Please retry. ' + error.data.message,
                    position: 'topRight'
                });
            }
        );
    };

    function fetchDetailsFromDB(gitRepoId) {
        $http.get("/api/showSfdcConnectionDetails?gitRepoId=" + gitRepoId).then(function (response) {
            $scope.lstSFDCConnectionDetails = response.data;
        }, function (error) {
            console.log(error);
        });
        $scope.sfdcOrg = {
            id: '',
            orgName: '',
            testLevel: 'NoTestRun',
            environment: '0',
            userName: '',
            instanceURL: '',
            authorize: 'Authorize',
            save: 'Save',
            testConnection: 'Test Connection',
            delete: 'Delete',
            oauthSuccess: 'false',
            oauthFailed: 'false',
            oauthSaved: 'false',
            disabledForm: 'false',
            branchConnectedTo: '',
            boolActive: false,
        };
    }

    $scope.deleteConnection = function (sfdcOrg) {
        $http.delete("/api/deleteSfdcConnectionDetails?sfdcDetailsId=" + sfdcOrg.id).then(function (response) {
            fetchDetailsFromDB($scope.repoId);
            iziToast.success({
                timeout: 5000,
                icon: 'fa fa-chrome',
                title: 'OK',
                message: 'SFDC connection deleted successfully'
            });
        }, function (error) {

        })
    };

    function checkIfInValid(objData) {
        return objData === undefined || objData === null || objData === '';
    }

});

connect2Deploy.controller('appPageRepoController', function ($scope, $http, $location) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $scope.lstRepositoryFromApi = [];

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }

    $scope.tableHeaders = ['Linked Services', 'Username', 'Actions'];
    $scope.services = [];
    $http.get("/api/fetchAllLinkedServices?userEmail=" + $scope.userName).then(function (response) {
        let data = response.data;
        debugger;
        try {
            const lstData = JSON.parse(data);
            for (let i = 0; i < lstData.length; i++) {
                $scope.services.push(lstData[i]);
            }
        }catch (e) {

        }
    }, function (error) {
        debugger;
    });


    $scope.connectGit = function(gitName){
        if(gitName === 'GitHub'){

        }
        if(gitName === 'GitHub Enterprise'){

        }
    };

    $scope.disconnectGit = function(gitName){

    };

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };
    $scope.fetchRepo = function () {
        if ($scope.repoName) {
            fetchRepoFromApi();
        }
    };

    function fetchRepoFromApi() {
        $scope.lstRepositoryFromApi = [];
        $http.get("/api/fetchRepository" + "?repoName=" + $scope.repoName + "&" + "repoUser=" + $scope.userName).then(function (response) {
            let gitRepositoryFromQuery = JSON.parse(response.data.gitRepositoryFromQuery);
            let repositoryWrappers = response.data.repositoryWrappers;
            for (let i = 0; i < gitRepositoryFromQuery.items.length; i++) {
                const data = {
                    active: true,
                    repositoryName: gitRepositoryFromQuery.items[i].name,
                    repositoryId: gitRepositoryFromQuery.items[i].id,
                    repositoryURL: gitRepositoryFromQuery.items[i].html_url,
                    repositoryOwnerAvatarUrl: $scope.avatar_url,
                    repositoryOwnerLogin: gitRepositoryFromQuery.items[i].owner.login,
                    repositoryFullName: gitRepositoryFromQuery.items[i].full_name,
                    ownerHtmlUrl: gitRepositoryFromQuery.items[i].owner.html_url,
                    owner: $scope.userName,
                    full_name: gitRepositoryFromQuery.items[i].full_name
                };
                if (repositoryWrappers !== undefined && repositoryWrappers !== null && repositoryWrappers !== '' && repositoryWrappers.length > 0) {
                    for (let j = 0; j < repositoryWrappers.length; j++) {
                        if (repositoryWrappers[j].repository.repositoryId !== gitRepositoryFromQuery.items[i].id) {
                            $scope.lstRepositoryFromApi.push(data);
                        }
                    }
                } else {
                    $scope.lstRepositoryFromApi.push(data);
                }
            }
        }, function (error) {

        });
    }

    $scope.connectAndCreateApp = function (data) {
        $http.post("/api/createWebHook", data).then(function (response) {
                fetchRepoFromApi();
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: 'WebHook created successfully'
                });
            }, function (error) {
                console.log(error);
                iziToast.error({
                    title: 'Error',
                    message: 'Not able to create WebHook, Please retry.',
                    position: 'topRight'
                });
            }
        );
    }

});

connect2Deploy.controller('deploymentController', function ($scope, $http, $location, $routeParams, $mdDialog) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.githubOwner;
    $scope.avatar_url = localStorage.avatar_url;
    const repoId = $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.branchConnectedTo = $routeParams.branchConnectedTo;
    $scope.branchName = $routeParams.branchConnectedTo;
    $scope.lstDeployments = [];

    $scope.cancelDeployment = function (deploymentJobId) {
        $http.get("/api/cancelDeployment?deploymentJobId=" + deploymentJobId,).then(function (response) {
            console.log(response);
        }, function (error) {
            console.log(error);
        })
    };

    $scope.logoutFunction = function (sfdcDetails) {
        logoutFunctionCaller($location);
    };

    // table headers that we need to show
    $scope.tableHeaders = ['Job No.', 'PR No.', 'Source Branch', 'Validation Status', 'Generated Package', 'Deployment Status'];

    sse = new EventSource('/asyncDeployments?userName=' + $scope.userName + '&repoId=' + $scope.repoId + '&branchName=' + $scope.branchName);
    sse.addEventListener("message", function (objMessageEvent) {
        if (objMessageEvent !== undefined && objMessageEvent !== null &&
            objMessageEvent.data !== undefined && objMessageEvent.data !== null) {
            $scope.lstDeployments = JSON.parse(objMessageEvent.data);
            $scope.$apply();
        }
    });

    $scope.showAlert = function (ev, deploymentId) {
        const deploymentList = $scope.lstDeployments;
        let packageString = '';
        for (let i = 0; i < deploymentList.length; i++) {
            if (deploymentList[i].id === deploymentId) {
                packageString = deploymentList[i].packageXML;
                break;
            }
        }

        $mdDialog.show({
            controller: function ($scope) {
                $scope.msg = packageString;
                $scope.closeDialog = function () {
                    $mdDialog.hide();
                }
            },
            template: '<div class="md-padding" style="border: 1px solid black;margin: 10px;">' +
                '<button type="button" class="close" ng-click="closeDialog()" aria-label="Close">\n' +
                '  <span aria-hidden="true">×</span>\n' +
                '</button>' +
                '<pre>{{msg | decodeURIComponent}}</pre></div>',
            parent: angular.element(document.body),
            clickOutsideToClose: true,
            fullscreen: true
        });
    };

    $scope.downloadValidation = function (jobNo, type) {
        $http.get("/api/fetchLogs" + "?jobNo=" + jobNo + "&" + "type=" + type + "&" + "repoId=" + repoId).then(function (response) {
            if (response.data !== undefined && response.data !== null && response.data !== '') {
                // any kind of extension (.txt,.cpp,.cs,.bat)
                var filename = type + "_" + jobNo + ".txt";
                var blob = new Blob([response.data.join('\n')], {
                    type: "text/plain;charset=utf-8"
                });

                saveAs(blob, filename);
            }
        })
    }

});

connect2Deploy.controller('scheduledDeploymentController', function ($scope, $http, $location, $routeParams, $mdDialog) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.githubOwner;
    $scope.avatar_url = localStorage.avatar_url;
    const repoId = $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.branchConnectedTo = $routeParams.branchConnectedTo;
    $scope.branchName = $routeParams.branchConnectedTo;


});

connect2Deploy.controller('registerController', function ($scope, $http, $location, $routeParams) {
    $scope.register = function (userEntity) {
        if (userEntity !== undefined && userEntity !== null && userEntity.password === userEntity.RepeatPassword) {
            $http.post("/register", userEntity).then(function (response) {
                    $location.path("/apps/dashboard/success");
                }, function (error) {
                    iziToast.error({
                        title: 'Error',
                        message: 'User Creation Failed. ' + error.data.message,
                        position: 'topRight'
                    });
                }
            );
        } else {

        }
    }
});

$(window).on("beforeunload", function () {
    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
});