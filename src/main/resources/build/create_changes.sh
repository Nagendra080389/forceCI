#!/bin/bash

echo 'create Changes started'
echo $1
echo $2
echo $3
cd $2
echo 'Starting file diff'
echo 'Creating destructive changes'
sed -n /^D/p diff.txt | sed 's/^..//' | sed /^src/p | sed /^src\\/package\.xml/d > destructive.txt
echo 'Destructive changes created'
echo 'Creating change list'
sed /^D/d diff.txt | sed 's/^..//' | sed /^src/p | sed /^src\\/package\.xml/d | sed 's/.*\ ->\ //' > temp.txt
echo 'Splitting changes'
sed -n '/-meta.xml$/p' temp.txt > meta.txt
sed '/-meta.xml$/d' temp.txt > orig.txt
echo 'Creating pair names'
sed 's/$/-meta.xml/' orig.txt > final.txt
sed 's/.........$//' meta.txt >> final.txt
echo 'Joining changes'
cat temp.txt >> final.txt
echo 'Making unique'
sort -uo final.txt final.txt
echo 'Unique'
cat final.txt
echo 'File diff done.'

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

if [ -e final.txt ]
then
        ARRAY=()
        while read CFILE
            do
                    case "$CFILE"
                    in
                        *.app) TYPENAME="AuraDefinitionBundle";;
                        *.cmp) TYPENAME="AuraDefinitionBundle";;
                        *.css) TYPENAME="AuraDefinitionBundle";;
                        *.evt) TYPENAME="AuraDefinitionBundle";;
                        *.js) TYPENAME="AuraDefinitionBundle";;
                        *.svg) TYPENAME="AuraDefinitionBundle";;
                        *.auradoc) TYPENAME="AuraDefinitionBundle";;
                        *.design) TYPENAME="AuraDefinitionBundle";;
                        *) TYPENAME="UNKNOWN";;
                    esac

                    if [ "$TYPENAME" == "AuraDefinitionBundle" ]
                        then
                                if [ $(contains "${ARRAY[@]}" $(basename -- "$(dirname -- "$CFILE")")) != "y" ]
                                then
                                    CFILENAME="$CFILE"
                                    ARRAY+=($(basename -- "$(dirname -- "$CFILE")"))
                                    replace="src/aura/"$(basename -- "$(dirname -- "$CFILE")")
                                    sed "s|$CFILENAME|$replace|" final.txt >> temp_final.txt
                                    mv -- temp_final.txt final.txt
                                else
                                    continue
                                fi
                        fi
            done < final.txt
else
echo Change file not found!
fi

cat final.txt | xargs -I {} cp -r --parents {} $1
echo 'Changes created.'