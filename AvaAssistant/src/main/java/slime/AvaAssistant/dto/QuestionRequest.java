package slime.AvaAssistant.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class QuestionRequest {

    private String enunciado;
    private Map<String, String> alternativas;

}