package fr.livelovecite.myhouselists;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import fr.livelovecite.R;


class ResidentsUserListAdapter extends ArrayAdapter<BackendlessUser>{
    private List<BackendlessUser> users;
    private int layoutResourceId;
    private Context context;

    ResidentsUserListAdapter(Context context, int layoutResourceId, List<BackendlessUser> users) {
        super(context,layoutResourceId,users);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.users = users;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final UserHolder holder;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder=new UserHolder();
        holder.User= users.get(position);

        holder.ownerName = convertView.findViewById(R.id.ownerName);
        holder.ownerImage = convertView.findViewById(R.id.ownerImage);

        convertView.setTag(holder);
        setupItem(holder);
        return convertView;
    }

    private void setupItem(final UserHolder holder) {


        holder.ownerName.setText((String)holder.User.getProperty("name"));
        if(holder.User.getProperty("facebookId") == null  || TextUtils.equals(holder.User.getProperty("facebookId").toString(),"pas_de_token"))
            holder.ownerImage.setImageResource(R.drawable.default_user_icon);
        else {
            final String pp = "https://graph.facebook.com/" +holder.User.getProperty("facebookId") + "/picture?type=large";
            Picasso.with(getContext())
                    .load(pp)
                    .placeholder( R.drawable.default_user_icon)
                    .into(holder.ownerImage);
        }

    }
    private static class UserHolder {
        BackendlessUser User;
        TextView ownerName;
        ImageView ownerImage;
    }
}
