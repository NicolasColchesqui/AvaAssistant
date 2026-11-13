
package slime.AvaAssistant.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TesseractService {


    public String extrairTexto(Path imagem) {

        try {
            // Primeira tentativa: reconhecimento em bloco de texto.
            String resultadoBloco = executarTesseract(imagem, 6);

            // Segunda tentativa: reconhecimento de textos em regiões distintas.
            String resultadoRegioes = executarTesseract(imagem, 11);

            // Escolhe o resultado que conseguiu reconhecer mais conteúdo.
            String melhorResultado = resultadoBloco.length()
                    >= resultadoRegioes.length()
                    ? resultadoBloco
                    : resultadoRegioes;

            if (melhorResultado.isBlank()) {
                throw new RuntimeException(
                        "Nenhum texto foi reconhecido na imagem."
                );
            }

            System.out.println("=== TEXTO RECONHECIDO PELO OCR ===");
            System.out.println(melhorResultado);
            System.out.println("=================================");

            return melhorResultado;

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao reconhecer texto: " + erro.getMessage(),
                    erro
            );
        }
    }

    private String executarTesseract(
            Path imagem,
            int modo
    ) throws Exception {

        Path bundledExecutable = AppRuntimePaths.bundled("tesseract/tesseract.exe");
        String executable = Files.isRegularFile(bundledExecutable)
                ? bundledExecutable.toString()
                : "C:\\Program Files\\Tesseract-OCR\\tesseract.exe";

        ProcessBuilder builder = new ProcessBuilder(
                executable,
                imagem.toAbsolutePath().toString(),
                "stdout",
                "-l",
                "por+eng",
                "--oem",
                "1",
                "--psm",
                String.valueOf(modo),
                "--dpi",
                "300",
                "-c",
                "preserve_interword_spaces=1"
        );

        // Usa o diretório de idiomas distribuído com o aplicativo, se existir.
        Path bundledTessdata = AppRuntimePaths.bundled("tesseract/tessdata");
        if (Files.isDirectory(bundledTessdata)) {
            builder.environment().put("TESSDATA_PREFIX", bundledTessdata.getParent().toString());
        }

        // Separa mensagens internas do texto reconhecido.
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);

        Process processo = builder.start();

        StringBuilder resultado = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        processo.getInputStream(),
                        StandardCharsets.UTF_8
                ))) {

            String linha;

            while ((linha = reader.readLine()) != null) {
                resultado.append(linha).append("\n");
            }
        }

        int codigo = processo.waitFor();

        if (codigo != 0) {
            throw new RuntimeException(
                    "Tesseract falhou no modo " + modo
            );
        }

        return resultado.toString().trim();
    }
}
