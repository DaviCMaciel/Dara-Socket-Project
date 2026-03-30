import javax.swing.*;
import java.awt.*;

public class JanelaJogo extends JFrame {
    // Liga a interface ao "cérebro" do jogo
    private Tabuleiro backend;

    // Liga a interface ao
    private Conexao rede;
    
    // Matriz de botões que o utilizador vai ver
    private JButton[][] botoes = new JButton[5][6];
    
    // Componentes do Chat
    private JTextPane areaChat;
    private String historicoHTML = "";
    private JTextField campoTexto;

    // --- ATRIBUTOS DE ESTADO (A memória da Janela) ---
    private int jogadorAtual = Tabuleiro.JOGADOR1;
    private int lOrigem = -1; // Guarda a linha do 1º clique na movimentação
    private int cOrigem = -1; // Guarda a coluna do 1º clique na movimentação
    private boolean modoCaptura = false; // Indica se o jogador deve remover uma peça inimiga
    private boolean jogoFinalizado = false; // Indica se o jogo deve parar ou não
    private int meuId; // Guarda se sou o Jogador 1 ou 2 (definido após a conexão)
    private int oponenteId; // Guarda o ID do oponente (1 ou 2)

    public JanelaJogo(Tabuleiro tabuleiro, int id) {
        this.backend = tabuleiro;
        this.meuId = id;
        this.oponenteId = (id == Tabuleiro.JOGADOR1) ? Tabuleiro.JOGADOR2 : Tabuleiro.JOGADOR1;

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

        // 3. Criar o Painel Lateral Direito (Chat + Botões)
        JPanel painelDireito = new JPanel(new BorderLayout());
        painelDireito.setPreferredSize(new Dimension(300,600));

        //Topo da lateral: Título e Botão de Desistência
        JPanel painelTopoDireito = new JPanel(new GridLayout(2, 1)); // 2 linhas, 1 coluna
        JLabel labelChat = new JLabel("--- CHAT ---", SwingConstants.CENTER);
        painelTopoDireito.add(labelChat);

        JButton btnDesistir = new JButton("Desistir / Sair");
        btnDesistir.setBackground(Color.RED);
        btnDesistir.setForeground(Color.WHITE);
        btnDesistir.addActionListener(e -> Desistir());
        painelTopoDireito.add(btnDesistir);

        painelDireito.add(painelTopoDireito, BorderLayout.NORTH);

        // Centro da lateral: Área de histórico do Chat
        areaChat = new JTextPane();
        areaChat.setEditable(false); // O utilizador não apaga o histórico
        areaChat.setContentType("text/html"); // Permite usar HTML para formatar o texto
        JScrollPane scrollChat = new JScrollPane(areaChat);
        areaChat.setBackground(new Color(245, 245, 245));
        painelDireito.add(scrollChat, BorderLayout.CENTER);

        // Rodapé da lateral: Campo de texto para enviar mensagens
        campoTexto = new JTextField();
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        campoTexto.setBorder(BorderFactory.createCompoundBorder(campoTexto.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        campoTexto.addActionListener(e -> enviarMensagem());
        painelDireito.add(campoTexto, BorderLayout.SOUTH);

        // 4. Adicionar a lateral direita à janela principal
        add(painelDireito, BorderLayout.EAST);

        // 5. Mostrar a janela
        setLocationRelativeTo(null); // Centraliza no ecrã
        setVisible(true);
    }

    public void setRede(Conexao rede) {
        this.rede = rede;
        adicionarMensagemChat("Sistema", "Conexão estabelecida!", "gray");
    }

    private void adicionarMensagemChat(String remetente, String mensagem, String cor) {
        // Montamos uma linha com cor e espaçamento
        String novaLinha = "<div style='margin-bottom: 5px; color: " + cor + ";'>" + "<b>" + remetente + ":</b> " + mensagem + "</div>";
    
        historicoHTML += novaLinha;
    
        // Atualizamos o componente com o HTML completo
        areaChat.setText("<html><body style='font-family: Arial; font-size: 12px;'>" + historicoHTML + "</body></html>");
    
        // Move o cursor para o final do texto, garantindo que a última mensagem fique visível.
        SwingUtilities.invokeLater(() -> areaChat.setCaretPosition(areaChat.getDocument().getLength())); 
    }

    private void Desistir() {
        if (jogoFinalizado) {
            return; // Se o jogo já terminou, não é possível desistir (a janela já pode ser fechada normalmente)
        }
        int resposta = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja desistir?", "Confirmar Desistência", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            if (rede != null) rede.enviar("SURRENDER;");
            System.exit(0);
        }
    }

    private void alternarTurno() {
        jogadorAtual = (jogadorAtual == Tabuleiro.JOGADOR1) ? Tabuleiro.JOGADOR2 : Tabuleiro.JOGADOR1;
    }

    private void aoClicar(int l, int c) {
        if (jogoFinalizado) {
            adicionarMensagemChat("Sistema", "O jogo já terminou! Reinicie para jogar novamente.", "gray");
            return;
        }

        if (rede == null) {
            adicionarMensagemChat("Sistema", "Aguardando conexão com o oponente...", "gray");
            return;
        }

        if (jogadorAtual != meuId) {
            adicionarMensagemChat("Sistema", "Espere sua vez! É a vez do oponente.", "gray");
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
        if (backend.removerPeca(meuId, l, c)) {
            atualizarTabuleiro();

            // ENVIO REDE: Comando;Linha;Coluna
            rede.enviar("REMOVE;" + l + ";" + c);

            modoCaptura = false;

            alternarTurno();
            
            adicionarMensagemChat("Sistema", "Peça inimiga removida! Próxima jogada: Jogador " + jogadorAtual, "gray");
            
            int vencedor = backend.verificarVencedor();
            
            if (vencedor != 0) {
                JOptionPane.showMessageDialog(this, "O JOGADOR " + vencedor + " VENCEU!");
                jogoFinalizado = true;
            }
        } else {
            adicionarMensagemChat("Sistema", "Jogada inválida! Escolha uma peça inimiga para remover.", "gray");
        }
    }
    
    
    private void faseColocacao(int l, int c) {
        if (backend.colocarPeca(jogadorAtual, l, c)) {
            atualizarTabuleiro();

            // ENVIO REDE: Comando;Linha;Coluna
            rede.enviar("DROP;" + l + ";" + c);

            alternarTurno();
            adicionarMensagemChat("Sistema", "Peça colocada! Próxima jogada: Jogador " + jogadorAtual, "gray");
        } else {
            adicionarMensagemChat("Sistema", "Jogada inválida! Tenta outra casa (lembra-te: trios são proibidos agora).", "gray");
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
                adicionarMensagemChat("Sistema", "Peça selecionada! Agora escolha para onde mover.", "gray");
            } else {
                adicionarMensagemChat("Sistema", "Selecione uma peça sua para mover.", "gray");
            }
        } else {
            // Segundo clique: tentar mover para a nova posição
            if (backend.moverPeca(jogadorAtual, lOrigem, cOrigem, l, c)) {
                atualizarTabuleiro();

                // ENVIO REDE: Comando;L_Origem;C_Origem;L_Destino;C_Destino
                rede.enviar("MOVE;" + lOrigem + ";" + cOrigem + ";" + l + ";" + c);
                
                // Verificar se formou um trio após a movimentação
                if (backend.formouTrio(l, c, jogadorAtual)) {
                    modoCaptura = true; // Ativa o modo de captura para o próximo clique
                    adicionarMensagemChat("Sistema", "Trio formado! Escolha uma peça inimiga para remover.", "gray");
                } else {
                    alternarTurno();
                    adicionarMensagemChat("Sistema", "Peça movida! Próxima jogada: Jogador " + jogadorAtual, "gray");
                }
            } else {
                adicionarMensagemChat("Sistema", "Jogada inválida! Tente mover para uma casa adjacente vazia.", "gray");
            }
            // Resetar a seleção
            lOrigem = -1;
            cOrigem = -1;
        }
    }

    private void enviarMensagem() {
        String msg = campoTexto.getText();
        if (!msg.isEmpty()) {
            adicionarMensagemChat("Você", msg, "blue");

            //ENVIO REDE: Comando; Mensagem
            rede.enviar("CHAT;" + msg);

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

    public void processarMensagemRede(String msg) {
        // 1. Dividimos a mensagem usando o separador ";" que combinamos
        String[] partes = msg.split(";");
        String comando = partes[0];

        // LOG de depuração
        System.out.println("Mensagem recebida da rede: " + msg);

        // 2. Usamos um "Switch" para decidir o que fazer com cada comando
        switch (comando) {
            case "CHAT":
                // Exemplo: CHAT;Olá amigo
                adicionarMensagemChat("Oponente", partes[1], "red");
                break;

            case "DROP":
                // Exemplo: DROP;1;2 (Oponente colocou peça na linha 1, coluna 2)
                int lDrop = Integer.parseInt(partes[1]);
                int cDrop = Integer.parseInt(partes[2]);
            
                // Usamos o ID do oponente para colocar a peça correta
                backend.colocarPeca(oponenteId, lDrop, cDrop);
                atualizarTabuleiro();
                alternarTurno();
                break;

            case "MOVE":
                // Exemplo: MOVE;1;2;1;3 (Oponente moveu de 1,2 para 1,3)
                int lOrig = Integer.parseInt(partes[1]);
                int cOrig = Integer.parseInt(partes[2]);
                int lDest = Integer.parseInt(partes[3]);
                int cDest = Integer.parseInt(partes[4]);
            
                // Usamos o ID do oponente para mover  a peça correta
                backend.moverPeca(oponenteId, lOrig, cOrig, lDest, cDest);
                atualizarTabuleiro();

                if (backend.formouTrio(lDest, cDest, oponenteId)) {
                    adicionarMensagemChat("Sistema", "O oponente formou um trio, espere sua vez", "gray");
                    // O oponente tem direito a uma captura, então não alternamos o turno
                } else {
                    alternarTurno();
                }
                break;
            
            case "REMOVE":
                // Exemplo: REMOVE;0;4 (Oponente removeu a MINHA peça em 0,4)
                int lRem = Integer.parseInt(partes[1]);
                int cRem = Integer.parseInt(partes[2]);
            
                // O oponente removeu uma peça minha, então usamos meu ID para remover a peça correta
                backend.removerPeca(oponenteId, lRem, cRem);
                atualizarTabuleiro();

                // Verifica se o jogador perdeu após a remoção
                int vencedor = backend.verificarVencedor();
                if (vencedor != 0) {
                    JOptionPane.showMessageDialog(this, "O JOGADOR " + vencedor + "VENCEU!");
                    jogoFinalizado = true;
                } else {
                    alternarTurno();
                    adicionarMensagemChat("Sistema", "O oponente removeu uma de suas peças!", "gray");
                }
                break;
            
            case "SURRENDER":
                JOptionPane.showMessageDialog(this, "O oponente desistiu! Você venceu!");
                jogoFinalizado = true;
                System.exit(0);
                break;
        }
    }
}