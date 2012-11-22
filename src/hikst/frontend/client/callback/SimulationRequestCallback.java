package hikst.frontend.client.callback;

import hikst.frontend.client.pages.SimulatonResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;


public class SimulationRequestCallback implements AsyncCallback<Integer> {
	
	public SimulationRequestCallback()
	{
		
	}
	
	@Override
	public void onFailure(Throwable caught) {
		Window.alert(caught.getMessage());
	}

	@Override
	public void onSuccess(Integer Simulation_ID) {
		Window.alert("lagret!" + Simulation_ID );
		RootLayoutPanel.get().add(new SimulatonResult(Simulation_ID));
	}
}
