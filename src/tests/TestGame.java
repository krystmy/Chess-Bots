package tests;

import Experiment.NewBot;
import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.LazySearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class TestGame {
    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    public static final String STARTING_POSITION = "rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq e3";
    
    private ArrayBoard board;
    
    public static void main(String[] args) {
        TestGame game = new TestGame();
        long startTime = System.currentTimeMillis();
        game.play();
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    public TestGame() {

        setupWhitePlayer(new NewBot<ArrayMove, ArrayBoard>(), 5, 3);
        setupBlackPlayer(new NewBot<ArrayMove, ArrayBoard>(), 5, 3);
    }
    
    public void play() {
       this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
       Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
       
       int turn = 0;
       
       /* Note that this code does NOT check for stalemate... */
       while (!board.inCheck() || board.generateMoves().size() > 0) {
    	   
           currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
           System.out.printf("%3d: " + board.fen() + "\n", turn);
           this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
           
           turn++;
           
       }
    }
    
    public Searcher<ArrayMove, ArrayBoard> setupPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        searcher.setDepth(depth);
        searcher.setCutoff(cutoff);
        searcher.setEvaluator(new SimpleEvaluator());
        return searcher; 
    }
    public void setupWhitePlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.whitePlayer = setupPlayer(searcher, depth, cutoff);
    }
    public void setupBlackPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.blackPlayer = setupPlayer(searcher, depth, cutoff);
    }
}
