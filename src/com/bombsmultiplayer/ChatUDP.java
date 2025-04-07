package com.bombsmultiplayer;

import java.io.*;
import java.net.*;

public class ChatUDP extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private JogoGUI gui;
    private boolean isServer;

    public ChatUDP(JogoGUI gui, int port, boolean isServer) throws SocketException {
        this.gui = gui;
        this.isServer = isServer;
        this.socket = new DatagramSocket(port);
    }

    public void configurarDestino(String ip, int port) throws UnknownHostException {
        this.address = InetAddress.getByName(ip);
        this.port = port;
    }

    public void enviarMensagem(String msg) throws IOException {
        if (address == null && !isServer) {
            throw new IllegalStateException("Endereço de destino não configurado");
        }
        
        byte[] buffer = msg.getBytes();
        DatagramPacket packet;
        
        if (isServer) {
            // Servidor envia para o último cliente que mandou mensagem
            packet = new DatagramPacket(buffer, buffer.length, address, port);
        } else {
            // Cliente sempre envia para o servidor
            packet = new DatagramPacket(buffer, buffer.length, address, port);
        }
        
        socket.send(packet);
    }

    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                
                // Atualiza o endereço do remetente (importante para o servidor)
                if (isServer) {
                    this.address = packet.getAddress();
                    this.port = packet.getPort();
                }
                
                gui.adicionarMensagemChat(received);
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void parar() {
        socket.close();
    }
}