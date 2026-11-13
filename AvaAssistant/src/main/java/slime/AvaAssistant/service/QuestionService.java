
package slime.AvaAssistant.service;

import slime.AvaAssistant.integration.OllamaRequest;
import slime.AvaAssistant.integration.OllamaResponse;
import slime.AvaAssistant.dto.QuestionRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class QuestionService {

    private static final String MODEL = "gemma3:4b";

    private final RestClient restClient;

    public QuestionService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    // ANALISAR QUESTÃO RECEBIDA COMO TEXTO
    public String analisarQuestao(String questao) {

        if (questao == null || questao.isBlank()) {
            throw new IllegalArgumentException(
                    "A questão enviada está vazia."
            );
        }

        OllamaRequest request = new OllamaRequest();

        request.setModel(MODEL);
        request.setPrompt(questao);
        request.setStream(false);

        // Mantém o modelo carregado por 10 minutos.
        request.setKeep_alive("10m");

        // Configurações para respostas mais consistentes.
        request.setOptions(Map.of(
                "num_predict", 180,
                "temperature", 0.0,
                "top_k", 20,
                "top_p", 0.8,
                "num_ctx", 4096,
                "seed", 42
        ));

        OllamaResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);

        if (response == null || response.getResponse() == null) {
            throw new IllegalStateException(
                    "O Ollama não retornou uma resposta válida."
            );
        }

        return response.getResponse().trim();
    }

    // ANALISAR QUESTÃO E ALTERNATIVAS ESTRUTURADAS
    public String analisarQuestao(QuestionRequest questao) {

        if (questao == null || questao.getEnunciado() == null) {
            throw new IllegalArgumentException(
                    "O enunciado da questão não foi informado."
            );
        }

        if (questao.getAlternativas() == null
                || questao.getAlternativas().isEmpty()) {
            throw new IllegalArgumentException(
                    "A questão não possui alternativas."
            );
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Você é um assistente especializado em resolver
                questões acadêmicas e técnicas.

                Sua prioridade é fornecer respostas corretas,
                coerentes e compatíveis com as alternativas.

                ETAPAS OBRIGATÓRIAS:

                1. Interprete o enunciado.
                2. Resolva a questão independentemente das alternativas.
                3. Analise individualmente todas as alternativas.
                4. Compare o resultado obtido com cada alternativa.
                5. Identifique a alternativa que corresponde ao resultado.
                6. Confira novamente a letra e o conteúdo selecionados.

                REGRAS:

                - Não escolha uma alternativa por suposição.
                - Não confunda o número da alternativa com seu conteúdo.
                - Não altere as letras fornecidas.
                - Não invente valores.
                - Se nenhuma alternativa corresponder ao resultado,
                  informe que não encontrou uma opção compatível.
                - Se não houver informações suficientes, informe a incerteza.
                - A letra e o texto da resposta devem corresponder
                  exatamente à mesma alternativa.

                ENUNCIADO:
                """);

        prompt.append(questao.getEnunciado())
                .append("\n\n");

        prompt.append("ALTERNATIVAS:\n");

        questao.getAlternativas().forEach((letra, alternativa) -> {
            prompt.append(letra)
                    .append(") ")
                    .append(alternativa)
                    .append("\n");
        });

        prompt.append("""

                FORMATO OBRIGATÓRIO:

                RESPOSTA:
                [LETRA] - [Texto da alternativa correta]

                RESUMO:
                [Explique brevemente por que a alternativa está correta.]

                VERIFICAÇÃO FINAL:
                Antes de responder, confirme que a letra escolhida
                corresponde ao texto da alternativa e ao resultado obtido.

                Não apresente o processo de raciocínio.
                Seja objetivo.
                """);

        return analisarQuestao(prompt.toString());
    }
}
