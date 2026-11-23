#!/bin/sh
# Renders the artwork the themes load: the sonar drawings in art/ and the sea chart background
# built around them. Requires rsvg-convert (librsvg).
set -e
cd "$(dirname "$0")/.."
mkdir -p src/main/resources/images
for svg in art/*.svg; do
  name=$(basename "$svg" .svg)
  rsvg-convert "$svg" -o "src/main/resources/images/$name.png"
  echo "rendered $name.png"
done
python3 scripts/draw-chart.py
