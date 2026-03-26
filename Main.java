public class Main {
    public static void main(String[] args) {
        // 1. Criamos o cérebro
        Tabuleiro meuTabuleiro = new Tabuleiro();
        
        // 2. Criamos a cara e entregamos o cérebro a ela
        new JanelaJogo(meuTabuleiro);
    }
}