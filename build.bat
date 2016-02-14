if exist .\build rmdir /Q /S .\build

mkdir .\build

javac -d .\build  -cp "dependencies/OSBot 2.4.39.jar;" catherby_fisher\src\CatherbyFisher.java
jar cfe build\CatherbyFisher.jar CatherbyFisher -C .\build CatherbyFisher.class -C .\build CatherbyFisher$State.class -C .\build CatherbyFisher$1.class

del .\build\*.class