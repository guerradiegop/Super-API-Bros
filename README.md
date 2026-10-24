# Super API Bros — Rede dos Horizontes

Expedição de plataforma em **Java 17**, em português, com dez regiões, seis exploradores pixelados e modos solo ou cooperativo local. Reúna moedas, restaure centrais de comunicação com **HTTP real** e resolva algoritmos para reconectar os horizontes.

## Executar

Requer Java 17+ com desktop Windows, Linux ou macOS. Baixe o repositório, extraia e execute:

```sh
java -jar dist/super-api-bros.jar
```

Também há `run.bat` e `sh run.sh`. O JAR está incluído. Após editar fontes, recompile: os scripts `run` só compilam quando o JAR não existe. Não exige internet, Maven, cadastro ou chave externa. O servidor HTTP é local (`127.0.0.1`, porta livre), inicia com a aplicação e encerra com ela. Os dados não persistem entre execuções.

## Controles e seleção

| Ação | Solo | Dupla no mesmo teclado |
|---|---|---|
| Mover | A/D ou ←/→ | P1: ←/→; P2: A/D |
| Saltar | W, ↑ ou Espaço | P1: ↑; P2: W |
| Conectar à central | E ou Enter | Enter ou E, com os dois próximos |
| Pausar/continuar | P | P, pausa ambos |
| Escolher personagens | C na pausa ou no encerramento | C na pausa ou no encerramento |
| Nova expedição | N na pausa; R no encerramento | Igual |

Na seleção inicial, **M** alterna solo/dupla (também por clique). O modo só pode mudar antes de iniciar uma expedição. As setas escolhem P1; A/D escolhem P2 na dupla. **Tab** alterna o jogador selecionado para os atalhos **1–6** ou cliques nos retratos. **Enter** confirma. Esc fecha a seleção aberta durante a pausa, mantendo a pausa. Todos têm a mesma física e colisões; aparência não concede vantagens.

| Explorador | Referência visual |
|---|---|
| Ney | Neymar: cabelo claro e camisa amarela |
| Leo | Messi: barba e camisa listrada |
| Cris | Cristiano Ronaldo: cabelo escuro e camisa vermelha |
| Dila | Dilma: cabelo curto e conjunto vermelho |
| Nita | Anitta: cabelo longo escuro, roupa roxa e brincos |
| Vivi | Virginia: cabelo longo loiro e roupa rosa |

São caricaturas originais desenhadas em código, sem fotografias ou assets externos.

## Campanha e regras

* **10 fases de 8.640 unidades**: aumento exato de **80%** sobre as 4.800 anteriores. Todas as rotas foram reconstruídas em 12 blocos com variantes limitadas pela física de salto, 36 plataformas elevadas e 12 fendas.
* **36 moedas por fase**, acessíveis antes das centrais. Todas devem ser coletadas para acessar os desafios; a contagem é compartilhada na dupla.
* **Duas centrais e dois desafios por fase:** uma de API (requisição HTTP e interpretação do JSON) e outra de lógica computacional. Podem ser resolvidas em qualquer ordem. O portal exige ambas concluídas; cancelar, falhar ou acertar apenas uma não abre a passagem.
* **Energia inicial 3, máximo 7.** Há quatro corações por fase. Cada um concede +1, é removido ao coletar e não reaparece após perder energia. No máximo de energia, o coração permanece para uma coleta posterior. Corações e moedas são compartilhados na dupla.
* Contato lateral com inimigos ou queda custa uma energia. Na dupla, o dano é compartilhado e os dois reaparecem juntos com proteção temporária, evitando cobrança dupla no mesmo passo. Saltar sobre inimigos os derrota.
* A primeira central concluída ativa o checkpoint no final da rota. Moedas, corações consumidos e centrais resolvidas permanecem após dano; plataformas móveis/frágeis retornam ao estado inicial. Uma fase nova restaura os itens e os dois desafios.
* A corda limita a **distância entre os personagens a 500 unidades**, metade da largura visível de 1.000 unidades (não metade do mapa). Movimento que esticaria a corda é limitado; os parceiros precisam avançar juntos. A câmera acompanha o ponto médio. Ambos devem se aproximar da central e atravessar a saída.

