# Version simple
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
mkdir target\classes -Force

# Lister tous les fichiers Java
Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName } > sources.txt

# Compiler
javac -cp "C:\apache-tomcat-9.0.112\lib\servlet-api.jar" -d "target\classes" -encoding UTF-8 "@sources.txt"

Remove-Item sources.txt

# Créer le JAR
cd target\classes
jar -cvf ..\..\framework.jar *
cd ..\..

Write-Host "framework.jar cree!" -ForegroundColor Green