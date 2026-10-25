package br.edu.superapi;

import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public final class ExpansionTests {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    public static void run(ApiClient client) throws Exception {
        check(Lesson.ALL.length == 25 && LogicChallenge.COUNT == 25, "25 desafios de cada tipo");
        for (Lesson lesson : Lesson.ALL) {
            String auth = lesson.authenticated() ? "Bearer aluno-api" : "";
            if (lesson.authenticated()) check(client.send(lesson.method(), lesson.path(), "", "application/json", lesson.requestBody()).get(7, TimeUnit.SECONDS).statusCode() == 401, "Autenticação: " + lesson.title());
            if (!lesson.requestBody().isEmpty()) {
                check(client.send(lesson.method(), lesson.path(), auth, "", lesson.requestBody()).get(7, TimeUnit.SECONDS).statusCode() == 415, "Tipo: " + lesson.title());
                check(client.send(lesson.method(), lesson.path(), auth, "application/json", "{").get(7, TimeUnit.SECONDS).statusCode() == 400, "JSON: " + lesson.title());
                check(client.send(lesson.method(), lesson.path(), auth, "application/json", "{}").get(7, TimeUnit.SECONDS).statusCode() == 422, "Valor: " + lesson.title());
            }
            for (String region : GameModel.REGION_NAMES) check(!(lesson.title() + lesson.explanation() + lesson.contract() + lesson.answer()).contains(region), "Contexto independente de região");
        }
        for (int seed = 0; seed < 50; seed++) for (int kind = 10; kind < 25; kind++) {
            Random random = new Random(seed); int a = 2 + random.nextInt(8), b = 2 + random.nextInt(5), n = 3 + random.nextInt(5);
            int repeated = a; for (int i = 0; i < b; i++) repeated *= 2;
            int sum = 0; for (int i = 1; i <= n; i++) sum += i;
            int[] values = {a,b,n}; Arrays.sort(values);
            String[] expected = {a <= b ? "verdadeiro" : "falso",a <= b ? "verdadeiro" : "falso","1",""+n,""+(a*b),""+values[0],""+values[2],""+sum,""+repeated,""+(a+b+b),""+n,""+((n-1+b)%n),""+(a+b),""+a,""+(a+n)};
            var puzzle = LogicChallenge.create(kind, new Random(seed));
            check(puzzle.accepts(expected[kind-10]) && !puzzle.accepts("errado"), "Gabarito lógica " + kind);
        }
        Set<String> apis = new HashSet<>(), logic = new HashSet<>();
        for (int seed = 0; seed < 100; seed++) {
            GameModel m = new GameModel(seed); Set<String> selectedApi = new HashSet<>(), selectedLogic = new HashSet<>();
            for (int phase = 0; phase < 10; phase++) { m.level = phase; selectedApi.add(m.apiLesson().title()); selectedLogic.add(m.logicChallenge().title()); }
            check(selectedApi.size() == 10 && selectedLogic.size() == 10, "Sorteio sem reposição"); apis.addAll(selectedApi); logic.addAll(selectedLogic);
        }
        check(apis.size() == 25 && logic.size() == 25, "Todos os desafios participam do sorteio");
        Set<Integer> tracks = new HashSet<>();
        for (int region = 0; region < 10; region++) {
            byte[] first = RegionMusic.compose(region), second = RegionMusic.compose(region);
            check(Arrays.equals(first, second), "Música fixa por região"); tracks.add(Arrays.hashCode(first));
            check(first.length > RegionMusic.RATE * 20 && first.length % 2 == 0, "Loop com duração e PCM válidos");
            int peak = 0; long energy = 0;
            for (int i = 0; i < first.length; i += 2) { int sample = (short)((first[i] & 255) | (first[i + 1] << 8)); peak = Math.max(peak, Math.abs(sample)); energy += Math.abs(sample); }
            check(peak < 32767 && energy > 1000000, "Faixa audível sem clipping");
        }
        check(tracks.size() == 10, "Dez composições distintas");
        SwingUtilities.invokeAndWait(() -> {
            for (int[] size : new int[][]{{760,500},{1080,620},{1400,800}}) {
                for (Lesson lesson : Lesson.ALL) {
                    var view = new CentralViews.Api(lesson, client.baseUri().toString());
                    view.output.setText("HTTP 401 • 25 ms\nContent-Type: application/json; charset=utf-8\nWWW-Authenticate: Bearer\n\n{\"error\":\"Envie Authorization: Bearer aluno-api.\"}");
                    view.setSize(size[0], size[1]); layout(view); inspect(view);
                }
                for (int kind = 0; kind < 25; kind++) { var view = new CentralViews.Logic(LogicChallenge.create(kind, new Random(2))); view.setSize(size[0], size[1]); layout(view); inspect(view); }
            }
        });
        System.out.println("OK: " + checks + " verificações de bancos, centrais e músicas.");
    }
    private static void layout(Container root) { root.doLayout(); for (Component c : root.getComponents()) if (c instanceof Container child) layout(child); }
    private static void inspect(Container root) {
        check(!(root instanceof JScrollPane), "Central sem barras de rolagem");
        if (root instanceof JTextArea text) check(text.getPreferredSize().height <= text.getHeight(), "Texto completo: " + text.getText());
        if (root instanceof JComboBox<?>) return; // Renderer panes are intentionally not displayed as children.
        for (Component c : root.getComponents()) if (c instanceof Container child) { check(c.getWidth() > 0 && c.getHeight() > 0, "Controle visível: " + c.getClass().getName()); inspect(child); }
    }
}
