package massiveImport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Utils.DBConnector;
import Utils.Logger;

import net.sf.sevenzipjbinding.SevenZipException;

public class YagoFileHandler {

	//private static fields
	private static final String TSV = ".tsv";
	private static final String TSV_7Z = TSV + ".7z";
	private static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator");
	private static final String TEMP_DIR = HOME_DIR +"temp_yago_files" +  System.getProperty("file.separator");
	private static final String ZIP_FILE_DEST_DIR = TEMP_DIR + "7z_files" +  System.getProperty("file.separator");
	private static final String TSV_FILE_DEST_DIR = TEMP_DIR + "tsv_files" +  System.getProperty("file.separator");
	private static final String FILTERED_TSV_FILE_DEST_DIR = TEMP_DIR + "filtered_tsv_files" +  System.getProperty("file.separator");
	private static final String HAS_GENDER = "<hasGender>";

	// static yago files names
	public static final String YAGO_TYPES = "yagoTypes";
	public static final String YAGO_FACTS = "yagoFacts";
	public static final String YAGO_LITERAL_FACTS = "yagoLiteralFacts";
	public static final String YAGO_HUMAN_ANSWERS = "yagoHumanAnswers";

	// instance fields
	private Set<String> entityTypes = null;
	private Set<String> predicateTypes = null;
	private Set<String> litertalTypes = null;
	private Set<String> relevantEntities= null;

	public YagoFileHandler() {
		getTypes();
		relevantEntities = new HashSet<String>(); // will contain names of interesting entities
	}

	private void getEntityTypes()  { // can be changed in the future
		entityTypes = new HashSet<String>(); 
		fillCollectionEntitiesFromDB("riddle","definitions", "type", entityTypes);
	}

	private void getPredicateTypes() { // can be changed in the future
		predicateTypes = new HashSet<String>(); 
		fillCollectionEntitiesFromDB("riddle", "predicates", "predicate", predicateTypes);
	}

	private void getLiteralTypes() { // can be changed in the future
		litertalTypes = new HashSet<String>(); 
		fillCollectionEntitiesFromDB("riddle", "predicates", "predicate", litertalTypes);

	}

	private void fillCollectionEntitiesFromDB(String schema, String tableName, String entityType, Set<String> collection) {
		String columnName = "yago_" + entityType;
		List<Map<String,Object>> rs = null;
		String query = "SELECT " + tableName + "." + columnName +  " FROM " + tableName + ";";
		rs = DBConnector.executeQuery(schema,query);
		for (Map<String,Object> row : rs )
			collection.add((String)row.get(columnName));
	}

	private void getTypes() {
		getEntityTypes();
		getPredicateTypes();
		getLiteralTypes();

	}

	public static String getFilteredTsvFileDestDir() {
		return FILTERED_TSV_FILE_DEST_DIR;
	}


