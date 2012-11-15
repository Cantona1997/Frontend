package hikst.frontend.client.callback;

import hikst.frontend.client.pages.HikstComposite;
import hikst.frontend.client.pages.NewObject;
import hikst.frontend.client.pages.ViewUsagePatterns;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class SaveUsagePatternCallback implements AsyncCallback<Void> {
	
	HikstComposite hikstCompositeParent;
	
	public SaveUsagePatternCallback(HikstComposite hikstCompositeParent){
		this.hikstCompositeParent = hikstCompositeParent;
	}

	@Override
	public void onFailure(Throwable caught) {
		Window.alert("Failed to save usage pattern");
		
	}

	@Override
	public void onSuccess(Void result) {
		Window.alert("Amazing! Usage pattern saved!");
		RootLayoutPanel.get().add(
				new ViewUsagePatterns((NewObject) hikstCompositeParent
						.getHikstCompositeParent()));
	}

}
