#!/bin/bash
export LESSCHARSET=utf-8

echo 'Starting git diff'
echo $4
echo $1

cd $4
git diff --name-status $1^ $1 > diff.txt
echo 'Git diff done.'
