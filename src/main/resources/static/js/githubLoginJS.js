var connect2Deploy = angular.module("connect2Deploy", ['ngRoute', 'angularjs-dropdown-multiselect']);
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
        .when('/apps/dashboard/app/:repoName/:repoId', {
            templateUrl: './html/repoDetails.html',
            controller: 'repoController',
        })
        .when('/apps/dashboard/createApp', {
            templateUrl: './html/addApp.html',
            controller: 'appPageRepoController',
        })
        .when('/apps/error', {
            templateUrl: './html/error.html'
        })
        .otherwise({
            redirectTo: '/index'
        });

    // use the HTML5 History API
    $locationProvider.html5Mode(true);

});

connect2Deploy.controller('indexController', function ($scope, $http, $location) {
    $scope.redirectJS = function () {
        window.open('https://github.com/login/oauth/authorize?client_id=0b5a2cb25fa55a0d2b76&redirect_uri=https://forceci.herokuapp.com/gitAuth&scope=repo,user:email&state=Mv4nodgDGEKInu6j2vYBTLoaIVNSXhb4NWuUE8V2', '_self');
    };

    let accessToken = $.cookie("ACCESS_TOKEN");
    if (accessToken !== undefined && accessToken !== null && accessToken !== '') {
        $location.path("/apps/dashboard");
    } else {
        $location.path("/index");
    }
});

