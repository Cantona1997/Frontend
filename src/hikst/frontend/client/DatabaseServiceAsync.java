package hikst.frontend.client;

import hikst.frontend.client.callback.ObjectImpactCallback;
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
import hikst.frontend.shared.UsagePattern;
import hikst.frontend.shared.ViewSimulationObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;




/**
 * The async counterpart of <code>DatabaseService</code>.
 */
public interface DatabaseServiceAsync {

	void getData(int sim_description_id, AsyncCallback<List<Plot>> callback)
	throws IllegalArgumentException;
	void getSimulationDescriptionsIDs(AsyncCallback<List<Integer>> callback)
	throws IllegalArgumentException;
	void getSimulation(int sim_description_id, AsyncCallback<Description> callback)
	throws IllegalArgumentException;
	void getSimulations(AsyncCallback<List<Description>> callback)
	throws IllegalArgumentException;
	void authenticate(LoginRequest request, AsyncCallback<Boolean> callback) 
	throws IllegalArgumentException;
	void logOff(AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	void register(RegisterRequest request, AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	void exists(String username, AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	void requestSimulation(SimulationRequest request,
			AsyncCallback<Integer> callback);
	void deleteObject(int object_id, AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	void deleteSimulations(int id, AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	//void updateObject(int id ,SimulatorObject object, AsyncCallback<Boolean> callback)
	//throws IllegalArgumentException;
	void changeAccessLevel(String username, String access_level,
			AsyncCallback<Boolean> callback)
	throws IllegalArgumentException;
	void getSimulationStatus(SimulationTicket ticket,
			AsyncCallback<Integer> callback);
	void setUp(String hostname, String port, String name, String user,
			String password, AsyncCallback<Boolean> callback);
	void settingsLoadable(AsyncCallback<Boolean> callback);
	void loadObject(int id, AsyncCallback<HikstObjectTree> callback);
	void getSimObjects(AsyncCallback<ArrayList<HikstObject>> callback);
	void saveObject(HikstObject simObject, AsyncCallback<Integer> callback);
	void getImpactTypes(AsyncCallback<ArrayList<ImpactType>> callback);
	void getViewSimulationObjects(AsyncCallback<ArrayList<ViewSimulationObject>> callback);
	void saveUsagePattern(UsagePattern usagePattern,
			AsyncCallback<Void> callback);
	void getUsagePatterns(AsyncCallback<ArrayList<UsagePattern>> callback);
	void getObjectImpact(ArrayList<Integer> childObjects,
			ArrayList<ImpactDegree> impactDegrees,
			AsyncCallback<HashMap<String, HashMap<Integer, String>>> objectImpactCallback);
}
