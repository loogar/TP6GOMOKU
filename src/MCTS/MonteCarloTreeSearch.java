package MCTS;


import java.util.List;
import java.util.Scanner;



public class MonteCarloTreeSearch {

    private static final int WIN_SCORE = 10;
    private int level;
    private int oponent;
    private int iLast = -1;
   	private int jLast = -1;
   	private int consecutiveCount;
    private int N;
   	private byte Turn;
   	private byte[] Board;
   	private AIPlayer AI;
   	private int Duration = 0;
   	private String Playername;
   	
   	

    public MonteCarloTreeSearch() {
        this.level = 3;
    }
    
    public MonteCarloTreeSearch(int n, int dist, int dep,int lev,String name)
	{
		this.N = n;
		this.AI = new AIPlayer(dist, dep, consecutiveCount, N);
		this.Board = new byte[N * N];
		this.Playername=name;
		if(lev==1) {this.consecutiveCount=2;}
		if(lev==2) {this.consecutiveCount=3;}
		if(lev==3) {this.consecutiveCount=5;}
	}

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private int getMillisForCurrentLevel() {
        return 2 * (this.level - 1) + 1;
    }

    public Board findNextMove(Board board, int playerNo) {
        long start = System.currentTimeMillis();
        long end = start + 60 * getMillisForCurrentLevel();

        oponent = 3 - playerNo;
        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.getState().setBoard(board);
        rootNode.getState().setPlayerNo(oponent);

        while (System.currentTimeMillis() < end) {
            // Phase 1 - Selection
            Node promisingNode = selectPromisingNode(rootNode);
            // Phase 2 - Expansion
            if (promisingNode.getState().getBoard().checkStatus() == Board.length)
                expandNode(promisingNode);

            // Phase 3 - Simulation
            Node nodeToExplore = promisingNode;
            if (promisingNode.getChildArray().size() > 0) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);
            // Phase 4 - Update
            backPropogation(nodeToExplore, playoutResult);
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getState().getBoard();
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (node.getChildArray().size() != 0) {
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }

    private void expandNode(Node node) {
        List<State> possibleStates = node.getState().getAllPossibleStates();
        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            newNode.getState().setPlayerNo(node.getState().getOpponent());
            node.getChildArray().add(newNode);
        });
    }

    private void backPropogation(Node nodeToExplore, int playerNo) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            if (tempNode.getState().getPlayerNo() == playerNo)
                tempNode.getState().addScore(WIN_SCORE);
            tempNode = tempNode.getParent();
        }
    }

    private int simulateRandomPlayout(Node node) {
        Node tempNode = new Node(node);
        State tempState = tempNode.getState();
        int boardStatus = tempState.getBoard().checkStatus();

        if (boardStatus == oponent) {
            tempNode.getParent().getState().setWinScore(Integer.MIN_VALUE);
            return boardStatus;
        }
        while (boardStatus == Board.length) {
            tempState.togglePlayer();
            tempState.randomPlay();
            boardStatus = tempState.getBoard().checkStatus();
        }

        return boardStatus;
    } 
    
	
	private void printState()
	{
		printBoard(Board);
	}

	
	private void printBoard(byte[] board)
	{
		System.out.println("It's " + Playername+ "'s turn");
		//System.out.printf("Turn %2d:\t", Duration);
		for(int i =0; i<N;i++)
			System.out.print("  |  " + (i));
		System.out.println(" |");
		for(int i =0; i<N;i++)
			System.out.print("------");
		System.out.println(" |");
		
		
		for(int i=0; i<N;i++) {
			System.out.print((i) + " |");
			for (int j = 0; j < N; j++)
			{
				String sign = board[i * N + j] == 0 ? "0" : board[i * N + j] == 1 ? "2" : "1";
				if (i == iLast && j == jLast)
					System.out.printf("%1s", sign + "  |  ");
				else
					System.out.printf("%1s", sign + "  |  ");
			}
			System.out.println("");
			
			for(int j =0; j<N;j++)
				System.out.print("------");
			System.out.println("");
			
		}	

	}
	private void makeMove(int i, int j)
	{

		if (i >= 0 && i < N &&
				j >= 0 && j < N &&
				Board[i * N + j] != 3 - Turn)
		{
			iLast = i;
			jLast = j;
			Board[i * N + j] = (byte) Turn;
		}
	}

	private int[] askMove()
	{
		Scanner in = new Scanner(System.in);
		while (true)
		{
			System.out.printf("Please enter the row number\n>>> ");
			int i = in.nextInt();
			System.out.printf("Please enter the column number\n>>> ");
			int j = in.nextInt();

			if (i >= 0 && i < N && j >= 0 && j < N && Board[i * N + j] == 0)
				return new int[]{i, j};
			else
				System.out.printf("Wrong move {%d, %d}. Try again...\n", i, j);
		}

	}

	public void gameOn()
	{
		printState();
		Turn = 1;

		do
		{
			Turn = (byte) (3 - Turn);

			// AI
			if (Turn == 1)
			{
				int[] move = AI.getMove(Board, Turn);
				makeMove(move[0], move[1]);
			}
			// Player
			else
			{
				int[] move = askMove();
				makeMove(move[0], move[1]);
			}

			Duration++;
			printState();
		}
		while (!gameOver());

		System.out.printf("Game over. %s won.", Turn == 2 ? "Player" : "AI");
	}


	private boolean gameOver()
	{
		return gameOverFor(Board, iLast, jLast) != 0;
	}

	private int gameOverFor(byte[] board, int iCoordinate, int jCoordinate)
	{
		int playerSign = board[iCoordinate * N + jCoordinate];

		// Horizontal(-) Check
		int leftBorder = jCoordinate - consecutiveCount >= 0 ? jCoordinate - consecutiveCount : 0;
		int rightBorder = jCoordinate + consecutiveCount < N ? jCoordinate + consecutiveCount : N - 1;
		int hCounter = 1;
		for (int j = jCoordinate - 1; j >= leftBorder; j--)
		{
			if (board[iCoordinate * N + j] == playerSign)
				hCounter++;
			else
				break;
		}
		for (int j = jCoordinate + 1; j <= rightBorder; j++)
		{
			if (board[iCoordinate * N + j] == playerSign)
				hCounter++;
			else
				break;
		}
		if (hCounter >= consecutiveCount)
			return playerSign;


	int topBorder = iCoordinate - consecutiveCount >= 0 ? iCoordinate - consecutiveCount : 0;
		int bottomBorder = iCoordinate + consecutiveCount < N ? iCoordinate + consecutiveCount : N - 1;
		int vCounter = 1;
		for (int i = iCoordinate - 1; i >= topBorder; i--)
		{
			if (board[i * N + jCoordinate] == playerSign)
				vCounter++;
			else
				break;
		}
		for (int i = iCoordinate + 1; i <= bottomBorder; i++)
		{
			if (board[i * N + jCoordinate] == playerSign)
				vCounter++;
			else
				break;
		}
		if (vCounter >= consecutiveCount)
			return playerSign;


		int neswCounter = 1;
		int i = iCoordinate + 1;
		int j = jCoordinate + 1;
		while (i <= bottomBorder && j <= rightBorder)
		{
			if (board[i++ * N + j++] == playerSign)
				neswCounter++;
			else
				break;
		}

		i = iCoordinate - 1;
		j = jCoordinate - 1;
		while (i >= topBorder && j >= leftBorder)
		{
			if (board[i-- * N + j--] == playerSign)
				neswCounter++;
			else
				break;
		}

		if (neswCounter >= consecutiveCount)
			return playerSign;


		int nwseCounter = 1;
		i = iCoordinate + 1;
		j = jCoordinate - 1;
		while (i <= bottomBorder && j >= leftBorder)
		{
			if (board[i++ * N + j--] == playerSign)
				nwseCounter++;
			else
				break;
		}

		i = iCoordinate - 1;
		j = jCoordinate + 1;
		while (i >= topBorder && j <= rightBorder)
		{
			if (board[i-- * N + j++] == playerSign)
				nwseCounter++;
			else
				break;
		}
		if (nwseCounter >= consecutiveCount)
			return playerSign;

		return 0;

	}

}
