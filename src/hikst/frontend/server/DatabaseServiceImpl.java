package hikst.frontend.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLPermission;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpSession;

import hikst.frontend.client.DatabaseService;
import hikst.frontend.shared.Description;
import hikst.frontend.shared.HikstObject;
import hikst.frontend.shared.HikstObjectTree;
import hikst.frontend.shared.ImpactDegree;
import hikst.frontend.shared.ImpactType;
import hikst.frontend.shared.LoginRequest;
import hikst.frontend.shared.Plot;
import hikst.frontend.shared.RegisterRequest;
import hikst.frontend.shared.SimulationRequest;
import hikst.frontend.shared.SimulationTicket;
import hikst.frontend.shared.SimulatorObject;
import hikst.frontend.shared.UsagePattern;
import hikst.frontend.shared.ViewSimulationObject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DatabaseService {
	private static final int SALT_LENGTH = 20;
	private static final SecureRandom randomizer = new SecureRandom();

	@Override
	public List<Plot> getData(int sim_description_id)
			throws IllegalArgumentException {

		Connection connection = Settings.getDBC();
		List<Plot> plots = new ArrayList<Plot>();

		try {
			String query = "SELECT Time, Effect, Power_Consumption, Voltage, Current FROM Simulations WHERE Sim_Description_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, sim_description_id);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				Date time = new Date(set.getLong(1));
				float effect = set.getFloat(2);
				float power_consumption = set.getFloat(3);
				float voltage = set.getFloat(4);
				float current = set.getFloat(5);

				plots.add(new Plot(time, effect, power_consumption, voltage,
						current));
			}
		} catch (SQLException ex) {
			// send out error message some way....
			ex.printStackTrace();
		}

		return plots;
	}

	@Override
	public List<Integer> getSimulationDescriptionsIDs() {

		List<Integer> simulation_descriptions = new ArrayList<Integer>();

		Connection connection = Settings.getDBC();

		try {

			String query = "SELECT Simulation_Descriptions.ID FROM Simulation_Descriptions, Simulator_Queue_Objects WHERE Simulation_Descriptions.ID = Simulator_Queue_Objects.Simulation_Descriptions_ID and Status_ID=4";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				simulation_descriptions.add(set.getInt(1));
			}
		} catch (SQLException ex) {
			// send message back to client in some way...
			// paste in here...
			ex.printStackTrace();
			throw new IllegalArgumentException();
		}

		return simulation_descriptions;
	}

	@Override
	public Description getSimulation(int sim_description_id) {
		try {

			Connection connection = Settings.getDBC();

			String query = "SELECT Object_ID, minimumTime, maximumTime FROM Simulation_Descriptions WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);

			statement.setInt(1, sim_description_id);

			ResultSet set = statement.executeQuery();

			if (set.next()) {
				int object_id = set.getInt(1);
				long startTime = set.getLong(2);
				long endTime = set.getLong(3);
				List<Plot> plots = new ArrayList<Plot>();

				String anotherQuery = "SELECT Time, Effect, Power_Consumption, Voltage, Current FROM Simulations WHERE Sim_Description_ID=?";
				PreparedStatement anotherStatement = connection
						.prepareStatement(anotherQuery);
				anotherStatement.setInt(1, sim_description_id);
				ResultSet anotherSet = anotherStatement.executeQuery();

				while (anotherSet.next()) {
					Date time = new Date(anotherSet.getLong(1));
					float effect = anotherSet.getFloat(2);
					float power_consumption = anotherSet.getFloat(3);
					float voltage = anotherSet.getFloat(4);
					float current = anotherSet.getFloat(5);

					plots.add(new Plot(time, effect, power_consumption,
							voltage, current));
				}

				return new Description(sim_description_id, plots, startTime,
						endTime);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<Description> getSimulations() throws IllegalArgumentException {

		List<Description> simulations = new ArrayList<Description>();

		List<Integer> descriptionIDs = getSimulationDescriptionsIDs();

		for (int i = 0; i < descriptionIDs.size(); i++) {
			simulations.add(this.getSimulation(descriptionIDs.get(i)));
		}

		return simulations;
	}

	@Override
	public boolean authenticate(LoginRequest request) {

		// return true;
		String username = request.getUsername();
		String password = request.getPassword();
		System.out.println(password);
		Connection connection = Settings.getDBC();

		try {
			String query = "SELECT Firstname, Lastname, Email, Password, Salt, Access_Level_ID FROM Users WHERE Username=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);
			ResultSet set = statement.executeQuery();

			if (set.next()) {
				String firstname = set.getString(1);
				String lastname = set.getString(2);
				String email = set.getString(3);
				String passwordFromDB = set.getString(4);
				String salt = set.getString(5);
				int access_level_id = set.getInt(6);

				String access_level = String.valueOf(access_level_id);

				try {
					String hashedPassword = getHash(password);
					System.out.println(hashedPassword);
					// log-in attempt successful!!
					if (passwordFromDB.equals(hashedPassword)) {
						HttpSession session = getSession();

						User user = new User(username, firstname, lastname,
								email, salt, access_level);
						session.setAttribute(hashedPassword, user);

						return true;
					} else {
						return false;
					}
				} catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
					return false;
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
					return false;
				}
			} else {
				return false;
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		/*
		 * catch(AccessLevelNotFoundException ex) { ex.printStackTrace(); return
		 * false; }
		 */

	}

	private String getHash(String key) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		/*
		 * String text = key; messageDigest.update(text.getBytes("UTF-8"));
		 * byte[] hash = messageDigest.digest(); return new String(hash);
		 */
		messageDigest.update(key.getBytes());
		return new sun.misc.BASE64Encoder().encode(messageDigest.digest());

	}

	private String generateSalt() {
		byte[] salt = new byte[SALT_LENGTH];
		randomizer.nextBytes(salt);
		return new String(salt);
	}

	private HttpSession getSession() {
		return this.getThreadLocalRequest().getSession();
	}

	@Override
	public boolean logOff() {

		HttpSession session = getSession();
		session.invalidate();
		return true;
	}

	@Override
	public boolean register(RegisterRequest request) {
		Connection connection = Settings.getDBC();

		try {
			String query = "INSERT INTO Users VALUES(?,?,?,?,?,?,?);";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, request.getUsername());
			statement.setString(2, request.getFirstname());
			statement.setString(3, request.getLastname());
			statement.setString(4, request.getEmail());

			String salt = generateSalt();
			String hashedPassword = getHash(request.getPassword());
			statement.setString(5, hashedPassword);
			statement.setString(6, salt);
			// sets to 0 for regular user-access
			statement.setInt(7, 0);

			statement.executeQuery();

			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean exists(String username) {

		Connection connection = Settings.getDBC();

		try {
			String query = "SELECT Username FROM Users WHERE Username=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);
			ResultSet set = statement.executeQuery();

			return set.next();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// private HikstObject getHikstObject(int id) throws SQLException {
	// Connection connection = Settings.getDBC();
	// String query =
	// "SELECT Id,Name, Effect, Voltage, Current, Longitude, Latitude, self_temperature, target_temperature, base_area, base_height WHERE ID=?";
	// PreparedStatement statement = connection.prepareStatement(query);
	// statement.setInt(1, id);
	//
	// ResultSet set = statement.executeQuery();
	//
	// if (set.next()) {
	// HikstObject hikstObject = new HikstObject();
	// hikstObject.setID(set.getInt(1));
	// hikstObject.name = set.getString(1);
	// hikstObject.effect = set.getDouble(2);
	// hikstObject.voltage = set.getDouble(3);
	// hikstObject.current = set.getDouble(5);
	// hikstObject.longitude = set.getDouble(6);
	// hikstObject.latitude = set.getDouble(7);
	// hikstObject.self_temperature = set.getDouble(8);
	// hikstObject.target_temperature = set.getDouble(9);
	// hikstObject.base_area = set.getDouble(10);
	// hikstObject.base_height = set.getDouble(11);
	// return hikstObject;
	// } else {
	// return null;
	// }
	// }

	public ArrayList<ImpactType> getImpactTypes() {
		Connection connection = Settings.getDBC();
		ArrayList<ImpactType> impactTypes = new ArrayList<ImpactType>();

		try {
			String query = "SELECT ID, Name FROM Type";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				int id = set.getInt(1);
				String key = set.getString(2);

				ImpactType type = new ImpactType();
				type.ID = id;
				type.name = key;

				impactTypes.add(type);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return impactTypes;
	}

	public ArrayList<ViewSimulationObject> getViewSimulationObjects() {
		Connection connection = Settings.getDBC();
		ArrayList<ViewSimulationObject> viewsimobjects = new ArrayList<ViewSimulationObject>();

		try {
			String query = "SELECT Objects.Name, Simulation_Descriptions.ID, Status.Name FROM Objects, Simulation_Descriptions, Status, Simulator_Queue_Objects "
					+ "WHERE Simulation_Descriptions.Object_ID = Objects.ID "
					+ "AND Simulation_Descriptions.ID = Simulator_Queue_Objects.Simulation_Descriptions_ID "
					+ "AND Simulator_Queue_Objects.Status_ID = Status.ID";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				int id = set.getInt(2);
				String obj_Name = set.getString(1);
				String stat_name = set.getString(3);

				ViewSimulationObject object = new ViewSimulationObject();

				object.setID(id);
				object.Object_Name = obj_Name;
				object.Status_Name = stat_name;

				viewsimobjects.add(object);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return viewsimobjects;

	}

	@Override
	public boolean deleteObject(int object_id) {

		try {
			Connection connection = Settings.getDBC();

			String query = "DELETE FROM Part_Objects WHERE Father_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, object_id);
			statement.executeUpdate();

			String nextQuery = "DELETE FROM Part_Objects WHERE Son_ID=?";
			PreparedStatement nextStatement = connection
					.prepareStatement(nextQuery);
			nextStatement.setInt(1, object_id);
			nextStatement.executeUpdate();

			String anotherQuery = "DELETE FROM Objects WHERE ID=?";
			PreparedStatement anotherStatement = connection
					.prepareStatement(anotherQuery);
			anotherStatement.setInt(2, object_id);
			anotherStatement.executeUpdate();

			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public Integer requestSimulation(SimulationRequest request) {

		try {
			Connection connection = Settings.getDBC();

			String query = "INSERT INTO Simulation_Descriptions(Object_ID, timeIntervall,minimumTime,maximumTime) VALUES(?,?,?,?) RETURNING ID";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, request.getSimulation_object_id());
			statement.setLong(2, request.getIntervall());
			statement.setLong(3, request.getFrom());
			statement.setLong(4, request.getTo());
			ResultSet set = statement.executeQuery();
			// TODO: Feil på linjen over sql exception....

			int description_id = 0;

			if (set.next()) {
				description_id = set.getInt(1);
			} else {
				description_id = Integer.MAX_VALUE;
			}

			String anotherQuery = "INSERT INTO Simulator_Queue_Objects(Simulator_ID,Status_ID,Simulation_Descriptions_ID) VALUES(?,?,?) RETURNING ID";
			PreparedStatement anotherStatement = connection
					.prepareStatement(anotherQuery);
			anotherStatement.setNull(1, java.sql.Types.INTEGER);
			// 5 er id`en til "Pending" i Status-tabellen
			anotherStatement.setInt(2, 5);
			anotherStatement.setInt(3, description_id);
			ResultSet anotherSet = anotherStatement.executeQuery();

			int queue_id = 0;

			if (anotherSet.next()) {
				queue_id = anotherSet.getInt(1);
			} else {
				queue_id = Integer.MAX_VALUE;
			}

			return queue_id;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}

	}

	@Override
	public boolean deleteSimulations(int id) {

		try {
			Connection connection = Settings.getDBC();

			String query = "DELETE FROM Simulations WHERE Sim_Description_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			statement.executeUpdate();

			String anotherQuery = "DELETE FROM Simulation_Descriptions WHERE ID=?";
			PreparedStatement anotherStatement = connection
					.prepareStatement(anotherQuery);
			anotherStatement.setInt(1, id);
			anotherStatement.executeUpdate();

			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean changeAccessLevel(String username, String access_level) {

		try {
			Connection connection = Settings.getDBC();

			int access_level_id = AccessLevel.getInstance().getID(access_level);

			String query = "UPDATE Users set Access_Level_ID=? WHERE Username=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, access_level_id);
			statement.setString(2, username);

			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		} catch (AccessLevelIDNotFoundException ex) {
			ex.printStackTrace();
			return true;
		}
	}

	@Override
	public int getSimulationStatus(SimulationTicket ticket) {

		try {
			Connection connection = Settings.getDBC();

			int queueID = ticket.getQueueID();
			System.out.println("Ticket queue id: " + ticket.getQueueID());
			System.out.println("Ticket description id: "
					+ ticket.getDescriptionID());
			String query = "SELECT Status_ID FROM Simulator_Queue_Objects WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, queueID);
			ResultSet set = statement.executeQuery();

			if (set.next()) {
				return set.getInt(1);
			} else {
				return Integer.MAX_VALUE;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();

			return Integer.MAX_VALUE;
		}
	}

	@Override
	public boolean setUp(String hostname, String port, String name,
			String user, String password) {

		return Settings.writeConfig(hostname, port, name, user, password);

	}

	@Override
	public boolean settingsLoadable() {

		return Settings.loadable();
	}

	@Override
	public HikstObjectTree loadObject(int id) {

		try {

			return getTree(id);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	private HikstObjectTree getTree(int id) throws SQLException {
		HikstObjectTree treeItem = new HikstObjectTree();
		HikstObject hikstObject = getObject(id);

		treeItem.setItem(hikstObject);

		ArrayList<Integer> sons = hikstObject.sons;

		for (int index = 0; index < sons.size(); index++) {
			treeItem.addSon(getTree(sons.get(index)));
		}

		return treeItem;
	}

	private HikstObject getObject(int id) {
		Connection connection = Settings.getDBC();

		String query = "SELECT name, effect, voltage, current, usage_pattern_id,latitude, longitude,"
				+ "self_temperature, target_temperature, base_area, base_height, heat_loss_rate from objects where id=?";

		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();

			if (set.next()) {
				HikstObject hikstObject = new HikstObject();
				hikstObject.setID(id);
				hikstObject.name = set.getString(1);
				hikstObject.effect = set.getDouble(2);
				hikstObject.voltage = set.getDouble(3);
				hikstObject.current = set.getDouble(4);
				hikstObject.usage_pattern_ID = set.getInt(5);
				hikstObject.latitude = set.getDouble(6);
				hikstObject.longitude = set.getDouble(7);
				hikstObject.self_temperature = set.getDouble(8);
				hikstObject.target_temperature = set.getDouble(9);
				hikstObject.base_area = set.getDouble(10);
				hikstObject.base_height = set.getDouble(11);
				hikstObject.heat_loss_rate = set.getDouble(12);
				hikstObject.sons = getHikstObjectChildren(hikstObject.getID());
				return hikstObject;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public ArrayList<HikstObject> getSimObjects() {

		ArrayList<HikstObject> hikstObjects = new ArrayList<HikstObject>();

		try {
			Connection connection = Settings.getDBC();
			String query = "SELECT id, name, effect, voltage, current, usage_pattern_id,latitude, longitude,"
					+ "self_temperature, target_temperature, base_area, base_height, heat_loss_rate from objects";
			PreparedStatement preparedStatement = connection
					.prepareStatement(query);
			ResultSet set = preparedStatement.executeQuery();

			while (set.next()) {
				HikstObject hikstObject = new HikstObject();
				hikstObject.setID(set.getInt(1));
				hikstObject.name = set.getString(2);
				hikstObject.effect = set.getDouble(3);
				hikstObject.voltage = set.getDouble(4);
				hikstObject.current = set.getDouble(5);
				hikstObject.usage_pattern_ID = set.getInt(6);
				hikstObject.latitude = set.getDouble(7);
				hikstObject.longitude = set.getDouble(8);
				hikstObject.self_temperature = set.getDouble(9);
				hikstObject.target_temperature = set.getDouble(10);
				hikstObject.base_area = set.getDouble(11);
				hikstObject.base_height = set.getDouble(12);
				hikstObject.heat_loss_rate = set.getDouble(13);
				hikstObject.sons = getHikstObjectChildren(hikstObject.getID());
				hikstObject.impactDegrees = getImpactDegrees(hikstObject
						.getID());
				hikstObjects.add(hikstObject);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return hikstObjects;
	}

	private ArrayList<ImpactDegree> getImpactDegrees(int ObjectID)
			throws SQLException {
		ArrayList<ImpactDegree> impactDegrees = new ArrayList<ImpactDegree>();

		Connection connection = Settings.getDBC();

		String query = "SELECT * FROM IMPACT_DEGREES WHERE Object_ID=?";

		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setInt(1, ObjectID);
		ResultSet set = preparedStatement.executeQuery();

		while (set.next()) {
			ImpactDegree impactDegree = new ImpactDegree(
					set.getInt("object_id"));
			impactDegree.percent = set.getDouble("percent");
			impactDegree.type_id = set.getInt("type_id");
			impactDegrees.add(impactDegree);
		}

		return impactDegrees;
	}

	private ArrayList<Integer> getHikstObjectChildren(int ObjectID)
			throws SQLException {
		ArrayList<Integer> children = new ArrayList<Integer>();

		Connection connection = Settings.getDBC();

		String query = "select son_id from part_objects where father_id=?";

		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setInt(1, ObjectID);
		ResultSet set = preparedStatement.executeQuery();

		while (set.next()) {
			children.add(set.getInt(1));
		}

		return children;
	}

	@Override
	public int saveObject(HikstObject simObject) {

		try {
			String query;
			if (simObject.getID() != null) {
				query = "UPDATE Objects set Name=?, Effect=?, Voltage=?, Current=?,"
						+ " Usage_Pattern_ID=?, Latitude=?, Longitude=?, Self_Temperature=?"
						+ ", Target_Temperature=?, Base_Area=?, Base_Height=?,Heat_Loss_Rate=? where ID=?";
			} else {
				query = "INSERT INTO Objects (Name,Effect,Voltage,Current,Usage_Pattern_ID,Latitude,Longitude"
						+ ",Self_Temperature,Target_Temperature,Base_Area,Base_Height,Heat_Loss_Rate) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) Returning *";
			}
			PreparedStatement preparedStatement = Settings.getDBC()
					.prepareStatement(query);

			preparedStatement.setString(1, simObject.name);

			if (simObject.effect != null) {
				preparedStatement.setDouble(2, simObject.effect);
			} else {
				preparedStatement.setNull(2, java.sql.Types.DOUBLE);
			}

			if (simObject.voltage != null) {
				preparedStatement.setDouble(3, simObject.voltage);
			} else {
				preparedStatement.setNull(3, java.sql.Types.DOUBLE);
			}

			if (simObject.current != null) {
				preparedStatement.setDouble(4, simObject.current);
			} else {
				preparedStatement.setNull(4, java.sql.Types.DOUBLE);
			}

			if (simObject.usage_pattern_ID != null) {
				preparedStatement.setDouble(5, simObject.usage_pattern_ID);
			} else {
				preparedStatement.setNull(5, java.sql.Types.INTEGER);
			}

			if (simObject.latitude != null) {
				preparedStatement.setDouble(6, simObject.latitude);
			} else {
				preparedStatement.setNull(6, java.sql.Types.DOUBLE);
			}

			if (simObject.longitude != null) {
				preparedStatement.setDouble(7, simObject.longitude);
			} else {
				preparedStatement.setNull(7, java.sql.Types.DOUBLE);
			}

			if (simObject.self_temperature != null) {
				preparedStatement.setDouble(8, simObject.self_temperature);
			} else {
				preparedStatement.setNull(8, java.sql.Types.DOUBLE);
			}

			if (simObject.target_temperature != null) {
				preparedStatement.setDouble(9, simObject.target_temperature);
			} else {
				preparedStatement.setNull(9, java.sql.Types.DOUBLE);
			}

			if (simObject.base_area != null) {
				preparedStatement.setDouble(10, simObject.base_area);
			} else {
				preparedStatement.setNull(10, java.sql.Types.DOUBLE);
			}

			if (simObject.base_height != null) {
				preparedStatement.setDouble(11, simObject.base_height);
			} else {
				preparedStatement.setNull(11, java.sql.Types.DOUBLE);
			}

			if (simObject.heat_loss_rate != null) {
				preparedStatement.setDouble(12, simObject.heat_loss_rate);
			} else {
				preparedStatement.setNull(12, java.sql.Types.DOUBLE);
			}

			if (simObject.getID() != null) {
				preparedStatement.setInt(13, simObject.getID());
				preparedStatement.executeUpdate();
			} else {
				ResultSet set = preparedStatement.executeQuery();

				if (set.next()) {
					// returning the new id of the object
					simObject.setID(set.getInt(1));
				}
			}

			// Store all the Child objects
			for (int childIndex = 0; childIndex < simObject.sons.size(); childIndex++) {
				saveChild(simObject.getID(), simObject.sons.get(childIndex));
			}

			// Store all the Impact degrees
			for (ImpactDegree id : simObject.impactDegrees) {
				addImpactDegree(id, simObject.getID());
			}

			// returning the existing object-id
			return simObject.getID();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		// returning -1 to indicate that the server couldnt save the object,
		// something must then be wrong with the code
		return -1;
	}

	public void saveChild(int fatherID, int childID) {
		try {
			String query = "INSERT INTO Part_Objects(father_id,son_id) VALUES(?,?);";
			Connection connection = Settings.getDBC();
			PreparedStatement preparedStatement = connection
					.prepareStatement(query);
			preparedStatement.setInt(1, fatherID);
			preparedStatement.setInt(2, childID);
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {

		}
	}

	private void addImpactDegree(ImpactDegree impactDegree, int object_id) {

		try {
			String query = "INSERT INTO IMPACT_DEGREES(Type_ID, Percent,Object_ID) VALUES(?,?,?);";
			Connection connection = Settings.getDBC();
			PreparedStatement preparedStatement;
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, impactDegree.type_id);
			preparedStatement.setDouble(2, impactDegree.percent);
			preparedStatement.setInt(3, object_id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void saveUsagePattern(UsagePattern usagePattern) {

		try {
			if (usagePattern.getID() == -1) {
				String query = "INSERT INTO Usage_Pattern(name,actual,c00,c01,c02,c03,c04,c05,c06"
						+ ",c07,c08,c09,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21,c22,c23)"
						+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				PreparedStatement statement = Settings.getDBC()
						.prepareStatement(query);
				System.out.println(usagePattern.name);
				statement.setString(1, usagePattern.name);
				statement.setBoolean(2, usagePattern.actual);
				for (int i = 0; i < usagePattern.pattern.length; i++) {
					statement.setInt(3 + i, usagePattern.pattern[i]);
				}
				statement.executeUpdate();
			} else {
				String query = "UPDATE Usage_Pattern SET name=?, actual=?,"
						+ " c00=?, c01=?, c02=?, c03=?, c04=?, c05=?, c06=?, c07=?, "
						+ "c08=?, c09=?,  c10=?, c11=?, c12=?, c13=?, c14=?, c15=?, c16=?, "
						+ "c17=?, c18=?,  c19=?, c20=?, c21=?, c22=?, c23=? WHERE id=?";
				PreparedStatement statement = Settings.getDBC()
						.prepareStatement(query);
				int index = 1;
				statement.setString(index++, usagePattern.name);
				statement.setBoolean(index++, usagePattern.actual);

				for (int i = 0; i < usagePattern.pattern.length; i++) {
					statement.setInt(index++, usagePattern.pattern[i]);
				}

				statement.setInt(index++, usagePattern.getID());
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayList<UsagePattern> getUsagePatterns() {

		ArrayList<UsagePattern> usagePatterns = new ArrayList<UsagePattern>();

		try {
			String query = "SELECT * FROM Usage_Pattern";
			PreparedStatement statement = Settings.getDBC().prepareStatement(
					query);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				UsagePattern usagePattern = new UsagePattern(set.getInt("id"));
				usagePattern.name = set.getString("name");
				usagePattern.actual = set.getBoolean("actual");
				usagePattern.pattern = new int[24];

				for (int index = 0; index < usagePattern.pattern.length; index++) {
					usagePattern.pattern[index] = set.getInt(index + 3);

				}
				usagePatterns.add(usagePattern);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return usagePatterns;

	}
}