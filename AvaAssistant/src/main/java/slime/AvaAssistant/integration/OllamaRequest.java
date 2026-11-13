
package slime.AvaAssistant.integration;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class OllamaRequest {

    private String model;
    private String prompt;
    private boolean stream;

    private String keep_alive;

    private Map<String, Object> options;
}
