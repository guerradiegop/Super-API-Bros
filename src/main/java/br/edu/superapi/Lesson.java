package br.edu.superapi;

import java.net.http.HttpResponse;
import java.util.Map;

public record Lesson(String title, String explanation, String contract, String method, String path,
                     int status, String field, String answer, String question) {
    public static final Lesson[] ALL = {
        new Lesson("01 / Jardim dos endpoints",
            "O jardim perdeu sua rota. Consulte a central para restaurar a passagem.\n"
            + "Este terminal é o cliente e a central é o servidor. HTTP transporta\n"
            + "a requisição e a resposta. APIs servem para consultar dados e acionar serviços.\n"
            + "GET consulta um recurso; o endpoint é o endereço desse recurso.",
            "GET /world/1\nSem corpo ou autenticação. Resposta: 200 + JSON com world e gate.",
            "GET", "/world/1", 200, "world", "Jardim dos endpoints", "Qual é o valor de world no JSON recebido?"),
        new Lesson("02 / Fortaleza das chaves",
            "A fortaleza exige uma chave. Filtre a consulta: ?color=blue. Cabeçalhos enviam\n"
            + "metadados. Authorization identifica a credencial apresentada ao servidor.\n"
            + "401 significa credencial ausente ou inválida; 400 indica requisição inválida.\n"
            + "Tokens reais são secretos. aluno-api é apenas uma credencial de demonstração.",
            "GET /keys?color=blue\nAuthorization: Bearer aluno-api\nResposta: 200 + JSON com key e gate.",
            "GET", "/keys?color=blue", 200, "key", "blue", "Qual é o valor de key no JSON recebido?"),
        new Lesson("03 / Oficina das pontes",
            "A ponte desabou. Envie uma ordem de construção com POST. O corpo contém o JSON\n"
            + "e Content-Type informa seu formato. 201 significa criação bem-sucedida.\n"
            + "415: formato não aceito. 422: dados válidos no formato, mas fora da regra.\n"
            + "A oficina confirma a ordem; o registro da construção é temporário.",
            "POST /bridges\nContent-Type: application/json\nCorpo: {\"material\":\"wood\"}\nResposta: 201 + JSON com bridge, material e gate.",
            "POST", "/bridges", 201, "bridge", "built", "Qual é o valor de bridge no JSON recebido?"),
        new Lesson("04 / Cavernas de cristal",
            "A névoa esconde a saída. Consulte os sensores da caverna.\nGET lê as condições; a query seleciona a região monitorada.",
            "GET /weather?region=crystal\nResposta: 200 + JSON com weather e gate.",
            "GET", "/weather?region=crystal", 200, "weather", "clear", "Qual condição aparece em weather?"),
        new Lesson("05 / Deserto dos sinais",
            "O farol está apagado. Restaure sua configuração com PUT.\nPUT substitui a configuração; repetir a mesma ordem mantém o resultado.",
            "PUT /beacon\nContent-Type: application/json\nCorpo: {\"signal\":\"on\"}\nResposta: 200 + JSON com beacon e gate.",
            "PUT", "/beacon", 200, "beacon", "lit", "Qual estado aparece em beacon?"),
        new Lesson("06 / Cidade suspensa",
            "Uma interferência isolou a cidade. Remova a origem com DELETE.\nApresente sua credencial para autorizar a remoção. Esta central retorna JSON com status 200.",
            "DELETE /interference\nAuthorization: Bearer aluno-api\nResposta: 200 + JSON com interference e gate.",
            "DELETE", "/interference", 200, "interference", "removed", "Qual resultado aparece em interference?")
    };
    public boolean accepts(String sentMethod, String sentPath, HttpResponse<String> response) {
        if (!method.equals(sentMethod) || !path.equals(sentPath) || response.statusCode() != status) return false;
        try {
            Map<String, String> json = Json.parse(response.body());
            return "open".equals(json.get("gate")) && answer.equals(json.get(field));
        } catch (IllegalArgumentException ex) { return false; }
    }
}
