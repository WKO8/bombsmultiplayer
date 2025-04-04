package com.bombsmultiplayer;

import java.util.Random;

public class Tabuleiro {
    private final int tamanho = 5;
    private final boolean[][] bombas;
    private final int[][] vizinhanca;
    private int jogadas;

    public Tabuleiro() {
        bombas = new boolean[tamanho][tamanho];
        vizinhanca = new int[tamanho][tamanho];
        jogadas = 0;
        inicializarBombas();
        calcularVizinhanca();
    }

    private void inicializarBombas() {
        Random rand = new Random();
        int bombasColocadas = 0;
        int totalBombas = 1; // 8 bombas para 5x5 (cerca de 30%)
        
        while (bombasColocadas < totalBombas) {
            int x = rand.nextInt(tamanho);
            int y = rand.nextInt(tamanho);
            if (!bombas[x][y]) {
                bombas[x][y] = true;
                bombasColocadas++;
            }
        }
    }

    private void calcularVizinhanca() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (!bombas[i][j]) {
                    vizinhanca[i][j] = contarBombasVizinhas(i, j);
                }
            }
        }
    }

    private int contarBombasVizinhas(int x, int y) {
        int count = 0;
        for (int i = Math.max(0, x-1); i <= Math.min(x+1, tamanho-1); i++) {
            for (int j = Math.max(0, y-1); j <= Math.min(y+1, tamanho-1); j++) {
                if (bombas[i][j]) count++;
            }
        }
        return count;
    }

    public boolean isVezJogador1() {
        return jogadas % 2 == 0;
    }
    public void incrementarJogada() {
        jogadas++;
    }
    public void resetJogadas() {
        jogadas = 0;
    }
    public int getTamanho() { return tamanho; }
    public boolean temBomba(int x, int y) { return bombas[x][y]; }
    public int getVizinhanca(int x, int y) { return vizinhanca[x][y]; }
}