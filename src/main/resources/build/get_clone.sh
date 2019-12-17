#!/bin/bash
echo $1
echo $2
echo $3
echo $4

echo 'Starting git clone'

cd $1 || exit

ls ~/data/
rm -rf $1
git config --global user.email $3
git config --global user.name $4
git clone $2 .
