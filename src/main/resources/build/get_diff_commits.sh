#!/bin/bash
export LESSCHARSET=utf-8

echo 'Starting git diff'
cd ..
git diff --name-status $1^ $1 > diff.txt
echo 'Git diff done.'