package hikst.frontend.client.callback;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;

import hikst.frontend.client.pages.NewObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class ObjectImpactCallback implements
		AsyncCallback<HashMap<String, HashMap<Integer, String>>> {

	private NewObject newobject;

	public ObjectImpactCallback(NewObject newOb) {
		this.newobject = newOb;
	}

	@Override
	public void onFailure(Throwable caught) {
		Window.alert(caught.getMessage());

	}

	@Override
	public void onSuccess(HashMap<String, HashMap<Integer, String>> result) {
		//childObjList
		HashMap<Integer, String> submap = result.get("childObjList");

		Iterator<Entry<Integer, String>> submapIterator = submap.entrySet()
				.iterator();

		while (submapIterator.hasNext()) {
			newobject.childObjList.add(new Label(submapIterator.next()
					.getValue()));
		}

		//impactDegList
		submap = result.get("impactDegList");

		submapIterator = submap.entrySet().iterator();

		while (submapIterator.hasNext()) {
			newobject.impactDegList.add(new Label(submapIterator.next()
					.getValue()));
		}
	}
}
