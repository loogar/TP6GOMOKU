package Gomoku;

public abstract class Player {
	
	protected String name;
	protected int id;
	
	public Player(String name, int id) {
		this.name = name; 
		this.id = id;
	}
	
	public abstract int [] getMove(final int [][] board);

	public String getName() {
		return name;
	}
	public int getId() {
		return this.id;
	}
}
