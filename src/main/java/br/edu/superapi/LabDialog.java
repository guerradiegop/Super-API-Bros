package br.edu.superapi;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;

/** Editor livre: o aluno lê o contrato, testa erros e interpreta a resposta. */
public final class LabDialog extends JDialog {
    private final JTextArea output;
    private CompletableFuture<HttpResponse<String>> pending;
    private boolean validResponse;
    private boolean solved;
    public LabDialog(Window owner, ApiClient client, Lesson lesson) {
        super(owner, "Central de comunicação — " + lesson.title(), ModalityType.APPLICATION_MODAL);
        CentralViews.Api view = new CentralViews.Api(lesson, client.baseUri().toString());
        output = view.output;
        var method = view.method; var path = view.path; var token = view.token;
        var type = view.type; var body = view.body; var answer = view.answer;
        var send = view.send; var confirm = view.confirm; var back = view.back; var feedback = view.feedback;
        CentralViews.install(this, view, new Dimension(1080, 650));
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
                    String headers = "Content-Type: " + response.headers().firstValue("Content-Type").orElse("—");
                    for (String name : new String[]{"Allow", "Location", "WWW-Authenticate"})
                        if (response.headers().firstValue(name).isPresent()) headers += "\n" + name + ": " + response.headers().firstValue(name).orElse("");
                    output.setText("HTTP " + response.statusCode() + " • " + millis + " ms\n" + headers + "\n\n" + response.body());
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
        answer.addActionListener(e -> confirm.doClick());
    }
    public boolean solved() { return solved; }
    @Override public void dispose() {
        if (pending != null) pending.cancel(true);
        super.dispose();
    }
}
