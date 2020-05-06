var connect2Deploy = angular.module("connect2Deploy", ['ngRoute', 'angularjs-dropdown-multiselect', 'ngSanitize', 'angularUtils.directives.dirPagination', 'ngMaterial', 'ngMessages', 'ui.bootstrap.datetimepicker']);

connect2Deploy.filter('decodeURIComponent', function () {
    return window.decodeURIComponent;
});


let listenerAdded = false;

localStorage.setItem('avatar_url', "../images/connectdevelop-brands.svg");

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
            templateUrl: './html/verificationEmailSuccess.html',
            controller: 'verifyEmailController',
        })
        .when('/apps/register', {
            templateUrl: './html/register.html',
            controller: 'registerController',
        })
        .when('/apps/dashboard/app/:appName', {
            templateUrl: './html/appDashboard.html',
            controller: 'dashBoardAppController',
        })
        .when('/apps/dashboard/app/linkedService/:linkedService/:repoName/:repoId', {
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
        .when('/apps/dashboard/scheduledDeploymentsDetails/:scheduledJobId', {
            templateUrl: './html/scheduledDeploymentsDetails.html',
            controller: 'scheduledDeploymentDetailsController',
        })
        .when('/apps/error', {
            templateUrl: './html/error.html'
        })
        .otherwise({
            redirectTo: '/index'
        });

});

function checkToken($location) {
    let accessToken = $.cookie("ACCESS_TOKEN");
    return accessToken !== undefined && accessToken !== null && accessToken !== '';
}

function logoutFunctionCaller($location) {
    $('#sessionExpiredModal').modal('hide');
    $('#logoutModal').modal('hide');
    $.removeCookie("CONNECT2DEPLOY_TOKEN", {path: '/'});
    localStorage.removeItem('userEmail');
    $location.path("/index");
}

connect2Deploy.controller('indexController', function ($scope, $http, $location, $mdDialog, $routeParams) {
    let cookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    let redirect_git = new URL($location.absUrl()).searchParams.get('redirect_git');
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + cookie;
    if (redirect_git !== undefined && redirect_git !== null && redirect_git !== '' && redirect_git) {
        $http.get("/api/fetchAccessTokens?userEmail=" + localStorage.getItem('userEmail')).then(function (response) {
            if (response.data !== undefined && response.data !== null) {
                Object.keys(response.data).forEach(key => {
                    let replacedKey = key.replace(/\s+/g, '_');
                    $.cookie(replacedKey + '_token', response.data[key]);
                });
                $location.path("/apps/dashboard/createApp");
            }
        }, function (error) {
            console.error(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });
    }

    $scope.login = function (userEntity) {
        if (typeof grecaptcha !== 'undefined') {
            if (userEntity !== undefined && userEntity !== null) {
                const siteKey = '6Lcr3uUUAAAAAPnCZdcC9qTt-GKFVl9U1fmpHHRt';
                grecaptcha.execute(siteKey, {action: 'register'}).then(function (response) {
                    userEntity.googleReCaptchaV3 = response;
                    $http.post("/loginConnect", userEntity).then(function (response) {
                        localStorage.setItem('userEmail', response.data);
                        $location.path("/apps/dashboard");
                    }, function (error) {
                        $scope.user.emailId = '';
                        $scope.user.password = '';
                        iziToast.error({
                            title: 'Error',
                            message: 'Login Failed! ' + error.data.message,
                            position: 'topRight'
                        });
                    })
                })
            }
        }
    };
    $scope.redirectJS = function () {
        window.open('https://github.com/login/oauth/authorize?client_id=0b5a2cb25fa55a0d2b76&redirect_uri=https://forceci.herokuapp.com/gitAuth&scope=repo,user:email&state=Mv4nodgDGEKInu6j2vYBTLoaIVNSXhb4NWuUE8V2', '_self');
    };
    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
});

connect2Deploy.controller('dashBoardAppController', function ($scope, $http, $location, $route, $routeParams) {

    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $scope.appName = $routeParams.appName;
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;

    $scope.fetchRepo = function () {
        if ($scope.repoName && $scope.appName) {
            fetchRepoFromApi();
        }
    };

    function fetchRepoFromApi() {
        $scope.lstRepositoryFromApi = [];
        $http.get("/api/fetchRepository" + "?repoName=" + $scope.repoName + "&" + "appName=" + $scope.appName).then(function (response) {
            let gitRepositoryFromQuery = JSON.parse(response.data.gitRepositoryFromQuery);
            let repositoryWrappers = response.data.repositoryWrappers;
            let repositoryIdsFromDB = new Set();
            if (repositoryWrappers !== undefined && repositoryWrappers !== null && repositoryWrappers !== '' && repositoryWrappers.length > 0) {
                for (let j = 0; j < repositoryWrappers.length; j++) {
                    repositoryIdsFromDB.add(repositoryWrappers[j].repository.repositoryId);
                }
            }
            for (let i = 0; i < gitRepositoryFromQuery.items.length; i++) {
                const data = {
                    active: true,
                    repositoryName: gitRepositoryFromQuery.items[i].name,
                    repositoryId: gitRepositoryFromQuery.items[i].id,
                    repositoryURL: gitRepositoryFromQuery.items[i].html_url,
                    repositoryOwnerAvatarUrl: gitRepositoryFromQuery.items[i].owner.avatar_url,
                    repositoryOwnerLogin: gitRepositoryFromQuery.items[i].owner.login,
                    repositoryFullName: gitRepositoryFromQuery.items[i].full_name,
                    ownerHtmlUrl: gitRepositoryFromQuery.items[i].owner.html_url,
                    owner: gitRepositoryFromQuery.items[i].owner.login,
                    full_name: gitRepositoryFromQuery.items[i].full_name
                };
                if (!repositoryIdsFromDB.has(gitRepositoryFromQuery.items[i].id)) {
                    $scope.lstRepositoryFromApi.push(data);
                }
            }
        }, function (error) {
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });
    }

    $scope.connectAndCreateApp = function (data) {
        data.linkedService = $scope.appName;
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
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
            }
        );
    }

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };
});

