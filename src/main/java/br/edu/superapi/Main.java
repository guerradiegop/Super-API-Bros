package br.edu.superapi;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("O jogo precisa de ambiente gráfico. Execute em um desktop Windows, Linux ou macOS.");
            return;
        }
        AcademyServer server = new AcademyServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        System.out.println("API didática ativa em " + server.baseUri());
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Super API Bros • Aprenda APIs jogando");
            GamePanel game = new GamePanel(new ApiClient(server.baseUri()));
            window.setContentPane(game); window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.pack(); window.setLocationRelativeTo(null); window.setResizable(false);
            window.setVisible(true); game.requestFocusInWindow();
        });
    }
}
