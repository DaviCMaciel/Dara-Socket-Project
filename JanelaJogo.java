import javax.swing.*;
import java.awt.*;

public class JanelaJogo extends JFrame {
    // Liga a interface ao "cérebro" do jogo
    private Tabuleiro backend;
    
    // Matriz de botões que o utilizador vai ver
    private JButton[][] botoes = new JButton[5][6];
    
    // Componentes do Chat
    private JTextArea areaChat;
    private JTextField campoTexto;

    // --- ATRIBUTOS DE ESTADO (A memória da Janela) ---
    private int jogadorAtual = Tabuleiro.JOGADOR1;
    private int lOrigem = -1; // Guarda a linha do 1º clique na movimentação
    private int cOrigem = -1; // Guarda a coluna do 1º clique na movimentação
    private boolean modoCaptura = false; // Indica se o jogador deve remover uma peça inimiga
    private boolean jogoFinalizado = false;

    public JanelaJogo(Tabuleiro tabuleiro) {
        this.backend = tabuleiro;

        // 1. Configurações básicas da janela
        setTitle("Jogo Dara - Davi Coelho Maciel");
        setSize(1000, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Organiza em Norte, Sul, Centro, etc.

        // 2. Criar o Tabuleiro (Centro)
        JPanel painelTabuleiro = new JPanel(new GridLayout(5, 6));
        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 6; c++) {
                botoes[l][c] = new JButton(" ");
                botoes[l][c].setFont(new Font("Arial", Font.BOLD, 20));
                
                // Guardamos a posição para saber onde o utilizador clicou
                final int linha = l;
                final int coluna = c;
                
                // O que acontece quando clicamos?
                botoes[l][c].addActionListener(e -> aoClicar(linha, coluna));
                
                painelTabuleiro.add(botoes[l][c]);
            }
        }
        add(painelTabuleiro, BorderLayout.CENTER);

        // 3. Criar o Chat (Direita)
        JPanel painelDireito = new JPanel(new BorderLayout());
        areaChat = new JTextArea(20, 25);
        areaChat.setEditable(false); // O utilizador não apaga o histórico
        
        campoTexto = new JTextField();
        campoTexto.addActionListener(e -> enviarMensagem());

        painelDireito.add(new JLabel("--- CHAT ---"), BorderLayout.NORTH);
        painelDireito.add(new JScrollPane(areaChat), BorderLayout.CENTER);
        painelDireito.add(campoTexto, BorderLayout.SOUTH);

        add(painelDireito, BorderLayout.EAST);

        // Mostrar a janela
        setLocationRelativeTo(null); // Centraliza no ecrã
        setVisible(true);
    }

    private void alternarTurno() {
        jogadorAtual = (jogadorAtual == Tabuleiro.JOGADOR1) ? Tabuleiro.JOGADOR2 : Tabuleiro.JOGADOR1;
    }

    private void aoClicar(int l, int c) {
        if (jogoFinalizado) {
            areaChat.append("O jogo já terminou! Reinicie para jogar novamente.\n");
            return;
        }
        if (modoCaptura) {
            faseCaptura(l, c);
            return;
        } else {
            if (backend.isFaseColocacao()) {
                faseColocacao(l, c);
            } else {
                faseMovimentacao(l, c);
            }
        }
    }
    
    private void faseCaptura(int l, int c) {
        // O jogador deve escolher uma peça inimiga para remover
        if (backend.removerPeca(jogadorAtual, l, c)) {
            atualizarTabuleiro();
            alternarTurno();
            areaChat.append("Peça inimiga removida! Próxima jogada: Jogador " + jogadorAtual + "\n");
            modoCaptura = false;
            int vencedor = backend.verificarVencedor();
            if (vencedor != 0) {
                JOptionPane.showMessageDialog(this, "O JOGADOR " + vencedor + " VENCEU!");
                jogoFinalizado = true;
            }
        } else {
            areaChat.append("Jogada inválida! Escolha uma peça inimiga para remover.\n");
        }
    }
    
    
    private void faseColocacao(int l, int c) {
        if (backend.colocarPeca(jogadorAtual, l, c)) {
            atualizarTabuleiro();
            alternarTurno();
            areaChat.append("Peça colocada! Próxima jogada: Jogador " + jogadorAtual + "\n");
        } else {
            areaChat.append("Jogada inválida! Tenta outra casa (lembra-te: trios são proibidos agora).\n");
        }
    }

    private void faseMovimentacao(int l, int c) {
        if (lOrigem == -1 && cOrigem == -1) {
            // Primeiro clique: selecionar a peça a mover
            if (backend.getPeca(l, c) == jogadorAtual) {
                atualizarTabuleiro(); // Limpa qualquer amarelo de seleções anteriores
                lOrigem = l;
                cOrigem = c;
                botoes[l][c].setBackground(Color.YELLOW); // Destaca a peça selecionada
                areaChat.append("Peça selecionada! Agora escolha para onde mover.\n");
            } else {
                areaChat.append("Selecione uma peça sua para mover.\n");
            }
        } else {
            // Segundo clique: tentar mover para a nova posição
            if (backend.moverPeca(jogadorAtual, lOrigem, cOrigem, l, c)) {
                atualizarTabuleiro();
                
                // Verificar se formou um trio após a movimentação
                if (backend.formouTrio(l, c, jogadorAtual)) {
                    modoCaptura = true; // Ativa o modo de captura para o próximo clique
                    areaChat.append("Trio formado! Escolha uma peça inimiga para remover.\n");
                } else {
                    alternarTurno();
                    areaChat.append("Peça movida! Próxima jogada: Jogador " + jogadorAtual + "\n");
                }
            } else {
                areaChat.append("Jogada inválida! Tente mover para uma casa adjacente vazia.\n");
            }
            // Resetar a seleção
            lOrigem = -1;
            cOrigem = -1;
        }
    }

    private void enviarMensagem() {
        String msg = campoTexto.getText();
        if (!msg.isEmpty()) {
            areaChat.append("Você: " + msg + "\n");
            campoTexto.setText(""); // Limpa o campo
        }
    }

    private void atualizarTabuleiro() {
        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 6; c++) {
                int peca = backend.getPeca(l, c);
                botoes[l][c].setBackground(null); // Limpa cores de destaque
                if (peca == Tabuleiro.JOGADOR1) {
                    botoes[l][c].setText("X");
                    botoes[l][c].setForeground(Color.BLUE);
                } else if (peca == Tabuleiro.JOGADOR2) {
                    botoes[l][c].setText("O");
                    botoes[l][c].setForeground(Color.RED);
                } else {
                    botoes[l][c].setText(" ");
                }
            }
        }
    }
}