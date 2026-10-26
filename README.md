# Super API Bros — Rede dos Horizontes

Expedição de plataforma em **Java 17**, em português, com dez regiões, seis exploradores pixelados e modos solo ou cooperativo local. Reúna moedas, restaure centrais de comunicação com **HTTP real** e resolva algoritmos para reconectar os horizontes.

## Executar

Esta é a branch **sem-instalador**: código atual, testes e JAR compilado, sem os binários de instalação no histórico desta branch. A `main` mantém o instalador e seu histórico.

Para baixar somente esta branch sem trazer o histórico das demais:

```sh
git clone --single-branch --branch sem-instalador --depth 1 https://github.com/guerradiegop/Super-API-Bros.git
```

O empacotamento manual continua disponível em `scripts/package-windows.ps1`; seus arquivos de saída são ignorados pelo Git nesta branch.

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
| Habilidade | K ou F | P1: K; P2: F |
| Voo: subir/descer | ↑/↓ ou W/S | P1: ↑/↓; P2: W/S |
| Tela cheia | F11; Esc sai | Igual |
| Conectar à central | E ou Enter | Enter ou E, com os dois próximos |
| Pausar/continuar | P | P, pausa ambos |
| Escolher personagens | C na pausa ou no encerramento | C na pausa ou no encerramento |
| Nova expedição | N na pausa; R no encerramento | Igual |

Na seleção inicial, **M** alterna solo/dupla (também por clique). O modo só pode mudar antes de iniciar uma expedição. As setas escolhem P1; A/D escolhem P2 na dupla. **Tab** alterna o jogador selecionado para os atalhos **1–6** ou cliques nos retratos. **Enter** confirma. Esc fecha a seleção aberta durante a pausa, mantendo a pausa. Todos têm o mesmo tamanho de colisão, mas cada explorador possui uma habilidade própria. Trocar personagem cancela o efeito ativo e mantém o uso já consumido.

| Explorador | Referência visual |
|---|---|
| Ney | Neymar: cabelo claro e camisa amarela |
| Leo | Messi: barba e camisa listrada |
| Cris | Cristiano Ronaldo: cabelo escuro e camisa vermelha |
| Dila | Dilma: cabelo curto e conjunto vermelho |
| Nita | Anitta: cabelo longo escuro, roupa roxa e brincos |
| Vivi | Virginia: cabelo longo loiro e roupa rosa |

São caricaturas originais desenhadas em código, sem fotografias ou assets externos.

### Habilidades

| Explorador | Habilidade | Efeito |
|---|---|---|
| Nita | Ataque explosivo | Elimina todos os inimigos visíveis na área de exploração |
| Dila | Ímã coletor | Coleta moedas e corações visíveis; corações excedentes permanecem quando a energia chega a 7 |
| Ney | Super velocidade | Velocidade e alcance horizontal do salto aumentam 150% (2,5×) por 10 segundos |
| Cris | Invencibilidade | Proteção contra inimigos e quedas por 5 segundos; uma queda retorna ao checkpoint ativo ou início sem gastar energia |
| Vivi | Voar com jatinho | Voo controlável por 10 segundos; solte os comandos verticais para pairar |
| Leo | Bolada na cara | Dispara até 5 bolas guiadas, uma para cada um dos 5 inimigos vivos mais próximos |

Cada jogador tem **um uso de habilidade por fase**. Morrer, retornar ao checkpoint, esperar ou trocar de personagem não recupera esse uso. Pausas, seleção e desafios congelam a duração dos efeitos. Dano cancela efeitos ativos; avançar de fase ou iniciar uma nova expedição restaura o uso. Na dupla, a corda continua limitando voo e super velocidade. A proteção de Cris não protege o parceiro contra inimigos.

A janela pode ser redimensionada. **F11** alterna tela cheia no monitor atual, e **Esc** retorna à janela. A imagem mantém a proporção 5:3, com barras nas bordas quando necessário; os cliques acompanham a escala. Alternar a tela durante a exploração pausa a expedição: pressione **P** para continuar.

## Campanha e regras

