package com.bombsmultiplayer;

public class Jogador {
    private String nome;
    private int pontos;
    private int pontosSerie;

    public Jogador(String nome) {
        this.nome = nome;
        this.pontos = 0;
        this.pontosSerie = 0;
    }
    

    // Getters e Setters
    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public int getPontosSerie() { return pontosSerie; }
    public void addPonto() { pontos++; }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
    
    public void resetPontos() { pontos = 0; }
    public void addSerie() { pontosSerie += pontos; }
}