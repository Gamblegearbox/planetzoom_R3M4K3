# Planet Zoooom - A procedural generated planet

Planet Zoooom is an application that has been developed during a project semester by four students at HTW Berlin. The application is written in Java and has been implemented with [Lightweight Java Game Library - LWJGL 3](www.lwjgl.org).

The projects intention was to generate a fully **procedural** earth-like planet. The application should feature a controllable camera and the generated mesh should adjust it's resolution dynamically - according to the viewers distance.

This repository represents a log of the development process. So you shouldn't search for things like clean code or a lasting architecture. Instead you may use this as an inspiration or as a starting point for your own projects. There are certainly parts that can be improved in many ways. In the current version some implementations are trade offs between architecture and performance due to our limited time and knowledge.

Actual features of the final version:
- procedural mesh generation
- free camera control
- mesh modification via [Perlin Noise](http://www.kenperlin.com)
- real time adjustment of noise parameters and atmosphere shaders
- different surface shaders (incl. wireframe!)
- backface and frustum culling (+ freezable mesh update to analyze it)
- basic collision detection (between camera and planet)
- dynamic mesh resolution (yet very limited)

## How to run the application

### Linux
Just execute "run.sh"

### ROTW
- copy /src/res to /build/res

### Build from console
javac -d ./build -classpath ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar -sourcepath ./src -target 1.8 -g:source,lines,vars -source 1.8 ./src/planetZoooom/*.java

### Start from console
java -Djava.library.path=./libs/lwjgl_3.0.0a/native -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar planetZoooom.Game

HINT: If you have problems starting the application (e.g. under OSX) try the following parameter in Run Configurations -> VM arguments: -XstartOnFirstThread

## Acknowledgment
Special thanks goes to Prof. Dr. Tobias Lenz for his support, patience and for pointing us into the right direction.

In addition we want to thank:
- Stefan Gustavson for his Java implementation of Perlin Noise (http://staffwww.itn.liu.se/~stegu/simplexnoise/SimplexNoise.java)
- The authors of the "Procedural Content Generation in Games" book (http://pcgbook.com)
- Last but not least Ondrej Linda for his bachelor thesis "Generation of planetary models by means of fractcal algorithms" (https://dip.felk.cvut.cz/browse/pdfcache/lindao1_2007bach.pdf)

