package br.edu.superapi;

import java.util.Random;

/** Generated, deterministic exercises; never evaluates user-provided code. */
public record LogicChallenge(String title, String prompt, String answer, String hint) {
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
            default -> throw new IllegalArgumentException("Tipo de lógica desconhecido");
        };
    }
}
