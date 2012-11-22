package hikst.frontend.client.pages;


import hikst.frontend.client.MyService;
import hikst.frontend.client.MyServiceAsync;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;



import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;

public class EmailAdmin extends Composite implements HasText {

	private static EmailAdminUiBinder uiBinder = GWT
			.create(EmailAdminUiBinder.class);

	interface EmailAdminUiBinder extends UiBinder<Widget, EmailAdmin> {
	}

	public EmailAdmin() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	MainPage mainPage;
	//private static final Logger log = Logger.getLogger(SendMail.class.getName());

	@UiField TextBox mailFrom;
	@UiField TextBox mailTo;
	@UiField TextBox mailSub;
	@UiField TextBox mailMes;
	@UiField Button mailSend;
	@UiField TextBox mailFromPass;
	@UiField Button backButton;

	public EmailAdmin(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
		
	}
	@UiHandler("mailFrom")
	void onMailFrom(ClickEvent e){
		mailFrom.setText("sysutgruppe4@gmail.com");
		mailFromPass.setText("ostekake1");
		mailTo.setText("eurokro@gmail.com");
		mailSub.setText("subject");
		mailMes.setText("message");
	}
	@UiHandler("mailTo")
	void onMailTo(ClickEvent e){
		
	}
	@UiHandler("mailSub")
	void onMailSub(ClickEvent e){
		
	}
	@UiHandler("mailMes")
	void onMailMes(ClickEvent e){
		
	}
	
	@UiHandler("mailSend")
	void onMailSend(ClickEvent e){
		MyServiceAsync svc = (MyServiceAsync) GWT.create(MyService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svc;
        endpoint.setServiceEntryPoint("/myService");
        svc.sendMail(mailFrom.getText(), mailFromPass.getText() , mailTo.getText(),  mailSub.getText(), mailMes.getText(), callback);
	}
	
	@UiHandler("backButton")
	void onBackButton(ClickEvent e){
		RootLayoutPanel.get().add(new MainPage());
	}
	
	AsyncCallback callback = new AsyncCallback()
	{
	    public void onFailure(Throwable caught)
	    {
	        Window.alert("fail! " + caught.getMessage());
	    }
	
	    public void onSuccess(Object result)
	    {
	        Window.alert("success!");
	        //mainPage = new MainPage();
			RootLayoutPanel.get().add(new MainPage());
	    }
	    
	    
	};

}
