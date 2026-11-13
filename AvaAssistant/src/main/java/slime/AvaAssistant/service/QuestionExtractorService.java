
package slime.AvaAssistant.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class QuestionExtractorService {

    // Limpa o HTML e prepara o conteúdo para a IA.
    public String prepararConteudoParaIA(String html) {

        Document documento = Jsoup.parse(html);

        // Remove elementos que normalmente não contêm questões.
        documento.select(
                "script, style, noscript, svg, nav, header, footer, " +
                        "aside, iframe, button, input, select, option"
        ).remove();

        // Remove elementos ocultos.
        documento.select(
                "[hidden], [aria-hidden=true], [style*=display:none]"
        ).remove();

        // Recupera o texto da página sem depender de classes CSS.
        String texto = documento.body().text();

        // Limita o tamanho enviado ao Ollama.
        if (texto.length() > 4000) {
            texto = texto.substring(0, 4000);
        }

        return texto;
    }

    // Mantido para facilitar a inspeção durante o desenvolvimento.
    public String inspecionarPagina(String html) {

        Document documento = Jsoup.parse(html);

        return "TÍTULO: " + documento.title()
                + "\n\nCONTEÚDO:\n"
                + prepararConteudoParaIA(html);
    }
}
