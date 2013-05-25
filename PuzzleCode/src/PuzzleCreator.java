import puzzleAlgorithm.*;
import massiveImport.*;
import Utils.*;
public class PuzzleCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.initialize(false);
		MassiveImporter.runMassiveImporter();
		AlgorithmRunner.runAlgorithm();

	}

}
