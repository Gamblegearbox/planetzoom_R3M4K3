#!/bin/bash



echo "... deleting old build"
rm -rf ./build

echo "... copying res files into fresh build folder"
mkdir build
cp -r ./src/res ./build/res

echo "... compiling"
javac -d ./build -classpath ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar -sourcepath ./src -target 1.8 -g:source,lines,vars -source 1.8 ./src/planetZoooom/*.java

echo "... starting"
java -Djava.library.path=./libs/lwjgl_3.0.0a/native -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar planetZoooom.Main