package com.bombsmultiplayer;

import java.io.*;
import java.net.*;

import javafx.application.Platform;

public class ServidorTCP {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Servidor aguardando conexão...");
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Cliente conectado!");
    }

    public void enviarSeed(long seed) throws IOException {
        if (out != null) {
            out.println("SEED:" + seed);
            System.out.println("Seed enviada para o cliente: " + seed);
        }
    }

    public void enviarNovaSeed(long seed) throws IOException {
        if (out != null) {
            out.println("NOVA_SEED:" + seed);
            System.out.println("Nova seed enviada para o cliente: " + seed);
        }
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
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexões: " + e.getMessage());
        }
    }
}