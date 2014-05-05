#!/bin/bash

cwd=`pwd`

# LyX --> LaTeX
find . -type f -name \*.lyx -exec "$1" --export latex {} \;

# LaTeX & BibTeX --> XML
latexml --destination=references.bib.xml references.bib
find . -type f -name \*.tex -exec latexml --destination={}.xml {} \;

# XML --> HTML & PNG
find . -type f -name \*.tex.xml -exec latexmlpost --sitedirectory="$cwd" --format=html4 --bibliography="$cwd"/references.bib.xml --css="$cwd"/customRules.css --destination={}.html {} \;

# remove all the generated files that are no longer needed
rm references.bib.xml
find . -type f -name LaTeXML.cache -exec rm {} \;
find . -type f -name \*.tex -exec rm {} \;
find . -type f -name \*.tex.xml -exec rm {} \;

# rename the generated html files
for file in $(find . -type f -name \*.tex.xml.html); do mv ${file} ${file%.tex.xml.html}.html; done
