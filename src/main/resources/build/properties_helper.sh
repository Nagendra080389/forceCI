#!/usr/bin/env bash
#/bin/sh

file="./env/$1.properties"

if [ -f "$file" ]
then
  echo "$file found."

  while IFS='=' read -r key value || [ -n "$key" ]
  do
    key=$(echo $key | tr '.' '_')
    eval "export ${key}='${value}'"
    export KEYCHAIN+=',${'$key'}'
  done < "$file"

  KEYCHAIN=${KEYCHAIN:1:${#KEYCHAIN}}

  while read -rd $'\0' f; do
    envsubst "$KEYCHAIN" < "$f" > "$f.tmp"
    cat "$f.tmp" > "$f"
    rm "$f.tmp"
  done < <(find $2 -type f ! \( -name "*-meta.xml" -o -name "package.xml" \) -print0)

else
  echo "$file not found."
fi