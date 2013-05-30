package massiveImport;
import java.io.IOException;
import Utils.DBConnector;
import Utils.Logger;


public class MassiveImporter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static boolean runMassiveImporter() {
						
				Logger.writeToLog("Starting importing process...");
		
				YagoFileHandler y = new YagoFileHandler();
				
				Logger.writeToLog("Downloading and extracting yago files from website...");
		
				y.getFilesFromURL(); // download yago files
				
				Logger.writeToLog("Filtering TSV files...");
		
				y.createFilteredYagoFiles(); // create TSVs with relevant data only
		//
		//		Logger.writeToLog("Importing TSV files to DB...");
		//		
		//		DBConnector.executeSqlScript("dbproject", "c:\\Users\\yonatan\\temp_yago_files\\script.sql");
		//
		////		// IMPORT data to tables
		////		String type_table = "yago_type";
		////		String fact_table = "yago_fact";
		////		String literal_fact_table = "yago_literal_fact";
		////		String schema_name = "dbproject";
		////		
		////		DBConnector.createSchema(schema_name);
		////		
		////		DBConnector.createTable(schema_name, type_table);		
		////		DBConnector.executeSql(schema_name, DBConnector.buildImportSql(YagoFileHandler.YAGO_TYPES, type_table));
		////
		////		DBConnector.createTable(schema_name, fact_table);		
		////		DBConnector.executeSql(schema_name, DBConnector.buildImportSql(YagoFileHandler.YAGO_FACTS, fact_table));
		////
		////		DBConnector.createTable(schema_name, literal_fact_table);		
		////		DBConnector.executeSql(schema_name, DBConnector.buildImportSql(YagoFileHandler.YAGO_LITERAL_FACTS, literal_fact_table));
		//
		//		//y.deleteAllYagoFiles(); // delete all temporary files and folders
		//		
		//		Logger.writeToLog("Finished importing process!");
		//
				return true;
	}

}