#!/bin/bash
echo $1
echo $2

echo 'Starting git clone'
cd $1
git clone $2 .
