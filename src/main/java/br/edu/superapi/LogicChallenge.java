package br.edu.superapi;

import java.util.Random;

/** Generated, deterministic exercises; never evaluates user-provided code. */
public record LogicChallenge(String title, String prompt, String answer, String hint) {
    public static final int COUNT = 25;
    public boolean accepts(String input) { return input != null && answer.equalsIgnoreCase(input.trim()); }
    public static LogicChallenge create(int kind, Random random) {
        int a = 2 + random.nextInt(8), b = 2 + random.nextInt(5), n = 3 + random.nextInt(5);
        return switch (kind) {
            case 0 -> new LogicChallenge("Acumulador", "soma = " + a + "\nRepita " + n + " vezes: soma = soma + " + b + "\nQual será soma?", "" + (a + n * b), "Some o incremento uma vez por repetição.");
            case 1 -> new LogicChallenge("Decisão", "x = " + a + "\nSe x % 2 == 0: saída = x * " + b + "\nSenão: saída = x + " + b + "\nQual será saída?", "" + (a % 2 == 0 ? a * b : a + b), "% representa o resto da divisão.");
            case 2 -> new LogicChallenge("Memória", "v = [" + a + ", " + b + ", " + n + "]\nÍndices começam em 0.\nQual é v[0] + v[2]?", "" + (a + n), "O índice zero indica o primeiro elemento.");
            case 3 -> new LogicChallenge("Sinal binário", "Converta o binário " + Integer.toBinaryString(a + b) + " em decimal.", "" + (a + b), "Da direita para a esquerda: pesos 1, 2, 4, 8...");
            case 4 -> new LogicChallenge("Conjunção", "a = " + a + "; b = " + b + "\n(a > b) E (a % 2 == 0)\nResponda verdadeiro ou falso.", "" + (a > b && a % 2 == 0 ? "verdadeiro" : "falso"), "E exige que ambas as condições sejam verdadeiras.");
            case 5 -> new LogicChallenge("Fila de mensagens", "Fila inicial: [" + a + ", " + b + "]\nRemova o primeiro elemento; acrescente " + n + " ao fim.\nQual é agora o primeiro elemento?", "" + b, "Uma fila retira os elementos na ordem de chegada.");
            case 6 -> new LogicChallenge("Pilha de sinais", "Pilha vazia. Empilhe " + a + ", depois " + b + ", depois " + n + ".\nRetire o topo uma vez. Qual valor está no topo agora?", "" + b, "O último que entra é o primeiro que sai.");
            case 7 -> new LogicChallenge("Laço limitado", "x = 0\nEnquanto x < " + (n * b - 1) + ": x = x + " + b + "\nQuantas vezes o corpo será executado?", "" + n, "A condição é testada antes de cada repetição.");
            case 8 -> new LogicChallenge("Troca de registros", "a = " + a + "; b = " + b + "\ntemp = a; a = b; b = temp\nQual é o valor final de b?", "" + a, "A variável temporária preserva o valor original.");
            case 9 -> new LogicChallenge("Busca", "Lista: [" + a + ", " + (a + b) + ", " + (a + b * 2) + ", " + (a + b * 3) + "]\nÍndices começam em 0. Em qual índice está " + (a + b * 2) + "?", "2", "Conte as posições, começando pelo zero.");
            case 10 -> new LogicChallenge("Disjunção", "x = " + a + "; y = " + b + "\n(x < y) OU (x == y)\nResponda verdadeiro ou falso.", a <= b ? "verdadeiro" : "falso", "OU precisa de ao menos uma condição verdadeira.");
            case 11 -> new LogicChallenge("Negação", "x = " + a + "\nNÃO (x > " + b + ")\nResponda verdadeiro ou falso.", a <= b ? "verdadeiro" : "falso", "NÃO inverte o resultado lógico.");
            case 12 -> new LogicChallenge("Resto de divisão", "Há " + (a * b + 1) + " pacotes. Cada lote comporta " + b + ".\nQuantos pacotes sobram após formar todos os lotes completos?", "1", "Use o resto da divisão inteira.");
            case 13 -> new LogicChallenge("Contagem regressiva", "x = " + (n * b) + "\nEnquanto x > 0: x = x - " + b + "\nQuantas repetições ocorrem?", "" + n, "Conte cada subtração até atingir zero.");
            case 14 -> new LogicChallenge("Laços aninhados", "total = 0\nRepita " + a + " vezes:\n  Repita " + b + " vezes: total = total + 1\nQual é total?", "" + (a * b), "O laço interno se repete a cada passo do externo.");
            case 15 -> new LogicChallenge("Menor registro", "Valores: [" + a + ", " + b + ", " + n + "]\nQual é o menor valor?", "" + Math.min(a, Math.min(b, n)), "Compare todos os registros.");
            case 16 -> new LogicChallenge("Maior registro", "Valores: [" + a + ", " + b + ", " + n + "]\nQual é o maior valor?", "" + Math.max(a, Math.max(b, n)), "Guarde o maior encontrado a cada comparação.");
            case 17 -> new LogicChallenge("Soma de sequência", "total = 0\nPara i de 1 até " + n + " (inclusive): total = total + i\nQual é total?", "" + (n * (n + 1) / 2), "Inclua o limite final na soma.");
            case 18 -> new LogicChallenge("Duplicação", "x = " + a + "\nRepita " + b + " vezes: x = x * 2\nQual é x?", "" + (a * (1 << b)), "Cada passo dobra o valor atual.");
            case 19 -> new LogicChallenge("Função de ajuste", "ajustar(x) retorna x + " + b + "\nQual é ajustar(ajustar(" + a + "))?", "" + (a + 2 * b), "Resolva primeiro a chamada interna.");
            case 20 -> new LogicChallenge("Contagem de pares", "Percorra os inteiros de 1 até " + (2 * n) + " (inclusive).\nQuantos são pares?", "" + n, "Um número par tem resto zero ao dividir por 2.");
            case 21 -> new LogicChallenge("Índice circular", "Um buffer tem " + n + " posições, de 0 a " + (n - 1) + ".\nPartindo do índice " + (n - 1) + ", avance " + b + " posições.\nAo passar do fim, volte a 0. Qual é o índice final?", "" + ((n - 1 + b) % n), "Use o resto da divisão pelo tamanho do buffer.");
            case 22 -> new LogicChallenge("Ordenação", "Ordene em ordem crescente: [" + (a + b) + ", " + a + ", " + (a + 2 * b) + "]\nQual valor ocupará a posição do meio?", "" + (a + b), "O valor central fica entre o menor e o maior.");
            case 23 -> new LogicChallenge("Divisão inteira", "x = " + (a * b + b - 1) + "\ny = x DIV " + b + "\nDIV descarta a parte fracionária. Qual é y?", "" + a, "Conte somente grupos completos.");
            case 24 -> new LogicChallenge("Atualização de memória", "v = [" + a + ", " + b + ", " + n + "]\nv[1] = v[0] + v[2]\nÍndices começam em 0. Qual é v[1]?", "" + (a + n), "Use os valores originais das posições 0 e 2.");
            default -> throw new IllegalArgumentException("Tipo de lógica desconhecido");
        };
    }
}
