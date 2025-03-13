# Planet Zoooom - R3M4K3

10 years ago I was part of the project this one is forked from, we had lot's of ideas we could not fit into the given time frame. Now I found it interesting to revisit the code. Let's see what this leads to on a "every once and a while" base :D

Check out the original: https://github.com/AndreasHennig/planetzoom

## How to run the application

### Linux
Just execute "run.sh"

### ROTW
- copy /src/res to /build/res
- Build from console
```
javac -d ./build -classpath ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar -sourcepath ./src -target 1.8 -g:source,lines,vars -source 1.8 ./src/planetZoooom/*.java
```

- Start from console
```
java -Djava.library.path=./libs/lwjgl_3.0.0a/native -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp ./build:./libs/lwjgl_util.jar:./libs/lwjgl_3.0.0a/jar/lwjgl.jar:./libs/PNGDecoder.jar planetZoooom.PlanetZoooom
```
HINT: If you have problems starting the application (e.g. under OSX) try the following parameter in Run Configurations -> VM arguments: -XstartOnFirstThread
