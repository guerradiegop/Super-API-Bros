package br.edu.superapi;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Shared, scrollable central surfaces; also renderable without a desktop. */
final class CentralViews {
    static final Color BG = new Color(10, 23, 37), CARD = new Color(22, 39, 55),
            INK = new Color(235, 243, 249), MUTED = new Color(166, 188, 205), MINT = new Color(108, 239, 202);
    static JPanel stack() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p;
    }
    static JTextArea text(String value, int rows, boolean code) {
        JTextArea a = new JTextArea(value, rows, 30);
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setFont(new Font(code ? Font.MONOSPACED : Font.SANS_SERIF, Font.PLAIN, 14));
        a.setBackground(CARD); a.setForeground(INK); a.setCaretColor(MINT);
        a.setBorder(new EmptyBorder(8, 10, 8, 10)); return a;
    }
    static JLabel label(String value) {
        JLabel l = new JLabel(value); l.setForeground(MUTED); l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); return l;
    }
    static JPanel card(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(CARD);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel heading = label(title); heading.setForeground(MINT); heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14));
        p.add(heading, BorderLayout.NORTH); p.add(content); return p;
    }
    static JPanel header(String eyebrow, String title, String subtitle) {
        JPanel p = stack(); p.add(label(eyebrow)); p.add(Box.createVerticalStrut(8));
        JLabel h = label(title); h.setForeground(INK); h.setFont(h.getFont().deriveFont(Font.BOLD, 25)); p.add(h);
        p.add(Box.createVerticalStrut(8)); p.add(label(subtitle)); p.add(Box.createVerticalStrut(20)); return p;
    }
    static JTextField field() {
        JTextField f = new JTextField(); f.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        f.setBackground(BG); f.setForeground(INK); f.setCaretColor(MINT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(61, 84, 102)), new EmptyBorder(8, 10, 8, 10)));
        return f;
    }
    static JButton button(String title, boolean primary) {
        JButton b = new JButton(title); b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setOpaque(true); b.setContentAreaFilled(true); b.setBackground(primary ? MINT : CARD); b.setForeground(primary ? BG : INK);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? MINT : MUTED), new EmptyBorder(10, 16, 10, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    static JScrollPane scroll(JComponent component) {
        JScrollPane p = new JScrollPane(component); p.setBorder(BorderFactory.createEmptyBorder()); p.getViewport().setBackground(CARD);
        p.getVerticalScrollBar().setUnitIncrement(16); return p;
    }
    static void install(JDialog dialog, JPanel view, Dimension preferred) {
        JScrollPane scroll = scroll(view); dialog.setContentPane(scroll);
        Rectangle screen = dialog.getOwner() == null ? GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
                : dialog.getOwner().getGraphicsConfiguration().getBounds();
        dialog.setSize(Math.min(preferred.width, screen.width - 40), Math.min(preferred.height, screen.height - 60));
        dialog.setLocationRelativeTo(dialog.getOwner()); dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    static final class Api extends JPanel {
        final JTextArea output = text("A resposta da central aparecerá aqui.", 8, true);
        final JComboBox<String> method = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        final JTextField path = field(), token = field(), type = field(), body = field(), answer = field();
        final JTextArea feedback = text("Envie o protocolo e interprete a resposta para ativar o checkpoint.", 2, false);
        final JButton send = button("Enviar requisição", true), confirm = button("Restabelecer central", true), back = button("Voltar à travessia", false);
        Api(Lesson lesson, String server) {
            setLayout(new BorderLayout(16, 16)); setBackground(BG); setBorder(new EmptyBorder(24, 24, 24, 24));
            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
            top.add(header("CENTRAL 01  /  COMUNICAÇÃO", lesson.title(), "Restaure o sinal. Preserve seu percurso."), BorderLayout.NORTH);
            JTextArea brief = text(lesson.explanation() + "\n\n" + lesson.contract() + "\n\nServidor: " + server, 5, false);
            top.add(card("Protocolo de conexão", scroll(brief))); add(top, BorderLayout.NORTH);
            JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8)); fields.setOpaque(false); path.setText("/");
            method.setBackground(BG); method.setForeground(INK);
            String[] names = {"Método HTTP", "Caminho + query", "Authorization · opcional", "Content-Type · opcional", "Corpo JSON · opcional"};
            JComponent[] controls = {method, path, token, type, body};
            for (int i = 0; i < names.length; i++) {
                JLabel l = label(names[i]); l.setLabelFor(controls[i]);
                JPanel row = new JPanel(new BorderLayout(0, 4)); row.setOpaque(false);
                row.add(l, BorderLayout.NORTH); row.add(controls[i]); fields.add(row);
            }
            JPanel request = new JPanel(new BorderLayout(0, 12)); request.setOpaque(false);
            request.add(fields); request.add(send, BorderLayout.SOUTH);
            JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0)); columns.setOpaque(false);
            columns.add(card("01  Prepare a requisição", request)); columns.add(card("02  Leia a resposta", scroll(output))); add(columns);
            JPanel result = new JPanel(new BorderLayout(8, 8)); result.setOpaque(false);
            JTextArea question = text(lesson.question(), 2, false); result.add(question, BorderLayout.NORTH);
            JPanel response = new JPanel(new BorderLayout(8, 8)); response.setOpaque(false); response.add(answer, BorderLayout.NORTH); response.add(feedback); result.add(response);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); actions.add(back); actions.add(confirm); result.add(actions, BorderLayout.SOUTH);
            answer.getAccessibleContext().setAccessibleName(lesson.question()); add(card("03  Interprete o sinal", result), BorderLayout.SOUTH);
        }
    }
    static final class Logic extends JPanel {
        final JTextField answer = field();
        final JTextArea feedback = text("Leia a sequência e encontre o resultado.", 2, false);
        final JButton submit = button("Restabelecer sequência", true), back = button("Voltar à travessia", false);
        Logic(LogicChallenge challenge) {
            setLayout(new BorderLayout(16, 16)); setBackground(BG); setBorder(new EmptyBorder(24, 24, 24, 24));
            add(header("CENTRAL 02  /  LÓGICA", challenge.title(), "Complete a sequência. Abra o próximo horizonte."), BorderLayout.NORTH);
            add(card("01  Analise a sequência", scroll(text(challenge.prompt(), 10, true))));
            JPanel result = new JPanel(new BorderLayout(8, 8)); result.setOpaque(false); result.add(answer, BorderLayout.NORTH); result.add(feedback);
            answer.getAccessibleContext().setAccessibleName("Resultado do algoritmo");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); actions.add(back); actions.add(submit); result.add(actions, BorderLayout.SOUTH);
            add(card("02  Informe o resultado", result), BorderLayout.SOUTH);
        }
    }
}
