
package slime.AvaAssistant.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import slime.AvaAssistant.service.ScreenCaptureService;
import slime.AvaAssistant.service.TesseractService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AvaAssistantApp extends Application {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ScreenSelectionWindow screenSelectionWindow =
            new ScreenSelectionWindow();

    private final ScreenCaptureService screenCaptureService =
            new ScreenCaptureService();

    private final TesseractService tesseractService =
            new TesseractService();

    private TextArea campoQuestao;
    private Label perguntaResultado;
    private Label respostaResultado;
    private Label resumoResultado;
    private Label status;
    private Button botaoAnalisar;
    private Button botaoCapturar;

    private final ObservableList<HistoricoItem> historico =
            FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {

        // CABEÇALHO COMPACTO
        Label logo = new Label("✦");
        logo.setStyle("""
                -fx-background-color: #F07828;
                -fx-text-fill: #111214;
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-padding: 4 8;
                -fx-background-radius: 7;
                """);

        Label titulo = criarTitulo("AvaAssistant", 14);

        Label tokens = new Label("100 tokens");
        tokens.setStyle("""
                -fx-text-fill: #00C66A;
                -fx-background-color: #102A20;
                -fx-font-size: 10px;
                -fx-font-weight: bold;
                -fx-padding: 4 8;
                -fx-background-radius: 12;
                """);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox cabecalho = new HBox(
                8, logo, titulo, espaco, tokens
        );

        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(5, 0, 6, 0));

        // NOVA QUESTÃO
        Label tituloNovaQuestao =
                criarTitulo("Nova questão", 16);

        Label tituloCaptura = criarTitulo(
                "⌗  Capturar questão na tela", 12
        );

        botaoCapturar = new Button(
                "Selecionar região da tela"
        );

        botaoCapturar.setMaxWidth(Double.MAX_VALUE);
        botaoCapturar.setPrefHeight(30);
        botaoCapturar.setStyle(estiloBotaoPrincipal());

        botaoCapturar.setOnAction(event -> capturarQuestao());

        Label instrucao = new Label(
                "Ou cole o texto da questão abaixo."
        );

        instrucao.setStyle("""
                -fx-text-fill: #999999;
                -fx-font-size: 10px;
                """);

        campoQuestao = new TextArea();
        campoQuestao.setPromptText(
                "Cole o enunciado e as alternativas..."
        );

        campoQuestao.setWrapText(true);
        campoQuestao.setPrefHeight(45);
        campoQuestao.setMinHeight(45);
        campoQuestao.setMaxHeight(65);
        campoQuestao.setStyle(estiloCampo());

        botaoAnalisar = new Button("Analisar questão");
        botaoAnalisar.setMaxWidth(Double.MAX_VALUE);
        botaoAnalisar.setPrefHeight(30);
        botaoAnalisar.setStyle(estiloBotaoPrincipal());
        botaoAnalisar.setOnAction(event -> analisarQuestao());

        VBox painelEntrada = new VBox(
                6,
                tituloCaptura,
                botaoCapturar,
                instrucao,
                campoQuestao,
                botaoAnalisar
        );

        painelEntrada.setPadding(new Insets(10));
        painelEntrada.setStyle("""
                -fx-background-color: #111214;
                -fx-border-color: #383838;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);

        // RESULTADO COMPACTO
        Label tituloResultado = criarTitulo("Resultado", 15);

        Label labelPergunta = criarTitulo(
                "Pergunta identificada", 12
        );

        perguntaResultado = criarTextoResultado(
                "A pergunta aparecerá aqui."
        );

        respostaResultado = new Label(
                "A resposta aparecerá aqui."
        );

        respostaResultado.setWrapText(true);
        respostaResultado.setStyle("""
                -fx-text-fill: #F07828;
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                """);

        Label labelResumo = criarTitulo("Resumo", 12);

        resumoResultado = criarTextoResultado(
                "O resumo aparecerá aqui."
        );

        VBox painelResultado = new VBox(
                6,
                labelPergunta,
                perguntaResultado,
                criarDivisor(),
                respostaResultado,
                criarDivisor(),
                labelResumo,
                resumoResultado
        );

        painelResultado.setPadding(new Insets(12));
        painelResultado.setStyle("""
                -fx-background-color: #1D1E21;
                -fx-background-radius: 9;
                """);

        // HISTÓRICO COMPACTO
        Label tituloHistorico = criarTitulo(
                "Histórico de perguntas", 13
        );

        ListView<HistoricoItem> listaHistorico =
                new ListView<>(historico);

        listaHistorico.setPrefHeight(85);
        listaHistorico.setMinHeight(65);
        listaHistorico.setMaxHeight(120);

        listaHistorico.setStyle("""
                -fx-background-color: #111214;
                -fx-control-inner-background: #111214;
                -fx-border-color: transparent;
                -fx-font-size: 11px;
                """);

        listaHistorico.setCellFactory(lista -> new ListCell<>() {

            @Override
            protected void updateItem(
                    HistoricoItem item,
                    boolean vazio) {

                super.updateItem(item, vazio);

                if (vazio || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #111214;");
                } else {
                    Label icone = new Label("◴");

                    icone.setStyle("""
                            -fx-text-fill: #F07828;
                            -fx-font-size: 12px;
                            """);

                    Label texto = new Label(item.titulo());

                    texto.setStyle("""
                            -fx-text-fill: white;
                            -fx-font-size: 11px;
                            """);

                    texto.setMaxWidth(390);
                    texto.setEllipsisString("...");

                    HBox linha = new HBox(7, icone, texto);
                    linha.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(linha);
                    setStyle("""
                            -fx-background-color: #111214;
                            -fx-padding: 3 0;
                            """);
                }
            }
        });

        listaHistorico.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, selecionada) -> {

                    if (selecionada != null) {
                        exibirResultado(
                                selecionada.pergunta(),
                                selecionada.resposta(),
                                selecionada.resumo()
                        );

                        status.setText(
                                "Análise recuperada do histórico."
                        );
                    }
                });

        VBox painelHistorico = new VBox(
                6,
                tituloHistorico,
                listaHistorico
        );

        painelHistorico.setPadding(
                new Insets(6, 0, 6, 0)
        );

        // CONTEÚDO EM UMA COLUNA
        VBox conteudo = new VBox(
                8,
                cabecalho,
                criarDivisor(),
                tituloNovaQuestao,
                painelEntrada,
                tituloResultado,
                painelResultado,
                criarDivisor(),
                painelHistorico
        );

        conteudo.setPadding(
                new Insets(8, 12, 6, 12)
        );

        conteudo.setMaxWidth(520);

        StackPane centralizador = new StackPane(conteudo);
        centralizador.setAlignment(Pos.TOP_CENTER);
        centralizador.setStyle(
                "-fx-background-color: #111214;"
        );

        ScrollPane rolagem = new ScrollPane(centralizador);
        rolagem.setFitToWidth(true);

        rolagem.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        rolagem.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        rolagem.setStyle("""
                -fx-background: #111214;
                -fx-background-color: #111214;
                -fx-border-color: transparent;
                """);

        // RODAPÉ
        status = new Label("AvaAssistant pronto.");

        status.setStyle("""
                -fx-text-fill: #999999;
                -fx-font-size: 10px;
                """);

        status.setMaxWidth(Double.MAX_VALUE);

        HBox rodape = new HBox(status);
        rodape.setPadding(
                new Insets(5, 12, 8, 12)
        );

        // JANELA PEQUENA
        BorderPane layout = new BorderPane();
        layout.setCenter(rolagem);
        layout.setBottom(rodape);

        layout.setStyle(
                "-fx-background-color: #111214;"
        );

        Scene scene = new Scene(layout, 520, 540);

        stage.setTitle("AvaAssistant");
        stage.setMinWidth(480);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.show();
    }

    // CAPTURA DE TELA E OCR
    private void capturarQuestao() {

        botaoCapturar.setDisable(true);
        status.setText("Selecione a região da questão...");

        try {
            screenSelectionWindow.selecionarRegiao()
                    .thenApplyAsync(regiao -> {

                        try {
                            Path imagem = screenCaptureService.capturarRegiao(
                                    (int) Math.round(regiao.getMinX()),
                                    (int) Math.round(regiao.getMinY()),
                                    (int) Math.round(regiao.getWidth()),
                                    (int) Math.round(regiao.getHeight())
                            );

                            try {
                                return tesseractService.extrairTexto(imagem);
                            } finally {
                                Files.deleteIfExists(imagem);
                            }

                        } catch (Exception erro) {
                            throw new RuntimeException(
                                    "Erro no processamento da imagem: "
                                            + erro.getMessage(),
                                    erro
                            );
                        }
                    })
                    .whenComplete((texto, erro) -> {

                        Platform.runLater(() -> {

                            botaoCapturar.setDisable(false);

                            if (erro != null) {

                                Throwable causa = erro;

                                while (causa.getCause() != null) {
                                    causa = causa.getCause();
                                }

                                status.setText(
                                        "Erro na captura: "
                                                + causa.getMessage()
                                );

                                return;
                            }

                            if (texto == null || texto.isBlank()) {
                                status.setText(
                                        "Nenhum texto identificado na região."
                                );
                                return;
                            }

                            campoQuestao.setText(texto);

                            status.setText(
                                    "Questão capturada com sucesso!"
                            );
                        });
                    });

        } catch (Exception erro) {

            botaoCapturar.setDisable(false);

            status.setText(
                    "Erro ao abrir seleção: " + erro.getMessage()
            );
        }
    }

    // COMUNICAÇÃO COM O BACKEND
    private void analisarQuestao() {

        String conteudo = campoQuestao.getText().trim();

        if (conteudo.isEmpty()) {
            status.setText(
                    "Cole uma questão antes de analisar."
            );
            return;
        }

        botaoAnalisar.setDisable(true);
        status.setText("Analisando questão...");

        String json = "{\"conteudo\":\""
                + escaparJson(conteudo)
                + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/questoes/pagina"
                ))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        CompletableFuture.runAsync(() -> {

            try {
                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {

                    botaoAnalisar.setDisable(false);

                    if (response.statusCode() >= 200
                            && response.statusCode() < 300) {

                        String retorno = response.body();

                        String pergunta = extrairCampo(
                                retorno,
                                "PERGUNTA:",
                                "RESPOSTA:"
                        );

                        String resposta = extrairCampo(
                                retorno,
                                "RESPOSTA:",
                                "RESUMO:"
                        );

                        String resumo = extrairCampo(
                                retorno,
                                "RESUMO:",
                                null
                        );

                        exibirResultado(
                                pergunta,
                                resposta,
                                resumo
                        );

                        String titulo = pergunta.isBlank()
                                ? limitarTexto(conteudo, 45)
                                : limitarTexto(pergunta, 45);

                        historico.add(0, new HistoricoItem(
                                titulo,
                                pergunta,
                                resposta,
                                resumo
                        ));

                        if (historico.size() > 5) {
                            historico.remove(5);
                        }

                        status.setText("Análise concluída.");

                    } else {
                        status.setText(
                                "Erro HTTP " + response.statusCode()
                        );
                    }
                });

            } catch (Exception erro) {

                Platform.runLater(() -> {
                    botaoAnalisar.setDisable(false);

                    status.setText(
                            "Erro de conexão: " + erro.getMessage()
                    );
                });
            }
        });
    }

    // EXIBIR RESULTADO
    private void exibirResultado(
            String pergunta,
            String resposta,
            String resumo) {

        perguntaResultado.setText(
                pergunta.isBlank()
                        ? "Pergunta não identificada."
                        : pergunta
        );

        respostaResultado.setText(
                resposta.isBlank()
                        ? "Resposta não identificada."
                        : resposta
        );

        resumoResultado.setText(
                resumo.isBlank()
                        ? "Resumo não disponível."
                        : resumo
        );
    }

    // EXTRAIR CAMPOS DA IA
    private String extrairCampo(
            String texto,
            String campo,
            String proximoCampo) {

        String textoMaiusculo =
                texto.toUpperCase(Locale.ROOT);

        int inicio = textoMaiusculo.indexOf(campo);

        if (inicio == -1) {
            return "";
        }

        inicio += campo.length();

        int fim = proximoCampo == null
                ? texto.length()
                : textoMaiusculo.indexOf(
                proximoCampo, inicio
        );

        if (fim == -1) {
            fim = texto.length();
        }

        return texto.substring(inicio, fim).trim();
    }

    private String limitarTexto(String texto, int limite) {
        if (texto.length() <= limite) {
            return texto;
        }

        return texto.substring(0, limite - 3) + "...";
    }

    private String escaparJson(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // COMPONENTES VISUAIS
    private Label criarTitulo(String texto, int tamanho) {
        Label label = new Label(texto);

        label.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: %dpx;
                -fx-font-weight: bold;
                """.formatted(tamanho));

        return label;
    }

    private Label criarTextoResultado(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);

        label.setStyle("""
                -fx-text-fill: #C5C5C5;
                -fx-font-size: 11px;
                """);

        return label;
    }

    private Region criarDivisor() {
        Region divisor = new Region();
        divisor.setPrefHeight(1);
        divisor.setMinHeight(1);
        divisor.setMaxHeight(1);

        divisor.setStyle(
                "-fx-background-color: #383838;"
        );

        return divisor;
    }

    private String estiloCampo() {
        return """
                -fx-control-inner-background: #1A1B1E;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #777777;
                -fx-highlight-fill: #F07828;
                -fx-background-color: #1A1B1E;
                -fx-border-color: transparent;
                -fx-background-radius: 7;
                -fx-font-size: 11px;
                """;
    }

    private String estiloBotaoPrincipal() {
        return """
                -fx-background-color: #F07828;
                -fx-text-fill: #111214;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-background-radius: 7;
                -fx-cursor: hand;
                """;
    }

    private record HistoricoItem(
            String titulo,
            String pergunta,
            String resposta,
            String resumo
    ) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
