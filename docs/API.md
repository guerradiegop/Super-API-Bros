# Contrato da API didática

Base: `http://127.0.0.1:PORTA` (veja a porta no laboratório ou no console do jogo). Servidor disponível somente enquanto o jogo estiver aberto. Respostas em UTF-8 com `Content-Type: application/json; charset=utf-8` e `Cache-Control: no-store`.

## GET /world/1

Sem autenticação ou corpo. Retorna 200:

```json
{"world":"Jardim dos endpoints","gate":"open"}
```

## GET /keys?color=blue

Cabeçalho obrigatório: `Authorization: Bearer aluno-api`. A query deve ser exatamente `color=blue` (o servidor intencionalmente só implementa esse filtro simples). Retorna 200:

```json
{"key":"blue","gate":"open"}
```

Sem token ou com token diferente: 401, cabeçalho `WWW-Authenticate: Bearer`. Com token correto e query diferente: 400.

## POST /bridges

Cabeçalho obrigatório: `Content-Type: application/json` (aceita parâmetro de charset). Corpo, até 4096 bytes, em UTF-8:

```json
{"material":"wood"}
```

Retorna 201 com `Location: /bridges/demo`:

```json
{"bridge":"built","material":"wood","gate":"open"}
```

Esta criação é uma demonstração sem persistência. Repetir POST produz novamente a resposta didática. O endereço em Location não implementa consulta. O corpo deve ser objeto com valores string: objetos aninhados, arrays, números, booleanos, null e chaves duplicadas não são aceitos por este contrato reduzido. Campos string adicionais são ignorados.

## Erros para explorar

| Status | Como provocar | O que corrigir |
|---|---|---|
| 400 | JSON incompleto ou query errada em `/keys` | Sintaxe ou parâmetro |
| 401 | Não enviar o token em `/keys` | Authorization |
| 404 | Consultar `/mundos` | Caminho |
| 405 | Enviar POST para `/world/1` | Método; veja `Allow` |
| 413 | Corpo maior que 4096 bytes | Tamanho |
| 415 | POST sem Content-Type JSON | Cabeçalho |
| 422 | Enviar `{"material":"stone"}` | Regra do campo |

Corpos de erro têm o formato `{"error":"mensagem explicativa"}`. Erros de conexão e timeout não são status HTTP: significam que não foi obtida uma resposta dentro das condições esperadas.

O cliente do jogo usa timeout de conexão de 3 segundos e timeout da requisição de 5 segundos. Só permite a origem do servidor local e não segue redirecionamentos. APIs reais podem exigir HTTPS, paginação e controle de taxa, temas de extensão para a turma.

## GET /weather?region=crystal

Consulta os sensores das Cavernas de cristal. Query exata obrigatória: `region=crystal`; outra query retorna 400. Sem autenticação ou corpo. Resposta 200:

```json
{"weather":"clear","gate":"open"}
```

## PUT /beacon

Restaura o farol do Deserto dos sinais. Exige `Content-Type: application/json` e corpo `{"signal":"on"}`. Resposta 200:

```json
{"beacon":"lit","gate":"open"}
```

Aplica os mesmos limites de corpo e erros 400, 413 e 415 de POST /bridges. Campo signal ausente ou diferente de on retorna 422. A confirmação é demonstrativa, sem persistência; repetir PUT mantém o mesmo resultado.

## DELETE /interference

Remove a interferência da Cidade suspensa. Exige `Authorization: Bearer aluno-api`; token ausente ou incorreto retorna 401 com `WWW-Authenticate: Bearer`. Sem corpo. Resposta 200:

```json
{"interference":"removed","gate":"open"}
```

A remoção é demonstrativa e repetível. Usa 200 com JSON (não 204), para permitir interpretar a confirmação. Todos os novos endpoints rejeitam métodos incorretos com 405 e Allow.

## POST /heaters

Sem autenticação. Exige `Content-Type: application/json` e corpo `{"fuel":"thermal"}`. Resposta 201, `Location: /heaters/demo` e JSON:

```json
{"heat":"warm","gate":"open"}
```

## PUT /purifier

Exige `Authorization: Bearer aluno-api`, `Content-Type: application/json` e corpo `{"filter":"carbon"}`. Resposta 200:

```json
{"air":"clean","gate":"open"}
```

Nos dois endpoints: método incorreto → 405 com Allow; tipo incorreto → 415; JSON inválido → 400; corpo acima de 4096 bytes → 413; valor obrigatório ausente ou incorreto → 422. No purificador, token ausente/incorreto → 401 com WWW-Authenticate. São confirmações demonstrativas sem persistência; Location do aquecedor não implementa GET.

## GET /tides?mode=low

Sem autenticação. Query exata obrigatória `mode=low`; outra query retorna 400. Resposta 200:

```json
{"tide":"low","gate":"open"}
```

## POST /reactor

Exige `Content-Type: application/json` e corpo `{"cell":"solar"}`. Sem autenticação. Retorna 201 com `Location: /reactor/demo`:

```json
{"reactor":"online","gate":"open"}
```

Método incorreto: 405; tipo incorreto: 415; JSON inválido: 400; corpo acima de 4096 bytes: 413; célula ausente/incorreta: 422. Confirmação demonstrativa, sem persistência e sem GET no Location.

## Distribuição na campanha

Os dez protocolos são embaralhados entre as dez regiões, sem repetição por partida. Cada região também contém uma central independente de lógica computacional, validada localmente e sem chamada HTTP. Todas as moedas da fase e, no cooperativo, a proximidade dos dois personagens são necessárias para acessar as centrais. As duas centrais devem estar concluídas para abrir o portal; `gate: open` numa resposta HTTP confirma apenas o protocolo API, não substitui o desafio de lógica.
