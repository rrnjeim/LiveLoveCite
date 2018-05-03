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
import android.support.v4.content.ContextCompat;
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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;
import fr.livelovecite.report.reportJob;
import fr.livelovecite.report.userReport;
import fr.livelovecite.uplaods.Job;


public class JobListAdapter extends ArrayAdapter<Job>
        implements Filterable {
    private List<Job> jobs;
    private int layoutResourceId;
    private Context context;
    private JobsFilter jobsFilter;


    public JobListAdapter(Context context, int layoutResourceId, List<Job> jobs) {
        super(context,layoutResourceId,jobs);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.jobs=jobs;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        JobHolder holder;
        if (convertView==null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new JobHolder();

            holder.ownerName = convertView.findViewById(R.id.ownerName2);
            holder.ownerImage = convertView.findViewById(R.id.ownerImage2);

            holder.jobTitle = convertView.findViewById(R.id.tvjobTitle);
            holder.jobLocation = convertView.findViewById(R.id.jobLocation);
            holder.jobDescription = convertView.findViewById(R.id.jobDescription);
            holder.jobPublicationDate = convertView.findViewById(R.id.jobPublicationDate);
            holder.jobWadge = convertView.findViewById(R.id.jobWadge);
            holder.jobImage = convertView.findViewById(R.id.jobImage);


            holder.contactBTN = convertView.findViewById(R.id.contactJobButton);
            holder.reportBTN = convertView.findViewById(R.id.reportBTN);


            convertView.setTag(holder);
        }
        else {holder =(JobHolder ) convertView.getTag();}

        final Job jobInteraction;
        jobInteraction = jobs.get(position);

        holder.jobImage.setTag(jobInteraction);
        holder.jobImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Job jobInteraction = ( Job ) v.getTag();
                if(jobInteraction.getPicture()==null) return;

                Intent fullScreenIntent = new Intent(getContext(), FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", jobInteraction.getPicture());
                getContext().startActivity(fullScreenIntent);
            }
        });

        holder.contactBTN.setTag(jobInteraction);
        holder.contactBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_contact);
                dialog.setCancelable(true);
                try {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }catch (NullPointerException e){e.printStackTrace();}

                //Call user
                TextView callUserBTN = dialog.findViewById(R.id.contactCall);
                TextView messageUserBTN = dialog.findViewById(R.id.contactMessage);
                if(jobInteraction.getNumberContact()!=null && !jobInteraction.getNumberContact().isEmpty()){
                    callUserBTN.setText(jobInteraction.getNumberContact());
                    messageUserBTN.setText(jobInteraction.getNumberContact());
                }
                callUserBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = "tel:" + jobInteraction.getNumberContact().trim() ;
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(uri));
                        getContext().startActivity(intent);
                    }
                });
                if(jobInteraction.getNumberContact()==null || jobInteraction.getNumberContact().trim().equals("")){
                    dialog.findViewById(R.id.messageUserBTN).setVisibility(View.GONE);
                    dialog.findViewById(R.id.callUserBTN).setVisibility(View.GONE);
                    callUserBTN.setVisibility(View.GONE);
                    callUserBTN.setVisibility(View.GONE);

                }

                //Message user
                messageUserBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse("smsto:"+jobInteraction.getNumberContact());
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        it.putExtra("sms_body", "Hello, "+
                                "I saw your job offer :"+jobInteraction.getTitre()+" on Live Love Cité\n\nSent from Live Love Cite Mobile App");
                        getContext().startActivity(it);
                    }
                });

                //Email user
                final TextView emailUserBTN = dialog.findViewById(R.id.contactEmail);
                if(jobInteraction.getEmailContact()==null || jobInteraction.getEmailContact().isEmpty()){
                    dialog.findViewById(R.id.emailUserBTN).setVisibility(View.GONE);
                    emailUserBTN.setVisibility(View.GONE);
                }
                String email="";
                if(jobInteraction.getEmailContact()!=null
                        && Patterns.EMAIL_ADDRESS.matcher( jobInteraction.getEmailContact()).matches())
                    email=jobInteraction.getEmailContact();

                emailUserBTN.setText(email);
                emailUserBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/html");
                        i.putExtra(android.content.Intent.EXTRA_EMAIL  ,  new String[]{jobInteraction.getEmailContact()});
                        i.putExtra(Intent.EXTRA_SUBJECT, ""+jobInteraction.getTitre()+" on Live Love Cité");
                        i.putExtra(Intent.EXTRA_TEXT   , "Hello "+ jobInteraction.getOwnerName()
                                +",\nI saw your job offer: "+jobInteraction.getTitre()
                                +" on Live Love Cité\n\nSent from Live Love Cité Mobile App");
                        try {
                            getContext().startActivity(Intent.createChooser(i, context.getString(R.string.send_email)+"..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                dialog.show();
            }
        });

        holder.reportBTN.setTag(jobInteraction);
        holder.reportBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Job jobInteraction = ( Job ) v.getTag();

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
                        .load(jobInteraction.getPicture())
                        .into(imageReport);
                ImageView userImage = dialog.findViewById(R.id.userReport);
                if (jobInteraction.getOwnerImage() == null  || TextUtils.equals(jobInteraction.getOwnerImage() ,"pas_de_token"))
                    userImage.setImageResource(R.drawable.default_user_icon);
                else {
                    String pp = "https://graph.facebook.com/" + jobInteraction.getOwnerImage()  + "/picture?type=large";
                    Picasso.with(getContext()).load(pp).placeholder(R.drawable.default_user_icon).into(userImage);
                }
                submitReport.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Job jobInteraction = ( Job ) v.getTag();

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
                                reportJob reportJob= new reportJob();
                                reportJob.setNameReported(jobInteraction.getOwnerName() );
                                reportJob.setJobReported(jobInteraction.getObjectId());
                                reportJob.setReason(textReport.getText().toString());
                                // Report item
                                Backendless.Persistence.save(reportJob, new AsyncCallback<reportJob>() {
                                    @Override
                                    public void handleResponse(reportJob response) {
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
                                userReport.setUserReported(jobInteraction.getOwnerId());
                                userReport.setReason(textReport.getText().toString());
                                Backendless.Persistence.save(userReport, new AsyncCallback<userReport>() {
                                    @Override
                                    public void handleResponse(userReport response) {
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

        holder.jobLocation.setTag(jobInteraction);
        holder.jobLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Job jobInteraction = ( Job ) v.getTag();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+jobInteraction.getLocation());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                getContext().startActivity(mapIntent);
            }
        });

        holder.jobDescription.setTag(jobInteraction);
        holder.jobDescription.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Job jobInteraction = ( Job ) v.getTag();
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(jobInteraction.getTitre());
                alertDialog.setMessage(jobInteraction.getDescription());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        setupItem(holder,jobInteraction);

        return convertView;
    }
    private void setupItem(final JobHolder holder, final Job jobInteraction) {

        holder.jobTitle.setText(jobInteraction.getTitre());
        holder.jobLocation.setText(jobInteraction.getLocation());
        holder.jobDescription.setText(jobInteraction.getDescription());
        holder.jobPublicationDate.setText(jobInteraction.getCreatedFormatted());
        holder.jobWadge.setText(jobInteraction.getRemuneration());

        if(jobInteraction.getPicture()==null)
            holder.jobImage.setBackgroundResource(R.drawable.ic_image);

        else
        Picasso.with(getContext()).load(jobInteraction.getPicture()).into(holder.jobImage);

        holder.ownerName.setText(jobInteraction.getOwnerName() );
        if (jobInteraction.getOwnerImage()  == null
                || TextUtils.equals(jobInteraction.getOwnerImage() ,"pas_de_token"))
            holder.ownerImage.setImageResource(R.drawable.default_user_icon);
        else {
            String pp = "https://graph.facebook.com/" +jobInteraction.getOwnerImage()  + "/picture?type=large";
            Picasso.with(getContext())
                    .load(pp)
                    .placeholder(R.drawable.default_user_icon)
                    .into(holder.ownerImage);
        }

    }
        private static class JobHolder {
            TextView ownerName, jobTitle, jobLocation, jobDescription , jobPublicationDate, jobWadge;
            ImageView ownerImage, jobImage;
            ImageButton reportBTN;
            Button contactBTN;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (jobsFilter == null)
            jobsFilter = new JobsFilter();
        return jobsFilter;
    }
    private class JobsFilter extends Filter {
        private List<Job> jobInteractionList = new ArrayList<>();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = jobInteractionList;
                results.count = jobInteractionList.size();
            }
            else {
                // We perform filtering operation
                List<Job> jobInteractions = new ArrayList<>();

                for (Job p : jobInteractions) {
                    if (p.getTitre().contains(constraint) || p.getDescription().contains(constraint))
                        jobInteractions.add(p);
                }

                results.values = jobInteractions;
                results.count = jobInteractions.size();

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
                jobInteractionList = (List<Job>) results.values;
                notifyDataSetChanged();
            }

        }

    }

}
