package fr.livelovecite.push;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;

class PushMessagesListAdapter extends ArrayAdapter<AdminMessagePush> {
    private List<AdminMessagePush> messages;
    private int layoutResourceId;
    private Context context;

    PushMessagesListAdapter(Context context, int layoutResourceId, List<AdminMessagePush> messages) {
        super(context,layoutResourceId,messages);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        MessageHolder holder;

        if (convertView==null) {
            LayoutInflater inflater = (( Activity ) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new MessageHolder();

            holder.titleMessage = convertView.findViewById(R.id.titleMessage);
            holder.contentMessage = convertView.findViewById(R.id.contentMessage);
            holder.deleteMessage = convertView.findViewById(R.id.deleteMessage);

            convertView.setTag(holder);
        }
        else {holder = (MessageHolder ) convertView.getTag();}

        final AdminMessagePush adminMessagePush;
        adminMessagePush = messages.get(position);

        holder.contentMessage.setTag(adminMessagePush);
        holder.contentMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AdminMessagePush messages = (AdminMessagePush ) v.getTag();
                if(messages.getTag()==null || messages.getTag().equals("llc_admin_message")) {
                    Intent messageDetailsIntent = new Intent(context, PushMessageDetails.class);
                    messageDetailsIntent.putExtra("Message Detail", messages);
                    getContext().startActivity(messageDetailsIntent);
                }
                else if(messages.getTag().equals("llc_admin_image")){
                    Intent fullScreenIntent = new Intent(getContext(), FullscreenActivity.class);
                    fullScreenIntent.putExtra("Image", messages.getContentMessage());
                    getContext().startActivity(fullScreenIntent);
                }
            }
        });


        holder.deleteMessage.setTag(adminMessagePush);
        holder.deleteMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                messages.remove(position);
                                saveMessagesList(messages);
                                notifyDataSetChanged();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Delete ?").setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }
        });

        setupItem(holder, adminMessagePush);
        return convertView;
    }

    private static class MessageHolder {

        AdminMessagePush Message;
        TextView titleMessage;
        TextView contentMessage;
        ImageButton deleteMessage;
    }

    private void setupItem(final MessageHolder holder, final AdminMessagePush adminMessagePush) {

        holder.titleMessage.setText(adminMessagePush.getTitleMessage());

        if(adminMessagePush.getTag()==null || adminMessagePush.getTag().equals("llc_admin_message"))
            holder.contentMessage.setText(adminMessagePush.getContentMessage());
        else if(adminMessagePush.getTag().equals("llc_admin_image"))
            holder.contentMessage.setText("[image]");

    }

    private void saveMessagesList(List<AdminMessagePush> ListToSave){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(ListToSave);
        prefsEditor.putString("MessagesList", json);
        prefsEditor.apply();
    }

}
