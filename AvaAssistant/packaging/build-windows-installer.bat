@echo off
setlocal
cd /d "%~dp0.."

echo [1/4] Verificando ferramentas...
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERRO: Maven nao encontrado. Instale Maven e JDK 21 para gerar o instalador.
  pause
  exit /b 1
)
where jpackage >nul 2>nul
if errorlevel 1 (
  echo ERRO: jpackage nao encontrado. Use um JDK 21 completo, nao apenas JRE.
  pause
  exit /b 1
)

if not exist packaging\resources mkdir packaging\resources
if not exist packaging\resources\AvaAssistant.ico (
  echo AVISO: packaging\resources\AvaAssistant.ico nao existe. O instalador sera gerado sem icone customizado.
)

echo [2/4] Compilando o aplicativo...
call mvn -DskipTests clean package
if errorlevel 1 goto :failed

echo [3/4] Copiando dependencias...
if exist target\installer-input rmdir /s /q target\installer-input
mkdir target\installer-input
jar --create --file target\installer-input\AvaAssistant-desktop.jar -C target\classes .
if errorlevel 1 goto :failed
call mvn -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target\installer-input\lib
if errorlevel 1 goto :failed

REM Bundled native tools are optional during the first packaging pass.
REM Place the Windows Ollama distribution in packaging\ollama and Tesseract in packaging\tesseract.
if exist packaging\ollama xcopy packaging\ollama target\installer-input\ollama /E /I /Y >nul
if exist packaging\tesseract xcopy packaging\tesseract target\installer-input\tesseract /E /I /Y >nul

if not exist target\installer-output mkdir target\installer-output

echo [4/4] Gerando instalador Windows...
set ICON_ARG=
if exist packaging\resources\AvaAssistant.ico set ICON_ARG=--icon packaging\resources\AvaAssistant.ico
jpackage --type exe --name AvaAssistant --app-version 0.1.0 --vendor SlimeDev --input target\installer-input --main-jar AvaAssistant-desktop.jar --main-class slime.AvaAssistant.desktop.AvaAssistantDesktopLauncher --class-path "lib/*" --dest target\installer-output --win-menu --win-shortcut --win-dir-chooser %ICON_ARG%
if errorlevel 1 goto :failed

echo.
echo Instalador gerado em target\installer-output
 echo ATENCAO: esta primeira compilacao ainda nao inclui Ollama/modelo/Tesseract, a menos que as pastas packaging correspondentes tenham sido preenchidas.
pause
exit /b 0

:failed
echo.
echo Falha durante a geracao. Confira as mensagens acima.
pause
exit /b 1
