#!/bin/sh
TEMPLATE_DIR="$(dirname "$0")"
for file in "$TEMPLATE_DIR"/*.mjml; do
  if [ -f "$file" ]; then
    output="${file%.mjml}.html"
    echo "Converting $file to $output"
    npx mjml "$file" -o "$output"
  fi
done
