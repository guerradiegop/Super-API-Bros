package br.edu.superapi;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;

/** Editor livre: o aluno lê o contrato, testa erros e interpreta a resposta. */
public final class LabDialog extends JDialog {
    private final JTextArea output = area("Envie uma requisição para inspecionar a resposta HTTP.", 8);
    private CompletableFuture<HttpResponse<String>> pending;
    private boolean validResponse;
    private boolean solved;
    public LabDialog(Window owner, ApiClient client, Lesson lesson) {
        super(owner, "Central de comunicação — " + lesson.title(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        setContentPane(root);
        JTextArea intro = area(lesson.explanation() + "\n\nPROTOCOLO DA CENTRAL\n" + lesson.contract()
                + "\n\nServidor: " + client.baseUri(), 12);
        root.add(intro, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 5));
        JComboBox<String> method = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        JTextField path = new JTextField("/"), token = new JTextField(), type = new JTextField(), body = new JTextField();
        fields.add(new JLabel("Método HTTP")); fields.add(method);
        fields.add(new JLabel("Caminho + query")); fields.add(path);
        fields.add(new JLabel("Authorization (opcional)")); fields.add(token);
        fields.add(new JLabel("Content-Type (opcional)")); fields.add(type);
        fields.add(new JLabel("Corpo JSON (opcional)")); fields.add(body);
        JButton send = new JButton("Enviar requisição HTTP");
        fields.add(new JLabel("Consulte o protocolo desta central.")); fields.add(send);
        center.add(fields, BorderLayout.NORTH);
        center.add(new JScrollPane(output), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        JPanel south = new JPanel(new GridLayout(0, 1, 4, 4));
        south.add(new JLabel(lesson.question()));
        JTextField answer = new JTextField(); south.add(answer);
        JLabel feedback = new JLabel("Uma resposta correta + sua interpretação restabelecem esta central."); south.add(feedback);
        JButton confirm = new JButton("Validar resposta e restabelecer central"); south.add(confirm);
        JButton back = new JButton("Retomar travessia"); south.add(back);
        root.add(south, BorderLayout.SOUTH);
        send.addActionListener(event -> {
            validResponse = false; send.setEnabled(false);
            String sentMethod = (String) method.getSelectedItem(), sentPath = path.getText().trim();
            long started = System.nanoTime();
            output.setText(sentMethod + " " + client.baseUri() + sentPath + "\nAguardando resposta…");
            try {
                pending = client.send(sentMethod, sentPath, token.getText().trim(), type.getText().trim(), body.getText());
                pending.whenComplete((response, error) -> SwingUtilities.invokeLater(() -> {
                    if (!isDisplayable()) return;
                    send.setEnabled(true);
                    if (error != null) {
                        output.append("\nFalha de conexão ou timeout. Confira se o servidor está ativo e tente novamente.");
                        return;
                    }
                    long millis = (System.nanoTime() - started) / 1_000_000;
                    output.setText(sentMethod + " " + response.uri() + "\nHTTP " + response.statusCode() + " • " + millis + " ms\n");
                    response.headers().map().forEach((key, values) -> output.append(key + ": " + String.join(", ", values) + "\n"));
                    output.append("\n" + response.body());
                    validResponse = lesson.accepts(sentMethod, sentPath, response);
                    feedback.setText(validResponse ? "Sucesso! Leia o JSON e responda à pergunta." : "Ainda não: compare método, endpoint, cabeçalhos e corpo com o contrato.");
                }));
            } catch (IllegalArgumentException ex) {
                send.setEnabled(true); output.setText("Requisição inválida: " + ex.getMessage());
            }
        });
        confirm.addActionListener(event -> {
            if (validResponse && lesson.answer().equals(answer.getText().trim())) { solved = true; dispose(); }
            else feedback.setText("Envie a requisição da missão e copie o valor solicitado, sem aspas.");
        });
        back.addActionListener(event -> dispose());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 750); setLocationRelativeTo(owner);
    }
    private static JTextArea area(String text, int rows) {
        JTextArea area = new JTextArea(text, rows, 60);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        return area;
    }
    public boolean solved() { return solved; }
    @Override public void dispose() {
        if (pending != null) pending.cancel(true);
        super.dispose();
    }
}
