# Super API Bros

Jogo de plataforma 2D em **Java 17**, em português, para aprender consumo de APIs jogando e programando. Explore seis fases, pule sobre bugs, colete dados e monte requisições HTTP nos terminais para liberar os portais.

Visual e personagens próprios, inspirados no gênero de plataformas clássicas. Não utiliza imagens, músicas, personagens ou marcas da Nintendo como assets.

## Jogar

Requer **Java 17 ou superior com suporte a desktop** (Windows, Linux ou macOS). Baixe o repositório em **Code → Download ZIP**, extraia e execute:

```sh
java -jar dist/super-api-bros.jar
```

O JAR executável já está incluído. Também há `run.bat` (Windows, duplo clique) e `sh run.sh` (Linux/macOS). O jogo abre uma janela; não funciona como jogo gráfico em terminal headless/SSH. Caso `java` não seja encontrado, instale um JDK 17+ e configure o PATH. Em máquinas escolares, confirme que aplicações Java podem abrir uma porta local.

**Não exige internet, Maven, Gradle, cadastro ou chave de API.** Um servidor HTTP real inicia em `127.0.0.1`, numa porta livre, e encerra com o jogo. O endereço aparece no terminal e no laboratório. Os dados são didáticos e não persistem entre execuções.

| Controle | Ação |
|---|---|
| Enter | Iniciar |
| A/D ou setas esquerda/direita | Mover |
| Espaço, W ou seta para cima | Pular |
| E, perto do terminal `{ }` | Conectar à central HTTP |
| P | Pausar/continuar; o jogo pausa ao perder foco |
| R, após vitória ou derrota | Reiniciar |

Vá até o terminal, leia o contrato, preencha os campos e envie a requisição. O painel mostra status, cabeçalhos, duração e corpo da resposta. Interprete o campo JSON solicitado para liberar a passagem. Alcance a bandeira para avançar. São três vidas; resolver o laboratório ativa um checkpoint. Erros HTTP não custam vidas e podem ser repetidos livremente.

## O que os alunos aprendem

| Fase | Prática | Conceitos |
|---|---|---|
| Jardim dos endpoints | `GET /world/1` | Cliente, servidor, endpoint, contrato, JSON, 200, 404, 405 |
| Fortaleza das chaves | `GET /keys?color=blue` | Query, cabeçalhos, token didático, 400 e 401 |
| Oficina das pontes | `POST /bridges` | Corpo JSON, Content-Type, 201, 415 e 422 |
| Cavernas de cristal | `GET /weather?region=crystal` | Consulta de sensores e filtro por região |
| Deserto dos sinais | `PUT /beacon` | Substituição de configuração e idempotência |
| Cidade suspensa | `DELETE /interference` | Remoção autenticada e interpretação do resultado |

Uma API é uma interface que permite a programas usar funcionalidades e dados de outros componentes. APIs podem servir para consultar endereços, integrar pagamentos ou acessar informações meteorológicas. Este projeto ensina **APIs HTTP**; nem toda API usa HTTP ou JSON.

O jogo realmente faz `HttpClient.sendAsync` para um `HttpServer`: não simula respostas no cliente. A interface não bloqueia enquanto espera a rede. O cliente verifica o status e interpreta o JSON antes de liberar a missão. O servidor expõe apenas o endereço local. Não há conexão com APIs públicas nesta versão: a atividade de extensão está no guia do professor.

## Compilar e testar

Com **JDK 17+** (`java`, `javac` e `jar` no PATH):

```sh
# Linux/macOS
sh build.sh test
sh run.sh
```

```bat
rem Windows
build.bat test
run.bat
```

O build gera `dist/super-api-bros.jar`. Execute o build novamente após editar o código; `run` só compila automaticamente quando o JAR ainda não existe. Os testes independem de interface gráfica e internet; validam HTTP real, JSON, bloqueio de URLs externas, salto, colisão, checkpoint, avanço e vitória.

## Estudar o código

* `ApiClient.java`: constrói e envia as requisições; ponto de partida dos exercícios.
* `AcademyServer.java`: atende requisições, valida entradas e responde em JSON.
* `Lesson.java`: contratos, perguntas e validação das missões.
* `LabDialog.java`: editor HTTP e inspeção das respostas.
* `GameModel.java`: movimento, colisões, vidas e progressão, sem dependência de janela.
* `GamePanel.java`: desenho procedural e teclado.
* `Json.java`: parser **limitado a objetos de strings**, suficiente para este contrato; não é um parser JSON genérico.

Veja [o guia de aula e exercícios](docs/GUIA-PROFESSOR.md) e [o contrato completo](docs/API.md). A automação em `.github/workflows/build.yml` compila e testa nos três sistemas e disponibiliza um JAR nos artifacts. O JAR versionado deve ser regenerado junto com alterações de código.

## Limites desta versão

As seis regiões têm 4.800 unidades de extensão cada (antes: 2.400), 12 plataformas elevadas (antes: 5), 18 dados e 11 inimigos (antes: 2). Floresta, fortaleza, oficina, cavernas de cristal, deserto e cidade suspensa usam paletas e silhuetas próprias com paralaxe. As posições dos buracos e escadas variam por região. Patrulheiros caminham no chão e em plataformas; saltadores pulam e drones oscilam no ar. Todos podem ser vencidos por um salto sobre o topo; contato lateral causa dano.

A interface se apresenta como **Rede dos Horizontes**: uma expedição para restabelecer centrais isoladas. Terminais, protocolos e respostas HTTP fazem parte da narrativa. Os controles e contratos continuam explícitos para manter a experiência acessível.

PUT e DELETE, como POST, são demonstrações sem persistência: repetir a mesma requisição retorna a mesma confirmação. A partida não salva em disco. POST demonstra criação e retorna `201` e `Location`, mas não mantém um cadastro de pontes nem implementa GET no endereço de Location. A credencial `aluno-api` é pública e exclusivamente didática. Para uma API de produção, use autenticação real, HTTPS e uma biblioteca JSON completa.