connect2Deploy.controller('verifyEmailController', function ($scope, $http, $location, $route, $routeParams) {
    $scope.connect2DeployToken = $routeParams.token;
    $scope.strMessage = '';
    if ($scope.connect2DeployToken !== undefined && $scope.connect2DeployToken !== null && $scope.connect2DeployToken !== '') {
        $http.get("/validateToken?token=" + $scope.connect2DeployToken).then(function (response) {
            if (response !== undefined && response !== null && response.data !== undefined && response.data !== null) {
                $scope.strMessage = response.data;
                if (response.data === 'Email Verified') {
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: response.data
                    });
                    $scope.boolIsSuccess = true;
                } else if (response.data === 'Email Already Verified') {
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: response.data
                    });
                }
            }
        }, function (error) {
            $scope.strMessage = error.data.message;
            iziToast.error({
                title: 'Error',
                message: error.data.message,
                position: 'topRight'
            });
        });
    }
});

connect2Deploy.controller('dashBoardController', function ($scope, $http, $location, $route, $routeParams) {

    let cookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $http.get("/api/fetchRepositoryInDB").then(function (response) {
        $scope.lstRepositoryData = [];
        if (response.data.length > 0) {
            for (let i = 0; i < response.data.length; i++) {
                $scope.lstRepositoryData.push(response.data[i].repository);
            }
        }
    }, function (error) {
        if (error.data.message === 'Unauthorized') {
            $('#sessionExpiredModal').modal("show");
        }
    });
    $scope.lstRepositoryData = [];

    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

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
                                $http.get("/api/fetchRepositoryInDB").then(function (response) {
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

connect2Deploy.controller('repoController', function ($scope, $http, $location, $routeParams, $mdDialog, $window) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.linkedService = $routeParams.linkedService;
    $scope.lstSFDCConnectionDetails = [];
    $scope.commitResponse = [];
    $scope.cherryPickDisable = true;
    $scope.cherryPickSuccess = false;
    $scope.cherryPickSuccessText = '';
    $scope.cherryPickErrorText = '';
    $scope.cherryPickError = false;
    let objWindow;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $scope.gitCommitSearch = {
        fromDate: new Date(),
        toDate: new Date()
    };
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

    const eventListenerCallBack = function (objEvent) {
        if (objWindow !== undefined && objWindow !== null && objEvent !== undefined && objEvent !== null &&
            objEvent.data !== undefined && objEvent.data !== null && objEvent.data.strDestinationId !== undefined && objEvent.data.strDestinationId !== null) {
            if (objEvent.data.strDestinationId === 'OauthPayload') {
                sfdcAccessTokenFromExternalPage = objEvent.data.sfdcAccessToken;
                sfdcUserNameFromExternalPage = objEvent.data.sfdcUserName;
                sfdcInstanceFromExternalPage = objEvent.data.sfdcInstanceURL;
                sfdcRefreshTokenFromExternalPage = objEvent.data.sfdcRefreshToken;
                let windowName = objEvent.data.windowName;
                if (windowName === 'ConnectWithOAuth' + $scope.repoId) {
                    objWindow.close();
                    $scope.sfdcOrg.oauthSuccess = 'true';
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: 'SFDC connection successful.'
                    });
                    window.removeEventListener('message', eventListenerCallBack, false);
                    $scope.$apply();
                }
            }

            if (objEvent.data.strDestinationId === 'OauthPayloadFailed') {
                sfdcAccessTokenFromExternalPage = '';
                sfdcUserNameFromExternalPage = '';
                sfdcInstanceFromExternalPage = '';
                sfdcRefreshTokenFromExternalPage = '';
                let windowName = objEvent.data.windowName;
                if (windowName === 'ConnectWithOAuth' + $scope.repoId) {
                    $scope.sfdcOrg.oauthSuccess = 'false';
                    iziToast.error({title: 'Error', message: 'Not able to create SFDC connection.', position: 'topRight'});
                    window.removeEventListener('message', eventListenerCallBack, false);
                    $scope.$apply();
                }
            }
        }

    };

    $http.get("/api/showSfdcConnectionDetails?gitRepoId=" + $scope.repoId).then(function (response) {
        $scope.lstSFDCConnectionDetails = response.data;
    }, function (error) {
        console.log(error);
        if (error.data.message === 'Unauthorized') {
            $('#sessionExpiredModal').modal("show");
        }
    });

    $http.get("/api/getAllBranches?strRepoId=" + $scope.repoId).then(function (response) {
        $scope.availableTags = response.data;
    }, function (error) {
        console.log(error);
        if (error.data.message === 'Unauthorized') {
            $('#sessionExpiredModal').modal("show");
        }
    });
    $scope.complete = function () {
        $("#branchNamel3").autocomplete({
            source: $scope.availableTags
        });
        $("#gitHubBranch").autocomplete({
            source: $scope.availableTags
        });
        $("#gitHubDestinationBranch").autocomplete({
            source: $scope.availableTags
        });
    };

    $scope.tableHeaders = ['', 'Commit Id', 'Date', 'Commit Message', 'Committer Name'];

    $scope.fetchCommits = function (gitCommitSearch) {
        $scope.cherryPickSuccess = false;
        $scope.cherryPickSuccessText = '';
        $scope.cherryPickErrorText = '';
        $scope.cherryPickError = false;
        $scope.cherryPickDisable = true;
        gitCommitSearch.userConnect2DeployToken = $scope.connect2DeployHeaderCookie;
        gitCommitSearch.linkedServiceName = $scope.linkedService;
        gitCommitSearch.repoId = $scope.repoId;
        $http.post("/api/fetchAllCommits", gitCommitSearch).then(function (response) {
            $scope.commitResponse = response.data;
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });
    };

    $scope.enableCherryPickButton = function () {
        let lstCommitIdsSelected = [];
        for (let i = 0; i < $scope.commitResponse.length; i++) {
            repoToken = $scope.commitResponse[0].repoToken;
            repoUserName = $scope.commitResponse[0].repoUserName;
            ghEnterpriseServerURL = $scope.commitResponse[0].ghEnterpriseServerURL;
            if ($scope.commitResponse[i].isSelected) {
                gitCloneURL = $scope.commitResponse[i].gitCloneURL;
                lstCommitIdsSelected.push($scope.commitResponse[i].commitId);
            }
        }
        if (lstCommitIdsSelected.length > 0) {
            $scope.cherryPickDisable = false;
        } else {
            $scope.cherryPickDisable = true;
        }
    }

    $scope.selectAndFireCherryPick = function () {
        $scope.cherryPickDisable = true;
        let lstCommitIdsSelected = [];
        let gitCloneURL = '';
        let repoToken = '';
        let repoUserName = '';
        let ghEnterpriseServerURL = '';
        const commitResponseCopy = [];
        Object.assign(commitResponseCopy, $scope.commitResponse);
        commitResponseCopy.sort(function (a, b) {
            return new Date(a.commitDate) - new Date(b.commitDate);
        });
        for (let i = 0; i < commitResponseCopy.length; i++) {
            repoToken = commitResponseCopy[0].repoToken;
            repoUserName = commitResponseCopy[0].repoUserName;
            ghEnterpriseServerURL = commitResponseCopy[0].ghEnterpriseServerURL;
            if (commitResponseCopy[i].isSelected) {
                gitCloneURL = commitResponseCopy[i].gitCloneURL;
                lstCommitIdsSelected.push(commitResponseCopy[i].commitId);
            }
        }
        let cherryPickRequest = {
            lstCommitIdsSelected: lstCommitIdsSelected,
            destinationBranch: $scope.gitCommitSearch.destinationBranch,
            newBranch: $scope.gitCommitSearch.newBranchName,
            gitCloneURL: gitCloneURL,
            repoToken: repoToken,
            repoUserName: repoUserName,
            ghEnterpriseServerURL: ghEnterpriseServerURL,
            linkedServiceName: $scope.linkedService,
            repoId: $scope.repoId
        };
        $http.post("/api/cherryPick", cherryPickRequest).then(function (response) {
            $scope.commitResponse = [];
            $scope.cherryPickDisable = true;
            console.log(response.data);
            const linkExpression = /(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[^\s]{2,}|www\.[a-zA-Z0-9]+\.[^\s]{2,})/gi;
            const regex = new RegExp(linkExpression);
            for (let i = 0; i < response.data.length; i++) {
                if (response.data[i].match(regex)) {
                    $scope.cherryPickSuccess = true;
                    $scope.cherryPickSuccessText = 'Cherry Picking Successful ! ' + '<a href="' + response.data[i] + '" target="_blank">Click Here</a>' + ' to create Pull Request';
                } else {
                    if (response.data[i].indexOf('**** GIT PUSH FAILED ****') > -1 || response.data[i].indexOf('**** GIT CHERRY PICK') > -1
                        || response.data[i].indexOf('**** GIT CREATION OF NEW BRANCH') > -1 || response.data[i].indexOf('**** GIT CHECKOUT OF') > -1
                        || response.data[i].indexOf('Already Exists ! Please try creating different branch.') > -1) {
                        $scope.cherryPickError = true;
                        $scope.cherryPickErrorText = response.data[i];
                    }
                }
            }
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
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
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
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
            const newWindow = objWindow = window.open(url, 'ConnectWithOAuth' + $scope.repoId, 'height=600,width=450,left=100,top=100');
            window.addEventListener('message', eventListenerCallBack, false);
            if (newWindow !== undefined && newWindow !== null) {
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
            id: checkIfInValid(sfdcOrg.id) ? null : sfdcOrg.id,
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
            connect2DeployUser: localStorage.getItem('userEmail'),
            linkedService: $scope.linkedService,
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
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
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
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
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
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        })
    };

    function checkIfInValid(objData) {
        return objData === undefined || objData === null || objData === '';
    }

});

connect2Deploy.controller('appPageRepoController', function ($scope, $http, $location, $routeParams, $mdDialog) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }

    $scope.tableHeaders = ['Linked Services', 'Username', 'Actions'];
    $scope.services = [];
    $http.get("/api/fetchAllLinkedServices?userEmail=" + $scope.userName).then(function (response) {
        let data = response.data;
        try {
            for (let i = 0; i < data.length; i++) {
                $scope.services.push(data[i]);
            }
        } catch (e) {

        }
    }, function (error) {
        if (error.data.message === 'Unauthorized') {
            $('#sessionExpiredModal').modal("show");
        }
        console.error(error);
    });


    $scope.connectGit = function (gitName, $event) {
        if (gitName === 'GitHub') {
            $http.get("/api/initiateGitHubFlow?userEmail=" + localStorage.getItem('userEmail')).then(function (response) {
                window.open(response.data, '_self');
            }, function (error) {
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
                console.error(error);
            });
        }
        if (gitName === 'GitHub Enterprise') {
            function DialogController($scope, $mdDialog) {
                $scope.hide = function () {
                    $mdDialog.hide();
                };

                $scope.cancel = function () {
                    $mdDialog.cancel();
                };

                $scope.answer = function (githubEnterprise) {
                    $mdDialog.hide();
                    githubEnterprise.userName = localStorage.getItem('userEmail');
                    githubEnterprise.requestFrom = 'apps/dashboard/createApp';
                    $http.post("/api/initiateGitHubEnterpriseFlow", githubEnterprise).then(function (response) {
                        window.open(response.data, '_self');
                    }, function (error) {
                        if (error.data.message === 'Unauthorized') {
                            $('#sessionExpiredModal').modal("show");
                        }
                        console.error(error);
                    });
                };
            }

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
                    '                        <md-icon md-svg-src="../images/ic_close_24px.svg" aria-label="Close dialog"></md-icon>\n' +
                    '                    </md-button>\n' +
                    '                </div>\n' +
                    '            </md-toolbar>\n' +
                    '            <md-content layout-padding="">\n' +
                    '                <div>\n' +
                    '                    <form name="githubForm">\n' +
                    '                        <md-input-container class="md-block" flex-gt-sm="">\n' +
                    '                            <label>Server Name</label>\n' +
                    '                            <input ng-model="githubEnterprise.serverURL">\n' +
                    '                        </md-input-container>\n' +
                    '                        <md-input-container class="md-block">\n' +
                    '                            <label>Client Id</label>\n' +
                    '                            <input ng-model="githubEnterprise.clientId">\n' +
                    '                        </md-input-container>\n' +
                    '                        <md-input-container class="md-block">\n' +
                    '                            <label>Client Secret</label>\n' +
                    '                            <input ng-model="githubEnterprise.clientSecret">\n' +
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
        }
    };

    $scope.disconnectGit = function (linkedServiceName, linkedServiceId) {
        $http.get("/api/deleteLinkedService?linkedServiceName=" + linkedServiceName + "&linkedServiceId=" + linkedServiceId).then(function (response) {
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: response.data
                });
                $http.get("/api/fetchAllLinkedServices?userEmail=" + $scope.userName).then(function (response) {
                    let data = response.data;
                    try {
                        for (let i = 0; i < data.length; i++) {
                            $scope.services.push(data[i]);
                        }
                    } catch (e) {

                    }
                }, function (error) {
                    if (error.data.message === 'Unauthorized') {
                        $('#sessionExpiredModal').modal("show");
                    }
                    console.error(error);
                });
            }, function (error) {
                iziToast.error({
                    title: 'Error',
                    message: 'Issue in disconnection ! ' + error.data.message,
                    position: 'topRight'
                });
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
            }
        );
    };

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

});