* **10 fases de 7.344 unidades**: redução exata de **15%** sobre as 8.640 anteriores. Cada rota tem 10 trechos de 3 a 5 plataformas elevadas e 10 fendas, com corredores seguros junto às centrais. As populações de inimigos foram preservadas.
* **30 moedas por fase**: 15 antes da central intermediária e 15 na segunda metade. A central intermediária exige as moedas da primeira metade; a final exige todas. A contagem é compartilhada na dupla.
* **Duas centrais e dois desafios por fase:** uma de API (requisição HTTP e interpretação do JSON) e outra de lógica computacional. A central API fica no meio exato (3.672 unidades) e ativa o checkpoint ao ser concluída; a central lógica fica no final e libera a próxima região. A lógica exige a API concluída. O portal exige ambas concluídas; cancelar, falhar ou acertar apenas uma não abre a passagem.
* **Energia inicial 3, máximo 7.** Há exatamente dois corações por fase, um em cada metade. Cada um concede +1, é removido ao coletar e não reaparece após perder energia. No máximo de energia, o coração permanece para uma coleta posterior. Corações e moedas são compartilhados na dupla.
* Contato lateral com inimigos ou queda custa uma energia. Na dupla, o dano é compartilhado e os dois reaparecem juntos com proteção temporária, evitando cobrança dupla no mesmo passo. Saltar sobre inimigos os derrota.
* Concluir a central API ativa o checkpoint no meio da rota. Moedas, corações consumidos e centrais resolvidas permanecem após dano; plataformas móveis/frágeis retornam ao estado inicial. Uma fase nova restaura os itens e os dois desafios.
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

Placas frágeis avisam por rachaduras, cedem após 36 passos (0,6 s) e reaparecem após 180 passos (3 s). Elevadores levam as moedas junto com a plataforma e têm amplitudes, ritmos e posições iniciais sorteados. Patrulheiros terrestres ficam apoiados em superfícies estáveis; os voadores mantêm suas rotas aéreas. Esses mecanismos permitem novas tentativas sem tornar a coleta impossível.

## Percursos de plataforma

O gerador combina seis famílias: escada ascendente, patamar, arco, patamar elevado com descida, sequência de apoios menores e subida com retorno. A distribuição evita formatos consecutivos iguais e inclui todas as famílias em cada fase. Cada trecho tem três moedas, mesmo quando possui quatro ou cinco plataformas.

Largura, altura, distância entre apoios, extensão dos corredores e largura das fendas variam por seed. Plataformas móveis e frágeis aparecem em posições diferentes, com maior frequência nos ambientes correspondentes. A geração reserva espaço para iniciar os saltos e mantém chão seguro no início, checkpoint e saída. As rotas podem ser atravessadas sem habilidades especiais, solo ou em dupla.

Morrer mantém o mesmo percurso. O reinício dos elevadores restaura a posição inicial sorteada e acompanha a moeda; itens consumidos não reaparecem. Uma nova expedição gera outra distribuição. Centrais só aceitam interação na altura da área de acesso, e não enquanto o personagem ou seu parceiro cai abaixo do chão.

## Sorteio e desafios

Cada nova expedição sorteia, sem repetição, a ordem das dez regiões, **10 entre 25 protocolos HTTP** e **10 entre 25 tipos de lógica**, atribuídos às dez etapas. Os parâmetros dos exercícios de lógica e as variantes das plataformas também mudam. A ordem da nova expedição é diferente da anterior. Perder energia, pausar ou trocar aparência **não** sorteia novamente.

Os protocolos HTTP incluem GET com filtros e paginação, POST de registros, PUT de configurações, PATCH de campos e DELETE de recursos, com contratos autenticados e públicos. Não ficam vinculados a uma região específica. As respostas são obtidas por `HttpClient.sendAsync` e `HttpServer`, com validação de método, caminho, status e JSON.

A central de lógica usa acumuladores, condições, índices de listas, binário, expressões booleanas, filas, pilhas, laços, troca de variáveis e busca, além de negação, disjunção, resto, divisão inteira, laços aninhados, mínimos/máximos, somas, duplicação, funções, contagem de pares, índices circulares, ordenação e atualização de memória. Não executa código fornecido pelo usuário. Resposta errada apresenta uma dica e não custa energia.