## Regiões e inimigos

| Região | Plataformas e ambiente | Inimigos por fase |
|---|---|---:|
| Jardim dos endpoints | Copa das árvores e escadas alternadas | 20 |
| Fortaleza das chaves | Muralhas e rotas elevadas | 20 |
| Oficina das pontes | Andaimes e placas frágeis | 20 |
| Cavernas de cristal | Cristais e escadas verticais | 20 |
| Deserto dos sinais | Dunas e saltos entre patamares | 20 |
| Cidade suspensa | Torres e elevadores alternados | 20 |
| Cordilheira branca | Neve e elevadores de gelo | 17 |
| Horizonte das cinzas | Ruínas e sucata que cede | 22 |
| Arquipélago das marés | Ilhas, ondas e elevadores | 22 |
| Reator da aurora | Reatores luminosos e placas instáveis | 22 |

Nas oito regiões existentes, as populações correspondem a `ceil(população anterior × 1,8)`: 11→20, 9→17 e 12→22. As duas novas têm 22 inimigos cada. Há patrulheiros, saltadores, drones, patinadores, espectros de gelo e sucateiros que aceleram por proximidade.

Placas frágeis avisam por rachaduras, cedem após 36 passos (0,6 s) e reaparecem após 180 passos (3 s). Elevadores levam as moedas junto com a plataforma. Esses mecanismos permitem novas tentativas sem tornar a coleta impossível.

## Sorteio e desafios

Cada nova expedição sorteia, sem repetição, a ordem das dez regiões, a distribuição dos dez protocolos HTTP e os dez tipos de lógica. Os parâmetros dos exercícios de lógica e as variantes das plataformas também mudam. A ordem da nova expedição é diferente da anterior. Perder energia, pausar ou trocar aparência **não** sorteia novamente.

Os protocolos HTTP incluem consulta de rota, chave autenticada, criação de ponte, clima, farol, remoção de interferência, aquecedor, purificador, marés e reator. Não ficam vinculados a uma região específica. As respostas são obtidas por `HttpClient.sendAsync` e `HttpServer`, com validação de método, caminho, status e JSON.

A central de lógica usa acumuladores, condições, índices de listas, binário, expressões booleanas, filas, pilhas, laços, troca de variáveis e busca. Não executa código fornecido pelo usuário. Resposta errada apresenta uma dica e não custa energia.

## Compilar e testar

Com JDK 17+ (`java`, `javac`, `jar`):

```sh
sh build.sh test
sh run.sh
```

No Windows: `build.bat test` e `run.bat`. O build gera o JAR versionado. Os testes verificam HTTP real, lógica, reprodutibilidade do sorteio, rotas em diferentes posições dos elevadores, cooperação, corda, moedas, corações, checkpoints, progressão e controles. `AllTests --render` gera capturas headless em `build/` para inspeção visual. A automação do GitHub compila e testa em Windows, Linux e macOS.

## Organização

* `GameModel`: estado da campanha, geração limitada dos mapas, física, itens e cooperação.
* `GamePanel`, `Explorer`, `RegionTheme`, `TerrainPlatform`: interface, avatares e desenho procedural.
* `Lesson`, `AcademyServer`, `ApiClient`, `LabDialog`: contratos e integração HTTP.
* `LogicChallenge`, `LogicDialog`: geração e interface de raciocínio computacional.
* `Json`: parser restrito a objetos com valores string, não um parser JSON genérico.

Veja [o contrato completo](docs/API.md) e [o guia do professor](docs/GUIA-PROFESSOR.md). A credencial pública `aluno-api` serve apenas à demonstração. POST/PUT/DELETE retornam confirmações sem persistência; os recursos indicados em Location não implementam GET. Não há salvamento da campanha em disco. A aplicação requer ambiente gráfico, não terminal headless/SSH.
