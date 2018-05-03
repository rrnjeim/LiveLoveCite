package fr.livelovecite.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fr.livelovecite.R;


class ActivityAdapter extends ArrayAdapter<Activity> {
    private List<Activity> activities;
    private int layoutResourceId;
    private Context context;

    ActivityAdapter(Context context,int layoutResourceId ,List<Activity> activities) {
        super(context,layoutResourceId,activities);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.activities = activities;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ActivityHolder holder;
        if (convertView==null) {
            LayoutInflater inflater = (( android.app.Activity ) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new ActivityHolder();

            holder.activityTitle = convertView.findViewById(R.id.ActivityTitle);
            holder.activityLocation = convertView.findViewById(R.id.ActivityLocation);
            holder.activityOccurence = convertView.findViewById(R.id.ActivityOccurence);
            holder.activityImage = convertView.findViewById(R.id.ActivityImage);

            convertView.setTag(holder);
        }
        else {
            holder =(ActivityHolder) convertView.getTag();}
        Activity activitytmp;
        activitytmp = activities.get(position);

        setupItem(holder, activitytmp);

        return convertView;
    }
        private void setupItem(final ActivityHolder holder, final Activity activity) {
            holder.activityTitle.setText(activity.getTitle());
            holder.activityLocation.setText(activity.getLocation());
            holder.activityOccurence.setText(activity.getOccurrence());
            Picasso.with(getContext()).load(activity.getImage()).into(holder.activityImage);

            holder.activityLocation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try{
                        String daddr="" , myMaison=holder.activityLocation.getText().toString();
                        JSONObject obj = new JSONObject(loadJSONFromAsset());
                        JSONArray m_jArry = obj.getJSONArray("Maisons");
                        for(int i=0;i<m_jArry.length();i++) {
                            if(myMaison.equals(m_jArry.getJSONObject(i).get("name").toString())){
                                daddr=m_jArry.getJSONObject(i).get("geo").toString();
                            }
                        }
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+daddr+"&mode=w");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        getContext().startActivity(mapIntent);
                    }
                    catch (JSONException e) {e.printStackTrace();}
                }
            });

            holder.activityImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent activityDetailIntent = new Intent(getContext(), ActivityDetailsActivity.class);

                    activityDetailIntent.putExtra("Image", activity.getImage());
                    activityDetailIntent.putExtra("Title", activity.getTitle());
                    activityDetailIntent.putExtra("Location", activity.getLocation());
                    activityDetailIntent.putExtra("Occurrence", activity.getOccurrence());
                    activityDetailIntent.putExtra("Description", activity.getDescription());
                    activityDetailIntent.putExtra("fbURL",activity.getFbLink());

                    getContext().startActivity(activityDetailIntent);
                    (( android.app.Activity ) context).overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);

                }
            });
    }
        private static class ActivityHolder {
        TextView activityTitle, activityLocation, activityOccurence;
        ImageView activityImage;
    }
    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getContext().getAssets().open("maisonGeo");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
