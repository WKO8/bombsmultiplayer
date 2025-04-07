package com.bombsmultiplayer;

import java.io.IOException;
import java.net.InetAddress;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
    
    private boolean modoOnline = false;
    private boolean ehServidor = false;
    private ServidorTCP servidor;
    private ClienteTCP cliente;
    private ChatUDP chat;
    private TextArea areaChat;
    private TextField campoMensagem;
    private Scene scene;
    private long seed;
    private int jogadas;
    private int dificuldade = 8;

    @Override
    public void start(Stage primaryStage) {
        // Diálogo inicial para escolher modo
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Local", "Local", "Online (Servidor)", "Online (Cliente)");
        dialog.setTitle("Bombs Multiplayer");
        dialog.setHeaderText("Selecione o modo de jogo");
        dialog.setContentText("Modo:");

        ChoiceDialog<Integer> dialogDificuldade = new ChoiceDialog<>(8, 1, 5, 8, 10, 12);
        dialogDificuldade.setTitle("Dificuldade");
        dialogDificuldade.setHeaderText("Selecione a quantidade de bombas");
        dificuldade = dialogDificuldade.showAndWait().orElse(8);

        String resultado = dialog.showAndWait().orElse("Local");
        
        // Primeiro cria a interface
        BorderPane root = new BorderPane();
        VBox painelSuperior = criarPainelControle();
        GridPane grid = criarGrade();
        VBox painelChat = criarInterfaceChat();
        
        root.setTop(painelSuperior);
        root.setCenter(grid);
        root.setRight(painelChat);
        root.setPadding(new Insets(10));

        scene = new Scene(root, 550, 500);
        primaryStage.setTitle("Bombs Multiplayer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Depois configura a rede (se necessário)
        if (resultado.contains("Online")) {
            modoOnline = true;
            ehServidor = resultado.contains("Servidor");
            configurarRede();

            if (!modoOnline) {
                this.seed = System.currentTimeMillis();
                tabuleiro = new Tabuleiro(seed, dificuldade);
            }
        } else {
            // Modo local
            this.seed = System.currentTimeMillis();
            tabuleiro = new Tabuleiro(seed, dificuldade);
            atualizarInterface();
        }
    }
    private VBox criarInterfaceChat() {
        areaChat = new TextArea();
        areaChat.setEditable(false);
        areaChat.setWrapText(true);
        areaChat.setPrefHeight(300);
        
        campoMensagem = new TextField();
        campoMensagem.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                enviarMensagemChat();
            }
        });
        
        Button btnEnviar = new Button("Enviar");
        btnEnviar.setOnAction(e -> enviarMensagemChat());
        
        HBox painelEnvio = new HBox(5, campoMensagem, btnEnviar);
        painelEnvio.setPadding(new Insets(5));
        
        VBox painelChat = new VBox(10, new Label("Chat"), areaChat, painelEnvio);
        painelChat.setPrefWidth(200);
        painelChat.setPadding(new Insets(5));
        
        return painelChat;
    }

    private void configurarRede() {
        try {
            if (ehServidor) {
                this.seed = System.currentTimeMillis();
                tabuleiro = new Tabuleiro(seed, dificuldade);
                servidor = new ServidorTCP();
                servidor.start(5555);
                
                servidor.enviarSeed(seed);
                servidor.enviarMensagem("ESTADO:" + tabuleiro.getJogadas() + ":" + 
                                      jogador1.getPontos() + ":" + jogador2.getPontos());
                
                chat = new ChatUDP(this, 5556, true);
            } else {
                // Cliente
                TextInputDialog dialog = new TextInputDialog("localhost");
                dialog.setTitle("Conectar ao Servidor");
                dialog.setHeaderText("Digite o IP do servidor");
                String ip = dialog.showAndWait().orElse("localhost");
    
                cliente = new ClienteTCP();
                cliente.start(ip, 5555);
                
                String mensagemSeed = cliente.receberMensagem();
                if (mensagemSeed != null && mensagemSeed.startsWith("SEED:")) {
                    this.seed = Long.parseLong(mensagemSeed.substring(5));
                    tabuleiro = new Tabuleiro(seed, dificuldade);
                }
                
                sincronizarEstadoInicial();
                chat = new ChatUDP(this, 5557, false);
                chat.configurarDestino(ip, 5556);
                
                // Desabilita o botão de reinício no cliente
                btnReiniciar.setDisable(true);
            }
            iniciarOuvinteJogadas();
            chat.start();
        } catch (IOException e) {
            Platform.runLater(() -> mostrarErroConexao(e));
        }
    }

    private void enviarMensagemChat() {
        String mensagem = campoMensagem.getText();
        if (!mensagem.isEmpty()) {
            try {
                String mensagemFormatada = (ehServidor ? "Servidor: " : "Cliente: ") + mensagem;
                chat.enviarMensagem(mensagemFormatada);
                areaChat.appendText("Você: " + mensagem + "\n");
                campoMensagem.clear();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro no Chat");
                alert.setHeaderText("Não foi possível enviar a mensagem");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void adicionarMensagemChat(String mensagem) {
        javafx.application.Platform.runLater(() -> {
            areaChat.appendText(mensagem + "\n");
        });
    }

    private void iniciarOuvinteJogadas() {
        new Thread(() -> {
            try {
                while (true) {
                    String mensagem = ehServidor ? 
                        servidor.receberMensagem() : 
                        cliente.receberMensagem();
                    
                    if (mensagem != null) {
                        if (mensagem.startsWith("JOGADA:")) {
                            int[] jogada = parseJogada(mensagem);
                            if (jogada != null) {
                                Platform.runLater(() -> {
                                    int x = jogada[0];
                                    int y = jogada[1];
                                    if (!tabuleiro.temBomba(x, y)) {
                                        processarJogadaRemota(x, y);
                                    }
                                });
                            }
                        } else if (mensagem.startsWith("NOVA_SEED:")) {
                            long novaSeed = Long.parseLong(mensagem.substring(10));
                            Platform.runLater(() -> {
                                this.seed = novaSeed;
                                reiniciarJogoComSeedAtual();
                            });
                        } else if (mensagem.startsWith("ESTADO:")) {
                            String[] partes = mensagem.split(":");
                            Platform.runLater(() -> {
                                this.tabuleiro.resetJogadas();
                                for (int i = 0; i < Integer.parseInt(partes[1]); i++) {
                                    this.tabuleiro.incrementarJogada();
                                }
                                this.jogador1.setPontos(Integer.parseInt(partes[2]));
                                this.jogador2.setPontos(Integer.parseInt(partes[3]));
                                atualizarInterface();
                            });
                        }
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> mostrarErroConexao(e));
            }
        }).start();
    }

    private int[] parseJogada(String mensagem) {
        if (mensagem.startsWith("JOGADA:")) {
            String[] parts = mensagem.substring(7).split(",");
            if (parts.length == 4) {
                jogador1.setPontos(Integer.parseInt(parts[2]));
                jogador2.setPontos(Integer.parseInt(parts[3]));
                return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
            }
        }
        return null;
    }

    private void jogar(int x, int y) {
        try {
            if (modoOnline) {
                if ((ehServidor && tabuleiro.isVezJogador1()) || 
                    (!ehServidor && !tabuleiro.isVezJogador1())) {
                    
                    if (ehServidor) {
                        servidor.enviarJogada(x, y);
                    } else {
                        cliente.enviarJogada(x, y);
                    }
                    
                    processarJogadaLocal(x, y);
                }
            } else {
                processarJogadaLocal(x, y);
            }
        } catch (IOException e) {
            Platform.runLater(() -> mostrarErroConexao(e));
        }
    }

    private void processarJogadaLocal(int x, int y) {
        try {
            if (tabuleiro.temBomba(x, y)) {
                fimDeJogo(x, y);
            } else {
                Jogador jogadorAtual = tabuleiro.isVezJogador1() ? jogador1 : jogador2;
                jogadorAtual.addPonto();
                
                Button btn = botoes[x][y];
                btn.setDisable(true);
                int vizinhas = tabuleiro.getVizinhanca(x, y);
                btn.setText(vizinhas > 0 ? String.valueOf(vizinhas) : "0");
                
                String cor = tabuleiro.isVezJogador1() ? "#ADD8E6" : "#90EE90";
                btn.setStyle("-fx-background-color: " + cor + ";");
                
                tabuleiro.incrementarJogada();
                atualizarInterface();
                
                if (modoOnline) {
                    try {
                        enviarJogada(x, y);
                    } catch (IOException e) {
                        Platform.runLater(() -> mostrarErroConexao(e));
                    }
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> mostrarErroConexao(e));
        }
    }

    private void processarJogadaRemota(int x, int y) {
        if (!tabuleiro.temBomba(x, y)) {
            Jogador jogadorAtual = tabuleiro.isVezJogador1() ? jogador2 : jogador1;
            jogadorAtual.addPonto();
            
            Button btn = botoes[x][y];
            btn.setDisable(true);
            int vizinhas = tabuleiro.getVizinhanca(x, y);
            btn.setText(vizinhas > 0 ? String.valueOf(vizinhas) : "0");
            
            // Mesma cor do jogador remoto
            String cor = tabuleiro.isVezJogador1() ? "#90EE90" : "#ADD8E6";
            btn.setStyle("-fx-background-color: " + cor + ";");
            
            tabuleiro.incrementarJogada();
            atualizarInterface();
        }
    }

    private void enviarJogada(int x, int y) throws IOException {
        String mensagem = String.format("JOGADA:%d,%d,%d,%d", 
            x, y, jogador1.getPontos(), jogador2.getPontos());
        if (ehServidor) {
            servidor.enviarMensagem(mensagem);
        } else {
            cliente.enviarMensagem(mensagem);
        }
    }

    private VBox criarPainelControle() {
        // Inicializa os labels primeiro
        lblJogador = new Label("Vez: " + (tabuleiro != null && tabuleiro.isVezJogador1() ? jogador1.getNome() : jogador2.getNome()));
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
        
        // Se o tabuleiro ainda não foi criado, cria um temporário
        Tabuleiro tabuleiroTemp = tabuleiro != null ? tabuleiro : new Tabuleiro(System.currentTimeMillis(), dificuldade);
        
        botoes = new Button[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button btn = new Button();
                btn.setMinSize(50, 50);
                btn.setFont(Font.font(14));
                
                final int x = i, y = j;
                btn.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY && tabuleiro != null) {
                        jogar(x, y);
                    }
                });
                
                botoes[i][j] = btn;
                grid.add(btn, j, i);
            }
        }
        return grid;
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
        if (modoOnline) {
            if (ehServidor) {
                // Servidor gera nova seed e envia para o cliente
                this.seed = System.currentTimeMillis();
                try {
                    servidor.enviarNovaSeed(seed);
                    reiniciarJogoComSeedAtual();
                } catch (IOException e) {
                    mostrarErroConexao(e);
                }
            }
            // Cliente não pode iniciar reinício, apenas responde ao servidor
        } else {
            // Modo local
            this.seed = System.currentTimeMillis();
            reiniciarJogoComSeedAtual();
        }
    }

    private void reiniciarJogoComSeedAtual() {
        tabuleiro = new Tabuleiro(seed, dificuldade);
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
        
        if (modoOnline && ehServidor) {
            try {
                servidor.enviarMensagem("ESTADO:" + tabuleiro.getJogadas() + ":" + 
                                      jogador1.getPontos() + ":" + jogador2.getPontos());
            } catch (IOException e) {
                mostrarErroConexao(e);
            }
        }
    }

    private void atualizarInterface() {
        lblJogador.setText("Vez: " + (tabuleiro.isVezJogador1() ? jogador1.getNome() : jogador2.getNome()));
        lblPontos1.setText(jogador1.getNome() + ": " + jogador1.getPontos() + " pontos");
        lblPontos2.setText(jogador2.getNome() + ": " + jogador2.getPontos() + " pontos");
        lblSerie.setText("Série: " + (tabuleiro.isVezJogador1() ? jogador2.getPontosSerie() : jogador1.getPontosSerie()) + " pontos");
    }

    private void sincronizarEstadoInicial() {
        if (modoOnline && !ehServidor) {
            new Thread(() -> {
                try {
                    // Recebe o estado inicial do servidor
                    String estado = cliente.receberMensagem();
                    if (estado != null && estado.startsWith("ESTADO:")) {
                        String[] partes = estado.split(":");
                        Platform.runLater(() -> {
                            // Verifica se os componentes UI já foram criados
                            if (lblJogador != null) {
                                this.jogadas = Integer.parseInt(partes[1]);
                                this.jogador1.setPontos(Integer.parseInt(partes[2]));
                                this.jogador2.setPontos(Integer.parseInt(partes[3]));
                                atualizarInterface();
                            }
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> mostrarErroConexao(e));
                }
            }).start();
        }
    }

    private void mostrarErroConexao(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Rede");
        alert.setHeaderText("Falha na comunicação");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
    
    public Jogador getJogador1() {
        return jogador1;
    }
    
    public Jogador getJogador2() {
        return jogador2;
    }
    public static void main(String[] args) {
        launch(args);
    }
}