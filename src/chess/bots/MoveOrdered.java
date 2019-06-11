package chess.bots;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import chess.board.ArrayMove;
import chess.bots.JamboreeSearcher.SearchTask;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class MoveOrdered<M extends Move<M>, B extends Board<M, B>> extends
AbstractSearcher<M, B> {

	private static final ForkJoinPool POOL = new ForkJoinPool();
	private static final int divideCutoff = 4;
	private static final double PERCENTAGE_SEQUENTIAL = 0.5;


	// GET BEST MOVE
	public M getBestMove(B board, int myTime, int opTime) {
		List<M> moves = board.generateMoves();
		return parallel(moves, divideCutoff, board, evaluator, ply).move;
	}

	// PARALLEL
	public BestMove<M> parallel(List<M> moves, int divideCutoff, B board,Evaluator<B> evaluator, int depth) {
		return POOL.invoke(new SearchTask<M, B>(moves, 0, moves.size(), divideCutoff, board, evaluator,
				depth, false, null, -evaluator.infty(), evaluator.infty()));
	}




	// SEARCH TASK CLASS
	@SuppressWarnings("serial")
	public class SearchTask<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>>{
		int lo, hi, depth, divideCutoff, alpha, beta;
		List<M> list;
		B board;
		M move;
		Evaluator<B> evaluator;
		boolean copy;

		public SearchTask(List<M> list, int lo, int hi, int divideCutoff, B board, Evaluator<B> evaluator,
				int depth, boolean copy, M move, int alpha, int beta) {
			this.list = list;
			this.lo = lo;
			this.hi = hi;
			this.divideCutoff = divideCutoff;
			this.board = board;
			this.evaluator = evaluator;
			this.depth = depth;
			this.copy = copy;
			this.move = move;     
			this.alpha = alpha;
			this.beta = beta;
		}

		public int compare(M mv1, M mv2) {
			return -Boolean.compare(mv1.isCapture(), mv2.isCapture());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected BestMove<M> compute() {
			if(copy) {
				board = this.board.copy();
				board.applyMove(move);
				list = board.generateMoves();
				Collections.sort(list, this::compare);
				lo = 0;
				hi = list.size();
			}
			if(depth <= cutoff || list.isEmpty()) {
				return AlphaBetaSearcher.alphabeta(evaluator, board, depth, alpha, beta);
			}

			BestMove<M> best = new BestMove<M>(null,-evaluator.infty());
			

			// if we need to copy the board, copy in child

			// sequential part
			// we are at the beginning of moves list so we run sequential, then we do need to copy                                     
			if((hi - lo) == list.size()) {
				int seq =  (int) (list.size() * PERCENTAGE_SEQUENTIAL);
				for(int i = 0; i < seq ; i++) {
					M mv = list.get(i);
					BestMove<M> sequential = new SearchTask<M, B>(list, i, i + 1 , divideCutoff, board, evaluator,
							depth-1, true, mv, -beta, -alpha).compute().negate();

					// if value is greater than alpha, set best and alpha
					if(sequential.value > alpha) {
						alpha = sequential.value;
						best = sequential;
						best.move = mv;
					}
					if(alpha >= beta) {
						return best;
					}
				}
				lo = seq;
				if(lo == hi) {
					return best;
				}
			}

			// CASE 2
			if((hi - lo <= divideCutoff) ) {
				SearchTask<M, B>[] threads = (SearchTask<M, B>[]) new SearchTask[hi - lo];
				int index = 0;
				for(int i = lo; i < hi - 1; i++) {
					threads[index] = new SearchTask<M, B>(list, 0, list.size(), divideCutoff,
							board, evaluator, depth - 1, true, list.get(i), -beta, -alpha);
					threads[index].fork();
					index++;
				}

				threads[index] = new SearchTask<M,B>(list, 0, list.size(), divideCutoff, board, evaluator,
						depth - 1, true, list.get(hi - 1), -beta, -alpha);
				BestMove<M> bestm = threads[index].compute().negate();
				bestm.move = list.get(hi - 1);
				if(bestm.value > best.value) {
					best = bestm;
					best.move = list.get(hi - 1);
				}
				if(best.value > alpha) {
					alpha = best.value;
				}
				if(alpha >= beta) {
					return best;
				}
				for(int i = 0; i < threads.length - 1; i++) {
					BestMove<M> result = threads[i].join().negate();
					if(result.value > best.value) {
						best = result;
						best.move = list.get(i + lo);
					}

					if(alpha >= beta) {
						return best;
					}
				}
				return best;
			}
			// CASE 3
			int mid = lo + (hi - lo) / 2;

			SearchTask<M, B> left = new SearchTask<M, B> (list, lo, mid, divideCutoff, board, evaluator, depth, false,
					move, alpha, beta);
			SearchTask<M, B> right = new SearchTask<M, B>(list, mid, hi, divideCutoff, board, evaluator, depth, false,
					move, alpha, beta);
			left.fork();
			BestMove<M> rResult = right.compute();
			BestMove<M> lResult = left.join();
			
			if((rResult.value > lResult.value) && (rResult.value > best.value)) {
				return rResult;
			} else if(lResult.value > best.value) {
				return lResult;
			} else {
				return best;
			}
		}
	}
}