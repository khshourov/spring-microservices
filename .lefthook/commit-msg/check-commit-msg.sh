#!/bin/bash

# Read the commit message file
commit_msg_file="$1"
commit_msg=$(cat "$commit_msg_file")

# Define the regex pattern for valid commit messages
pattern="^C[0-9][0-9]: .+$"

# Validate the commit message
if [[ ! $commit_msg =~ $pattern ]]; then
  echo "❌ Commit message must match the pattern 'CXX: <message>' where X is a digit (0-9)."
  echo "Example: 'C03: Add initial product landscape'"
  exit 1
fi