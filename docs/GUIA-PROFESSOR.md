# Guia de aula — 60 a 90 minutos

## Objetivos observáveis

Ao concluir, o aluno deve explicar o papel de cliente e servidor, montar GET e POST, localizar parâmetros/cabeçalhos/corpo, interpretar status HTTP e extrair um valor do JSON para usar numa aplicação.

## Roteiro

1. **10 min — contexto.** Pergunte como um aplicativo obtém dados de outro sistema. Apresente API como contrato de uso. Neste jogo, o cliente é o jogo; o servidor fornece dados e processa pedidos. HTTP é o transporte; JSON é um formato de dados.
2. **15 min — fase 1.** Peça um caminho inexistente antes do correto. Compare 404 e 200. Depois use POST no endpoint GET e observe 405 e Allow. O aluno deve descrever URL, método, status e corpo.
3. **15 min — fase 2.** Tente sem token, com token correto e filtro errado, e finalmente com ambos corretos. Compare autenticação e parâmetro. Nunca use uma credencial real nesta atividade.
4. **15 min — fase 3.** Envie sem Content-Type, com JSON quebrado e com material stone. Diferencie 415, 400 e 422 antes de obter 201. Observe Location e explique a simplificação de persistência desta API.
5. **20–35 min — código e discussão.** Execute os exercícios abaixo em duplas. Recompile e execute os testes após cada alteração.

## Exercícios no código

### 1. Consumir uma API em Java

Localize `ApiClient.send`. Identifique `URI`, `HttpRequest`, método, headers, body, `HttpClient.sendAsync` e `HttpResponse`. Em um arquivo `Practice.java`, use somente classes do JDK:

```java
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class Practice {
    public static void main(String[] args) throws Exception {
        // Copie a URL local exibida no laboratório do jogo, que deve estar aberto.
        var request = HttpRequest.newBuilder(URI.create(args[0] + "/world/1"))
            .timeout(Duration.ofSeconds(5))
            .header("Accept", "application/json")
            .GET().build();
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP " + response.statusCode());
        System.out.println(response.body());
    }
}
```

Execute `java Practice.java http://127.0.0.1:PORTA`, substituindo PORTA. Depois adapte para `/keys?color=blue` com `.header("Authorization", "Bearer aluno-api")`. Finalmente transforme a chamada em POST com `.POST(HttpRequest.BodyPublishers.ofString("{\"material\":\"wood\"}"))` e Content-Type adequado. Compare as respostas com o laboratório.

### 2. Alterar cliente e servidor

Adicione `/weather` no servidor com JSON `{"weather":"rain"}`. Consuma-o no cliente e altere a cor do cenário a partir do valor recebido. Não altere o cenário antes de verificar status e formato. Adicione testes para sucesso e erro.

### 3. Tratar falha de conexão

Use Practice.java com a URL após fechar o jogo. Observe a exceção. Adicione tratamento amigável e explique por que uma falha de conexão não é um 404. No jogo, veja como `whenComplete` devolve atualizações à thread Swing com `invokeLater`.

### 4. Extensão com API pública

O professor escolhe previamente uma API pública apropriada à turma e lê seu contrato atual. Crie um cliente separado com base HTTPS fixa, timeout e tratamento de falha, sem remover a restrição de origem do laboratório. Use uma biblioteca JSON completa para contratos complexos. Mantenha o jogo local para aulas sem internet. Não coloque tokens em Git, prints ou arquivos distribuídos.

## Avaliação rápida

Solicite que cada dupla entregue uma requisição com erro e sua correção, uma consulta GET em Java e uma criação POST. Pergunte: “Qual informação veio do servidor?”, “O que o cliente decidiu fazer com ela?” e “Qual é a diferença entre status HTTP e corpo JSON?”.

**Gabarito do jogo:** fase 1 → `Jardim dos endpoints`; fase 2 → `blue`; fase 3 → `built`; fase 4 → `clear`; fase 5 → `lit`; fase 6 → `removed`; fase 7 → `warm`; fase 8 → `clean`. A resposta digitada só é aceita depois de uma requisição válida para a missão.

## Continuação da expedição

Reserve uma sessão adicional para as três novas regiões e os percursos duplicados. Na caverna, compare filtros válidos e inválidos em GET /weather. No deserto, repita PUT /beacon e discuta idempotência e a ausência de persistência nesta demonstração. Na cidade, compare DELETE com e sem credencial e explique por que esta API retorna 200 com JSON em vez de 204 sem corpo.

A interface usa linguagem de expedição e centrais para preservar a imersão; os conceitos técnicos permanecem nos protocolos. Cada região tem um terminal que ativa o checkpoint, depois do qual perder energia não exige repetir a requisição.

## Neve e reconstrução

Na Cordilheira branca, peça uma ordem POST /heaters com combustível incorreto antes de thermal; compare 422 e 201. No Horizonte das cinzas, combine autenticação com validação do corpo: tente PUT /purifier sem credencial, sem Content-Type e com filtro incorreto antes de carbon. Discuta por que a confirmação é demonstrativa e não representa persistência.

Os quatro exploradores têm as mesmas habilidades. Use a seleção inicial ou P → C para trocar o visual. As duas novas regiões mantêm a exigência de todas as moedas antes da central. Oriente a esperar a altura dos elevadores e a saltar das placas de sucata enquanto racham; se uma placa desaparecer, ela reaparece após três segundos.
