package puzzleAlgorithm;

import java.util.List;

public class BoardSolution {
	
	private boolean isFound;
	public boolean isFound() {
		return isFound;
	}

	private PuzzleSquare[][] board;
	
	public PuzzleSquare[][] getBoard() {
		return board;
	}

	public List<PuzzleDefinition> getDefinitions() {
		return definitions;
	}

	private List<PuzzleDefinition> definitions;
	
	public BoardSolution(PuzzleSquare[][] board, List<PuzzleDefinition> definitions, boolean isFound) {
		this.board = board;
		this.definitions = definitions;
		this.isFound =true;
	}
}
