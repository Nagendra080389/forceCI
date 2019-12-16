#!/bin/bash
echo $1
echo $2
echo $3
echo $4

echo 'Starting git clone'
rpm -i xmlstarlet-1.3.0-1.fc16.i686.rpm
cd $1 || exit

git config --global user.email $3
git config --global user.name $4
git clone $2 .
