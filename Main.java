import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        // 1. Criamos o cérebro (Lógica do Dara)
        Tabuleiro meuTabuleiro = new Tabuleiro();

        // 2. Interface de escolha: Criamos um menu simples com botões
        String[] opcoes = {"Criar Sala (Servidor)", "Entrar em Sala (Cliente)"};
        int escolha = JOptionPane.showOptionDialog(null, 
                "Como deseja iniciar o Jogo Dara?", 
                "Configuração de Rede", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, opcoes, opcoes[0]);
        
        // Usamos um bloco try-catch porque operações de rede (Sockets)    
        try {
            if (escolha == 0) {
                // MODO SERVIDOR
                // Primeiro abrimos a janela para o utilizador não achar que o PC travou
                JanelaJogo janela = new JanelaJogo(meuTabuleiro, Tabuleiro.JOGADOR1);
                janela.setTitle("Dara - Servidor (Jogador 1)");
                
                // Criamos a conexão e aguardamos o oponente (o código para aqui até ele chegar)
                Conexao conexao = new Conexao(12345, janela);
                
                // Após a conexão ser estabelecida, entregamos a conexão à janela
                janela.setRede(conexao);
                
            } else if (escolha == 1) {
                // MODO CLIENTE
                // Pede o endereço IP do computador onde o Servidor está rodando
                String ip = JOptionPane.showInputDialog("Digite o IP do servidor:", "localhost");
                if (ip == null) return; // Se cancelar, fecha o programa

                JanelaJogo janela = new JanelaJogo(meuTabuleiro, Tabuleiro.JOGADOR2);
                janela.setTitle("Dara - Cliente (Jogador 2)");

                // Tenta ligar-se ao IP fornecido
                Conexao conexao = new Conexao(ip, 12345, janela);
                
                // Entregamos a conexão à janela
                janela.setRede(conexao);
            }
        } catch (Exception e) {
            // Caso ocorra qualquer erro na rede, mostramos uma mensagem amigável.
            JOptionPane.showMessageDialog(null, "Erro na conexão: " + e.getMessage());
            System.exit(0); // Encerra o programa em caso de falha crítica.
        }
    }
}