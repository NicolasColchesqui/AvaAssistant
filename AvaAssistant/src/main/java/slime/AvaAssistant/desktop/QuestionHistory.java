
package slime.AvaAssistant.desktop;

public class QuestionHistory {

    private final String titulo;
    private final String pergunta;
    private final String resposta;
    private final String resumo;

    public QuestionHistory(
            String titulo,
            String pergunta,
            String resposta,
            String resumo) {

        this.titulo = titulo;
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.resumo = resumo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPergunta() {
        return pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public String getResumo() {
        return resumo;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
