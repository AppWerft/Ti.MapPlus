#!/bin/bash

APPID=ti.map
VERSION=3.0.1

#cd android; ant clean; rm -rf /dist/*;rm -rf build/*; ant ;unzip -uo  dist/$APPID-android-$VERSION.zip  -d  ~/Documents/APPC_WORKSPACE/FlicTest/; cd  ..

cd android/; appc ti build -b; rm -rf /dist/*;rm -rf build/*; mkdir build; mkdir build/docs;ant -v;rm -rf  ~/Documents/MLearning/Baumkataster/modules/android/ti.map/*; unzip -uo  dist/$APPID-android-$VERSION.zip  -d  ~/Documents/MLearning/Baumkataster/; cd  ..

