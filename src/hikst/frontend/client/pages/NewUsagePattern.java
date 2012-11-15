package hikst.frontend.client.pages;

import hikst.frontend.client.DatabaseService;
import hikst.frontend.client.DatabaseServiceAsync;
import hikst.frontend.client.callback.SaveObjectCallback;
import hikst.frontend.client.callback.SaveUsagePatternCallback;
import hikst.frontend.shared.UsagePattern;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;


	
	

public class NewUsagePattern extends HikstComposite {


	private UsagePattern u = new UsagePattern();

	interface NewUsagePatternUiBinder extends UiBinder<Widget, NewUsagePattern> {
	}

	private static NewUsagePatternUiBinder uiBinder = GWT
			.create(NewUsagePatternUiBinder.class);

	@UiField
	TextBox name;
	@UiField
	RadioButton actual;
	@UiField
	RadioButton probability;
	@UiField
	FlexTable flexyTable;
	@UiField
	Button back;
	@UiField
	Button saveButton;
	@UiField
	FlowPanel eastPanel;
	@UiField
	Label nameLabel;
	@UiField
	FlowPanel centerPanel;

	TextBox[] range = new TextBox[24];
	TextBox[] input = new TextBox[24];

	DatabaseServiceAsync databaseService = GWT.create(DatabaseService.class);
	
	/**
	 * Main constructor
	 */
	public NewUsagePattern(HikstComposite hikstCompositeParent) {
		this.hikstCompositeParent = hikstCompositeParent;
		u = new UsagePattern();
		
		initWidget(uiBinder.createAndBindUi(this));
		generateFlexyTable();
		
		setValues();
	}

	/**
	 * Constructor used when editing a Usage Pattern
	 * 
	 * @param parent
	 * @param childObject
	 */
	public NewUsagePattern(HikstComposite hikstCompositeParent, UsagePattern u) {
		this(hikstCompositeParent);
		this.u = u;
		setValues();
	}

	/**
	 * Sets all clock inputs to ranges
	 */
	private void generateFlexyTable() {
		centerPanel.remove(flexyTable);
		flexyTable.clear();

		for (int i = 0; i <= 23; i++) {
			flexyTable.setWidget(0, i, new Label(i + ":00"));
		}

		for (int i = 0; i <= 23; i++) {
			range[i] = new TextBox();
			range[i].getElement().setAttribute("type", "range");
			range[i].getElement().setAttribute("min", "0");
			range[i].getElement().setAttribute("max", "100");
			range[i].getElement().setAttribute("step", "1");
			range[i].getElement()
					.setAttribute("style",
							"width: 20px;height: 300px; -webkit-appearance: slider-vertical;");
			range[i].addChangeHandler(rangeChanged(i));
			flexyTable.setWidget(1, i, range[i]);
		}

		for (int i = 0; i <= 23; i++) {
			input[i] = new TextBox();
			input[i].getElement().setAttribute("type", "number");
			input[i].getElement().setAttribute("min", "0");
			input[i].getElement().setAttribute("max", "100");
			input[i].getElement().setAttribute("step", "1");
			input[i].getElement().setAttribute("style", "width: 36px;");
			input[i].addChangeHandler(inputChanged(i));
			flexyTable.setWidget(2, i, input[i]);
		}

		centerPanel.add(flexyTable);

	}

	private ChangeHandler rangeChanged(final int i) {
		return new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				u.pattern[i] = Integer.parseInt(range[i].getValue());
				input[i].setText(Integer.toString(u.pattern[i]));
			}
		};
	}

	private ChangeHandler inputChanged(final int i) {
		return new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				u.pattern[i] = Integer.parseInt(input[i].getValue());
				range[i].setText(Integer.toString(u.pattern[i]));
			}
		};
	}

	public UsagePattern getUsagePattern() {
		u.name = name.getValue();
		u.actual = actual.getValue();
		return u;
	}

	private void setValues() {
		name.setValue(u.name);
		for (int i = 0; i <= 23; i++) {
			String val = Integer.toString(u.pattern[i]);
			range[i].setValue(val);
			input[i].setValue(val);
		}
		actual.setValue(u.actual);
		probability.setValue(!u.actual);
	}

	@UiHandler("back")
	void onBackClick(ClickEvent event) {
		RootLayoutPanel.get().add(
				new ViewUsagePatterns((NewObject) hikstCompositeParent
						.getHikstCompositeParent()));
	}

	@UiHandler("saveButton")
	void onSaveObject(ClickEvent event) {
		if (name.getValue().equals("")) {
			Window.alert("Change Name!");
		} else {
			
 			databaseService.saveUsagePattern(getUsagePattern(), new SaveUsagePatternCallback(hikstCompositeParent));
		}
	}

	@UiHandler("emailAdmin")
	void onEmailAdminClick(ClickEvent event) {
		RootLayoutPanel.get().add(new EmailAdmin());
	}
	@UiHandler("buttonLogout")
	void onButtonLogoutClick(ClickEvent event) {
		RootLayoutPanel.get().add(new Login());
	}

}
