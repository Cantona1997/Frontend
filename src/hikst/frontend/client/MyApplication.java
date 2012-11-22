package hikst.frontend.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
* Entry point classes define onModuleLoad().
*/
public class MyApplication implements EntryPoint
{

    public void onModuleLoad ()
    {
        // define the service you want to call
        MyServiceAsync svc = (MyServiceAsync) GWT.create(MyService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svc;
        endpoint.setServiceEntryPoint("/frontend/myService");
  
        // define a handler for what to do when the
        // service returns a result
        AsyncCallback callback = new AsyncCallback()
        {
            public void onSuccess (Object result)
            {
                RootPanel.get().add(new HTML(result.toString()));
            }

            public void onFailure (Throwable ex)
            {
                RootPanel.get().add(new HTML(ex.toString()));
            }
        };

        // execute the service
        //svc.myMethod("Do Stuff", callback);
    }
}