connect2Deploy.controller('dashBoardController', function ($scope, $http, $location) {
    $scope.lstRepositoryData = [];

    $http.get("/fetchUserName").then(function (response) {
        if (response.data !== undefined && response.data !== null) {
            $scope.userName = response.data.login;
            $scope.avatar_url = response.data.avatar_url;
            localStorage.setItem('githubOwner', response.data.login);
            localStorage.setItem('avatar_url', response.data.avatar_url);
            $http.get("/fetchRepositoryInDB?gitHubUser=" + response.data.login).then(function (response) {
                $scope.lstRepositoryData = [];
                if (response.data.length > 0) {
                    for (let i = 0; i < response.data.length; i++) {
                        $scope.lstRepositoryData.push(response.data[i].repository);
                    }
                }
            }, function (error) {

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
    });


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
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                    if (eachData.repositoryId) {
                        $http.delete("/deleteWebHook?repositoryName=" + eachData.repositoryName + "&repositoryId=" + eachData.repositoryId + "&repositoryOwner=" +
                            eachData.owner + "&webHookId=" + eachData.webHook.id).then(function (response) {
                            console.log(response);
                            if (response.status === 200 && response.data === 204) {
                                iziToast.success({
                                    timeout: 5000,
                                    icon: 'fa fa-chrome',
                                    title: 'OK',
                                    message: 'App deleted successfully'
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
        if (objData === undefined || objData === null || objData === '') {
            return true;
        } else {
            return false;
        }
    }

    // Toggle the side navigation
    $("#sidebarToggle, #sidebarToggleTop").on('click', function (e) {
        $("body").toggleClass("sidebar-toggled");
        $(".sidebar").toggleClass("toggled");
        if ($(".sidebar").hasClass("toggled")) {
            $('.sidebar .collapse').collapse('hide');
        }
    });

    // Close any open menu accordions when window is resized below 768px
    $(window).resize(function () {
        if ($(window).width() < 768) {
            $('.sidebar .collapse').collapse('hide');
        }
    });

    // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
    $('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function (e) {
        if ($(window).width() > 768) {
            var e0 = e.originalEvent,
                delta = e0.wheelDelta || -e0.detail;
            this.scrollTop += (delta < 0 ? 1 : -1) * 30;
            e.preventDefault();
        }
    });

    // Scroll to top button appear
    $(document).on('scroll', function () {
        var scrollDistance = $(this).scrollTop();
        if (scrollDistance > 100) {
            $('.scroll-to-top').fadeIn();
        } else {
            $('.scroll-to-top').fadeOut();
        }
    });

    // Smooth scrolling using jQuery easing
    $(document).on('click', 'a.scroll-to-top', function (e) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: ($($anchor.attr('data-href')).offset().top)
        }, 1000, 'easeInOutExpo');
        e.preventDefault();
    });

});

connect2Deploy.controller('repoController', function ($scope, $http, $location, $routeParams) {
    $scope.repoId = $routeParams.repoId;
    $scope.repoName = $routeParams.repoName;
    $scope.lstSFDCConnectionDetails = [];
    let objWindow;
    $scope.userName = localStorage.githubOwner;
    $scope.avatar_url = localStorage.avatar_url;
    $scope.sfdcOrg = {
        orgName: '',
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
    $scope.availableTags = [];
    let sfdcAccessTokenFromExternalPage;
    let sfdcUserNameFromExternalPage;
    let sfdcInstanceFromExternalPage;


    window.addEventListener('message', function (objEvent) {
        if (objEvent !== undefined && objEvent !== null &&
            objEvent.data !== undefined && objEvent.data !== null &&
            objEvent.data.strDestinationId !== undefined && objEvent.data.strDestinationId !== null) {
            if (objEvent.data.strDestinationId === 'OauthPayload') {
                sfdcAccessTokenFromExternalPage = objEvent.data.sfdcAccessToken;
                sfdcUserNameFromExternalPage = objEvent.data.sfdcUserName;
                sfdcInstanceFromExternalPage = objEvent.data.sfdcInstanceURL;
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
                if (objWindow !== undefined && objWindow !== null) {
                    objWindow.close();
                }
                $scope.sfdcOrg.oauthSuccess = 'false';
                iziToast.error({title: 'Error', message: 'Not able to create SFDC connection.', position: 'topRight'});
            }
        }
        $scope.$apply();

    });


    $http.get("/showSfdcConnectionDetails?gitRepoId=" + $scope.repoId).then(function (response) {
        $scope.lstSFDCConnectionDetails = response.data;
    }, function (error) {
        console.log(error);
    });

    $http.get("/getAllBranches?strRepoId=" + $scope.repoId).then(function (response) {
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
        $scope.sfdcOrg = {
            id: '',
            orgName: '',
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
            userName: sfdcOrg.userName,
            instanceURL: checkIfInValid(sfdcOrg.instanceURL) ? sfdcInstanceFromExternalPage : sfdcOrg.instanceURL,
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

        $http.post("/saveSfdcConnectionDetails", sfdcDetails).then(function (response) {
                $.removeCookie('SFDC_ACCESS_TOKEN', {path: '/'});
                $.removeCookie('SFDC_USER_NAME', {path: '/'});
                $.removeCookie('SFDC_INSTANCE_URL', {path: '/'});
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
        $http.get("/showSfdcConnectionDetails?gitRepoId=" + gitRepoId).then(function (response) {
            $scope.lstSFDCConnectionDetails = response.data;
        }, function (error) {
            console.log(error);
        });
        $scope.sfdcOrg = {
            id: '',
            orgName: '',
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
        $http.delete("/deleteSfdcConnectionDetails?sfdcDetailsId=" + sfdcOrg.id).then(function (response) {
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
    $scope.userName = localStorage.githubOwner;
    $scope.avatar_url = localStorage.avatar_url;
    $scope.lstRepositoryFromApi = [];

    $scope.fetchRepo = function (eachRepository) {
        $scope.lstRepositoryFromApi = [];
        if ($scope.repoName) {
            $http.get("/fetchRepository" + "?repoName=" + $scope.repoName + "&" + "repoUser=" + $scope.userName).then(function (response) {
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
                    if (repositoryWrappers !== undefined && repositoryWrappers !== null && repositoryWrappers !== '') {
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
    };

    $scope.connectAndCreateApp = function (data) {
        $http.post("/createWebHook", data).then(function (response) {
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
