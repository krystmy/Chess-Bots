package chess.bots;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;


public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {

    private static final ForkJoinPool POOL = new ForkJoinPool();
    private static final int divideCutoff = 4;
	
	public BestMove<M> parallel(List<M> moves, int divideCutoff, B board,Evaluator<B> evaluator, int depth, boolean child) {
        return POOL.invoke(new SearchTask<M, B>(moves, 0, moves.size(), divideCutoff, board, evaluator, depth, child, null));
    }
	
	public M getBestMove(B board, int myTime, int opTime) {
       
		List<M> moves = board.generateMoves();
		return parallel(moves, divideCutoff, board, evaluator, ply, false).move;
		
    }
	
	@SuppressWarnings("serial")
	private class SearchTask<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>>{
        int lo, hi, depth, divideCutoff;
        List<M> list;
        B board;
        M move;
        Evaluator<B> evaluator;
        boolean copy;

		public SearchTask(List<M> list, int lo, int hi, int divideCutoff, B board, Evaluator<B> evaluator, int depth, boolean copy, M move) {
			this.list = list;
			this.lo = lo;
            this.hi = hi;
            this.divideCutoff = divideCutoff;
            this.board = board;
            this.evaluator = evaluator;
            this.depth = depth;
            this.copy = copy;
            this.move = move;		
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected BestMove<M> compute() {
			if(copy) {
				board = this.board.copy();
			    board.applyMove(move);
			    list = board.generateMoves();
			    lo = 0;
			    hi = list.size();
			}
			if(depth <= cutoff || list.isEmpty()) {
				return SimpleSearcher.minimax(evaluator, this.board, depth);
			}
			
			if(hi - lo <= divideCutoff) {
				SearchTask<M, B>[] threads = (SearchTask<M, B>[]) new SearchTask[hi - lo];
				int index = 0;
				for(int i = lo; i < hi; i++) {
				    threads[index++] = new SearchTask<M, B>(list, 0, list.size(), divideCutoff, 
				    		board, evaluator, depth - 1, true, list.get(i));
				}			
				for(int i = 1; i < threads.length; i++) {
					threads[i].fork();
				}
				
				BestMove<M> best = threads[0].compute().negate();
				best.move = list.get(lo);
				for(int i = 1; i < threads.length; i++) {
					BestMove<M> result = threads[i].join().negate();
					if(result.value > best.value) {
						best = result;
						best.move = list.get(i + lo);
					}
				}
				return best;
			}
			
			int mid = lo + (hi - lo) / 2;
			
			SearchTask<M, B> left = new SearchTask<M, B>(list, lo, mid, divideCutoff, board, evaluator, depth, false, move);
			SearchTask<M, B> right = new SearchTask<M, B>(list, mid, hi, divideCutoff, board, evaluator, depth, false, move);
			
			left.fork();
			BestMove<M> rResult = right.compute();
			BestMove<M> lResult = left.join();
			
			if(rResult.value > lResult.value) {
				return rResult;
			} else {		
				return lResult;
			}
		}
	}
	
	
}