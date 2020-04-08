#!/bin/bash

echo 'Start Cherry Pick'

cd $5 || exit

echo 'Switched to '$5
git config --global user.email $6
git config --global user.name $7
git clone $1 .
if [ "$?" -eq "0" ]; then
  git checkout -B $2 origin/$2
    if [ "$?" -eq "0" ]; then
      git checkout -b $3
      if [ "$?" -eq "0" ]; then
        git cherry-pick $4
        if [ "$?" -eq "0" ]; then
          git push origin HEAD
            if [ "$?" -eq "0" ]; then
              echo "**** SUCCESS ****"
            else
              echo "**** GIT PUSH FAILED ****"
            fi
        else
          echo "**** GIT CHERRY PICK ****"
        fi
      else
        echo "**** GIT CREATION OF NEW BRANCH $3 FAIL ****"
      fi
    else
      echo "**** GIT CHECKOUT OF $2 FAIL ****"
    fi
fi