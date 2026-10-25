package br.edu.superapi;

import java.awt.*;
import javax.swing.*;

public final class LogicDialog extends JDialog {
    private boolean solved;
    public LogicDialog(Window owner, LogicChallenge challenge) {
        super(owner, "Central de lógica — " + challenge.title(), ModalityType.APPLICATION_MODAL);
        CentralViews.Logic view = new CentralViews.Logic(challenge);
        var answer = view.answer; var feedback = view.feedback; var submit = view.submit; var back = view.back;
        CentralViews.install(this, view, new Dimension(780, 650));
        submit.addActionListener(e -> {
            if (challenge.accepts(answer.getText())) { solved = true; dispose(); }
            else feedback.setText("Tente novamente. " + challenge.hint());
        });
        answer.addActionListener(e -> submit.doClick()); back.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(submit); setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }
    public boolean solved() { return solved; }
}
