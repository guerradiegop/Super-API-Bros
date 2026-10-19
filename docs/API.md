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
