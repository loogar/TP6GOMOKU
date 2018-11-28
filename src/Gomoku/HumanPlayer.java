package Gomoku;
import java.util.Scanner;

public class HumanPlayer extends Player {

	private final Scanner scan;
	public HumanPlayer(String name, int id, Scanner scan) {
		super(name, id);
		this.scan = scan;
		
	}

	@Override
	public int[] getMove(int[][] board) {
		System.out.print("\nEnter your line :");
		int line = scan.nextInt();
		scan.nextLine();
		System.out.print("\nEnter your column :");
		int col = scan.nextInt();
		scan.nextLine();
		return new int[] {line-1, col-1};
		
	}

}
