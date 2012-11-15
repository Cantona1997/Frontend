package hikst.frontend.client.callback;

import hikst.frontend.client.DatabaseService;
import hikst.frontend.client.DatabaseServiceAsync;
import hikst.frontend.client.pages.HikstComposite;
import hikst.frontend.client.pages.NewObject;
import hikst.frontend.client.pages.NewUsagePattern;
import hikst.frontend.client.pages.ViewObjects;
import hikst.frontend.client.pages.ViewUsagePatterns;
import hikst.frontend.shared.UsagePattern;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class ViewUsagePatternsCallback implements
		AsyncCallback<ArrayList<UsagePattern>> {

	private FlexTable usagePatternTable;
	private ViewUsagePatterns hikstCompositeParent;
	private DatabaseServiceAsync databaseService = GWT
			.create(DatabaseService.class);

	public ViewUsagePatternsCallback(FlexTable usagePatternTable,
			ViewUsagePatterns hikstCompositeParent) {
		this.usagePatternTable = usagePatternTable;
		this.hikstCompositeParent = hikstCompositeParent;
	}

	@Override
	public void onFailure(Throwable caught) {
		Window.alert("Server failure : " + caught.getMessage());
	}

	@Override
	public void onSuccess(ArrayList<UsagePattern> result) {
		updateTable(result);
	}

	private void updateTable(ArrayList<UsagePattern> usagePatterns) {
		usagePatternTable.clear();

		usagePatternTable.setWidget(0, 1, new Label("Name"));
		usagePatternTable.setWidget(0, 2, new Label("Actual"));

		for (int i = 0; i < usagePatterns.size(); i++) {
			final UsagePattern usagePattern = usagePatterns.get(i);

			int row = i + 1;
			usagePatternTable.setWidget(row, 0, new Button(
					"Choose UsagePattern", new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							NewObject panel = new NewObject(
									(NewObject) hikstCompositeParent.getHikstCompositeParent());
							panel.setUsagePattern(usagePattern.getID());
							RootLayoutPanel.get().add(panel);
						}

					}));
			usagePatternTable.setWidget(row, 1, new Label(usagePattern.name));
			usagePatternTable.setWidget(row, 2,
					new Label(String.valueOf(usagePattern.actual)));

			usagePatternTable.setWidget(row, 4, new Button("Modify",
					new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							RootLayoutPanel.get().add(
									new NewUsagePattern(hikstCompositeParent,
											usagePattern));
						}
					}));
		}
	}
}