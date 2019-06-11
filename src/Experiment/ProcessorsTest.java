package Experiment;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Searcher;

public class ProcessorsTest {

    public static final String STARTING_POSITION = "rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq e3";
    public static final String MIDDLE_POSITION = "1r1k1bnr/p2npQpp/2B5/q1p5/4P3/2NPB2P/PPP2PP1/R3K2R b KQ -";
    public static final String ENDING_POSITION = "QB5r/2kn2pp/q3p3/1R6/1b2P3/2NP3P/P1PK1PP1/8 b - -";

    private static ArrayBoard board;

	public static void main(String[] args) {
		Searcher<ArrayMove, ArrayBoard> jamboree = new JamboreeSearcher<>();
		System.out.println("jamboree:");
		test(jamboree, STARTING_POSITION);
		test(jamboree, MIDDLE_POSITION);
		test(jamboree, ENDING_POSITION);
		
		Searcher<ArrayMove, ArrayBoard> minimax = new ParallelSearcher<>();
		System.out.println("minimax:");
		test(minimax, ENDING_POSITION);
		test(minimax, STARTING_POSITION);
		test(minimax, MIDDLE_POSITION);


    }
	
	public static void test(Searcher<ArrayMove, ArrayBoard> searcher, String fen) {
		board = ArrayBoard.FACTORY.create().init(fen);
		searcher.setDepth(5);
		searcher.setEvaluator(new SimpleEvaluator());
		for(int i = 1; i <= 3; i++) {//warmup
			searcher.getBestMove(board, 0, 0);
		}
		for(int j = 1; j <= 5; j++) {// 5 trials
			long startTime = System.currentTimeMillis();
			searcher.getBestMove(board, 0, 0);
			long endTime = System.currentTimeMillis();
			System.out.println(endTime - startTime);
		}
		
	
	}
}

