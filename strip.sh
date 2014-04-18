#!/bin/bash

for FILE in $(find . -name "*.java")
do
	#echo "cat $FILE > $FILE"
	cat $FILE | awk '{ gsub(/\/\*.*\*\/ /, ""); print }' | awk '{ gsub(/  /, "\t"); print }' | head -n -4 > $FILE.tmp
	rm $FILE
	mv $FILE.tmp $FILE
done
