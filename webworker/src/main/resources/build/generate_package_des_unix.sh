#!/bin/bash

#1 - change file name
#2 - target file name
#3 - target folder name

echo Source file: $1
echo Target name: $2
echo Folder name: $3

echo Searching for script file $1...

function contains() {
    local n=$#
    local value=${!n}
    for ((i=1;i < $#;i++)) {
        if [ "${!i}" == "${value}" ]; then
            echo "y"
            return 0
        fi
    }
    echo "n"
    return 1
}


if [ -e $1 ]
then   
        SCRIPTFILE=$3'/'$2'.xml'
        echo Scriptfile is: $SCRIPTFILE
        echo ===PKGXML===
        echo Creating new destructiveChanges.xml
        echo '<?xml version="1.0" encoding="UTF-8"?><Package></Package>' > $SCRIPTFILE
        ARRAY=()
        while read CFILE
        do
                echo Analyzing file `basename $CFILE` 
                case "$CFILE"
                in
                        *.app) TYPENAME="CustomApplication";;
                        *.appMenu) TYPENAME="AppMenu";;
                        *.approvalProcess) TYPENAME="ApprovalProcess";;
                        *.assignmentRules) TYPENAME="AssignmentRules";;
                        *.authprovider) TYPENAME="AuthProvider";;
                        *.asset) TYPENAME="ContentAsset";;
                        *.autoResponseRules) TYPENAME="AutoResponseRules";;
                        *.cls) TYPENAME="ApexClass";;
                        *.community) TYPENAME="Community";;
                        *.component) TYPENAME="ApexComponent";;
                        *.connectedApp) TYPENAME="ConnectedApp";;
                        *.crt) TYPENAME="Certificate";;
                        *.cleanDataService) TYPENAME="CleanDataService";;
                        *.customPermission) TYPENAME="CustomPermission";;
                        *.dashboard) TYPENAME="Dashboard";;
                        *.duplicateRule) TYPENAME="DuplicateRule";;
                        *.dataSource) TYPENAME="ExternalDataSource";;
                        *.email) TYPENAME="EmailTemplate";;
                        *.escalationRules) TYPENAME="EscalationRules";;
                        *.flexipage) TYPENAME="FlexiPage";;
                        *.flow) TYPENAME="Flow";;
                        *.flowDefinition) TYPENAME="FlowDefinition";;
                        *.globalValueSet) TYPENAME="GlobalValueSet";;
                        *.group) TYPENAME="Group";;
                        *.homePageLayout) TYPENAME="HomePageLayout";;
                        *.labels) TYPENAME="CustomLabels";;
                        *.layout) TYPENAME="Layout";;
                        *.letter) TYPENAME="Letterhead";;
                        *.LeadConvertSetting) TYPENAME="LeadConvertSettings";;
                        *.managedTopics) TYPENAME="ManagedTopics";;
                        *.matchingRule) TYPENAME="MatchingRule";;
                        *.network) TYPENAME="Network";;
                        *.object) TYPENAME="CustomObject";;
                        *.objectTranslation) TYPENAME="CustomObjectTranslation";;
                        *.page) TYPENAME="ApexPage";;
                        *.permissionset) TYPENAME="PermissionSet";;
                        *.profile) TYPENAME="Profile";;
                        *.queue) TYPENAME="Queue";;
                        *.quickAction) TYPENAME="QuickAction";;
                        *.remoteSite) TYPENAME="RemoteSiteSetting";;
                        *.reportType) TYPENAME="ReportType";;
                        *.report) TYPENAME="Report";;
                        *.resource) TYPENAME="StaticResource";;
                        *.role) TYPENAME="Role";;
                        *.settings) TYPENAME="Settings";;
                        *.sharingRules) TYPENAME="SharingRules";;
                        *.standardValueSet) TYPENAME="StandardValueSet";;
                        *.site) TYPENAME="CustomSite";;
                        *.tab) TYPENAME="CustomTab";;
                        *.translation) TYPENAME="Translations";;
                        *.territory2Type) TYPENAME="Territory2Type";;
                        *.trigger) TYPENAME="ApexTrigger";;
                        *.workflow) TYPENAME="Workflow";;
                        *.messageChannel) TYPENAME="LightningMessageChannel";;
                        *) TYPENAME="UNKNOWN";;
                esac

                echo typeName is  "$TYPENAME"

                if [[ "$TYPENAME" == "CustomSite" && "$(basename -- "$(dirname -- "$CFILE")")" == "siteDotComSites" ]]
                then
                        TYPENAME="SiteDotCom"
                fi

                if [[ "$TYPENAME" == "UNKNOWN" && "$(basename -- "$(dirname -- "$CFILE")")" == "email" ]]
                then
                        TYPENAME="EmailTemplateFolder"
                fi
                if [[ "$TYPENAME" == "UNKNOWN" && "$(basename -- "$(dirname -- "$CFILE")")" == "reports" ]]
                then
                        TYPENAME="ReportTemplateFolder"
                fi
                if [[ "$TYPENAME" == "UNKNOWN" && "$(basename -- "$(dirname -- "$CFILE")")" == "dashboards" ]]
                then
                        TYPENAME="DashboardTemplateFolder"
                fi
                echo check the file here is "$(basename -- "$(dirname -- "$CFILE")")"
                if [[ "$TYPENAME" == "UNKNOWN" && "$(basename -- "$(dirname -- "$CFILE")")" == "aura" ]]
                then
                        TYPENAME="AuraDefinitionBundle"
                fi
                if [[ "$TYPENAME" == "UNKNOWN" && "$(basename -- "$(dirname -- "$CFILE")")" == "lwc" ]]
                then
                        TYPENAME="LightningComponentBundle"
                fi

                if [ "$TYPENAME" != "UNKNOWN" ]
                then
                        ENTITY=$(basename "$CFILE")
                        ENTITY="${ENTITY%.*}"

                        if [ "$TYPENAME" == "EmailTemplate" ]
                        then
                                ENTITY=$(basename -- "$(dirname -- "$CFILE")")"/"$ENTITY
                        fi
                        if [ "$TYPENAME" == "Report" ]
                        then
                                ENTITY=$(basename -- "$(dirname -- "$CFILE")")"/"$ENTITY
                        fi
                        if [ "$TYPENAME" == "Dashboard" ]
                        then
                                ENTITY=$(basename -- "$(dirname -- "$CFILE")")"/"$ENTITY
                        fi
                        echo for aura comps $(basename "$CFILE")
                        if [ "$TYPENAME" == "AuraDefinitionBundle" ]
                        then
                                if [[ $(contains "${ARRAY[@]}" $(basename "$CFILE")) != "y" && "$(basename "$CFILE")" != *-meta.xml ]]
                                then
                                    echo $(basename -- "$(dirname -- "$CFILE")")
                                    ARRAY+=$(basename "$CFILE")
                                    ENTITY=$(basename "$CFILE")
                                else
                                    continue
                                fi
                        fi
                        if [ "$TYPENAME" == "LightningComponentBundle" ]
                        then
                                if [[ $(contains "${ARRAY[@]}" $(basename "$CFILE")) != "y" && "$(basename "$CFILE")" != *-meta.xml ]]
                                then
                                    echo $(basename -- "$(dirname -- "$CFILE")")
                                    ARRAY+=$(basename "$CFILE")
                                    ENTITY=$(basename "$CFILE")
                                else
                                    continue
                                fi
                        fi
                        #Email template parsing
                        if [ "$TYPENAME" == "EmailTemplateFolder" ]
                        then
                                TYPENAME="EmailTemplate"
                                ENTITY=$(basename $ENTITY "-meta")
                        fi

                        #Report template parsing
                        if [ "$TYPENAME" == "ReportTemplateFolder" ]
                        then
                                TYPENAME="Report"
                                ENTITY=$(basename $ENTITY "-meta")
                        fi

                        #Dashboard template parsing
                        if [ "$TYPENAME" == "DashboardTemplateFolder" ]
                        then
                                TYPENAME="Dashboard"
                                ENTITY=$(basename $ENTITY "-meta")
                        fi
        
                        #Wohack - Matching rules hack
                        if [ "$TYPENAME" == "MatchingRule" ]
                        then
                                TYPENAME="MatchingRules"
                                ENTITY="*"
                        fi
                        #Wohack end

                        if grep -Fq ">$TYPENAME<" $SCRIPTFILE
                        then
                                #Wohack - Dont add additional asterisk if one already there
                                if [ "$TYPENAME" == "MatchingRules" ]
                                then
                                        #Do nothing
                                        echo Matching rule inbound...
                                else
                                        echo Generating new member for $ENTITY
                                        xmlstarlet ed -L -s "/Package/types[name='$TYPENAME']" -t elem -n members -v "$ENTITY" $SCRIPTFILE
                                fi
                                #Wohack end
                        else
                                echo Generating new $TYPENAME type
                                xmlstarlet ed -L -s /Package -t elem -n types -v "" $SCRIPTFILE
                                xmlstarlet ed -L -s '/Package/types[not(*)]' -t elem -n name -v "$TYPENAME" $SCRIPTFILE
                                echo Generating new member for $ENTITY
                                xmlstarlet ed -L -s "/Package/types[name='$TYPENAME']" -t elem -n members -v "$ENTITY" $SCRIPTFILE
                        fi
                else
                        echo ERROR: UNSUPPORTED FILE TYPE $CFILE
                fi
        done < $1
        echo Cleaning up destructiveChanges.xml
        xmlstarlet ed -L -s /Package -t elem -n version -v "48.0" $SCRIPTFILE
        xmlstarlet ed -L -i /Package -t attr -n xmlns -v "http://soap.sforce.com/2006/04/metadata" $SCRIPTFILE
        echo ====FINAL DESTRUCTIVE PACKAGE.XML=====
        cat $SCRIPTFILE
else
echo Change file not found!
fi