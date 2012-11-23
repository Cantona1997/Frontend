package hikst.frontend.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.google.gwt.user.client.Window;

public class Settings {
	private static Properties configFile = new Properties();;
	private final static String FILENAME = "frontend.properties";
	private static File file = new File(FILENAME);
	private static String db_hostname, db_port, db_db, db_user, db_pw;
	private static Connection dbc;

	private static Settings instance;

	public Settings() throws SettingsDotPrefNotFoundException {
		load();
	}

	public static boolean writeConfig(String hostname, String port, String db,
			String user, String password) {
		configFile.setProperty("DB_HOSTNAME", hostname);
		configFile.setProperty("DB_PORT", port);
		configFile.setProperty("DB_DB", db);
		configFile.setProperty("DB_USER", user);
		configFile.setProperty("DB_PW", password);

		try {
			configFile.store(new FileWriter(file), null);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}
	
/*
	private static String searchFolder(String folder, String fileName)
	{
		String fullPath = "";
		File dir = new File(folder);
		if (dir.isDirectory())
			{
			String[] children = dir.list();
			if (children != null) {
			for (int i=0; i<children.length; i++) {
			// Get filename of file or directory
			if (children[i].toLowerCase().indexOf(fileName.toLowerCase()) >= 0) {
				fullPath = folder + "/" + fileName;
				return fullPath;
			}
			else if (children[i].indexOf("..") < 0) {
				String subSearch = searchFolder(folder + "/" + children[i], fileName);	
				if (subSearch.toLowerCase().indexOf(fileName.toLowerCase()) >= 0) {
					return subSearch;
				}
				}
			}
			
			}
			return "File not found!";
		}
		return fullPath;
	}
*/
	public static boolean loadable() {
		// Hvis instanset ikke eksisterer må vi opprette det
		if (instance == null) {
			try {
				instance = new Settings();
			} catch (SettingsDotPrefNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	private void load() throws SettingsDotPrefNotFoundException {
		try {
			//configFile.load(new FileReader(file));

			db_hostname = "localhost";/*configFile.getProperty("DB_HOSTNAME");*/ 
			db_port = "5432";/*configFile.getProperty("DB_PORT");*/
			db_db = "db";/*configFile.getProperty("DB_DB");*/
			db_user = "sysut";/*configFile.getProperty("DB_USER");*/
			db_pw = "password";/*configFile.getProperty("DB_PW");*/
		} catch (Exception e) {
			Window.alert("Databasen er ute av drift");
			throw new SettingsDotPrefNotFoundException();
			
		}
	}

	/*
	 * Did anyone need a database connection??? This lets the application use
	 * the database connection everywhere.
	 */
	public static Connection getDBC() {
		if (openDatabaseConnection()) {
			return dbc;
		}
		return null;
	}

	private static boolean openDatabaseConnection() {

		try {
			if (!dbc.isClosed()) {
				return true;
			}
		} catch (Exception e) {
			// This is not unexpected and will always fail before the dbc is
			// created.
			// Please just continue!
		}

		try {
			Class.forName("org.postgresql.Driver");
			
			dbc = DriverManager
					.getConnection("jdbc:postgresql://" + db_hostname + ":"
							+ db_port + "/" + db_db, db_user, db_pw);
			return !dbc.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}
