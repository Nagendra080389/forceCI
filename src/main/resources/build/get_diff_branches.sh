#!/bin/bash
export LESSCHARSET=utf-8

targetBranch=$2
echo $targetBranch

echo 'Starting git diff'
cd ..
echo 'Switching to origin/'$targetBranch
git checkout -f origin/$targetBranch
echo 'Merging target branch ('$1') into current branch ('origin/$targetBranch')'
git merge --no-commit --no-ff $1
echo 'Merged. Looking for conflicts'
git ls-files -u | awk '{$1=$2=$3=""; print $0}' | awk '{ sub(/^[ \t]+/, ""); print }' | sort -u > conflicts.txt
if [ -s conflicts.txt ]
then
    echo 'Conflicts found. Please resolve conflicts prior validation...'
    echo '============================================================='
    echo -e '\n\nConflicting files:\n'
    cat conflicts.txt
    exit 1
else
    echo 'Starting git diff'
    git status -s > diff.txt
    echo 'Git diff done.'
    echo 'Clean double quotes'
    sed -i 's/\"//g' diff.txt
    echo 'Cleaned. Result:'
    cat diff.txt
fi