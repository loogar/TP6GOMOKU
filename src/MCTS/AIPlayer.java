package MCTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class AIPlayer {
		private double tPossibleMoves = 0;
		private double tEvaluateState = 0;
		private long nodesEvaluated = 0;
		private int Distance;	
		private int Depth;
		private int N;
		private int consecutiveCount;
		private final ArrayList<ArrayList<Integer>> everyPossibleMove;


		public AIPlayer(int dist, int dep, int consctv, int size)
		{
			this.Distance = dist;
			this.Depth = dep;
			this.consecutiveCount = consctv;
			this.N = size;
			everyPossibleMove = generateMoves();
		}


		public int[] getMove(byte[] desk, byte value)
		{
			tEvaluateState = 0;
			tPossibleMoves = 0;
			nodesEvaluated = 0;
			long startTime = System.nanoTime();

			int[] ab = alphabeta(desk.clone(), Integer.MIN_VALUE, Integer.MAX_VALUE, Depth, value);

			if (ab[0] <= -10000)
				System.out.println("\n");

			long stopTime = System.nanoTime();
			System.out.printf("Move [%d %d] Score [%d]" +
					          "\n Search tree nodes covered [%d]\n" +							 
							  "", ab[1], ab[2], ab[0], nodesEvaluated);

			return new int[]{ab[1], ab[2]};
		}

		private int[] alphabeta(byte[] sketchBoard, int alpha, int beta, int depth, byte forTurn)
		{
			long startTime = System.nanoTime();
			Collection<Integer> possibleMoves = possibleMovesPreGenerated(sketchBoard);
			long stopTime = System.nanoTime();
			tPossibleMoves += (double) (stopTime - startTime) / 1000000000.0;


			if (possibleMoves.isEmpty() || depth == 0)
			{
				startTime = System.nanoTime();
				int[] result = {evaluateState(sketchBoard), -1, -1};
				stopTime = System.nanoTime();
				tEvaluateState += (double) (stopTime - startTime) / 1000000000.0;
				return result;
			} else
			{
				int score;
				int iMove = -2;
				int jMove = -2;

				if (forTurn == 1)
				{
					
					score = Integer.MIN_VALUE;
					for (Integer move : possibleMoves)
					{
						sketchBoard[move] = (byte) forTurn;

						score = Math.max(score, alphabeta(sketchBoard, alpha, beta, depth - 1, (byte) (3 - forTurn))[0]);

						if (score > alpha)
						{
							alpha = score;
							iMove = move / N;
							jMove = move % N;
						}

						sketchBoard[move] = 0;
						nodesEvaluated++;

						if (beta <= alpha)
							break;
					}

					return new int[]{score, iMove, jMove};
				} else
				{
				score = Integer.MAX_VALUE;

					for (Integer move : possibleMoves)
					{
						sketchBoard[move] = forTurn;

						score = Math.min(score, alphabeta(sketchBoard, alpha, beta, depth - 1, (byte) (3 - forTurn))[0]);

						if (score < beta)
						{
							beta = score;
							iMove = move / N;
							jMove = move % N;
						}
						sketchBoard[move] = 0;
						nodesEvaluated++;

						if (beta <= alpha)
							break;
					}

					return new int[]{score, iMove, jMove};
				}
			}

		}




		private int calculateScore(int counter, boolean startFree, boolean endFree)
		{
			if (counter >= consecutiveCount)
			{
				return 10000;
			}
			else if (startFree && endFree)
			{
				if (counter == consecutiveCount - 1)
					return 900;
				else if (counter > 1)
					return counter * 100;

			} else if (startFree || endFree)
			{
				if (counter == consecutiveCount - 1)
					return 500;
				if (counter > 1)
					return counter * 10;
			}
			return 0;
		}


		private int evaluateState(byte[] board)
		{

		
			int p2nwse = nwseDiagonalCheck(board, 2);
			if (p2nwse == 10000)
				return -10000;

			int p2nesw = neswDiagonalCheck(board, 2);
			if (p2nesw == 10000)
				return -10000;

			int p2v = verticalCheck(board, 2);
			if (p2v == 10000)
				return -10000;

			int p2h = horizontalCheck(board, 2);
			if (p2h == 10000)
				return -10000;


			
			int p1nwse = nwseDiagonalCheck(board, 1);
			if (p1nwse == 10000)
				return 10000;

			int p1nesw = neswDiagonalCheck(board, 1);
			if (p1nesw == 10000)
				return 10000;

			int p1v = verticalCheck(board, 1);
			if (p1v == 10000)
				return 10000;

			int p1h = horizontalCheck(board, 1);
			if (p1h == 10000)
				return 10000;

			return Math.max(p1h, Math.max(p1v, Math.max(p1nesw, p1nwse))) - Math.max(p2h, Math.max(p2v, Math.max(p2nesw, p2nwse)));
		}

		private int verticalCheck(byte[] sketchBoard, int player)
		{
			int score 		= 0;
			int counter 	= 0;
			int startPos 	= -1;
			int endPos 		= -1;
			int index;

			for (int i = 0; i < N; i++)
			{
				for (int j = 0; j < N; j++)
				{
					index = j * N + i;
					if (sketchBoard[index] == player)
					{
						if (j == 0)
							counter = 0;

						counter++;
						startPos = (j - counter + 1)*N + i;
					} else
					{

						if (startPos != -1)
						{
							endPos = index;

						
							boolean startAtEdge = startPos < N;
							boolean endAtEdge = endPos < N;
							boolean startFree = !startAtEdge && sketchBoard[(startPos/N - 1)*N + startPos%N] == 0;
							boolean endFree = !endAtEdge && sketchBoard[endPos] == 0;
			                score = Math.max(score, calculateScore(counter, startFree, endFree));
							startPos = -1;
						}
						counter = 0;
					}
				}
			}
			return score;
		}

		private int horizontalCheck(byte[] sketchBoard, int player)
		{
			int score = 0;
			int counter = 0;
			int startPos = -1;
			int endPos = -1;
			int bigN = N * N;
			for (int index = 0; index < bigN; index++)
			{
				if (sketchBoard[index] == player)
				{
					if (index % N == 0)
						counter = 0;

					counter++;
					startPos = index - counter + 1;
				} else
				{

					if (startPos != -1)
					{
						endPos = index;

						
						boolean startAtEdge = startPos % N == 0;
						boolean endAtEdge = endPos % N == 0;

						boolean startFree = !startAtEdge && sketchBoard[startPos - 1] == 0;
						boolean endFree = !endAtEdge && sketchBoard[endPos] == 0;						
						score = Math.max(score, calculateScore(counter, startFree, endFree));
						startPos = -1;
					}
					counter = 0;
				}
			}

			return score;
		}

		private int neswDiagonalCheck(byte[] sketchBoard, int player)
		{
			boolean startsAtEdge = true;
			boolean endsAtEdge = true;

			boolean startFree;
			boolean endFree;

			int score = 0;
			int counter = 0;
			int startEdge = -1;
			int endEdge = -1;
			int index;


			for (int slice = 0; slice < 2 * N - 1; ++slice)
			{
				int z = slice < N ? 0 : slice - N + 1;
				for (int j = z; j <= slice - z; ++j)
				{
					index = j * N + slice - j;
					if (sketchBoard[index] == player)
					{
						if (j == 0)
							counter = 0;

						counter++;

						startsAtEdge = j - counter + 1 == 0;
						startEdge = startsAtEdge ? 0 : (j - counter) * N + slice - (j - counter);
					} else
					{

						if (startEdge != -1)
						{
							endEdge = index;
                            endsAtEdge = j == z;
							startFree = !startsAtEdge && sketchBoard[startEdge] == 0;
							endFree = !endsAtEdge && sketchBoard[endEdge] == 0;						
							score = Math.max(score, calculateScore(counter, startFree, endFree));


							startEdge = -1;
						}
						counter = 0;
					}

				}
			}
			return score;
		}



		private int nwseDiagonalCheck(byte[] sketchBoard, int player)
		{

			boolean startsAtEdge = true;
			boolean endsAtEdge = true;

			boolean startFree;
			boolean endFree;

			int score = 0;
			int counter = 0;
			int startEdge = -1;
			int endEdge = -1;
			int index;


			for (int slice = 0; slice < 2 * N - 1; ++slice)
			{
				int z = slice < N ? 0 : slice - N + 1;
				for (int j = z; j <= slice - z; ++j)
				{
					index = j * N + N - 1 - slice + j;
					if (sketchBoard[index] == player)
					{
						if (j == 0)
							counter = 0;

						counter++;

						startsAtEdge = j - counter + 1 == 0;
						startEdge = startsAtEdge ? 0 : (j - counter) * N + N - 1 - slice + (j - counter);
					} else
					{

						if (startEdge != -1)
						{
							endEdge = index;
							endsAtEdge = j == z;


				
							startFree = !startsAtEdge && sketchBoard[startEdge] == 0;
							endFree = !endsAtEdge && sketchBoard[endEdge] == 0;

							score = Math.max(score, calculateScore(counter, startFree, endFree));

							startEdge = -1;
						}
						counter = 0;
					}

				}
			}
			return score;
		}



		private Collection<Integer> possibleMoves(byte[] sketchBoard)
		{
			List<Integer> moves = new ArrayList<>();
			int move;
			byte[] tempBoard = sketchBoard.clone();

			for (int y = 0; y < N; y++)
				for (int x = 0; x < N; x++)
				{
					if (tempBoard[y * N + x] > 0)
					{
						
						int leftBorder = x - Distance < 0 ? 0 : x - Distance;
						int rightBorder = x + Distance >= N ? N - 1 : x + Distance;
						int topBorder = y - Distance < 0 ? 0 : y - Distance;
						int bottomBorder = y + Distance >= N ? N - 1 : y + Distance;

						for (int i = topBorder; i <= bottomBorder; i++)
							for (int j = leftBorder; j <= rightBorder; j++)
							{
								move = i * N + j;
								if (tempBoard[move] == 0 && manhattenDistance(i, j, y, x) <= Distance)
								{
									moves.add(move);
									tempBoard[move] = -1;
								}
							}
					}
				}


			return moves;
		}


		private ArrayList<ArrayList<Integer>> generateMoves()
		{
			ArrayList<ArrayList<Integer>> everyMove = new ArrayList<>();

			for (int y = 0; y < N; y++)
				for (int x = 0; x < N; x++)
				{
					ArrayList<Integer> moves = new ArrayList<>();

					int leftBorder = x - Distance < 0 ? 0 : x - Distance;
					int rightBorder = x + Distance >= N ? N - 1 : x + Distance;
					int topBorder = y - Distance < 0 ? 0 : y - Distance;
					int bottomBorder = y + Distance >= N ? N - 1 : y + Distance;

					for (int i = topBorder; i <= bottomBorder; i++)
						for (int j = leftBorder; j <= rightBorder; j++)
						{
							if (manhattenDistance(i, j, y, x) <= Distance)
							{
								moves.add(i * N + j);
							}
						}

					everyMove.add(moves);
				}
			return everyMove;
		}


		private Collection<Integer> possibleMovesPreGenerated(byte[] sketchBoard)
		{
			HashSet<Integer> moves = new HashSet<>();

			for (int y = 0; y < N; y++)
				for (int x = 0; x < N; x++)
				{
					if (sketchBoard[y * N + x] > 0)
					{
					
						for (Integer move : everyPossibleMove.get(y * N + x))
						{
							if (sketchBoard[move] == 0)
							{
								moves.add(move);
							}
						}
					}
				}

			return moves;
		}


		private int manhattenDistance(int[] a, int[] b)
		{
			return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
		}

		private int manhattenDistance(int ay, int ax, int by, int bx)
		{
			return Math.abs(ay - by) + Math.abs(ax - bx);
		}

}
