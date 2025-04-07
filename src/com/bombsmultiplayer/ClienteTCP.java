package com.bombsmultiplayer;

import java.io.*;
import java.net.*;

import javafx.application.Platform;

public class ClienteTCP {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String receberSeed() throws IOException {
        String msg = in.readLine();
        if (msg != null && msg.startsWith("SEED:")) {
            return msg.substring(5);
        }
        return null;
    }

    public void enviarJogada(int x, int y) throws IOException {
        if (out != null) {
            out.println("JOGADA:" + x + "," + y);
        }
    }

    public int[] receberJogada(JogoGUI jogo) throws IOException {
        if (in != null) {
            String msg = in.readLine();
            if (msg != null && msg.startsWith("JOGADA:")) {
                String[] parts = msg.substring(7).split(",");
                if (parts.length == 4) {
                    Platform.runLater(() -> {
                        jogo.getJogador1().setPontos(Integer.parseInt(parts[2]));
                        jogo.getJogador2().setPontos(Integer.parseInt(parts[3]));
                    });
                    return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
                }
            }
        }
        return null;
    }

    public void enviarMensagem(String msg) throws IOException {
        if (out != null) {
            out.println(msg);
        }
    }

    public String receberMensagem() throws IOException {
        return in != null ? in.readLine() : null;
    }

    

    public void stop() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexões: " + e.getMessage());
        }
    }
}