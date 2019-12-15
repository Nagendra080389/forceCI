var app = angular.module('forceCIApp', ["angularjs-dropdown-multiselect"]);
app.controller('orderFromController', function ($scope, $http, $attrs) {
    $scope.reposInDB = [];
    $scope.lstRepositoryData = [];
    //$scope.disabledForm = 'false';
    let sfdcAccessTokenFromExternalPage;
    let sfdcUserNameFromExternalPage;
    let sfdcInstanceFromExternalPage;
    let objWindow;


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
                //$scope.sfdcOrg.oauthSuccess = 'true';
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
                //$scope.sfdcOrg.oauthSuccess = 'false';
                iziToast.error({title: 'Error', message: 'Not able to create SFDC connection.', position: 'topRight'});
            }
        }
        $scope.$apply();

    });


    $http.get("/fetchUserName").then(function (response) {
        if (response.data !== undefined && response.data !== null) {
            $scope.userName = response.data.login;
            localStorage.setItem('githubOwner', response.data.login);
            $http.get("/fetchRepositoryInDB?gitHubUser=" + response.data.login).then(function (response) {
                if (response.data.length > 0) {
                    for (let i = 0; i < response.data.length; i++) {
                        let lstBranches = [];
                        $.each(response.data[i].repository.mapBranches, function (key, value) {
                            console.log(key);
                            lstBranches.push(key);
                        });
                        const sfdcOrg = {
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
                            multiBranchData: [],
                            multiExtraSettings: {enableSearch: true, showCheckAll: false, showUncheckAll: false},
                            multiSelectedBranches: []
                        };
                        sfdcOrg.multiBranchData = changeListToObjectList(lstBranches);
                        sfdcOrg.multiSelectedBranches = response.data[i].repository.lstSelectedBranches === undefined || null ? [] : changeListToObjectList(response.data[i].repository.lstSelectedBranches);
                        response.data[i].repository.sfdcOrg = sfdcOrg;
                        $scope.lstRepositoryData.push(response.data[i].repository);
                        $scope.reposInDB.push(response.data[i].repository.repositoryFullName);
                    }
                    $('#repoConnectedDialog').removeClass('hidden');
                }
            }, function (error) {

            });
            const avatarSpanTag = '<span class="absolute flex items-center justify-center w2 h2 z-2 ' +
                'nudge-right--4 pe-none" style="top: -15px">\n' +
                '          <img src=' + response.data.avatar_url + '>\n' +
                '        </span>';
            $(avatarSpanTag).insertAfter('#idSelectTab');
        }
    }, function (error) {

    });


    $scope.disconnectRepo = function (eachData) {
        if (eachData.repositoryId) {
            $http.delete("/deleteWebHook?repositoryName=" + eachData.repositoryName + "&repositoryId=" + eachData.repositoryId + "&repositoryOwner=" +
                eachData.owner + "&webHookId=" + eachData.webHook.id).then(function (response) {
                console.log(response);
                if (response.status === 200 && response.data === 204) {
                    $scope.lstRepositoryData.splice($scope.lstRepositoryData.indexOf(eachData), 1);
                    $scope.reposInDB.splice($scope.reposInDB.indexOf(eachData.repositoryFullName), 1);
                    if ($scope.lstRepositoryData.length === 0) {
                        $('#repoConnectedDialog').addClass('hidden');
                    }
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: 'WebHook deleted successfully'
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
    };

    $scope.fetchRepo = function () {
        if ($scope.repoName) {
            $http.get("/fetchRepository" + "?repoName=" + $scope.repoName + "&" + "repoUser=" + localStorage.getItem('githubOwner')).then(function (response) {
                $('#repoDialog').empty();
                for (let i = 0; i < response.data.items.length; i++) {
                    if (!$scope.reposInDB.includes(response.data.items[i].full_name)) {
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
                            '                <span class="repoFullName" data-repoName="' + response.data.items[i].name + '" data-repoId="' + response.data.items[i].id + '" ' +
                            'data-repoUrl="' + response.data.items[i].html_url + '" data-ownerAvatarUrl="' + response.data.items[i].owner.avatar_url + '" data-ownerlogin="' + response.data.items[i].owner.login + '" ' +
                            'data-ownerHtmlUrl="' + response.data.items[i].owner.html_url + '">' + response.data.items[i].full_name + '</span>\n' +
                            '                <div class="flex-auto"></div>\n' +
                            '                <button id="ember88" class="async-button default hk-button-sm--secondary ember-view connectButton" type="button">    Connect\n' +
                            '                </button>\n' +
                            '            </div>';
                        $('#repoDialog').append(eachNewDiv);
                    }
                }
                $('#repoDialog').removeClass('hidden');

            }, function (error) {

            });
        }
    };

    $(document).on("click", ".connectButton", function () {
        const $repositoryName = $(this).closest(".b--light-silver").find('span');
        const repositoryName = $repositoryName.attr('data-repoName');
        const repositoryId = $repositoryName.attr('data-repoId');
        const repositoryURL = $repositoryName.attr('data-repoUrl');
        const repositoryOwnerAvatarUrl = $repositoryName.attr('data-ownerAvatarUrl');
        const repositoryOwnerLogin = $repositoryName.attr('data-ownerlogin');
        const ownerHtmlUrl = $repositoryName.attr('data-ownerHtmlUrl');
        const repositoryFullName = $repositoryName.text();
        const data = {
            active: true,
            repositoryName: repositoryName,
            repositoryId: repositoryId,
            repositoryURL: repositoryURL,
            repositoryOwnerAvatarUrl: repositoryOwnerAvatarUrl,
            repositoryOwnerLogin: repositoryOwnerLogin,
            repositoryFullName: repositoryFullName,
            ownerHtmlUrl: ownerHtmlUrl,
            owner: localStorage.getItem('githubOwner')
        };
        $http.post("/createWebHook", data).then(function (response) {
                $repositoryName.closest(".b--light-silver").remove();
                $scope.lstRepositoryData.push(response.data);
                let lstBranches = [];
                $.each(response.data.mapBranches, function (key, value) {
                    console.log(key);
                    lstBranches.push(key);
                });

                const sfdcOrg = {
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
                    multiBranchData: [],
                    multiExtraSettings: {enableSearch: true, showCheckAll: false, showUncheckAll: false},
                    multiSelectedBranches: []
                };
                sfdcOrg.multiBranchData = changeListToObjectList(lstBranches);
                response.data.sfdcOrg = sfdcOrg;

                $scope.reposInDB.push(response.data.repositoryFullName);
                $('#repoConnectedDialog').removeClass('hidden');
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

    });

    $scope.authorize = function (eachData, $index) {
        let url = '';
        if (eachData.sfdcOrg.orgName === undefined || eachData.sfdcOrg.orgName === null || eachData.sfdcOrg.orgName === ''
            || eachData.sfdcOrg.userName === undefined || eachData.sfdcOrg.userName === null || eachData.sfdcOrg.userName === '' || (eachData.sfdcOrg.environment === '2'
                && (eachData.sfdcOrg.instanceURL === undefined || eachData.sfdcOrg.instanceURL === null || eachData.sfdcOrg.instanceURL === ''))) {
            iziToast.warning({title: 'Caution', message: 'Please fill in required fields.', position: 'center'});
            return;
        }
        if (eachData && eachData.sfdcOrg) {
            if (eachData.sfdcOrg.environment === '0') {
                url = 'https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + eachData.sfdcOrg.environment;
            } else if (eachData.sfdcOrg.environment === '1') {
                url = 'https://test.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + eachData.sfdcOrg.environment;
            } else {
                url = eachData.sfdcOrg.instanceURL + '/services/oauth2/authorize?response_type=code&client_id=3MVG9d8..z.hDcPLDlm9QqJ3hRVT2290hUCTtQVZJc4K5TAQQEi0yeXFAK' +
                    'EXd0TDKa3J8.s6XrzeFsPDL_mxt&prompt=login&redirect_uri=https://forceci.herokuapp.com/sfdcAuth&state=' + eachData.sfdcOrg.instanceURL;
            }
            const newWindow = objWindow = window.open(url, 'ConnectWithOAuth', 'height=600,width=450,left=100,top=100');
            if (window.focus) {
                newWindow.focus();
            }
        }
    };

    $scope.createNewConnection = function ($index) {
        $.removeCookie('SFDC_ACCESS_TOKEN', {path: '/'});
        $.removeCookie('SFDC_USER_NAME', {path: '/'});
        $.removeCookie('SFDC_INSTANCE_URL', {path: '/'});
        const sfdcOrg = {
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
            multiBranchData: $scope.lstRepositoryData[$index].sfdcOrg.multiBranchData === undefined || null ? [] : $scope.lstRepositoryData[$index].sfdcOrg.multiBranchData,
            multiExtraSettings: {enableSearch: true, showCheckAll: false, showUncheckAll: false},
            multiSelectedBranches: []
        };
        $scope.lstRepositoryData[$index].sfdcOrg = sfdcOrg;
        sfdcAccessTokenFromExternalPage = '';
        sfdcUserNameFromExternalPage = '';
        sfdcInstanceFromExternalPage = '';
        //$scope.disabledForm = 'false';
    };

    $scope.saveConnection = function (eachData, $index) {


        const sfdcDetails = {
            orgName: eachData.sfdcOrg.orgName,
            environment: eachData.sfdcOrg.environment,
            userName: eachData.sfdcOrg.userName,
            instanceURL: sfdcInstanceFromExternalPage,
            authorize: eachData.sfdcOrg.authorize,
            save: eachData.sfdcOrg.save,
            testConnection: eachData.sfdcOrg.testConnection,
            delete: eachData.sfdcOrg.delete,
            oauthSuccess: 'true',
            oauthFailed: eachData.sfdcOrg.oauthFailed,
            oauthSaved: eachData.sfdcOrg.oauthSaved,
            oauthToken: sfdcAccessTokenFromExternalPage,
            gitRepoId: eachData.repositoryId,
            lstSelectedBranches: changeListToObjectList(eachData.sfdcOrg.multiSelectedBranches)
        };
        if (eachData.sfdcOrg.orgName === undefined || eachData.sfdcOrg.orgName === null || eachData.sfdcOrg.orgName === '' ||
            eachData.sfdcOrg.userName === undefined || eachData.sfdcOrg.userName === null || eachData.sfdcOrg.userName === '') {
            return;
        }
        $http.post("/saveSfdcConnectionDetails", sfdcDetails).then(function (response) {
                if (response.data.userName && response.data.userName === $.cookie('SFDC_USER_NAME')) {
                    $.removeCookie('SFDC_ACCESS_TOKEN', {path: '/'});
                    $.removeCookie('SFDC_USER_NAME', {path: '/'});
                    $.removeCookie('SFDC_INSTANCE_URL', {path: '/'});
                    const gitRepoId = response.data.gitRepoId;
                    $http.get("/showSfdcConnectionDetails?gitRepoId=" + gitRepoId).then(function (response) {
                        $scope.lstRepositoryData[$index].sfdcConnectionDetails = response.data;
                    }, function (error) {
                        console.log(error);
                    });
                    iziToast.success({
                        timeout: 5000,
                        icon: 'fa fa-chrome',
                        title: 'OK',
                        message: 'SFDC connection created successfully'
                    });
                    const sfdcOrg = {
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
                        multiBranchData: [],
                        multiExtraSettings: {enableSearch: true, showCheckAll: false, showUncheckAll: false},
                        multiSelectedBranches: []
                    };
                    sfdcOrg.multiSelectedBranches = changeListToObjectList($scope.lstRepositoryData[$index].sfdcOrg.multiSelectedBranches);
                    $scope.lstRepositoryData[$index].sfdcOrg = sfdcOrg;
                }
            }, function (error) {
                console.log(error);
                iziToast.error({
                    title: 'Error',
                    message: 'SFDC connection failed, Please retry. ' + error,
                    position: 'topRight'
                });
            }
        );
    }

    $scope.showDataOnForm = function (eachSfdcConnection, eachdata) {
        let eachdataLocal = $scope.lstRepositoryData[$scope.lstRepositoryData.indexOf(eachdata)];
        let lstBranches = [];
        $.each(eachdataLocal.mapBranches, function (key, value) {
            lstBranches.push(key);
        });
        eachdataLocal.sfdcOrg = {
            orgName: eachSfdcConnection.orgName,
            environment: eachSfdcConnection.environment,
            userName: eachSfdcConnection.userName,
            instanceURL: eachSfdcConnection.instanceURL,
            authorize: eachSfdcConnection.authorize,
            save: eachSfdcConnection.save,
            testConnection: eachSfdcConnection.testConnection,
            delete: eachSfdcConnection.delete,
            oauthSuccess: eachSfdcConnection.oauthSuccess,
            oauthFailed: eachSfdcConnection.oauthFailed,
            oauthSaved: eachSfdcConnection.oauthSaved,
            disabledForm: 'true',
            multiBranchData: changeListToObjectList(lstBranches),
            multiExtraSettings: {enableSearch: true, showCheckAll: false, showUncheckAll: false},
            multiSelectedBranches: eachSfdcConnection.lstSelectedBranches === undefined || null ? [] : changeListToObjectList(eachSfdcConnection.lstSelectedBranches)
        };
        //$scope.disabledForm = 'true';


    }

    function changeListToObjectList(lstData) {
        let lstOfObjects = [];
        for (let i = 0; i < lstData.length; i++) {
            if($.type(lstData[i]) === 'string') {
                const branchData = {id: i + 1, label: lstData[i]};
                lstOfObjects.push(branchData);
            }
        }
        return lstOfObjects;
    }

    /*$scope.change = function (eachData) {
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
    };*/
});