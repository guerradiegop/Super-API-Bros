package br.edu.superapi;

import java.net.http.HttpResponse;
import java.util.Map;

public record Lesson(String title, String explanation, String contract, String method, String path,
                     int status, String field, String answer, String question) {
    public static final Lesson[] ALL = {
        new Lesson("01 / Jardim dos endpoints",
            "Uma API é uma interface: um contrato que permite a programas conversar.\n"
            + "Aqui o jogo é o cliente e outro componente é o servidor. HTTP transporta\n"
            + "a requisição e a resposta. APIs servem para consultar dados e acionar serviços.\n"
            + "GET consulta um recurso; o endpoint é o endereço desse recurso.",
            "GET /world/1\nSem corpo ou autenticação. Resposta: 200 + JSON com world e gate.",
            "GET", "/world/1", 200, "world", "Jardim dos endpoints", "Qual é o valor de world no JSON recebido?"),
        new Lesson("02 / Fortaleza das chaves",
            "Parâmetros de query filtram uma consulta: ?color=blue. Cabeçalhos enviam\n"
            + "metadados. Authorization identifica a credencial apresentada ao servidor.\n"
            + "401 significa credencial ausente ou inválida; 400 indica requisição inválida.\n"
            + "Tokens reais são secretos. aluno-api é apenas uma credencial de demonstração.",
            "GET /keys?color=blue\nAuthorization: Bearer aluno-api\nResposta: 200 + JSON com key e gate.",
            "GET", "/keys?color=blue", 200, "key", "blue", "Qual é o valor de key no JSON recebido?"),
        new Lesson("03 / Oficina das pontes",
            "POST envia dados para criar ou processar um recurso. O corpo contém o JSON\n"
            + "e Content-Type informa seu formato. 201 significa criação bem-sucedida.\n"
            + "415: formato não aceito. 422: dados válidos no formato, mas fora da regra.\n"
            + "Esta oficina é didática: não persiste pontes; cada POST retorna uma criação de demonstração.",
            "POST /bridges\nContent-Type: application/json\nCorpo: {\"material\":\"wood\"}\nResposta: 201 + JSON com bridge, material e gate.",
            "POST", "/bridges", 201, "bridge", "built", "Qual é o valor de bridge no JSON recebido?")
    };
    public boolean accepts(String sentMethod, String sentPath, HttpResponse<String> response) {
        if (!method.equals(sentMethod) || !path.equals(sentPath) || response.statusCode() != status) return false;
        try {
            Map<String, String> json = Json.parse(response.body());
            return "open".equals(json.get("gate")) && answer.equals(json.get(field));
        } catch (IllegalArgumentException ex) { return false; }
    }
}
