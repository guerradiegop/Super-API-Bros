package br.edu.superapi;

import java.net.http.HttpResponse;
import java.util.Map;

/** Independent communication protocols, sampled without replacement per expedition. */
public record Lesson(String title, String explanation, String contract, String method, String path,
                     int status, String field, String answer, String question, boolean authenticated, String requestBody) {
    public static final Lesson[] ALL = {
        define("Rota de comunicação", "Consulte o registro da rede para reencontrar o sinal.", "GET", "/world/1", 200, "world", "connected", false, ""),
        define("Chave de acesso", "Recupere a chave azul usando sua credencial.", "GET", "/keys?color=blue", 200, "key", "blue", true, ""),
        define("Ponte de dados", "Solicite uma ponte para reconectar dois canais.", "POST", "/bridges", 201, "bridge", "built", false, "{\"material\":\"wood\"}"),
        define("Leitura dos sensores", "Consulte o sensor remoto indicado pelo protocolo.", "GET", "/weather?sensor=remote", 200, "weather", "clear", false, ""),
        define("Farol de transmissão", "Restaure a configuração do sinal de orientação.", "PUT", "/beacon", 200, "beacon", "lit", false, "{\"signal\":\"on\"}"),
        define("Limpeza de canal", "Remova a interferência usando a credencial de manutenção.", "DELETE", "/interference", 200, "interference", "removed", true, ""),
        define("Controle térmico", "Ative o módulo que estabiliza a temperatura da central.", "POST", "/heaters", 201, "heat", "warm", false, "{\"fuel\":\"thermal\"}"),
        define("Filtro de circulação", "Substitua a configuração do filtro de ar da central.", "PUT", "/purifier", 200, "air", "clean", true, "{\"filter\":\"carbon\"}"),
        define("Sensor de fluxo", "Consulte o nível de fluxo do circuito de refrigeração.", "GET", "/tides?mode=low", 200, "tide", "low", false, ""),
        define("Fonte de energia", "Alimente o módulo de energia para sustentar a conexão.", "POST", "/reactor", 201, "reactor", "online", false, "{\"cell\":\"solar\"}"),
        define("Diagnóstico", "Verifique se os serviços essenciais respondem.", "GET", "/health", 200, "health", "ready", false, ""),
        define("Inventário", "Localize o componente de reparo no inventário.", "GET", "/inventory?item=cable", 200, "stock", "available", false, ""),
        define("Rota alternativa", "Selecione a rota de menor latência.", "GET", "/routes?sort=latency", 200, "route", "direct", false, ""),
        define("Arquivo de sinais", "Recupere a segunda página do arquivo.", "GET", "/signals?page=2", 200, "page", "second", false, ""),
        define("Acesso ao cofre", "Consulte a credencial armazenada no cofre protegido.", "GET", "/vault", 200, "access", "granted", true, ""),
        define("Reserva de canal", "Reserve um canal de transmissão para a equipe.", "POST", "/channels", 201, "channel", "reserved", true, "{\"band\":\"short\"}"),
        define("Entrega de pacote", "Registre a entrega de um pacote prioritário.", "POST", "/packets", 201, "packet", "received", false, "{\"priority\":\"high\"}"),
        define("Sincronização", "Envie um marcador para sincronizar os relógios.", "POST", "/sync", 201, "clock", "synced", false, "{\"pulse\":\"start\"}"),
        define("Cópia de segurança", "Solicite uma cópia da configuração ativa.", "POST", "/backups", 201, "backup", "saved", true, "{\"scope\":\"config\"}"),
        define("Antena", "Ajuste a direção da antena de comunicação.", "PUT", "/antenna", 200, "antenna", "aligned", false, "{\"direction\":\"north\"}"),
        define("Regulador", "Defina a tensão do circuito auxiliar.", "PUT", "/voltage", 200, "voltage", "stable", false, "{\"mode\":\"safe\"}"),
        define("Prioridade de tráfego", "Atualize somente a prioridade da fila.", "PATCH", "/queue", 200, "queue", "optimized", true, "{\"priority\":\"rescue\"}"),
        define("Modo de escuta", "Atualize o modo de escuta sem substituir as outras opções.", "PATCH", "/receiver", 200, "receiver", "listening", false, "{\"mode\":\"listen\"}"),
        define("Limpeza de cache", "Remova registros antigos do cache local.", "DELETE", "/cache", 200, "cache", "empty", false, ""),
        define("Revogação de sessão", "Revogue uma sessão inválida com autorização.", "DELETE", "/sessions/expired", 200, "session", "revoked", true, "")
    };
    private static Lesson define(String title, String context, String method, String path, int status,
                                 String field, String answer, boolean auth, String body) {
        String contract = method + " " + path + (auth ? "\nAuthorization: Bearer aluno-api" : "\nSem autenticação.")
                + (body.isEmpty() ? "\nSem corpo." : "\nContent-Type: application/json\nCorpo: " + body)
                + "\nResposta: " + status + " + JSON com " + field + " e gate.";
        return new Lesson(title, context, contract, method, path, status, field, answer,
                "Qual é o valor de " + field + " no JSON?", auth, body);
    }
    public boolean accepts(String sentMethod, String sentPath, HttpResponse<String> response) {
        if (!method.equals(sentMethod) || !path.equals(sentPath) || response.statusCode() != status) return false;
        try {
            Map<String, String> json = Json.parse(response.body());
            return "open".equals(json.get("gate")) && answer.equals(json.get(field));
        } catch (IllegalArgumentException ex) { return false; }
    }
}
