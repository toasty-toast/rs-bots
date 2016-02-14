if exist .\build rmdir /Q /S .\build

mkdir .\build

javac -d .\build  -cp "dependencies/OSBot 2.4.39.jar;" catherby_fisher\src\CatherbyFisher.java
jar cfe build\CatherbyFisher.jar CatherbyFisher -C .\build CatherbyFisher.class -C .\build CatherbyFisher$State.class -C .\build CatherbyFisher$1.class

javac -d .\build -cp "dependencies/OSBot 2.4.39.jar;" herb_cleaner\src\HerbCleaner.java
jar cfe build\HerbCleaner.jar HerbCleaner -C .\build HerbCleaner.class -C .\build HerbCleaner$State.class -C .\build HerbCleaner$1.class

del .\build\*.class