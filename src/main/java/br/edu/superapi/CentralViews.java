package br.edu.superapi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Compact central surfaces with all instructions, inputs and feedback visible together. */
final class CentralViews {
    static final Color BG = new Color(10, 23, 37), CARD = new Color(22, 39, 55),
            INK = new Color(235, 243, 249), MUTED = new Color(166, 188, 205), MINT = new Color(108, 239, 202);
    static JLabel label(String value) {
        JLabel l = new JLabel(value); l.setForeground(MUTED); l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12)); return l;
    }
    /** Fits wrapped text to its allocated area, including long error messages. */
    static class Copy extends JTextArea {
        Copy(String text) {
            super(text); setEditable(false); setLineWrap(true); setWrapStyleWord(true);
            setBackground(CARD); setForeground(INK); setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            setMargin(new Insets(6, 6, 6, 6)); setMinimumSize(new Dimension(0, 0));
        }
        @Override public void doLayout() {
            super.doLayout(); fit();
        }
        private void fit() {
            if (getWidth() < 20 || getHeight() < 20) return;
            for (int size = 14; size >= 8; size--) {
                setFont(getFont().deriveFont((float)size));
                if (getPreferredSize().height <= getHeight()) break;
            }
        }
        @Override public void setText(String text) { super.setText(text); if (getFont() != null) fit(); }
    }
    static JTextArea text(String value, int rows, boolean code) {
        Copy a = new Copy(value); if (code) a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14)); return a;
    }
    static JPanel card(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(6, 8)); p.setBackground(CARD); p.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel heading = label(title); heading.setForeground(MINT); heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        p.add(heading, BorderLayout.NORTH); p.add(content); return p;
    }
    static JPanel header(String eyebrow, String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 3)); p.setOpaque(false);
        p.add(label(eyebrow)); JLabel h = label(title); h.setForeground(INK); h.setFont(h.getFont().deriveFont(Font.BOLD, 22));
        p.add(h); p.add(label(subtitle)); return p;
    }
    static JTextField field() {
        JTextField f = new JTextField(); f.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        f.setBackground(BG); f.setForeground(INK); f.setCaretColor(MINT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(61, 84, 102)), new EmptyBorder(5, 7, 5, 7))); return f;
    }
    static JButton button(String title, boolean primary) {
        JButton b = new JButton(title); b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        b.setOpaque(true); b.setContentAreaFilled(true); b.setBackground(primary ? MINT : CARD); b.setForeground(primary ? BG : INK);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? MINT : MUTED), new EmptyBorder(8, 10, 8, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    static void install(JDialog dialog, JPanel view, Dimension preferred) {
        dialog.setContentPane(view);
        GraphicsConfiguration config = dialog.getOwner() == null ? GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                : dialog.getOwner().getGraphicsConfiguration();
        Rectangle bounds = config.getBounds(); Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        int width = Math.min(preferred.width, bounds.width - insets.left - insets.right - 16);
        int height = Math.min(preferred.height, bounds.height - insets.top - insets.bottom - 16);
        dialog.setSize(width, height); dialog.setMinimumSize(new Dimension(Math.min(760, width), Math.min(540, height)));
        dialog.setLocationRelativeTo(dialog.getOwner()); dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    static JPanel row(String title, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false); JLabel label = label(title); label.setLabelFor(input);
        p.add(label, BorderLayout.NORTH); p.add(input); return p;
    }
    static final class Api extends JPanel {
        final JTextArea output = text("A resposta da central aparecerá aqui.", 0, true);
        final JComboBox<String> method = new JComboBox<>(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE"});
        final JTextField path = field(), token = field(), type = field(), body = field(), answer = field();
        final JTextArea feedback = text("Envie o protocolo e interprete a resposta para ativar o checkpoint.", 0, false);
        final JButton send = button("Enviar requisição", true), confirm = button("Restabelecer central", true), back = button("Voltar", false);
        Api(Lesson lesson, String server) {
            setLayout(new BorderLayout(12, 12)); setBackground(BG); setBorder(new EmptyBorder(16, 16, 16, 16));
            add(header("CENTRAL 01 / COMUNICAÇÃO", lesson.title(), "Restaure o sinal. Preserve seu percurso."), BorderLayout.NORTH);
            JPanel columns = new JPanel(new GridLayout(1, 3, 12, 0)); columns.setOpaque(false);
            columns.add(card("01  Protocolo", text(lesson.explanation() + "\n\n" + lesson.contract() + "\n\nServidor local:\n" + server, 0, false)));
            JPanel fields = new JPanel(new GridLayout(5, 1, 0, 6)); fields.setOpaque(false); path.setText("/");
            method.setBackground(BG); method.setForeground(INK);
            String[] names = {"Método HTTP", "Caminho + query", "Authorization · se exigido", "Content-Type · se exigido", "Corpo JSON · se exigido"};
            JComponent[] controls = {method, path, token, type, body};
            for (int i = 0; i < names.length; i++) fields.add(row(names[i], controls[i]));
            JPanel request = new JPanel(new BorderLayout(0, 10)); request.setOpaque(false); request.add(fields); request.add(send, BorderLayout.SOUTH);
            columns.add(card("02  Requisição", request));
            JPanel result = new JPanel(new BorderLayout(6, 8)); result.setOpaque(false); result.add(output);
            JPanel interpretation = new JPanel(new BorderLayout(0, 6)); interpretation.setOpaque(false);
            JTextArea question = text(lesson.question(), 0, false); question.setPreferredSize(new Dimension(100, 45));
            interpretation.add(question, BorderLayout.NORTH); interpretation.add(answer, BorderLayout.SOUTH);
            answer.getAccessibleContext().setAccessibleName(lesson.question()); result.add(interpretation, BorderLayout.SOUTH);
            columns.add(card("03  Resposta e interpretação", result)); add(columns);
            JPanel footer = new JPanel(new BorderLayout(12, 0)); footer.setOpaque(false); feedback.setPreferredSize(new Dimension(200, 48)); footer.add(feedback);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6)); actions.setOpaque(false); actions.add(back); actions.add(confirm); footer.add(actions, BorderLayout.EAST);
            add(footer, BorderLayout.SOUTH);
        }
    }
    static final class Logic extends JPanel {
        final JTextField answer = field(); final JTextArea feedback = text("Leia a sequência e encontre o resultado.", 0, false);
        final JButton submit = button("Restabelecer sequência", true), back = button("Voltar", false);
        Logic(LogicChallenge challenge) {
            setLayout(new BorderLayout(12, 12)); setBackground(BG); setBorder(new EmptyBorder(20, 20, 20, 20));
            add(header("CENTRAL 02 / LÓGICA", challenge.title(), "Complete a sequência. Abra o próximo horizonte."), BorderLayout.NORTH);
            add(card("01  Analise a sequência", text(challenge.prompt(), 0, true)));
            JPanel result = new JPanel(new BorderLayout(8, 8)); result.setOpaque(false); result.add(row("02  Informe o resultado", answer), BorderLayout.NORTH);
            feedback.setPreferredSize(new Dimension(100, 44)); result.add(feedback);
            answer.getAccessibleContext().setAccessibleName("Resultado do algoritmo");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); actions.add(back); actions.add(submit); result.add(actions, BorderLayout.SOUTH);
            add(result, BorderLayout.SOUTH);
        }
    }
}
