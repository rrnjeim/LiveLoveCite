package fr.livelovecite.setup;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class LoaderHelper {
    public static String getJson(Context context, String json){
        return parseFileToString(context, json);
    }
    public static String parseFileToString( Context context, String filename )
    {
        try
        {
            InputStream stream = context.getAssets().open( filename );
            int size = stream.available();

            byte[] bytes = new byte[size];
            stream.read(bytes);
            stream.close();

            return new String( bytes );

        } catch ( IOException e ) {
            Log.i("GuiFormData", "IOException: " + e.getMessage() );
        }
        return null;
    }
}