	private int getFileFromURL(String yagoFile) {
		URI uri = null;
		String zip_7z_file_path = ZIP_FILE_DEST_DIR + yagoFile + TSV_7Z;
		File zip_7z_file = new File(zip_7z_file_path);

		File tsv_file = new File(TSV_FILE_DEST_DIR + yagoFile + TSV);

		if  (tsv_file.exists()) { // TSV file exists
			Logger.writeToLog(yagoFile + TSV + " already exists.");
			return 1;
		}

		if (zip_7z_file.exists()) {
			Logger.writeToLog(yagoFile + TSV + " already downloaded.");
		}
		else { // need to download
			String urlStr = "http://www.mpi-inf.mpg.de/yago-naga/yago/download/yago/";
			try {
				uri = new URI(urlStr);
				uri = uri.resolve(yagoFile + TSV_7Z); 
				org.apache.commons.io.FileUtils.copyURLToFile(uri.toURL(), new File(zip_7z_file_path));
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		// extract yago 7z file
		Logger.writeToLog("Extracting " + zip_7z_file_path + " ...");

		// Get current time
		long start = System.currentTimeMillis();

		try {
			new SevenZipJBindingExtractor().extract(zip_7z_file_path, TSV_FILE_DEST_DIR);
		} catch (SevenZipException e) {
			Logger.writeErrorToLog("SevenZipException while extracting " + zip_7z_file_path + " .");
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.writeErrorToLog("IOException while extracting " + zip_7z_file_path + " ." );
			return 0;
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()-start;

		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;

		Logger.writeToLog("Finished Extracting " + zip_7z_file_path + "in " + elapsedTimeSec + "seconds." );


		return 1;

	}

	private BufferedReader getFileReader(String yagoFile) throws IOException{

		// file should have been created by now by getFileFromURL

		return new BufferedReader(new InputStreamReader (new FileInputStream(TSV_FILE_DEST_DIR + yagoFile + TSV),"UTF-8"));
	}

	private BufferedWriter getFileWriter(String yagoFile) throws IOException {

		// create file 
		File f =new File(FILTERED_TSV_FILE_DEST_DIR + yagoFile + TSV); // for example: FILE_DEST_DIR\light_yago_types.tsv
		if (!f.getParentFile().exists()) // create directory of logFile
			f.getParentFile().mkdirs();
		if (f.exists()) // delete if exists, to start with clean file
			f.delete();
		f.createNewFile();

		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
	}

	private String getProperName(String input) {

		String returnString;
		returnString= input.replace('_', ' ').replace('-', ' ').replaceAll("\\.", "");  // remove .,- and add spaces
		returnString = returnString.substring(1, returnString.length()-1); // trim <,>
		//remove _(....)
		int i = returnString.lastIndexOf('(');
		if (i<=0) // also for entities with 1 char e.g. (_)
			return returnString.toLowerCase();
		else 
			return returnString.substring(0, i-1).toLowerCase(); // get rid of ' ' +  '(... )' in end of entity names
	}

	private boolean containsNonEnglishChars(String input) {
		boolean i =  !input.matches("[a-z0-9 ]+");
		return i;

	}
	private int parseYagoTypes() throws IOException {
		int count = 0;
		int row = 1;
		String line = null;

		BufferedReader br = getFileReader(YAGO_TYPES);
		BufferedWriter bw = getFileWriter(YAGO_TYPES);

		Logger.writeToLog("scanning " + YAGO_TYPES + " ...");

		// Get current time
		long start = System.currentTimeMillis();

		try {
			while ((line = br.readLine()) != null)   {
				String[] lineColumns = line.split("\\t");
				String[] decomposedYagoID = lineColumns[0].split("_");
				if ((decomposedYagoID.length != 4) || (decomposedYagoID == null)) {
					Logger.writeErrorToLog("Invalid yagoID in line #" + row);
				}
				else {
					if ((lineColumns[1].length() <=50) && entityTypes.contains(lineColumns[3])) {

						String properName = getProperName(lineColumns[1]); // get clean entity name
						if (!containsNonEnglishChars(properName)) { // subject is of a relevant type and English letters only
							relevantEntities.add(lineColumns[1]);

							String newLine = lineColumns[1] + decomposedYagoID[1] + "\t"
									+ lineColumns[2] + "\t" 
									+ lineColumns[3] + "\t" 
									+ properName.replaceAll(" ", "") + "\t";

							StringBuffer buf = new StringBuffer(newLine);

							String[] entityNameDivided = properName.split(" ");
							if (entityNameDivided.length > 1) { // create additional information if there word count in entity > 1
								buf.append("(");
								for (int i = 0; i<entityNameDivided.length; ++i) {
									buf.append(entityNameDivided[i].length());
									if (i == entityNameDivided.length - 1) //last word in entity
										buf.append(")");
									else 
										buf.append(",");
								}
							}

							bw.write(buf.toString());
							bw.newLine();
							count++;
						}
					} 
				}
				row++;
			}
		} catch (IOException e) {
			Logger.writeErrorToLog("IOException in parseYagoTypes");
			return 0;
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		Logger.writeToLog(String.format("Finished scanning " + YAGO_TYPES + " : %d rows copied in %f seconds.", count, elapsedTimeSec));

		// close readers and commit changes
		br.close();
		bw.close();

		return 1;
	}

	private int parseYagoFacts() throws IOException {
		int count = 0;
		int row = 1;
		String line = null;

		BufferedReader br = getFileReader(YAGO_FACTS);
		BufferedWriter bw = getFileWriter(YAGO_FACTS);
		BufferedWriter bwAnswers = getFileWriter(YAGO_HUMAN_ANSWERS);

		Logger.writeToLog("scanning " + YAGO_FACTS + " ...");

		// Get current time
		long start = System.currentTimeMillis();

		try {
			while ((line = br.readLine()) != null)   {
				String[] lineColumns = line.split("\\t");	
				String[] decomposedYagoID = lineColumns[0].split("_");
				if ((decomposedYagoID.length != 4) || (decomposedYagoID == null)) {
					Logger.writeErrorToLog("Invalid yagoID in line #" + row);
				}
				else {
					boolean subjectHit = relevantEntities.contains(lineColumns[1]);
					boolean objectHit = relevantEntities.contains(lineColumns[3]);
					if ((subjectHit || objectHit) 
							&& (lineColumns[1].length() <= 50) && (lineColumns[3].length() <=50)
							&& (predicateTypes.contains(lineColumns[2]))) { // fact has relevant typeID for either subject or object and relevant fact

						String newLine = lineColumns[1] + decomposedYagoID[1] + "\t"
								+ lineColumns[2] + "\t" 
								+ lineColumns[3] + decomposedYagoID[3] + "\t";

						if (subjectHit) {
							String subjectLine = newLine + "1"; 
							bw.write(subjectLine); // write one line for subject matched
							bw.newLine();
							count++;
						}

						if (objectHit) {
							String objectLine = newLine + "0"; 
							bw.write(objectLine); // write one line for object matched
							bw.newLine();
							count++;
						}
					}

					if (subjectHit && (lineColumns[1].length() <= 50) && (lineColumns[2].compareTo(HAS_GENDER) == 0)) { // subject is human
						String properName = getProperName(lineColumns[1]); // human name in this predicate is in the subject
						int index = properName.indexOf(' ');
						String answerLine = null;
						if (index != -1)  { // at least one name
							answerLine = lineColumns[1] + decomposedYagoID[1] + "\t" + properName.substring(0, index) + "\tfirstname"; 
							bwAnswers.write(answerLine);
							bwAnswers.newLine();
							index = properName.lastIndexOf(' ');
							answerLine = lineColumns[1] + decomposedYagoID[1] + "\t" + properName.substring(index + 1, properName.length()) + "\tlastname";
							bwAnswers.write(answerLine);
							bwAnswers.newLine();
						}
					}
				}
				row++;
			}
		} catch (IOException e) {
			Logger.writeErrorToLog("IOException in parseYagoFacts");
			return 0;
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		Logger.writeToLog(String.format("Finished scanning " + YAGO_FACTS + " : %d rows copied in %f seconds.", count, elapsedTimeSec));

		// close readers and commit changes
		br.close();
		bw.close();
		bwAnswers.close();

		return 1;
	}

	private int parseYagoLiteralFacts() throws IOException {
		int count = 0;
		int row = 1;
		String line = null;
		String[] lineColumns = null;

		BufferedReader br = getFileReader(YAGO_LITERAL_FACTS);
		BufferedWriter bw = getFileWriter(YAGO_LITERAL_FACTS);

		Logger.writeToLog("scanning " + YAGO_LITERAL_FACTS + " ...");

		// Get current time
		long start = System.currentTimeMillis();

		try {
			while ((line = br.readLine()) != null)   {
				lineColumns = line.split("\\t");
				String[] decomposedYagoID = lineColumns[0].split("_");
				if ((decomposedYagoID.length != 4) || (decomposedYagoID == null)) {
					Logger.writeErrorToLog("Invalid yagoID in line #" + row);
				}
				else {
					if (lineColumns[1].length() <=50 && relevantEntities.contains(lineColumns[1]) && litertalTypes.contains(lineColumns[2])) { // checking by entity name because there are many rows with no yagoID
						String properLiteral = lineColumns[3].substring(1, lineColumns[3].lastIndexOf('"'));
						int index = properLiteral.indexOf('#');
						if (index != -1)
							properLiteral = properLiteral.substring(0, index - 1); // -1 to get rid of '-' char before '#' 

						String newline = lineColumns[1] + decomposedYagoID[1] + "\t" + lineColumns[2] + "\t" + properLiteral;
						bw.write(newline);
						bw.newLine();
						count++;
					}
				}
				row++;
			}

		} catch (IOException e) {
			Logger.writeErrorToLog("IOException in parseYagoLiteralFacts");
			return 0;
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		Logger.writeToLog(String.format("Finished scanning " + YAGO_LITERAL_FACTS + " : %d rows copied in %f seconds.", count, elapsedTimeSec));

		// close readers and commit changes
		br.close();
		bw.close();

		return 1;
	}

	public void deleteAllYagoFiles() {
		deleteYagoFile(YAGO_TYPES);
		deleteYagoFile(YAGO_FACTS);
		deleteYagoFile(YAGO_LITERAL_FACTS);

		//deleting empty directories
		deleteFileOrDirectory(ZIP_FILE_DEST_DIR);
		deleteFileOrDirectory(TSV_FILE_DEST_DIR);
		deleteFileOrDirectory(FILTERED_TSV_FILE_DEST_DIR);
		deleteFileOrDirectory(TEMP_DIR);

	}

	private void deleteYagoFile(String yagoFile) {
		// delete 7z file
		deleteFileOrDirectory(ZIP_FILE_DEST_DIR + yagoFile + TSV_7Z);
		//delete tsv file
		deleteFileOrDirectory(TSV_FILE_DEST_DIR + yagoFile + TSV);
		//delete filtered TSV
		deleteFileOrDirectory(FILTERED_TSV_FILE_DEST_DIR +  yagoFile + TSV);
	}

	private void deleteFileOrDirectory(String file) {
		File f;
		f = new File(file);
		if (f.exists())
			if (f.isFile() || (f.isDirectory() && (f.list().length == 0))) // file or empty directory
				f.delete();
	}

	public int createFilteredYagoFiles() {

		// create filtered TSV files
		try {
			parseYagoTypes();
			parseYagoFacts();
			parseYagoLiteralFacts();
		} catch (IOException e) {
			return 0;
		}
		return 1;
	}

	public void getFilesFromURL() {

		getFileFromURL(YAGO_TYPES);
		getFileFromURL(YAGO_FACTS);
		getFileFromURL(YAGO_LITERAL_FACTS);
	}
}