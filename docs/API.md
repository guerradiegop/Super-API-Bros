# Protocolos das centrais

São 25 contratos independentes do cenário. Cada expedição sorteia 10, sem repetição. A base é `http://127.0.0.1:PORTA`, mostrada na central; o servidor encerra com a aplicação. Todas as respostas usam JSON UTF-8, sem cache. Recursos são demonstrativos, sem persistência.

| Desafio | Requisição | Authorization | Corpo JSON | Status | Campo esperado |
|---|---|---|---|---:|---|
| Rota de comunicação | `GET /world/1` | — | — | 200 | `world = connected` |
| Chave de acesso | `GET /keys?color=blue` | `Bearer aluno-api` | — | 200 | `key = blue` |
| Ponte de dados | `POST /bridges` | — | `{"material":"wood"}` | 201 | `bridge = built` |
| Leitura dos sensores | `GET /weather?sensor=remote` | — | — | 200 | `weather = clear` |
| Farol de transmissão | `PUT /beacon` | — | `{"signal":"on"}` | 200 | `beacon = lit` |
| Limpeza de canal | `DELETE /interference` | `Bearer aluno-api` | — | 200 | `interference = removed` |
| Controle térmico | `POST /heaters` | — | `{"fuel":"thermal"}` | 201 | `heat = warm` |
| Filtro de circulação | `PUT /purifier` | `Bearer aluno-api` | `{"filter":"carbon"}` | 200 | `air = clean` |
| Sensor de fluxo | `GET /tides?mode=low` | — | — | 200 | `tide = low` |
| Fonte de energia | `POST /reactor` | — | `{"cell":"solar"}` | 201 | `reactor = online` |
| Diagnóstico | `GET /health` | — | — | 200 | `health = ready` |
| Inventário | `GET /inventory?item=cable` | — | — | 200 | `stock = available` |
| Rota alternativa | `GET /routes?sort=latency` | — | — | 200 | `route = direct` |
| Arquivo de sinais | `GET /signals?page=2` | — | — | 200 | `page = second` |
| Acesso ao cofre | `GET /vault` | `Bearer aluno-api` | — | 200 | `access = granted` |
| Reserva de canal | `POST /channels` | `Bearer aluno-api` | `{"band":"short"}` | 201 | `channel = reserved` |
| Entrega de pacote | `POST /packets` | — | `{"priority":"high"}` | 201 | `packet = received` |
| Sincronização | `POST /sync` | — | `{"pulse":"start"}` | 201 | `clock = synced` |
| Cópia de segurança | `POST /backups` | `Bearer aluno-api` | `{"scope":"config"}` | 201 | `backup = saved` |
| Antena | `PUT /antenna` | — | `{"direction":"north"}` | 200 | `antenna = aligned` |
| Regulador | `PUT /voltage` | — | `{"mode":"safe"}` | 200 | `voltage = stable` |
| Prioridade de tráfego | `PATCH /queue` | `Bearer aluno-api` | `{"priority":"rescue"}` | 200 | `queue = optimized` |
| Modo de escuta | `PATCH /receiver` | — | `{"mode":"listen"}` | 200 | `receiver = listening` |
| Limpeza de cache | `DELETE /cache` | — | — | 200 | `cache = empty` |
| Revogação de sessão | `DELETE /sessions/expired` | `Bearer aluno-api` | — | 200 | `session = revoked` |

Envie `Content-Type: application/json` quando houver corpo. Objetos aceitam apenas valores string: números, listas, objetos aninhados e chaves duplicadas são rejeitados. Campos adicionais são ignorados. As queries devem corresponder exatamente ao contrato.

Cada sucesso inclui `"gate":"open"`. O POST retorna também `Location: CAMINHO/demo`; esses recursos não implementam GET. A ponte inclui `"material":"wood"` na resposta.

| Erro | Significado |
|---|---|
| 400 | Query incorreta ou JSON malformado |
| 401 | Credencial ausente/incorreta; consulte WWW-Authenticate |
| 404 | Endpoint desconhecido |
| 405 | Método incorreto; consulte Allow |
| 413 | Corpo maior que 4096 bytes |
| 415 | Content-Type ausente ou incorreto |
| 422 | Campos obrigatórios ausentes ou com valores incorretos |

O cliente tem timeout de conexão de 3 segundos e de resposta de 5 segundos; só aceita a origem local e não segue redirecionamentos. `aluno-api` é uma credencial pública didática.

A central API exige as 15 moedas da primeira metade e ativa o checkpoint. A central lógica final exige a API concluída e todas as 30 moedas. Na dupla, ambos devem estar próximos. `gate: open` confirma o protocolo, mas não substitui o desafio de lógica.
