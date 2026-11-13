
package slime.AvaAssistant.controller;

import slime.AvaAssistant.dto.PageContentRequest;
import slime.AvaAssistant.dto.QuestionRequest;
import slime.AvaAssistant.service.QuestionService;
import slime.AvaAssistant.service.QuestionExtractorService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/questoes")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionExtractorService extractorService;

    public QuestionController(
            QuestionService questionService,
            QuestionExtractorService extractorService) {

        this.questionService = questionService;
        this.extractorService = extractorService;
    }

    @PostMapping("/pagina")
    public String receberConteudoPagina(
            @RequestBody PageContentRequest pagina) {

        System.out.println("RECEBI UMA REQUISICAO EM /pagina");

        String conteudo = extractorService.prepararConteudoParaIA(
                pagina.getConteudo()
        );

        System.out.println("=== CONTEUDO ENVIADO PARA IA ===");
        System.out.println(conteudo);
        System.out.println("================================");

        String prompt = """
                Você é um assistente especializado em resolver questões
                acadêmicas e técnicas.

                Analise cuidadosamente o enunciado e as alternativas.

                PROCESSO OBRIGATÓRIO:

                1. Identifique exatamente o que a questão solicita.
                2. Resolva a questão antes de escolher uma alternativa.
                3. Compare o resultado obtido com o conteúdo de cada alternativa.
                4. Confira se a letra escolhida corresponde ao texto da alternativa.
                5. Faça uma verificação final da resposta.

                REGRAS IMPORTANTES:

                - Não escolha uma alternativa apenas por sua posição.
                - Não invente valores ou informações ausentes.
                - Não confunda letras com números.
                - Se uma alternativa estiver ilegível, considere isso na análise.
                - Nunca apresente uma letra incompatível com o texto da resposta.
                - Se não for possível determinar a resposta, informe a incerteza.
                - Não invente uma alternativa para preencher uma resposta.
                - Não apresente cálculos ou explicações.

                FORMATO OBRIGATÓRIO:

                Pergunta: [resumo curto do enunciado]

                Resposta: [LETRA] - [texto da alternativa correta]

                Retorne somente os dois campos acima.

                CONTEÚDO CAPTURADO:
                """ + conteudo;

        return questionService.analisarQuestao(prompt);
    }

    @GetMapping("/teste")
    public String testarOllama() {
        return questionService.analisarQuestao(
                "Explique brevemente o que é Java."
        );
    }

    @PostMapping("/inspecionar")
    public String inspecionarPagina(
            @RequestBody PageContentRequest pagina) {

        return extractorService.inspecionarPagina(
                pagina.getConteudo()
        );
    }

    @PostMapping("/analisar")
    public String analisarQuestao(
            @RequestBody QuestionRequest questao) {

        return questionService.analisarQuestao(questao);
    }
}
