package Gomoku;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import MCTS.MonteCarloTreeSearch;





public class Gomoku {

	public static final int BOARD_SIZE = 9;
	public static Scanner scan;
	public static String name;

	// GAME MANAGEMENT
	// Checks whether a move is valid against the current board
	// feel free to re-use ^^
	public static boolean valid(int[] move, int[][] board) {
		if (move[0] < 0 || move[0] >= BOARD_SIZE || move[1] < 0 || move[1] >= BOARD_SIZE)
			return false;
		else if (board[move[0]][move[1]] != 0)
			return false;
		else
			return true;
	}

	/**
	 * Checks whether the game has ended and returns -1: the game is over and no
	 * player won (it is not possible to play anymore) 0: if no player has won so
	 * far (the game continues !) 1: if black player has won (the game is over) 2:
	 * if white player has won (the game is over)
	 * 
	 * Feel free to re-use !
	 * @param board
	 * @return int indicating wether game has ended
	 */
	static public int evaluate(int[][] board) {

		// determine if there are still some positions left
		boolean ended = true;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (board[i][j] == 0) {
					ended = false;
				} else {
					// check in 8 directions
					for (int di = -1; di <= 1; di++) {
						for (int dj = -1; dj <= 1; dj++) {
							if (di != 0 || dj != 0) {
								int chk = check(board, board[i][j], i, j, new int[] {di, dj}, 1);
								
								if(chk>=5) return board[i][j];
							}
						}
					}
				}
			}
		}
		return ended ? -1 : 0;
	}

	/**
	 * Recursive helper function to find chains in the board. Stops when reaches the
	 * sides or when color changes (!=p)
	 * 
	 * @param board
	 * @param p
	 * @param i
	 * @param j
	 * @param cnt
	 * @return
	 */
	static public int check(int[][] board, int p, int i, int j, int[] dir, int cnt) {
		//System.out.println(i + " " + j);
		if (i > BOARD_SIZE-1 || i < 0 || j < 0 || j > BOARD_SIZE-1)
			return cnt;
		else if (board[i][j] != p)
			return cnt;
		else {
				return check(board, p, i + dir[0], j + dir[1], dir, cnt + 1);
		}
	}

	public static int gameLoop(int[][] board, Player[] players) {
		Random rand = new Random(System.currentTimeMillis());
		int currentPlayer = rand.nextInt(2);
		int status = 0;
		while (status == 0) {
			System.out.println("It's " + players[currentPlayer].getName() + "'s turn");
			printBoard(board);
			int[] move = players[currentPlayer].getMove(board);
			while (!valid(move, board)) {
				System.out.println("NOPE ! Try again! ");
				move = players[currentPlayer].getMove(board);
			}
			board[move[0]][move[1]] = currentPlayer + 1;
			status = evaluate(board);
			currentPlayer = currentPlayer == 0 ? 1 : 0;
		}
		return status;
	}


	private static void printBoard(int[][] board) {
		  
		for(int i =0; i<BOARD_SIZE;i++)
			System.out.print("  |  " + (i+1));
		System.out.println(" |");
		for(int i =0; i<BOARD_SIZE;i++)
			System.out.print("------");
		System.out.println(" |");
		
		for(int i=0; i<BOARD_SIZE;i++) {
			System.out.print((i+1) + " |");
			for(int j=0;j<BOARD_SIZE;j++) {
				 System.out.print("  " + board[i][j] + "  |");
			}
			
			System.out.println("");
			
			for(int j =0; j<BOARD_SIZE;j++)
				System.out.print("------");
			System.out.println("");
			
		}	

	}

	// PLAYER CREATION
	public static Player createHumanPlayer(int id) {
		
		System.out.println("What is your name ? ");
		 name = scan.nextLine();
		//scan.nextLine();
		
		return new HumanPlayer(name,id,scan);
	}

	private static Player createComputerPlayer() {
		// TODO Auto-generated method stub
		System.out.println("Please select difficulty level 1- Easy 2-Medium 3-Hard");
		System.out.print("\n Your choice number ? ");
		int level = scan.nextInt();
		
		MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(9, 2, 4,level,name);
		mcts.gameOn();
		return null;
	}

	public static void main(String[] args) {

		Player[] players = new Player[2];
		int[][] board = new int[9][9];

		// Scanner init
		scan = new Scanner(System.in);

		System.out.println("== GO-MOKU ==");
		System.out.println("Who do you want to play with ? ");
		System.out.println("1) Human");
		System.out.println("2) Computer");
		System.out.print("\n Your choice ? ");
		int choice = scan.nextInt();
		System.out.println(choice);
		scan.nextLine();
		while (choice != 1 && choice != 2) {
			System.out.print("\n Wrong choice... Your choice ? ");
			choice = scan.nextInt();
		}

		players[0] = createHumanPlayer(0);

		switch (choice) {
		case 1:
			players[1] = createHumanPlayer(1);
			break;
		case 2:
			players[2] = createComputerPlayer();
			break;
		}

		int winner = gameLoop(board, players);
		System.out.println("============================");
		System.out.println("=== WINNER IS PLAYER : " + winner + " ===");
		System.out.println("============================");
	}

}
