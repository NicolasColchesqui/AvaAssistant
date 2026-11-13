package slime.AvaAssistant.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Starts the local Ollama server when it is not already running. */
public final class OllamaRuntimeManager {

    private static final URI TAGS_URI = URI.create("http://localhost:11434/api/tags");
    private static final URI SHOW_URI = URI.create("http://localhost:11434/api/show");
    private static final String REQUIRED_MODEL = "gemma3:4b";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private Process ollamaProcess;
    private boolean startedByApplication;

    public void startAndValidate() {
        if (!isOllamaAvailable()) {
            startServerProcess();
            waitUntilAvailable();
        } else {
            System.out.println("[AvaAssistant] Ollama já está em execução.");
        }

        if (!isModelInstalled()) {
            stop();
            throw new IllegalStateException(
                    "O modelo " + REQUIRED_MODEL + " não foi encontrado no Ollama. "
                    + "Abra o terminal uma vez e execute: ollama pull " + REQUIRED_MODEL
            );
        }

        System.out.println("[AvaAssistant] Ollama pronto; modelo " + REQUIRED_MODEL + " encontrado.");
    }

    private void startServerProcess() {
        Path executable = findOllamaExecutable();
        if (executable == null) {
            throw new IllegalStateException(
                    "O Ollama não foi encontrado. Instale o Ollama e confirme que "
                    + "'ollama' funciona no terminal do Windows."
            );
        }

        try {
            ollamaProcess = new ProcessBuilder(executable.toString(), "serve")
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            startedByApplication = true;
            System.out.println("[AvaAssistant] Iniciando Ollama: " + executable);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível iniciar o Ollama.", e);
        }
    }

    private Path findOllamaExecutable() {
        String configured = System.getProperty("avaassistant.ollama.path");
        if (configured != null && !configured.isBlank()) {
            Path path = Path.of(configured);
            if (Files.isRegularFile(path)) return path;
        }

        Path bundled = AppRuntimePaths.bundled("ollama/ollama.exe");
        if (Files.isRegularFile(bundled)) return bundled;

        List<Path> candidates = List.of(
                Path.of("C:/Users/" + System.getProperty("user.name") + "/AppData/Local/Programs/Ollama/ollama.exe"),
                Path.of("C:/Program Files/Ollama/ollama.exe")
        );
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) return candidate;
        }

        String pathValue = System.getenv("PATH");
        if (pathValue != null) {
            for (String folder : pathValue.split(java.io.File.pathSeparator)) {
                Path candidate = Path.of(folder).resolve(isWindows() ? "ollama.exe" : "ollama");
                if (Files.isRegularFile(candidate)) return candidate;
            }
        }
        return null;
    }

    private boolean isOllamaAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder(TAGS_URI)
                    .timeout(Duration.ofSeconds(2)).GET().build();
            return HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void waitUntilAvailable() {
        long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        while (System.nanoTime() < deadline) {
            if (ollamaProcess != null && !ollamaProcess.isAlive()) {
                throw new IllegalStateException("Ollama encerrou inesperadamente ao iniciar.");
            }
            if (isOllamaAvailable()) return;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Inicialização do Ollama interrompida.", e);
            }
        }
        throw new IllegalStateException("O Ollama não respondeu em 45 segundos.");
    }

    private boolean isModelInstalled() {
        try {
            String body = "{\"name\":\"" + REQUIRED_MODEL + "\"}";
            HttpRequest request = HttpRequest.newBuilder(SHOW_URI)
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void stop() {
        if (startedByApplication && ollamaProcess != null && ollamaProcess.isAlive()) {
            ollamaProcess.destroy();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
