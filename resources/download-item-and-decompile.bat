@echo off
echo Downloading
curl http://game.havenandhearth.com/res/%1 -o compiled/res/%1
echo Decompilling
decompile-item %1