connect2Deploy.controller('deploymentController', function ($scope, $http, $location, $routeParams, $mdDialog) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    const repoId = $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.branchConnectedTo = $routeParams.branchConnectedTo;
    $scope.branchName = $routeParams.branchConnectedTo;
    $scope.lstDeployments = [];

    $scope.cancelDeployment = function (deploymentJobId) {
        $http.get("/api/cancelDeployment?deploymentJobId=" + deploymentJobId,).then(function (response) {
            if (response.data !== undefined && response.data !== null && response.data === 'Success') {
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: response.data
                });
            } else {
                iziToast.error({
                    title: 'Error',
                    message: response.data,
                    position: 'topRight'
                });
            }
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
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
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    const repoId = $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.branchConnectedTo = $routeParams.branchConnectedTo;
    $scope.branchName = $routeParams.branchConnectedTo;
    $scope.scheduledJobsList = [];
    $scope.scheduledJobsUnitTestList = [];
    $scope.tableHeaders = ['Owner', 'Job Name', 'Organization', 'Start Time', 'Last Run', 'Status', 'Action'];
    $scope.tableHeadersForTests = ['Owner', 'Job Name', 'Organization', 'Threshold', 'Start Time', 'Last Run', 'Status', 'Action'];

    $scope.fetchAllJobs = function () {
        $http.get("/api/fetchAllScheduledJobs?connect2DeployUser=" + $scope.userName).then(function (response) {
            if (response.data.DeploymentJob !== undefined && response.data.DeploymentJob !== null) {
                $scope.scheduledJobsList = response.data.DeploymentJob;
            } else {
                $scope.scheduledJobsList = [];
            }
            if (response.data.TestingJob !== undefined && response.data.TestingJob !== null) {
                $scope.scheduledJobsUnitTestList = response.data.TestingJob;
            } else {
                $scope.scheduledJobsUnitTestList = [];
            }
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });
    }

    $scope.fetchAllJobs();
    $scope.onStatusChange = function (scheduledDeploymentJob) {
        $http.get("/api/updateScheduledJob?scheduledDeploymentJobId=" + scheduledDeploymentJob.id + '&boolActive=' + scheduledDeploymentJob.boolActive).then(function (returnedScheduledJob) {
                if (returnedScheduledJob.data === 'Success') {
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: 'Job updated successfully'
                    });
                    $scope.fetchAllJobs();
                }
            }, function (error) {
                console.log(error);
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
            }
        );
    }
    $scope.deleteJob = function (scheduleJob) {
        $http.delete("/api/deleteScheduledJob?scheduleJobId=" + scheduleJob.id).then(function (returnedScheduledJob) {
                if (returnedScheduledJob.data === 'Success') {
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: 'Job deleted successfully'
                    });
                }
                $scope.fetchAllJobs();
            }, function (error) {
                console.log(error);
                if (error.data.message === 'Unauthorized') {
                    $('#sessionExpiredModal').modal("show");
                }
            }
        );
    }

    /*Code for Scheduled deployment job started*/
    function ScheduledDialogController($scope, $mdDialog) {
        $scope.scheduledJob = {};
        $scope.availableTags = [];

        let userEmail = localStorage.getItem('userEmail');
        $http.get("/api/fetchSfdcConnectionDetailsByUser?connect2DeployUser=" + userEmail).then(function (response) {
            $scope.scheduledJob.sfdcConnections = response.data;
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.saveScheduleJob = function (answer) {
            $mdDialog.hide(answer);
        };

        $scope.openCalendar = function (e, scheduledJob) {
            scheduledJob.open = true;
        };

        $scope.onComplete = function () {
            $("#scheduledFromBranch").autocomplete({
                source: $scope.availableTags
            });
            $("#scheduledToBranch").autocomplete({
                source: $scope.availableTags
            });
        }


        $scope.onSFDCConnectionChange = function (selectedConnection) {
            let objSfdcSelected = null;
            for (let i = 0; i < $scope.scheduledJob.sfdcConnections.length; i++) {
                if (selectedConnection === $scope.scheduledJob.sfdcConnections[i].id) {
                    objSfdcSelected = $scope.scheduledJob.sfdcConnections[i];
                    break;
                }
            }
            if (objSfdcSelected !== null) {
                $scope.scheduledJob.orgUserEmail = objSfdcSelected.userName;
                $scope.scheduledJob.gitRepoId = objSfdcSelected.gitRepoId;
                $scope.scheduledJob.connect2DeployUserEmail = objSfdcSelected.connect2DeployUser;
                $scope.scheduledJob.connect2DeployUserId = objSfdcSelected.connect2DeployUserId;
                $scope.scheduledJob.type = 'DeploymentJob';

                $http.get("/api/fetchDetailsForScheduledJob?sfdcConnectionId=" + selectedConnection).then(function (response) {
                    $scope.availableTags = response.data;
                }, function (error) {
                    console.log(error);
                    if (error.data.message === 'Unauthorized') {
                        $('#sessionExpiredModal').modal("show");
                    }
                });
            }
        }

        $scope.startDateBeforeRender = function ($view, $dates, $leftDate, $upDate, $rightDate) {
            const minDate = moment().local().startOf($view).valueOf();
            for (let i = 0; i < $dates.length; i++) {
                if ($view === 'day' || $view === 'hour') {
                    if (minDate > $dates[i].localDateValue()) {
                        $dates[i].selectable = false;
                    }
                } else {
                    if (minDate >= $dates[i].localDateValue()) {
                        $dates[i].selectable = false;
                    }
                }
            }
        };

    }

    $scope.createNewJob = function (event) {
        const outerScope = $scope;
        $mdDialog.show({
            controller: ScheduledDialogController,
            templateUrl: '../html/scheduleJob.tpl.html',
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: false,
        }).then(function (scheduledJob) {
            $http.post("/api/saveScheduledJob", scheduledJob).then(function (returnedScheduledJob) {
                    outerScope.scheduledJobsList.push(returnedScheduledJob.data);
                }, function (error) {
                    console.log(error);
                    if (error.data.message === 'Unauthorized') {
                        $('#sessionExpiredModal').modal("show");
                    }
                }
            );
        }, function () {
            $scope.status = 'You cancelled the dialog.';
        });
    }
    /*Code for Scheduled deployment job ended*/

    /*Code for Scheduled Unit Testing job started*/
    function ScheduledDialogUnitTestingController($scope, $mdDialog) {
        $scope.scheduledTestingJob = {};

        let userEmail = localStorage.getItem('userEmail');
        $http.get("/api/fetchSfdcConnectionDetailsByUser?connect2DeployUser=" + userEmail).then(function (response) {
            $scope.scheduledTestingJob.sfdcConnections = response.data;
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.saveScheduleJob = function (answer) {
            $mdDialog.hide(answer);
        };

        $scope.openCalendar = function (e, scheduledTestingJob) {
            scheduledTestingJob.open = true;
        };

        $scope.onSFDCConnectionChange = function (selectedConnection) {
            let objSfdcSelected = null;
            for (let i = 0; i < $scope.scheduledTestingJob.sfdcConnections.length; i++) {
                if (selectedConnection === $scope.scheduledTestingJob.sfdcConnections[i].id) {
                    objSfdcSelected = $scope.scheduledTestingJob.sfdcConnections[i];
                    break;
                }
            }
            if (objSfdcSelected !== null) {
                $scope.scheduledTestingJob.orgUserEmail = objSfdcSelected.userName;
                $scope.scheduledTestingJob.gitRepoId = objSfdcSelected.gitRepoId;
                $scope.scheduledTestingJob.connect2DeployUserEmail = objSfdcSelected.connect2DeployUser;
                $scope.scheduledTestingJob.connect2DeployUserId = objSfdcSelected.connect2DeployUserId;
                $scope.scheduledTestingJob.type = 'TestingJob';
            }
        }

        $scope.startDateBeforeRender = function ($view, $dates, $leftDate, $upDate, $rightDate) {
            const minDate = moment().local().startOf($view).valueOf();
            for (let i = 0; i < $dates.length; i++) {
                if ($view === 'day' || $view === 'hour') {
                    if (minDate > $dates[i].localDateValue()) {
                        $dates[i].selectable = false;
                    }
                } else {
                    if (minDate >= $dates[i].localDateValue()) {
                        $dates[i].selectable = false;
                    }
                }
            }
        };

    }

    $scope.createNewJobTesting = function (event) {
        const outerScope = $scope;
        $mdDialog.show({
            controller: ScheduledDialogUnitTestingController,
            templateUrl: '../html/scheduleJobUnitTest.tpl.html',
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: false,
        }).then(function (scheduledTestingJob) {
            $http.post("/api/saveScheduledJob", scheduledTestingJob).then(function (returnedScheduledJob) {
                    outerScope.scheduledJobsUnitTestList.push(returnedScheduledJob.data);
                }, function (error) {
                    console.log(error);
                    if (error.data.message === 'Unauthorized') {
                        $('#sessionExpiredModal').modal("show");
                    }
                }
            );
        }, function () {
            $scope.status = 'You cancelled the dialog.';
        });
    }
    /*Code for Scheduled Unit Testing job ended*/

    $scope.runUnitTest = function (eachData) {
        $http.get("/api/runJob?scheduledJobId=" + eachData.id).then(function (response) {
            if (response.data === 'Error') {
                iziToast.error({
                    title: 'Error',
                    message: 'Error running Job.',
                    position: 'topRight'
                });
            } else {
                iziToast.success({
                    timeout: 5000,
                    icon: 'fa fa-chrome',
                    title: 'OK',
                    message: response.data
                });
            }
        }, function (error) {
            console.log(error);
            if (error.data.message === 'Unauthorized') {
                $('#sessionExpiredModal').modal("show");
            }
        });
    }

    $scope.viewReport = function (eachData) {
        $location.path('/apps/dashboard/scheduledDeploymentsDetails/' + eachData.id)
    }

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

});

connect2Deploy.controller('scheduledDeploymentDetailsController', function ($scope, $http, $location, $routeParams, $mdDialog, $mdSidenav) {
    $scope.connect2DeployHeaderCookie = $.cookie("CONNECT2DEPLOY_TOKEN");
    $http.defaults.headers.common['Authorization'] = 'Bearer ' + $scope.connect2DeployHeaderCookie;
    $scope.userName = localStorage.getItem('userEmail');
    $scope.avatar_url = localStorage.avatar_url;
    $scope.scheduledJobId = $routeParams.scheduledJobId;
    $scope.sfdcCodeCoverageOrg = null;
    $scope.scheduledTestJobDetailsAllClasses = [];
    $scope.orgCoverage = null;
    $scope.orgName = null;
    $scope.tableHeadersForTestDetails = ['Class', 'Method name', 'Result', 'Run time (sec)'];
    $scope.scheduledTestJobDetailsTableData = [];
    $scope.errorDetails = [];

    function millisToMinutesAndSeconds(millis) {
        var minutes = Math.floor(millis / 60000);
        var seconds = ((millis % 60000) / 1000).toFixed(0);
        return minutes + ":" + (seconds < 10 ? '0' : '') + seconds;
    }

    $http.get("/api/fetchDetailsForScheduledJobId?scheduledJobId=" + $scope.scheduledJobId).then(function (response) {
        if (response.data !== undefined && response.data !== null && response.data !== '') {
            $scope.sfdcCodeCoverageOrg = response.data;
            for (const lstSfdcCodeCoverageDetailsTest of $scope.sfdcCodeCoverageOrg.lstSfdcCodeCoverageDetailsTests) {
                for (const sfdcCodeCoverageListElement of lstSfdcCodeCoverageDetailsTest.sfdcCodeCoverageList) {
                    const tData = {
                        classOrTriggerName: lstSfdcCodeCoverageDetailsTest.nameOfClassOrTrigger,
                        methodName: sfdcCodeCoverageListElement.methodName,
                        result: sfdcCodeCoverageListElement.boolPassed,
                        runTime: millisToMinutesAndSeconds(sfdcCodeCoverageListElement.runTime),
                        error: sfdcCodeCoverageListElement.errorMessage === undefined ||
                        sfdcCodeCoverageListElement.errorMessage === null ? '-' : sfdcCodeCoverageListElement.errorMessage,
                        stacktrace: sfdcCodeCoverageListElement.stacktrace === undefined ||
                        sfdcCodeCoverageListElement.stacktrace === null ? '-' : sfdcCodeCoverageListElement.stacktrace,
                    }
                    $scope.scheduledTestJobDetailsTableData.push(tData);

                }
            }

            $scope.scheduledTestJobDetailsTableData.sort(function (a, b) {
                return a.classOrTriggerName.localeCompare(b.classOrTriggerName);
            });
            $scope.scheduledTestJobDetailsAllClasses = $scope.sfdcCodeCoverageOrg.lstSfdcCodeCoverageDetails;
            $scope.orgCoverage = $scope.sfdcCodeCoverageOrg.orgCoverage;
            $scope.orgName = $scope.sfdcCodeCoverageOrg.orgName;
        } else {
            $scope.sfdcCodeCoverageOrg = null;
            $scope.scheduledTestJobDetailsTableData = [];
            $scope.scheduledTestJobDetailsAllClasses = [];
            $scope.orgCoverage = null;
            $scope.orgName = null;
        }
    }, function (error) {
        console.log(error);
        if (error.data.message === 'Unauthorized') {
            $('#sessionExpiredModal').modal("show");
        }
    });


    $scope.toggleRight = function callBuildToggler(eachData , right){
        $scope.errorDetails = [];
        if (eachData !== undefined && eachData !== null && eachData.result === false) {
            for (let intCount = 0; intCount < $scope.scheduledTestJobDetailsTableData.length; intCount++) {
                if ($scope.scheduledTestJobDetailsTableData[intCount].classOrTriggerName === eachData.classOrTriggerName
                    && $scope.scheduledTestJobDetailsTableData[intCount].methodName === eachData.methodName) {
                    $scope.errorDetails.push($scope.scheduledTestJobDetailsTableData[intCount].error);
                    $scope.errorDetails.push($scope.scheduledTestJobDetailsTableData[intCount].stacktrace);
                }
            }
            return $mdSidenav(right).toggle();
        } else {
            return null;
        }
    }

    $scope.tableToExcel = function(scheduledTestJobDetailsTableData, $event) {
        if(scheduledTestJobDetailsTableData !== undefined && scheduledTestJobDetailsTableData !== null){
            var confirm = $mdDialog.prompt()
                .title('Name of the File?')
                .placeholder('FileName')
                .ariaLabel('FileName')
                .targetEvent($event)
                .required(true)
                .ok('Download')
                .cancel('Cancel');

            $mdDialog.show(confirm).then(function(result) {
                const shallowCopyTestData = [...scheduledTestJobDetailsTableData];
                let toJson = angular.toJson(shallowCopyTestData);
                try {
                    /* generate a worksheet */
                    var ws = XLSX.utils.json_to_sheet(JSON.parse(toJson));
                    /* add to workbook */
                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, result);

                    /* write workbook and force a download */
                    XLSX.writeFile(wb, result+".xlsx");
                }catch (e) {
                    console.log(e);
                }
            }, function() {
                console.log('You cancelled dialog');
            });
        }
    };

    $scope.logoutFunction = function () {
        logoutFunctionCaller($location);
    };

})

connect2Deploy.controller('RightCtrl', function ($scope, $timeout, $mdSidenav) {
    $scope.close = function () {
        // Component lookup should always be available since we are not using `ng-if`
        $mdSidenav('right').close();
    };
})

connect2Deploy.controller('registerController', function ($scope, $http, $location, $routeParams) {
    $scope.register = function (userEntity) {
        if (typeof grecaptcha !== 'undefined') {
            if (userEntity !== undefined && userEntity !== null && userEntity.password === userEntity.RepeatPassword) {
                const siteKey = '6Lcr3uUUAAAAAPnCZdcC9qTt-GKFVl9U1fmpHHRt';
                grecaptcha.execute(siteKey, {action: 'register'}).then(function (response) {
                    userEntity.googleReCaptchaV3 = response;
                    $http.post("/register", userEntity).then(function (userResponse) {
                            $location.path("/apps/dashboard/success");
                        }, function (error) {
                            iziToast.error({
                                title: 'Error',
                                message: 'User Creation Failed. ' + error.data.message,
                                position: 'topRight'
                            });
                        }
                    );
                })
            }
        }
    }
});

$(window).on("beforeunload", function () {
    if (sse !== undefined && sse !== null && sse !== '') {
        sse.close();
    }
});