## Pontuação

* Cada inimigo derrotado vale **100 pontos**, por salto, explosão ou bolada. O mesmo inimigo só pontua uma vez.
* Ao atravessar a saída de uma fase concluída, soma-se `máximo(0, 6000 − 10 × segundos de travessia)`. Segundos parciais são arredondados para cima. Em 2 minutos, o bônus é 4.800; a partir de 10 minutos, é zero.
* O relógio conta apenas a simulação ativa: pausas, seleção, perda de foco e desafios das centrais não consomem o bônus. O tempo gasto em novas tentativas após morrer continua contando.
* Mortes e checkpoints preservam pontos e tempo; a fase seguinte reinicia somente o relógio da fase. Nova expedição zera o placar. Fases incompletas não dão bônus de tempo.
* Na dupla, o placar é compartilhado. O cabeçalho mostra pontos, tempo total e abates; a pausa mostra o bônus possível da fase. Vitória e derrota mostram a divisão entre pontos de inimigos e bônus de tempo.

## Música por região

Dez composições originais em estilo 8-bit são sintetizadas localmente: onda de pulso na melodia, baixo triangular e percussão de ruído. Cada região tem uma partitura, tonalidade e andamento fixos, independentes da seed, dos desafios e da ordem da campanha. São loops de oito compassos. A música pausa em abrigos, seleção e centrais; ao retornar à mesma região, a composição continua sendo a mesma.

| Região | Trilha |
|---|---|
| Jardim dos endpoints | Pulso verde |
| Fortaleza das chaves | Chaves em marcha |
| Oficina das pontes | Engrenagens |
| Cavernas de cristal | Ecos prismáticos |
| Deserto dos sinais | Sinais ao vento |
| Cidade suspensa | Órbita suspensa |
| Cordilheira branca | Flocos de sinal |
| Horizonte das cinzas | Última frequência |
| Arquipélago das marés | Corrente azul |
| Reator da aurora | Aurora elétrica |

**M durante a travessia ou pausa** liga/desliga a música; na seleção inicial, M continua alternando solo/dupla. Não precisa de arquivos de áudio externos nem internet. Se o computador não tiver saída de áudio disponível, a expedição continua silenciosamente.

## Compilar e testar

Com JDK 17+ (`java`, `javac`, `jar`):

```sh
sh build.sh test
sh run.sh
```

No Windows: `build.bat test` e `run.bat`. O build gera o JAR versionado. Os testes verificam HTTP real, lógica, reprodutibilidade do sorteio, rotas em diferentes posições dos elevadores, cooperação, corda, moedas, corações, checkpoints, progressão, controles, habilidades, durações, uso único e proporções. `AllTests --render` gera capturas headless em `build/` para inspeção visual. A automação do GitHub compila e testa em Windows, Linux e macOS.

## Organização

* `GameModel`: estado da campanha, geração limitada dos mapas, física, itens e cooperação.
* `GamePanel`, `Explorer`, `RegionTheme`, `TerrainPlatform`: interface, avatares e desenho procedural.
* `CentralViews`: telas das centrais com seções de instrução, resposta e ações, feedback em múltiplas linhas e conteúdo simultâneo sem barras de rolagem.
* `Viewport`, `WindowMode`: escala proporcional, coordenadas de clique e alternância de tela cheia.
* `Lesson`, `AcademyServer`, `ApiClient`, `LabDialog`: contratos e integração HTTP.
* `LogicChallenge`, `LogicDialog`: geração e interface de raciocínio computacional.
* `Json`: parser restrito a objetos com valores string, não um parser JSON genérico.

Veja [o contrato completo](docs/API.md) e [o guia do professor](docs/GUIA-PROFESSOR.md). A credencial pública `aluno-api` serve apenas à demonstração. POST/PUT/DELETE retornam confirmações sem persistência; os recursos indicados em Location não implementam GET. Não há salvamento da campanha em disco. A aplicação requer ambiente gráfico, não terminal headless/SSH.
