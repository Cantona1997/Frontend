package hikst.frontend.client.pages;

import hikst.frontend.client.DatabaseService;
import hikst.frontend.client.DatabaseServiceAsync;
import hikst.frontend.client.callback.ViewUsagePatternsCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewUsagePatterns extends HikstComposite {
	
	interface ViewUsagePatternsUiBinder extends UiBinder<Widget, ViewUsagePatterns> {
	}

	private static ViewUsagePatternsUiBinder uiBinder = GWT
	.create(ViewUsagePatternsUiBinder.class);
	@UiField ScrollPanel centerPanel;
	@UiField FlexTable flexyTable;
	@UiField Button newUsagePattern;
	@UiField Button backButton;
	
	private DatabaseServiceAsync databaseService = GWT.create(DatabaseService.class);
	
	public ViewUsagePatterns(NewObject hikstCompositeParent) {
		initWidget(uiBinder.createAndBindUi(this));
		this.hikstCompositeParent = hikstCompositeParent;
		initTable();
	}
	
	private void initTable()
	{		
		centerPanel.remove(flexyTable);
		databaseService.getUsagePatterns(new ViewUsagePatternsCallback(flexyTable, this));
		centerPanel.add(flexyTable);
	}
	
	@UiHandler("newUsagePattern")
	void onButtonNewUsagePattern(ClickEvent event){
		RootLayoutPanel.get().add(new NewUsagePattern(this));
	}
	
	@UiHandler("backButton")
	void onBackButtonClick(ClickEvent event) {
		RootLayoutPanel.get().add(new NewObject((ViewObjects) hikstCompositeParent.getHikstCompositeParent()));
	}
}

