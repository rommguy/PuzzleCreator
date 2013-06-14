package puzzleAlgorithm;

import gui.CrosswordView;
import gui.MainView;
import gui.WaitView;
import gui.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import main.PuzzleCreator;

import utils.DBUtils;
import utils.Logger;

public class AlgorithmWorker extends SwingWorker<BoardSolution, String> {
	// System.getProperty("file.separator")
	protected PuzzleSquare[][] board;
	protected List<PuzzleDefinition> definitions;
	private List<PuzzleDefinition> unSolved;
	protected List<Answer> answers;
	protected Set<Integer> usedEntities;
	private boolean success = false;

	private int[] topicsIds;
	private int difficulty;
	private WaitView view; // parent window which activated this thread

	public AlgorithmWorker(WaitView view, int[] topics, int difficulty) {
		this.topicsIds = topics;
		this.difficulty = difficulty;
		this.view = view;
		this.answers = new ArrayList<Answer>();
		this.unSolved = new ArrayList<PuzzleDefinition>();
		this.definitions = new ArrayList<PuzzleDefinition>();
		this.usedEntities = new HashSet<Integer>();
	}

	@Override
	protected BoardSolution doInBackground() {
		BoardSolution result = null;
		int size = 13;
		switch (difficulty) {
		case 0:
			size = 8;
			break;
		case 1:
			size = 11;
			break;
		case 2:
			size = 13;
			break;
		default:
			break;
		}

		publish("Retrieving possible answers from DataBase...");
		// TODO remove use of mock function after tests
		// createMockAnswers();
		answers = DBUtils.getPossibleAnswers(this.topicsIds, 11);
		Logger.writeToLog("Number of answers = " + answers.size());

		publish("Creating puzzle board...");
		createBoardFromTemplateFile(size, 1);
		Collections.sort(definitions);
		printBoard();
		printTopics();
		printBoardStatus();
		Logger.writeToLog("Optimizing board");
		optimizeBoard();
		printBoardStatus();
		publish("Sorting answers on board...");
		if (!fillBoard()) {
			success = false;
			Logger.writeErrorToLog("impossible data");
			publish("failed to create Puzzle");
			result = new BoardSolution(null, null, false, null);
		} else {
			success = true;
			Logger.writeToLog("success");
			publish("Retrieving hints and definitions from DataBase...");
			DBUtils.setHintsAndDefinitions(definitions);
			result = new BoardSolution(board, definitions, true, null);
			printResults();
			publish("Finished!");
		}
		return result;
	}

	@Override
	protected void done() {
		try {
			BoardSolution result = get();
			if (result.getResultException() != null) {
				Exception ex = result.getResultException();
				if (ex instanceof SQLException) {
					Utils.showDBConnectionErrorMessage();
				}
				if (ex instanceof IOException){
					//TODO show IO error message in GUI - can't read template file 
				}
			} else {
				CrosswordView crosswordView = (CrosswordView) CrosswordView.start(result);
				MainView.getView().setCrosswordView(crosswordView); // adds JPanel to MainView card
				if (success)
					view.setGoBtn(true);
				else
					view.setSkipBtnToTryAgain();
			}
		} catch (Exception ex) {
			Logger.writeErrorToLog("algorithm was interrupted before board was finished");
		}
	}

	@Override
	public void process(List<String> messages) {
		view.setProgressMessage(messages.get(messages.size() - 1));
	}

	private boolean fillBoard() {
		Deque<BoardState> stack = new ArrayDeque<BoardState>();
		boolean solved = false;
		unSolved.addAll(definitions);
		Collections.sort(unSolved);

		outerLoop: while (!solved) {

			PuzzleDefinition def = unSolved.get(0);
			List<Answer> possibleAnswers = def.getPossibleAnswers();
			innerLoop: while (!def.isSolved()) {
				if (possibleAnswers.size() == 0) {
					if (!popBoardState(stack)) {
						break outerLoop;
					}
					optimizeBoard();
					continue outerLoop;
				}
				int index = (int) Math.floor(Math.random() * possibleAnswers.size());
				Answer currentAnswer = possibleAnswers.get(index);

				/*
				 * check if this answers's entity was already used TODO The
				 * possible answers should be updated by entity id when the
				 * problematic answer is assigned
				 */
				if (usedEntities.contains(currentAnswer.getEntityId())) {
					possibleAnswers.remove(currentAnswer);
					continue innerLoop;
				}

				int row = def.getBeginRow();
				int column = def.getBeginColumn();
				char direction = def.getDirection();
				for (int letterIndex = 0; letterIndex < currentAnswer.length; letterIndex++) {
					if (!board[column][row].checkLetter(currentAnswer.getAnswerString().charAt(letterIndex), false)) {
						possibleAnswers.remove(currentAnswer);
						continue innerLoop;
					}
					switch (direction) {
					case 'r':
						column++;
						break;
					case 'd':
						row++;
						break;
					default:
						return false;
					}
				}

				// fill currentAnswer to the board
				pushBoardState(stack, def, currentAnswer);
				insertAnswer(def, currentAnswer);
			}

			if (!def.isSolved()) {
				if (!popBoardState(stack)) {
					break;
				}
				continue;
			}

			unSolved.remove(def);
			updateUnSolved();

			if (stack.size() == definitions.size())
				solved = true;

		}

		return solved;
	}

