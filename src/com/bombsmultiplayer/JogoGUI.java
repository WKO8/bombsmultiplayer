package com.bombsmultiplayer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class JogoGUI extends Application {
    private Tabuleiro tabuleiro;
    private final Jogador jogador1 = new Jogador("Jogador 1");
    private final Jogador jogador2 = new Jogador("Jogador 2");
    private Button[][] botoes = new Button[5][5];
    private Label lblJogador, lblPontos1, lblPontos2, lblSerie;
    private Button btnReiniciar;

    @Override
    public void start(Stage primaryStage) {
        tabuleiro = new Tabuleiro();
        GridPane grid = criarGrade();
        VBox painelSuperior = criarPainelControle();
        
        BorderPane root = new BorderPane();
        root.setTop(painelSuperior);
        root.setCenter(grid);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 350, 400);
        primaryStage.setTitle("Bombs Multiplayer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox criarPainelControle() {
        // Labels de status
        lblJogador = new Label("Vez: " + jogador1.getNome());
        lblPontos1 = new Label(jogador1.getNome() + ": 0 pontos");
        lblPontos2 = new Label(jogador2.getNome() + ": 0 pontos");
        lblSerie = new Label("Série: 0 pontos");
        
        // Botão de reinício
        btnReiniciar = new Button("Reiniciar Jogo");
        btnReiniciar.setOnAction(e -> reiniciarJogo());
        
        VBox painel = new VBox(10, lblJogador, lblPontos1, lblPontos2, lblSerie, btnReiniciar);
        painel.setPadding(new Insets(10));
        return painel;
    }

    private GridPane criarGrade() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        
        for (int i = 0; i < tabuleiro.getTamanho(); i++) {
            for (int j = 0; j < tabuleiro.getTamanho(); j++) {
                Button btn = new Button();
                btn.setMinSize(50, 50);
                btn.setFont(Font.font(14));
                
                final int x = i, y = j;
                btn.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        jogar(x, y);
                    }
                });
                
                botoes[i][j] = btn;
                grid.add(btn, j, i);
            }
        }
        return grid;
    }

    private void jogar(int x, int y) {
        if (tabuleiro.temBomba(x, y)) {
            fimDeJogo(x, y);
        } else {
            Jogador jogadorAtual = tabuleiro.isVezJogador1() ? jogador1 : jogador2;
            jogadorAtual.addPonto();
            
            Button btn = botoes[x][y];
            btn.setDisable(true);
            btn.setText(String.valueOf(tabuleiro.getVizinhanca(x, y)));
            
            // Pinta de acordo com o jogador
            if (tabuleiro.isVezJogador1()) {
                btn.setStyle("-fx-background-color: #ADD8E6;"); // Azul
            } else {
                btn.setStyle("-fx-background-color: #90EE90;"); // Verde
            }
            
            tabuleiro.incrementarJogada();
            atualizarInterface();
        }
    }

    private void fimDeJogo(int x, int y) {
        botoes[x][y].setText("💣");
        botoes[x][y].setStyle("-fx-background-color: #FF6347;");
        
        Jogador perdedor = tabuleiro.isVezJogador1() ? jogador1 : jogador2;
        Jogador vencedor = tabuleiro.isVezJogador1() ? jogador2 : jogador1;
        
        vencedor.addSerie();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fim de Jogo");
        alert.setHeaderText(perdedor.getNome() + " perdeu!");
        alert.setContentText(vencedor.getNome() + " venceu com " + vencedor.getPontos() + " pontos!\n" +
                           "Pontos da série: " + vencedor.getPontosSerie());
        alert.showAndWait();
        
        reiniciarJogo();
    }

    private void reiniciarJogo() {
        tabuleiro = new Tabuleiro();
        for (int i = 0; i < tabuleiro.getTamanho(); i++) {
            for (int j = 0; j < tabuleiro.getTamanho(); j++) {
                botoes[i][j].setText("");
                botoes[i][j].setStyle("");
                botoes[i][j].setDisable(false);
            }
        }
        jogador1.resetPontos();
        jogador2.resetPontos();
        atualizarInterface();
    }

    private void atualizarInterface() {
        lblJogador.setText("Vez: " + (tabuleiro.isVezJogador1() ? jogador1.getNome() : jogador2.getNome()));
        lblPontos1.setText(jogador1.getNome() + ": " + jogador1.getPontos() + " pontos");
        lblPontos2.setText(jogador2.getNome() + ": " + jogador2.getPontos() + " pontos");
        lblSerie.setText("Série: " + (tabuleiro.isVezJogador1() ? jogador2.getPontosSerie() : jogador1.getPontosSerie()) + " pontos");
    }

    public static void main(String[] args) {
        launch(args);
    }
}