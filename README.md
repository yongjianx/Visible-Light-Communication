# Visible-Light-Communication
first: 请先在根目录下的.gitignore(不是app/.gitigore)配置如下\
\# Gradel directory\
.gradle\
/build

\# Files for the ART/Dalvik VM\
*.dex

\# built application files\
*.apk\
*.ap_

\# Java class files\
*.class

\# Local configuration file(sdk path, etc)\
/local.properties

\# idea files\
/.idea/libraries\
/.idea/workspace.xml\
/.idea/gradle.xml\
/.idea/misc.xml\
.idea

\# OSX files\
.DS_Store

\# intellij\
*.iml

\# android studio captures folder\
/captures

\#External native build folder generate in android studio 2.2 and later\
.externalNativeBuild

\# Generated files\
bin/\
gen/\
out/

and then clean the cached files it generates automatically\
git rm -r --cached .\
git add .

finally\
git commit -m ".gitignore works"


