package fr.livelovecite.setup;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import fr.livelovecite.R;

/**
 * A callback, which has ability to show loading dialog while response is being received.
 * Shows Toast with result's toString() on success.
 * Shows AlertDialog with error message on failure.
 * If overriding handleResponse and/or handleFault, should manually dismiss ProgressDialog with hideLoading() method
 * or calling super.handleResponse() or super.handleFault().
 *
 * @param <T> class to be received from server
 */
public class LoadingCallback<T> implements AsyncCallback<T>
{
    private Context context;
    private ProgressDialog progressDialog;

    /**
     * Create an instance with message "Loading...".
     *
     * @param context context to which ProgressDialog should be attached
     */
    protected LoadingCallback(Context context) {
        this( context, context.getString(R.string.loading) );
    }

    /**
     * Creates an instance with given message.
     *
     * @param context               context to which ProgressDialog should be attached
     * @param progressDialogMessage message to be shown on ProgressDialog
     */
    protected LoadingCallback( Context context, String progressDialogMessage )
    {
        this.context = context;
        progressDialog = new ProgressDialog( context );
        progressDialog.setMessage( progressDialogMessage );
    }

    /**
     * Creates an instance and can immediately show ProgressDialog
     *
     * @param context            context to which ProgressDialog should be attached
     * @param showProgressDialog set to true if want to immediately show ProgressDialog
     */
    public LoadingCallback( Context context, boolean showProgressDialog ) {
        this( context );
        if (showProgressDialog) showLoading();
        else hideLoading();
    }

    /**
     * Creates an instance and can immediately show ProgressDialog with given message
     *
     * @param context               context to which ProgressDialog should be attached
     * @param progressDialogMessage message to be shown on ProgressDialog
     * @param showProgressDialog    set to true if want to immediately show ProgressDialog
     */
    protected LoadingCallback( Context context, String progressDialogMessage, boolean showProgressDialog )
    {
        this( context, progressDialogMessage );
        progressDialog.show();
    }

    @Override
    public void handleResponse( T response )
    {
        progressDialog.dismiss();
    }

    @Override
    public void handleFault( BackendlessFault fault ) {
        progressDialog.dismiss();
        Log.e("Loading Callback", fault.getMessage());
    }

    /**
     * Shows ProgressDialog.
     */
    public void showLoading()
    {
        progressDialog.show();
    }

    /**
     * Hides ProgressDialog.
     */
    protected void hideLoading()
    {
        progressDialog.dismiss();
    }
}
