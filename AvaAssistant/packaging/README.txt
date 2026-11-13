AVAASSISTANT - EMPACOTAMENTO WINDOWS (PRIMEIRA ETAPA)

Objetivo
- A interface JavaFX e o backend Spring Boot iniciam no mesmo processo.
- O backend e encerrado quando a janela do aplicativo fecha.
- O Tesseract pode ser localizado em uma pasta distribuida junto ao aplicativo.

Preparacao do ambiente de build
- Windows 10/11 64-bit
- JDK 21 completo (incluindo jpackage)
- Maven instalado e disponivel no PATH
- WiX Toolset 3.x instalado e disponivel no PATH para gerar instalador .exe com jpackage

Gerar
- Execute packaging\build-windows-installer.bat com duplo clique.
- Saida esperada: target\installer-output\AvaAssistant-0.1.0.exe

Dependencias nativas offline
- Copie a distribuicao Windows do Ollama para packaging\ollama\ (incluindo ollama.exe).
- Copie o Tesseract para packaging\tesseract\, de modo que exista:
  packaging\tesseract\tesseract.exe
  packaging\tesseract\tessdata\por.traineddata
  packaging\tesseract\tessdata\eng.traineddata
- O modelo Gemma 3 4B ainda precisa ser exportado/fornecido no formato de armazenamento do Ollama.
  Nao basta copiar um unico arquivo GGUF: o Ollama usa manifests e blobs no seu model store.
- Esta etapa de build ainda nao copia/configura automaticamente o model store nem inicia o Ollama.
  Isso sera a etapa seguinte, apos validar o launcher JavaFX + Spring no Windows.

Observacao
- O instalador gerado pelo jpackage inclui o runtime Java, mas esta primeira versao nao deve
  ser anunciada como pacote offline completo enquanto Ollama, Gemma e Tesseract nao estiverem
  incluidos e testados em uma maquina limpa.