	private void optimizeBoard() {
		int size = board[0].length;
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				board[col][row].optimizeSquare();
			}
		}

		for (PuzzleDefinition def : definitions) {
			def.optimizeDefinition();
		}
	}

	/**
	 * This method creates a board state, and pushes it to the stack The board
	 * state includes a cloned board, with all squares cloned, and cloned
	 * definitions
	 * 
	 * @param stack
	 */
	private boolean pushBoardState(Deque<BoardState> stack, PuzzleDefinition lastDef, Answer currentAnswer) {
		int size = board[0].length;
		BoardState bs = new BoardState(size);
		List<PuzzleDefinition> clonedDefinitions = bs.getDefinitions();
		PuzzleSquare[][] clonedBoard = bs.getBoard();

		Set<Integer> stateUsedEntities = bs.getUsedEntites();
		for (int entityId : usedEntities) {
			stateUsedEntities.add(entityId);
		}

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				clonedBoard[col][row] = board[col][row].cloneSquare();
			}
		}

		for (PuzzleDefinition def : definitions) {
			if (def == lastDef) {
				PuzzleDefinition clonedLastDef = lastDef.cloneDefinition();
				clonedLastDef.setAnswer(currentAnswer);
				bs.setLastDef(clonedLastDef);
				clonedDefinitions.add(clonedLastDef);
			} else {
				clonedDefinitions.add(def.cloneDefinition());
			}
		}

		for (PuzzleDefinition def : clonedDefinitions) {
			int beginRow = def.getBeginRow();
			int beginCol = def.getBeginColumn();
			int length = def.getLength();

			switch (def.getDirection()) {
			case 'r':
				for (int col = beginCol; col < beginCol + length; col++) {
					clonedBoard[col][beginRow].addDefinition(def);
				}
				break;
			case 'd':
				for (int row = beginRow; row < beginRow + length; row++) {
					clonedBoard[beginCol][row].addDefinition(def);
				}
				break;
			default:
				Logger.writeErrorToLog("unknow direction '" + def.getDirection() + "'");
				return false;
			}
		}

		stack.push(bs);
		return true;
	}

	private boolean popBoardState(Deque<BoardState> stack) {
		if (stack.size() == 0) {
			return false;
		}
		BoardState bs = stack.pop();
		int size = board[0].length;

		// reset board letters
		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				board[col][row] = bs.getBoard()[col][row];
			}
		}

		// remove the assigned answer from the last definition solved in this
		// state
		PuzzleDefinition lastDef = bs.getLastDef();
		if (!lastDef.getPossibleAnswers().remove(lastDef.getAnswer())) {
			return false;
		}
		lastDef.setAnswer(new Answer("", -1));

		// reset definitions and used entities
		definitions.clear();
		definitions.addAll(bs.getDefinitions());
		usedEntities.clear();
		usedEntities.addAll(bs.getUsedEntites());

		unSolved.clear();
		for (PuzzleDefinition def : definitions) {
			if (!def.isSolved()) {
				unSolved.add(def);
			}
		}
		Collections.sort(unSolved);
		return true;
	}

	private void updateUnSolved() {
		List<PuzzleDefinition> newUnSolved = new ArrayList<PuzzleDefinition>();
		for (PuzzleDefinition def : unSolved) {
			if (!def.isSolved()) {
				newUnSolved.add(def);
			}
		}
		if (unSolved.size() != newUnSolved.size()) {
			unSolved.clear();
			unSolved.addAll(newUnSolved);
		}
		Collections.sort(unSolved);
	}

	private void insertAnswer(PuzzleDefinition def, Answer currentAnswer) {

		int row = def.getBeginRow();
		int column = def.getBeginColumn();
		for (int letterIndex = 0; letterIndex < currentAnswer.length; letterIndex++) {
			char direction = def.getDirection();
			board[column][row].checkLetter(currentAnswer.getAnswerString().charAt(letterIndex), true);
			switch (direction) {
			case 'r':
				column++;
				break;
			case 'd':
				row++;
				break;
			}
		}

		def.setAnswer(currentAnswer);
		def.markSolved();
		usedEntities.add(currentAnswer.getEntityId());
		// printBoard();

	}

	/**
	 * Create a new definition with the function params Insert definition to
	 * board definitions collection For each relevant square - add the
	 * definition to it's definitions
	 * 
	 * 
	 * @return
	 */
	private boolean insertDefinition(int beginRow, int beginCol, int length, char direction, int textRow, int textCol) {
		PuzzleDefinition def = new PuzzleDefinition(textRow, textCol, beginRow, beginCol, length, direction, this);
		definitions.add(def);

		switch (direction) {
		case 'r':
			for (int col = beginCol; col < beginCol + length; col++) {
				board[col][beginRow].addDefinition(def);
			}
			break;
		case 'd':
			for (int row = beginRow; row < beginRow + length; row++) {
				board[beginCol][row].addDefinition(def);
			}
			break;
		default:
			Logger.writeErrorToLog("unknow direction '" + direction + "'");
			return false;
		}

		return true;
	}

	private void printResults() {
		printBoard();
		for (PuzzleDefinition def : definitions) {
			Logger.writeToLog("def of length " + def.getLength() + " answer is :" + def.getAnswer().getAnswerString());
		}

	}

	private void printBoard() {
		Logger.writeToLog("Printing board:");
		for (int row = 0; row < board[0].length; row++) {
			String rowSt = "";
			for (int column = 0; column < board[0].length; column++) {
				PuzzleSquare square = board[column][row];
				if (!square.isLetter()) {
					rowSt += " # ";
				} else {
					if (square.getLetter() == 0) {
						rowSt += " - ";
					} else {
						rowSt += " " + square.getLetter() + " ";
					}
				}
			}
			Logger.writeToLog(rowSt);
			Logger.writeToLog("");

		}

	}

	private void printBoardStatus() {
		int counter = 0;
		for (PuzzleDefinition def : definitions) {
			Logger.writeToLog("def length: " + def.getLength() + " num of answers :" + def.getPossibleAnswers().size());
			counter += def.getPossibleAnswers().size();
		}
		Logger.writeToLog("Total number of possible answers = " + counter);
	}

	private void printTopics() {
		String topicsString = "Topics Ids: ";
		for (int i = 0; i < topicsIds.length; i++) {
			topicsString += topicsIds[i] + " ,";
		}
		Logger.writeToLog(topicsString);

	}

	private boolean createBoardFromTemplateFile(int size, int templateNum) {
		board = new PuzzleSquare[size][size];
		String fileName = "" + size + "x" + size + "_" + templateNum + ".tmp";
		File templateFile = new File(PuzzleCreator.appDir + "templates",
				fileName);
		try {
			FileReader in = new FileReader(templateFile);
			BufferedReader bin = new BufferedReader(in);
			bin.readLine();
			String line = bin.readLine();
			int row;
			int column;
			boolean emptySquare;
			int columnIndex;
			int rowIndex;
			int emptySquareIndex;
			String columnValue;
			String rowValue;
			while (line.startsWith("Puzzle")) {
				columnIndex = line.indexOf("column:");
				rowIndex = line.indexOf("row:");
				emptySquareIndex = line.indexOf("emptySquare:");
				if (rowIndex - columnIndex == 10) {
					columnValue = line.substring(columnIndex + 7, columnIndex + 9);
				} else {
					columnValue = "" + line.charAt(columnIndex + 7);
				}

				if (emptySquareIndex - rowIndex == 7) {
					rowValue = line.substring(rowIndex + 4, rowIndex + 6);
				} else {
					rowValue = "" + line.charAt(rowIndex + 4);
				}

				emptySquare = line.endsWith("true");

				column = Integer.parseInt(columnValue);
				row = Integer.parseInt(rowValue);
				board[column][row] = new PuzzleSquare(emptySquare, column, row);
				line = bin.readLine();
			}

			line = bin.readLine();
			int lengthIndex;
			String lengthValue;
			int length;
			int directionIndex;
			char direction;
			int textRowIndex;
			String textRowValue;
			int textRow;
			int textColIndex;
			String textColValue;
			int textCol;

			while (line != null) {
				rowIndex = line.indexOf("row:");
				columnIndex = line.indexOf("column:");
				lengthIndex = line.indexOf("length:");
				directionIndex = line.indexOf("direction:");
				textRowIndex = line.indexOf("textRow:");
				textColIndex = line.indexOf("textCol:");

				if (columnIndex - rowIndex == 7) {
					rowValue = line.substring(rowIndex + 4, rowIndex + 6);
				} else {
					rowValue = "" + line.charAt(rowIndex + 4);
				}

				if (lengthIndex - columnIndex == 10) {
					columnValue = line.substring(columnIndex + 7, columnIndex + 9);
				} else {
					columnValue = "" + line.charAt(columnIndex + 7);
				}

				if (directionIndex - lengthIndex == 10) {
					lengthValue = line.substring(lengthIndex + 7, lengthIndex + 9);
				} else {
					lengthValue = "" + line.charAt(lengthIndex + 7);
				}

				direction = line.charAt(directionIndex + 10);

				if (textColIndex - textRowIndex == 11) {
					textRowValue = line.substring(textRowIndex + 8, textRowIndex + 10);
				} else {
					textRowValue = "" + line.charAt(textRowIndex + 8);
				}

				textColValue = line.substring(textColIndex + 8);
				row = Integer.parseInt(rowValue);
				column = Integer.parseInt(columnValue);
				length = Integer.parseInt(lengthValue);
				textRow = Integer.parseInt(textRowValue);
				textCol = Integer.parseInt(textColValue);

				insertDefinition(row, column, length, direction, textRow, textCol);
				line = bin.readLine();
			}

			bin.close();
			in.close();
		} catch (IOException ex) {
			Logger.writeErrorToLog("failed to read template file : " + fileName);
			return false;
		}
		return true;
	}

}
