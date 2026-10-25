# Guia de aula — campanha individual ou cooperativa

## Objetivos

Montar requisições HTTP, interpretar status e JSON, distinguir autenticação de validação e executar mentalmente algoritmos simples. Na dupla, coordenar movimentos, administrar energia compartilhada e explicar as soluções ao parceiro.

## Roteiro sugerido

1. Apresente cliente, servidor, endpoint e contrato. A aplicação faz HTTP real para um servidor local, sem internet. Os recursos são demonstrativos, sem persistência.
2. Na seleção, escolha solo ou dupla com M. P1 usa as setas, P2 usa A/D/W. A corda equivale à metade da tela; os dois devem chegar às centrais e à saída.
3. Colete as 15 moedas da primeira metade para acessar a central API e as outras 15 antes da central final. Há dois corações por fase, um em cada metade. Eles concedem +1 energia até 7, cada coração uma única vez na fase; perder energia não os restaura. Na dupla, energia e itens são compartilhados.
4. Na central API, provoque um erro e compare com a resposta correta. Examine método, caminho, cabeçalhos, corpo, status e o campo solicitado. Só uma requisição válida seguida da interpretação correta restabelece a central.
5. Na central lógica, escreva os estados intermediários do algoritmo. Uma resposta incorreta apresenta dica, sem penalidade. Há 25 tipos de lógica, incluindo laços, condicionais, listas, binário, booleanos, filas, pilhas, funções, ordenação e índices circulares.
6. Explique por que concluir uma central não abre o portal. A API intermediária ativa o checkpoint; a lógica final exige a API e todas as moedas para liberar a saída. O checkpoint preserva os itens coletados e a central já resolvida.

As dez fases são embaralhadas e 10 de 25 protocolos e 10 de 25 tipos de lógica são sorteados sem repetição em cada nova partida; os números dos exercícios também variam. Não existe gabarito por número de fase. Pause com P para discussão; N na pausa inicia outra expedição com novo sorteio. A mesma partida preserva os desafios após dano ou pausa.

Use K para a habilidade de P1 e F para a de P2 (no solo, ambos funcionam). Cada explorador tem um efeito diferente, com um uso por jogador a cada fase, preservado após morte ou troca de personagem; veja a tabela no README. A corda continua valendo durante voo e super velocidade. Discuta o momento de usar os poderes. F11 alterna tela cheia com proporções preservadas; Esc sai. A troca de tela pausa a exploração e P retoma.

## Protocolos e campos esperados

| Requisição | Interpretação |
|---|---|
| GET /world/1 | world = connected |
| GET /keys?color=blue | key = blue |
| POST /bridges | bridge = built |
| GET /weather?sensor=remote | weather = clear |
| PUT /beacon | beacon = lit |
| DELETE /interference | interference = removed |
| POST /heaters | heat = warm |
| PUT /purifier | air = clean |
| GET /tides?mode=low | tide = low |
| POST /reactor | reactor = online |

A tabela acima mostra os dez protocolos originais, agora com contextos neutros. Consulte [API.md](API.md) para os 25 contratos, credenciais, corpos e erros. A credencial `aluno-api` é pública e didática: nunca use uma credencial real. Exemplos de investigação: 404 por caminho inexistente, 405 por método incorreto, 401 por token ausente, 415 por formato, 400 por JSON inválido e 422 por valor fora do contrato.

## Exercícios no código

* Identifique URI, método, headers e body em `ApiClient.send`; acompanhe o `sendAsync` e o retorno à thread Swing no diálogo.
* Acrescente um contrato e seus testes sem remover a restrição de origem local. Para APIs públicas, crie um cliente HTTPS separado e use biblioteca JSON completa.
* Use `new GameModel(seed)` nos testes para reproduzir um mapa e sua sequência de desafios; `restart()` inicia uma expedição nova, `restart(seed)` reproduz uma conhecida.
* Modifique uma família em `LogicChallenge.create` e valide respostas corretas/incorretas e diferentes parâmetros. Nunca avalie texto arbitrário como código.
* Investigue por que moedas, corações consumidos e centrais permanecem no respawn, mas são restaurados ao avançar de região.

## Avaliação

Peça uma requisição com erro e sua correção, uma explicação da resposta JSON e um registro passo a passo do desafio de lógica. Na dupla, alternem quem explica cada central. Dez regiões de 7.344 unidades demandam várias sessões; não é necessário concluir toda a campanha em uma única aula.
