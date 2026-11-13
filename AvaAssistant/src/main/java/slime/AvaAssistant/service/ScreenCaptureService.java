
package slime.AvaAssistant.service;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public class ScreenCaptureService {

    private static final int FATOR_AMPLIACAO = 3;

    public Path capturarRegiao(
            int x,
            int y,
            int largura,
            int altura
    ) {

        try {
            if (largura <= 0 || altura <= 0) {
                throw new IllegalArgumentException(
                        "A região selecionada é inválida."
                );
            }

            Robot robot = new Robot();

            Rectangle regiao = new Rectangle(
                    x,
                    y,
                    largura,
                    altura
            );

            BufferedImage imagemOriginal =
                    robot.createScreenCapture(regiao);

            int novaLargura = largura * FATOR_AMPLIACAO;
            int novaAltura = altura * FATOR_AMPLIACAO;

            // Amplia a imagem para facilitar o reconhecimento dos caracteres.
            BufferedImage imagemAmpliada = new BufferedImage(
                    novaLargura,
                    novaAltura,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = imagemAmpliada.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.drawImage(
                    imagemOriginal,
                    0,
                    0,
                    novaLargura,
                    novaAltura,
                    null
            );

            graphics.dispose();

            Path arquivo = Files.createTempFile(
                    "avaassistant-",
                    ".png"
            );

            boolean salvou = ImageIO.write(
                    imagemAmpliada,
                    "png",
                    arquivo.toFile()
            );

            if (!salvou) {
                throw new RuntimeException(
                        "Não foi possível salvar a imagem."
                );
            }

            return arquivo;

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao capturar a tela: " + erro.getMessage(),
                    erro
            );
        }
    }
}
