package com.bombsmultiplayer;

import java.util.Random;

public class Tabuleiro {
    private final int tamanho = 5;
    private final boolean[][] bombas;
    private final int[][] vizinhanca;
    private int jogadas;
    private final Random random;
    private int totalBombas;

    public Tabuleiro(long seed, int totalBombas) {
        this.totalBombas = totalBombas;
        this.bombas = new boolean[tamanho][tamanho];
        this.vizinhanca = new int[tamanho][tamanho];
        this.jogadas = 0;
        this.random = new Random(seed);
        
        inicializarBombas();
        calcularVizinhanca();
    }

    public void resetComNovaSeed(long novaSeed, int totalBombas) {
        // Reinicia todas as bombas e vizinhanças
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                bombas[i][j] = false;
                vizinhanca[i][j] = 0;
            }
        }
        
        // Reconfigura o random com a nova seed
        this.random.setSeed(novaSeed);
        this.totalBombas = totalBombas;
        this.jogadas = 0;
        
        // Recria as bombas e vizinhanças
        inicializarBombas();
        calcularVizinhanca();
    }

    private void inicializarBombas() {
        int bombasColocadas = 0;
        
        while (bombasColocadas < totalBombas) {
            int x = random.nextInt(tamanho);
            int y = random.nextInt(tamanho);
            if (!bombas[x][y]) {
                bombas[x][y] = true;
                bombasColocadas++;
            }
        }
    }

    private void calcularVizinhanca() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                vizinhanca[i][j] = bombas[i][j] ? -1 : contarBombasVizinhas(i, j);
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

    public void imprimirTabuleiroConsole() {
        System.out.println("Tabuleiro (Seed: " + this.random + ")");
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                System.out.print(bombas[i][j] ? "[X] " : "[" + vizinhanca[i][j] + "] ");
            }
            System.out.println();
        }
    }

    // Getters
    public int getTamanho() { return tamanho; }
    public int getJogadas() { return jogadas;  }
    public boolean temBomba(int x, int y) { return bombas[x][y]; }
    public int getVizinhanca(int x, int y) { return vizinhanca[x][y]; }
    public boolean isVezJogador1() { return jogadas % 2 == 0; }
    public void incrementarJogada() { jogadas++; }
    public void resetJogadas() { jogadas = 0; }
}