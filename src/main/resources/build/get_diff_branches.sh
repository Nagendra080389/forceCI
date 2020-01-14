#!/bin/bash
export LESSCHARSET=utf-8
echo $1
echo $2
echo $3
echo $4

echo 'Starting git diff'
cd $4
echo 'Switching to origin/'$1
git checkout -f origin/$1
echo 'Merging target branch (origin/'$3') into current branch (origin/'$1')'
git merge --no-commit --no-ff origin/$3
echo 'Merged. Looking for conflicts'
git ls-files -u | awk '{$1=$2=$3=""; print $0}' | awk '{ sub(/^[ \t]+/, ""); print }' | sort -u >conflicts.txt
if [ -s conflicts.txt ]; then
  echo 'Conflicts found. Please resolve conflicts prior validation...'
  echo '============================================================='
  echo -e '\n\nConflicting files:\n'
  cat conflicts.txt
  exit 1
else
  echo 'Starting git diff'
  git diff origin/$1 origin/$3 >diffPatch.patch
  git status -s >diff.txt
  echo 'Git diff done.'
  echo 'Clean double quotes'
  sed -i 's/\"//g' diff.txt
  echo 'Cleaned. Result:'
  cat diff.txt
fi
