package hikst.frontend.client.pages;


import java.util.Date;

import hikst.frontend.client.DatabaseService;
import hikst.frontend.client.DatabaseServiceAsync;
import hikst.frontend.client.Graph;
import hikst.frontend.client.Simulation;
import hikst.frontend.client.SplineGraf;
import hikst.frontend.client.callback.SimulationResultCallback;
import hikst.frontend.shared.Description;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SimulatonResult extends Composite {
	MainPage panelBack;
	public SplineGraf graf = new SplineGraf();
	@UiField
	FlowPanel centerPanel;
	private DatabaseServiceAsync databaseService = GWT
	.create(DatabaseService.class);
	private SimulationResultCallback callback1;
	private final int id;
	private static SimulatonResultUiBinder uiBinder = GWT.create(SimulatonResultUiBinder.class);


	interface SimulatonResultUiBinder extends UiBinder<Widget, SimulatonResult> {
	}

	public SimulatonResult(int id) {
		initWidget(uiBinder.createAndBindUi(this));
		this.id = id;
		centerPanel.add(graf.createChart());
		callback1 = new SimulationResultCallback(this);
		this.start();
	}
	
	public void start()
	{
		Timer timer = new Timer(){
			@Override
			public void run() {
				databaseService.getSimulation(id, callback1);
			}			
		};
		timer.scheduleRepeating(1000);
	}

	@UiField
	Button backButton;
	private AsyncCallback<Integer> callback ;
	
	
	@UiHandler("backButton")
	void onBackClick(ClickEvent event) {
		panelBack = new MainPage();
		RootLayoutPanel.get().add(panelBack);
	}

}

