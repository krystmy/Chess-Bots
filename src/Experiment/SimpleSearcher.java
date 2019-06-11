package Experiment;

import java.util.List;

import chess.bots.BestMove;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

/**
 * This class should implement the minimax algorithm as described in the
 * assignment handouts
 */
public class SimpleSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
	
    public M getBestMove(B board, int myTime, int opTime) {
        /* Calculate the best move*/
    	return minimax(this.evaluator, board, ply).move;
    }

    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> minimax(Evaluator<B> evaluator, B board, int depth) {

    	BestMove<M> bm = new BestMove<M>(-evaluator.infty());
    	if(depth == 0) {
			BestMove<M> best = new BestMove<M>(null, evaluator.eval(board));
			bm = best;
			return bm;
    	}
    	List<M> moves = board.generateMoves();
    	if(moves.isEmpty()) {
			if(board.inCheck()) {
				bm.value = -evaluator.mate() - depth;
				bm.move = null;
				return bm;
			} else {
				bm.value = evaluator.stalemate();
				bm.move = null;
				return bm;
			}
		}
    	for(M move : moves) {
			board.applyMove(move);
    		BestMove<M> Moved = minimax(evaluator, board, depth - 1);
    		Moved.move = move;
    		Moved = Moved.negate();
    		if(Moved.value > bm.value) {
    			bm = Moved;
    		}
    		board.undoMove();	
    	}
    	return bm;

    }

}