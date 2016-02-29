if exist .\build rmdir /Q /S .\build

mkdir .\build

javac -d .\build  -cp "dependencies/*;" catherby_fisher\CatherbyFisher.java shared\XPReporter.java shared\SkillUtils.java
jar cfe build\CatherbyFisher.jar CatherbyFisher -C .\build CatherbyFisher.class -C .\build CatherbyFisher$State.class -C .\build CatherbyFisher$1.class -C .\build SkillUtils.class -C .\build SkillUtils$1.class -C .\build SkillUtils$2.class -C .\build XPReporter.class

javac -d .\build -cp "dependencies/*;" herb_cleaner\HerbCleaner.java shared\XPReporter.java shared\SkillUtils.java
jar cfe build\HerbCleaner.jar HerbCleaner -C .\build HerbCleaner.class -C .\build HerbCleaner$State.class -C .\build HerbCleaner$1.class -C .\build SkillUtils.class -C .\build SkillUtils$1.class -C .\build SkillUtils$2.class -C .\build XPReporter.class

del .\build\*.class