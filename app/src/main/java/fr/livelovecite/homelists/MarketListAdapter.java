package fr.livelovecite.homelists;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;
import fr.livelovecite.report.reportMarket;
import fr.livelovecite.report.userReport;
import fr.livelovecite.setup.LoaderHelper;
import fr.livelovecite.uplaods.Market;


public class MarketListAdapter
        extends ArrayAdapter<Market> implements Filterable{
    private List<Market> items;
    private int layoutResourceId;
    private Context context;
    private ItemsFilter itemsFilter;

    public MarketListAdapter(Context context,int layoutResourceId, List<Market> items) {
        super(context,layoutResourceId,items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items=items;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        MarketHolder holder;
        if (convertView==null) {
            LayoutInflater inflater = ((Activity ) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new MarketHolder();

            holder.ownerName = convertView.findViewById(R.id.ownerName3);
            holder.ownerImage = convertView.findViewById(R.id.ownerImage3);

            holder.itemTitle = convertView.findViewById(R.id.itemTitle);
            holder.itemLocation = convertView.findViewById(R.id.itemLocation);
            holder.itemDescription = convertView.findViewById(R.id.ItemDescription);
            holder.itemPublicationDate = convertView.findViewById(R.id.itemPublicationDate);

            holder.itemPrice = convertView.findViewById(R.id.itemPrice);
            holder.itemImage = convertView.findViewById(R.id.ItemImage);

            holder.contactBTN = convertView.findViewById(R.id.contactMarketButton);
            holder.reportBTN = convertView.findViewById(R.id.reportBTN);

            convertView.setTag(holder);
        }
        else {
            holder =(MarketHolder ) convertView.getTag();}

        final Market marketIteraction;
        marketIteraction = items.get(position);

        holder.itemImage.setTag(marketIteraction);
        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Market marketIteraction = ( Market ) v.getTag();

                if(marketIteraction.getPicture()==null) return;

                Intent fullScreenIntent = new Intent(getContext(), FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", marketIteraction.getPicture());
                getContext().startActivity(fullScreenIntent);

            }
        });

        holder.itemDescription.setTag(marketIteraction);
        holder.itemDescription.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Market marketIteraction = ( Market ) v.getTag();
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(marketIteraction.getTitle());
                alertDialog.setMessage(marketIteraction.getDescription());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        holder.contactBTN.setTag(marketIteraction);
        holder.contactBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Backendless.Data.of(BackendlessUser.class).findById(marketIteraction.getOwnerId(), new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(final BackendlessUser response) {
                        Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.dialog_contact);
                        dialog.setCancelable(true);
                        try {
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        }catch (NullPointerException e){e.printStackTrace();}

                        //Call user
                        TextView callUserBTN = dialog.findViewById(R.id.contactCall);
                        TextView messageUserBTN = dialog.findViewById(R.id.contactMessage);
                        if(response.getProperty("mobile")!=null && !response.getProperty("mobile").toString().isEmpty()){
                            callUserBTN.setText(response.getProperty("mobile").toString());
                            messageUserBTN.setText(response.getProperty("mobile").toString());
                        }
                        callUserBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String uri = "tel:" + response.getProperty("mobile").toString().trim() ;
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse(uri));
                                getContext().startActivity(intent);
                            }
                        });
                        if(response.getProperty("mobile")==null || response.getProperty("mobile").toString().equals("")){
                            dialog.findViewById(R.id.messageUserBTN).setVisibility(View.GONE);
                            dialog.findViewById(R.id.callUserBTN).setVisibility(View.GONE);
                            callUserBTN.setVisibility(View.GONE);
                            messageUserBTN.setVisibility(View.GONE);

                        }

                        //Message user
                        messageUserBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse("smsto:"+response.getProperty("mobile"));
                                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                                it.putExtra("sms_body", "Intrested in buying "+marketIteraction.getTitle());
                                getContext().startActivity(it);

                            }
                        });
                        //Email user
                        final TextView emailUserBTN = dialog.findViewById(R.id.contactEmail);
                        if(response.getEmail()==null || response.getEmail().equals("")){
                            dialog.findViewById(R.id.emailUserBTN).setVisibility(View.GONE);
                            emailUserBTN.setVisibility(View.GONE);
                        }
                        String email="";
                        if(response.getEmail()!=null
                                && Patterns.EMAIL_ADDRESS.matcher( response.getEmail() ).matches())
                            email=response.getEmail();
                        else if(response.getProperty("email_bis")!=null)
                            email=response.getProperty("email_bis").toString();

                        emailUserBTN.setText(email);
                        emailUserBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/html");
                                i.putExtra(android.content.Intent.EXTRA_EMAIL  ,  new String[]{emailUserBTN.getText().toString()});
                                i.putExtra(Intent.EXTRA_CC, new String[]{"livelovecite@gmail.com"});
                                i.putExtra(Intent.EXTRA_SUBJECT, "Intrested in buying "+marketIteraction.getTitle());
                                i.putExtra(Intent.EXTRA_TEXT   , "Hello "+marketIteraction.getOwnerName()
                                        +", I saw your product: "+marketIteraction.getTitle()
                                        + " on Live Love Cité");
                                try {
                                    getContext().startActivity(Intent.createChooser(i, context.getString(R.string.send_email)+"..."));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        dialog.show();
                    }
                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {Toast.makeText( context, R.string.no_connection , Toast.LENGTH_SHORT ).show();}
                });
            }
        });


        holder.itemLocation.setTag(marketIteraction);
        holder.itemLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Market marketIteraction = ( Market ) v.getTag();
                try{

                    String daddr="" , myMaison=marketIteraction.getOwnerMaison();
                    String loadedJSONString = LoaderHelper.parseFileToString(context, "maisonGeo");
                    JSONObject obj = new JSONObject(loadedJSONString);
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

        holder.reportBTN.setTag(marketIteraction);
        holder.reportBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Market marketIteraction = ( Market ) v.getTag();

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.report_popup);
                dialog.setCancelable(true);
                dialog.show();
                final RadioGroup radioGroup = dialog.findViewById(R.id.radios);
                radioGroup.check(R.id.rd1);
                final EditText textReport = dialog.findViewById(R.id.whyReport);
                final Button submitReport = dialog.findViewById(R.id.okReport);
                ImageView imageReport = dialog.findViewById(R.id.imageReport);
                Picasso.with(getContext())
                        .load(marketIteraction.getPicture())
                        .into(imageReport);
                ImageView userImage = dialog.findViewById(R.id.userReport);
                if (marketIteraction.getOwnerImage() == null  || TextUtils.equals(marketIteraction.getOwnerImage(),"pas_de_token"))
                    userImage.setImageResource(R.drawable.default_user_icon);
                else {
                    String pp = "https://graph.facebook.com/" + marketIteraction.getOwnerImage() + "/picture?type=large";
                    Picasso.with(getContext()).load(pp).placeholder(R.drawable.default_user_icon).into(userImage);
                }

                submitReport.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Market marketIteraction = ( Market ) v.getTag();
                        if(textReport.getText()==null || textReport.getText().length()==0)
                            return;
                        else {
                            final ProgressDialog dialogRefresh = new ProgressDialog(getContext());
                            try {
                                dialogRefresh.show();
                            } catch (WindowManager.BadTokenException e) {e.printStackTrace();}
                            dialogRefresh.setCancelable(false);
                            dialogRefresh.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                            if(radioGroup.getCheckedRadioButtonId()==R.id.rd1){
                                reportMarket reportMarket= new reportMarket();
                                reportMarket.setNameReported(marketIteraction.getOwnerName());
                                reportMarket.setProductReported(marketIteraction.getObjectId());
                                reportMarket.setReason(textReport.getText().toString());
                                // Report item
                                Backendless.Persistence.save(reportMarket, new AsyncCallback<fr.livelovecite.report.reportMarket>() {
                                    @Override
                                    public void handleResponse(fr.livelovecite.report.reportMarket response) {
                                        dialogRefresh.dismiss();
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), R.string.report_sent, Toast.LENGTH_SHORT).show();}

                                    @Override
                                    public void handleFault(BackendlessFault fault) {dialogRefresh.dismiss();
                                        Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_LONG).show();}
                                });
                            }else {
                                // Report user
                                userReport userReport = new userReport();
                                userReport.setUserReported(marketIteraction.getOwnerId());
                                userReport.setReason(textReport.getText().toString());
                                Backendless.Persistence.save(userReport, new AsyncCallback<fr.livelovecite.report.userReport>() {
                                    @Override
                                    public void handleResponse(fr.livelovecite.report.userReport response) {
                                        dialogRefresh.dismiss();
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), R.string.report_sent, Toast.LENGTH_SHORT).show();}

                                    @Override
                                    public void handleFault(BackendlessFault fault) {dialogRefresh.dismiss();
                                        Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_LONG).show();}
                                });
                            }
                        }
                    }
                });
            }
        });
        setupItem(holder,marketIteraction);

        return convertView;
    }

    private void setupItem(final MarketHolder holder, final Market marketIteraction) {

        holder.itemTitle.setText(marketIteraction.getTitle());
        holder.itemPrice.setText(marketIteraction.getPrice());
        holder.itemDescription.setText(marketIteraction.getDescription());
        holder.itemLocation.setText(marketIteraction.getOwnerMaison());
        holder.itemPublicationDate.setText(marketIteraction.getCreatedFormatted());

        Picasso.with(getContext()).load(marketIteraction.getPicture()).into(holder.itemImage);

        holder.ownerName.setText(marketIteraction.getOwnerName());

        if (marketIteraction.getOwnerImage() == null
                || TextUtils.equals(marketIteraction.getOwnerImage(),"pas_de_token"))
            holder.ownerImage.setImageResource(R.drawable.default_user_icon);
        else {
            String pp = "https://graph.facebook.com/" + marketIteraction.getOwnerImage() + "/picture?type=large";
            Picasso.with(getContext()).load(pp).placeholder(R.drawable.default_user_icon).into(holder.ownerImage);
        }
    }
    private static class MarketHolder {
        TextView ownerName, itemTitle, itemDescription, itemPublicationDate, itemPrice, itemLocation;
        ImageView ownerImage, itemImage;
        ImageButton reportBTN;
        Button contactBTN;

    }
    @NonNull
    @Override
    public Filter getFilter() {
        if (itemsFilter == null)
            itemsFilter = new ItemsFilter();
        return itemsFilter;
    }
    private class ItemsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
// We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
// No filter implemented we return all the list
                results.values = items;
                results.count = items.size();
            }
            else {
// We perform filtering operation
                List<Market> marketIteractions = new ArrayList<>();

                for (Market p : items) {
                    if (p.getTitle().contains(constraint) || p.getDescription().contains(constraint) || p.gettags().contains(constraint))
                        marketIteractions.add(p);
                }

                results.values = marketIteractions;
                results.count = marketIteractions.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

// Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                items = (List<Market>) results.values;
                notifyDataSetChanged();
            }

        }

    }

}
