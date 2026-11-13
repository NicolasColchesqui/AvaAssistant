
package slime.AvaAssistant.desktop;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.CompletableFuture;

public class ScreenSelectionWindow {

    public CompletableFuture<Rectangle2D> selecionarRegiao() {

        CompletableFuture<Rectangle2D> resultado =
                new CompletableFuture<>();

        Rectangle2D limites = Screen.getPrimary().getBounds();

        Stage janela = new Stage();
        janela.initStyle(StageStyle.TRANSPARENT);
        janela.setAlwaysOnTop(true);

        Pane painel = new Pane();
        painel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.35);"
        );

        Rectangle selecao = new Rectangle();
        selecao.setFill(Color.web("#FF8C00", 0.20));
        selecao.setStroke(Color.web("#FF8C00"));
        selecao.setStrokeWidth(2);

        painel.getChildren().add(selecao);

        double[] inicio = new double[2];

        painel.setOnMousePressed(event -> {
            inicio[0] = event.getScreenX();
            inicio[1] = event.getScreenY();

            atualizarSelecao(
                    event, inicio, selecao, limites
            );
        });

        painel.setOnMouseDragged(event ->
                atualizarSelecao(
                        event, inicio, selecao, limites
                )
        );

        painel.setOnMouseReleased(event -> {

            atualizarSelecao(
                    event, inicio, selecao, limites
            );

            double x = Math.min(
                    inicio[0], event.getScreenX()
            );

            double y = Math.min(
                    inicio[1], event.getScreenY()
            );

            double largura = Math.abs(
                    event.getScreenX() - inicio[0]
            );

            double altura = Math.abs(
                    event.getScreenY() - inicio[1]
            );

            janela.close();

            if (largura < 5 || altura < 5) {
                resultado.cancel(false);
                return;
            }

            resultado.complete(new Rectangle2D(
                    x, y, largura, altura
            ));
        });

        Scene cena = new Scene(
                painel,
                limites.getWidth(),
                limites.getHeight()
        );

        cena.setFill(Color.TRANSPARENT);

        janela.setX(limites.getMinX());
        janela.setY(limites.getMinY());
        janela.setWidth(limites.getWidth());
        janela.setHeight(limites.getHeight());
        janela.setScene(cena);

        janela.setOnCloseRequest(event ->
                resultado.cancel(false)
        );

        janela.show();

        return resultado;
    }

    private void atualizarSelecao(
            MouseEvent event,
            double[] inicio,
            Rectangle selecao,
            Rectangle2D limites
    ) {
        double atualX = event.getScreenX();
        double atualY = event.getScreenY();

        selecao.setX(
                Math.min(inicio[0], atualX)
                        - limites.getMinX()
        );

        selecao.setY(
                Math.min(inicio[1], atualY)
                        - limites.getMinY()
        );

        selecao.setWidth(
                Math.abs(atualX - inicio[0])
        );

        selecao.setHeight(
                Math.abs(atualY - inicio[1])
        );
    }
}
