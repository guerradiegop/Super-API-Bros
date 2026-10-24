package br.edu.superapi;

import java.awt.*;
import javax.swing.*;

public final class LogicDialog extends JDialog {
    private boolean solved;
    public LogicDialog(Window owner, LogicChallenge challenge) {
        super(owner, "Central de lógica — " + challenge.title(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24)); setContentPane(root);
        JTextArea prompt = new JTextArea("Restaure a sequência da central.\n\n" + challenge.prompt());
        prompt.setEditable(false); prompt.setLineWrap(true); prompt.setWrapStyleWord(true);
        prompt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17)); root.add(new JScrollPane(prompt));
        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        JTextField answer = new JTextField(); answer.setToolTipText("Digite o resultado do algoritmo");
        actions.add(new JLabel("Resultado:")); actions.add(answer);
        JLabel feedback = new JLabel("Analise as instruções na ordem apresentada."); actions.add(feedback);
        JButton submit = new JButton("Restabelecer sequência"); actions.add(submit);
        JButton back = new JButton("Retomar travessia"); actions.add(back); root.add(actions, BorderLayout.SOUTH);
        submit.addActionListener(e -> {
            if (challenge.accepts(answer.getText())) { solved = true; dispose(); }
            else feedback.setText("Tente novamente. " + challenge.hint());
        });
        answer.addActionListener(e -> submit.doClick()); back.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(submit); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 490); setLocationRelativeTo(owner);
    }
    public boolean solved() { return solved; }
}
