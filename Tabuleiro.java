public class Tabuleiro {
    // Constantes usadas para representar o estado de cada célula do tabuleiro
    public static final int VAZIO = 0;
    public static final int JOGADOR1 = 1;
    public static final int JOGADOR2 = 2;
    public static final int MAX_PECAS = 12;

    // Matriz que representa o tabuleiro
    private int[][] tabuleiro = new int[5][6];

    // Boolean para indicar se a fase de colocação de peças já terminou
    private boolean faseColocacao = true;

    // Métodos
    public boolean isFaseColocacao() {
        return faseColocacao;
    }

    public int getPeca(int linha, int coluna) {
        return tabuleiro[linha][coluna];
    }

    // Contadores para o número de peças de cada jogador
    private int contadorJogador1 = 0;
    private int contadorJogador2 = 0;
    
    public Tabuleiro() {
        // Inicializa o tabuleiro com células vazias
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                tabuleiro[i][j] = VAZIO;
            }
        }
    }

    public boolean formouTrio(int linha, int coluna,int jogador) {
        int pecas_horizontais = 1;
        for (int i = coluna - 1; i>=0 && tabuleiro[linha][i] == jogador; i--) {
            pecas_horizontais++;
        }

        for (int i = coluna + 1; i < 6 && tabuleiro[linha][i] == jogador; i++) {
            pecas_horizontais++;
        }

        if (pecas_horizontais == 3){
            return true; // Formou um trio horizontal
        }

        int pecas_verticais = 1;
        for (int i = linha - 1; i >= 0 && tabuleiro[i][coluna] == jogador; i--) {
            pecas_verticais++;
        }

        for (int i = linha + 1; i < 5 && tabuleiro[i][coluna] == jogador; i++) {
            pecas_verticais++;
        }

        if (pecas_verticais == 3){
            return true; // Formou um trio vertical
        }

        return false; // Não formou um trio
    }

    public boolean colocarPeca(int jogador, int linha, int coluna) {
        if (tabuleiro[linha][coluna] != VAZIO || faseColocacao == false) {
            return false; // A célula já está ocupada
        }

        tabuleiro[linha][coluna] = jogador; // Coloca a peça do jogador na célula temporariamente
        if (formouTrio(linha, coluna, jogador)) { // Verifica se formou um trio
            tabuleiro[linha][coluna] = VAZIO; // Remove a peça do jogador da célula
            return false; // Não pode colocar a peça, pois formaria um trio na fase de colocação
        }

        if (jogador == JOGADOR1) {
            contadorJogador1++; // Incrementa o contador de peças do jogador 1
        } else {
            contadorJogador2++; // Incrementa o contador de peças do jogador 2
        }

        if (contadorJogador1 == MAX_PECAS && contadorJogador2 == MAX_PECAS) {
            faseColocacao = false; // A fase de colocação termina quando ambos os jogadores colocarem 12 peças
        }

        return true; // Pode colocar a peça, pois não formaria um trio na fase de colocação
    }

    public boolean moverPeca(int jogador, int linha_origem, int coluna_origem, int linha_destino, int coluna_destino) {
        // 1. Verificar se a célula de origem contém uma peça do jogador
        if (tabuleiro[linha_origem][coluna_origem] != jogador) {
            return false; // A célula de origem não contém uma peça do jogador
        }

        // 2. Verificar se a célula de destino está vazia
        if (tabuleiro[linha_destino][coluna_destino] != VAZIO) {
            return false; // A célula de destino já está ocupada
        }

        // 3. Verificar se a jogada é para uma célula adjacente
        if (Math.abs(linha_destino - linha_origem) + Math.abs(coluna_destino - coluna_origem) != 1) {
            return false; // A jogada não é para uma célula adjacente
        }

        // 4. Reazize a jogada
        tabuleiro[linha_origem][coluna_origem] = VAZIO; // Remove a peça da célula de origem
        tabuleiro[linha_destino][coluna_destino] = jogador; // Coloca a peça do jogador na célula de destino
        return true; // A jogada foi realizada com sucesso

    }

    public int verificarVencedor() {
        // Só verificamos a vitória se já passamos da fase de colocação 
        if (!faseColocacao) {
            if (contadorJogador1 <= 2) {
                return JOGADOR2; // Jogador 2 venceu
            } else if (contadorJogador2 <= 2) {
                return JOGADOR1; // Jogador 1 venceu
                }
        }
        return 0; // Nenhum jogador venceu ainda   
    }

    public boolean removerPeca(int jogador, int linha, int coluna) {
        int jogadorOponente = (jogador == JOGADOR1) ? JOGADOR2 : JOGADOR1;

        if (tabuleiro[linha][coluna] == jogadorOponente) {
            tabuleiro[linha][coluna] = VAZIO; // Remove a peça do oponente da célula
            if (jogadorOponente == JOGADOR1) {
                contadorJogador1--; // Decrementa o contador de peças do jogador 1
            } else {
                contadorJogador2--; // Decrementa o contador de peças do jogador 2
            }
            return true; // A peça do oponente foi removida com sucesso
        }

        return false; // A remoção não foi bem-sucedida, pois a célula não contém uma peça do oponente

    }



}
