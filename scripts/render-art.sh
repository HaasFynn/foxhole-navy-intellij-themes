#!/bin/sh
# Renders the SVG artwork in art/ to the PNG files the themes load. Requires rsvg-convert (librsvg).
set -e
cd "$(dirname "$0")/.."
mkdir -p src/main/resources/images
for svg in art/*.svg; do
  name=$(basename "$svg" .svg)
  rsvg-convert "$svg" -o "src/main/resources/images/$name.png"
  echo "rendered $name.png"
done
