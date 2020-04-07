#!/bin/bash

echo 'Start Cherry Pick'

cd $5 || exit

echo 'Switched to '$5
git clone $1 .
git checkout -f origin/$2
git checkout -b $3
git cherry-pick $4
git push origin HEAD