#!/bin/sh

# self delete this script after execution
rm -- "$0"

clear
echo "What is the name of your app: "
APP_NAME=$(gum input --placeholder "" | tr '[:upper:]' '[:lower:]')

mv ./api/src/main/java/org/bugzkit ./api/src/main/java/org/$APP_NAME
mv ./api/src/test/java/org/bugzkit ./api/src/test/java/org/$APP_NAME

clear
if ! gum confirm "Do you want to keep the docs?"; then
  rm -rf ./docs
  rm -rf .github/workflows/docs.yml
  sed -i '5d' README.md
fi

# replace all occurrences of bugzkit in all files
find . \
  -type f \
  -not -path "./node_modules/*" \
  -not -path "./target/*" \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.svelte-kit/*" \
  -exec sed -i "s/bugzkit/${APP_NAME}/Ig" {} + > /dev/null 2>&1

clear
echo "Name of your app is: $APP_NAME"
