// Essa classe é responsável por receber o conteúdo da página(chrome) que será enviado para o backend.
// Ela contém um único atributo chamado "conteudo", que é do tipo String.
// A anotação @Getter e @Setter do Lombok é usada para gerar automaticamente os métodos getters e setters para esse atributo, facilitando o acesso e a modificação do valor do conteúdo da página.
package slime.AvaAssistant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageContentRequest {

    private String conteudo;
}
