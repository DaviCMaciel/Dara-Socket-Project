import java.io.*;
import java.net.*;

public class Conexao extends Thread {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private JanelaJogo janela;

    // Construtor para o Servidor
    public Conexao(int porta, JanelaJogo janela) throws IOException {
        // Cria um "ouvido" na porta especifica (ex: 12345)
        ServerSocket servidor = new ServerSocket(porta);

        // O programa para aqui e "fica à espera" até que alguém se conecta
        this.socket = servidor.accept(); // Bloqueia até alguém ligar

        // Após conectar, podemos fechar o servidor para libertar a porta
        servidor.close();

        inicializar(janela);
    }

    // Construtor para o Cliente
    public Conexao(String ip, int porta, JanelaJogo janela) throws IOException {
        // Tenta estabelecer ligação com o IP e porta fornecidos
        this.socket = new Socket(ip, porta);
        inicializar(janela);
    }

    // Prepara os fluxos de entrada e saída de dados
    private void inicializar(JanelaJogo janela) throws IOException {
        this.janela = janela;

        // Cria os caminhos por onde os dados vão viajar
        this.entrada = new DataInputStream(socket.getInputStream());
        this.saida = new DataOutputStream(socket.getOutputStream());

        this.start(); // Inicia a Thread para ouvir mensagens
    }

    // Enviar mensagem para o outro computador
    public void enviar(String mensagem) {
        try {
            // writeUTF envia a string formatada corretamente
            saida.writeUTF(mensagem);
            // flush() garante que o dado sai do buffer e é enviado imediatamente
            saida.flush();
        } catch (IOException e) {
            System.out.println("Erro ao enviar: " + e.getMessage());
        }
    }

    // Thread fica em loop infinito à espera de novas mensagens do oponente
    @Override
    public void run() {
        try {
            while (true) {
                // Fica parado aqui até chegar uma mensagem (Bloqueante)
                String mensagemRecebida = entrada.readUTF();
                
                // Passa a mensagem para a janela processar
                janela.processarMensagemRede(mensagemRecebida);
            }
        } catch (IOException e) {
            // Acontece se o oponente fechar o jogo ou a internet cair
            System.out.println("Conexão perdida.");
        }
    }
}