#!/bin/bash
echo $1
echo $2
echo $3
echo $4
echo $5

echo 'Starting git clone'

cd $1 || exit

echo 'Switched to '$1
ls -a
git config --global user.email $3
git config --global user.name $4
git clone $2 .
git checkout -B $2 origin/$2
