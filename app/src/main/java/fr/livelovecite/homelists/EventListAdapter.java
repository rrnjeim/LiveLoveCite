package fr.livelovecite.homelists;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.QueryOptions;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


import fr.livelovecite.R;
import fr.livelovecite.report.reportEvent;
import fr.livelovecite.report.userReport;
import fr.livelovecite.uplaods.Event;
import fr.livelovecite.uplaods.Going;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;


public class EventListAdapter extends ArrayAdapter<EventInteraction>
        implements Filterable{
    private EventHolder holder;
    private List<EventInteraction> events;
    private List<EventInteraction> filteredEvents;
    private int layoutResourceId;
    private Context context;

    public EventListAdapter(Context context,int layoutResourceId ,List<EventInteraction> events) {
        super(context,layoutResourceId,events);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.events = events;
        this.filteredEvents=events;
    }
    //For this helper method, return based on filteredData
    public int getCount()
    {
        return filteredEvents.size();
    }

    //This should return a data object, not an int
    public EventInteraction getItem(int position)
    {
        return filteredEvents.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView==null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new EventHolder();

            holder.ownerName = convertView.findViewById(R.id.ownerName);
            holder.ownerImage = convertView.findViewById(R.id.ownerImage);
            holder.eventTitle = convertView.findViewById(R.id.tveventTitle);
            holder.eventImage = convertView.findViewById(R.id.eventImage);
            holder.eventLocation = convertView.findViewById(R.id.tveventLocation);
            holder.eventDay = convertView.findViewById(R.id.eventDay);
            holder.eventMonth = convertView.findViewById(R.id.eventMonth);
            holder.tvParticipating = convertView.findViewById(R.id.tvGoing);
            holder.goingBTN = convertView.findViewById(R.id.GoingBTN);
            holder.calendarBTN = convertView.findViewById(R.id.CalendarBTN);
            holder.shareBTN = convertView.findViewById(R.id.ShareBTN);
            holder.reportBTN = convertView.findViewById(R.id.reportBTN);

            convertView.setTag(holder);
        }
        else {holder=(EventHolder) convertView.getTag();}

        EventInteraction eventInteractiontmp;
        eventInteractiontmp = events.get(position);
        // CHECK IF USER IS GOING
        holder.goingBTN.setTag(eventInteractiontmp);
        holder.goingBTN.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    final EventInteraction eventInteraction = (EventInteraction) v.getTag();
                    holder.tvParticipating.setTag(v.getTag());
                    v.setEnabled(false);

                    if (!eventInteraction.getIsParticipating() && eventInteraction.getEvent().getObjectId()!=null) {

                        Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.unbounce);
                        myAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                setGoingBTNsetTextView(eventInteraction);
                                v.setBackgroundResource(R.drawable.ic_star_going);
                                Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                                v.startAnimation(myAnim);

                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        v.startAnimation(myAnim);

                        final BackendlessUser backendlessUser =Backendless.UserService.CurrentUser() ;

                        Going going = new Going();
                        going.setEventID(eventInteraction.getEvent().getObjectId());
                        going.setOwnerId(backendlessUser.getUserId());
                        Backendless.Persistence.save(going, new AsyncCallback<Going>() {
                            public void handleResponse(Going response) {
                                //Increment Participation Value
                                Backendless.Data.of(Event.class).findById(eventInteraction.getEvent().getObjectId(), new AsyncCallback<Event>() {
                                    @Override
                                    public void handleResponse(Event response) {
                                        eventInteraction.setIsParticipating(true);
                                        response.setParticipation(response.getParticipation() + 1);
                                        //
                                        Backendless.Persistence.save(response, new AsyncCallback<Event>() {
                                            @Override
                                            public void handleResponse(Event response) {
                                                eventInteraction.getEvent().setParticipation(eventInteraction.getEvent().getParticipation()+1);
                                                v.setEnabled(true);
                                                notifyDataSetChanged();
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault backendlessFault) {v.setEnabled(true);}
                                        });
                                    }
                                    @Override
                                    public void handleFault(BackendlessFault backendlessFault) {v.setEnabled(true);}
                                });
                                /////////////////////////////
                            }

                            public void handleFault(BackendlessFault fault) {
                                v.setBackgroundResource(R.drawable.ic_star_notgoing);
                                v.setEnabled(true);
                            }
                        });
                    }
                    else if(eventInteraction.getIsParticipating() && eventInteraction.getEvent().getObjectId()!=null){
                        BackendlessUser backendlessUser = Backendless.UserService.CurrentUser() ;
                        v.setEnabled(false);

                        Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.unbounce);
                        myAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                setGoingBTNsetTextView(eventInteraction);
                                v.setBackgroundResource(R.drawable.ic_star_notgoing);
                                Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                                v.startAnimation(myAnim);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        v.startAnimation(myAnim);

                        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                        queryBuilder.setWhereClause( "ownerId = '" +backendlessUser.getUserId() + "' " +
                                "AND eventID = '"+ eventInteraction.getEvent().getObjectId() + "'" );
                        //Find going and remove it
                        Backendless.Persistence.of(Going.class).find(queryBuilder, new AsyncCallback<List<Going>>() {
                            @Override
                            public void handleResponse(List<Going> response) {
                                //REMOVE GOING
                                if (response.size() >=1) {
                                    Backendless.Persistence.of(Going.class).remove(response.get(0), new AsyncCallback<Long>() {
                                        @Override
                                        public void handleResponse(Long l) {
                                            //Decrement Participation Value
                                            Backendless.Data.of(Event.class).findById(eventInteraction.getEvent().getObjectId(), new AsyncCallback<Event>() {
                                                @Override
                                                public void handleResponse(Event response) {

                                                    eventInteraction.setIsParticipating(false);

                                                    response.setParticipation(response.getParticipation() - 1);
                                                    Backendless.Persistence.save(response, new AsyncCallback<Event>() {
                                                        @Override
                                                        public void handleResponse(Event response) {
                                                            eventInteraction.getEvent().setParticipation(eventInteraction.getEvent().getParticipation()-1);
                                                            v.setEnabled(true);
                                                            notifyDataSetChanged();
                                                        }
                                                        @Override
                                                        public void handleFault(BackendlessFault backendlessFault) {v.setEnabled(true);}
                                                    });
                                                }
                                                @Override
                                                public void handleFault(BackendlessFault backendlessFault) {v.setEnabled(true);}
                                            });
                                            ///////////////////////////////
                                            }
                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            v.setBackgroundResource(R.drawable.ic_star_going);
                                            v.setEnabled(true);
                                        }
                                    });
                                }
                                /////////////////////////////////////////////////////////////////////////////////////////////////
                            }
                            public void handleFault(BackendlessFault fault) {Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();}
                        });
                    }
                }
            });

        holder.calendarBTN.setTag(eventInteractiontmp);
        holder.calendarBTN.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final EventInteraction eventInteraction = (EventInteraction) v.getTag();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(context.getString(R.string.add_calendar)+" ?")
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id) {

                                    Intent intent = new Intent(Intent.ACTION_EDIT);
                                    intent.setType("vnd.android.cursor.item/event");
                                    intent.putExtra(CalendarContract.Events.TITLE, eventInteraction.getEvent().getTitle());
                                    intent.putExtra(CalendarContract.Events.DESCRIPTION, eventInteraction.getEvent().getDescription());
                                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventInteraction.getEvent().getMaison());
                                    Calendar c = Calendar.getInstance();
                                    c.setTime(eventInteraction.getEvent().getStarttime());
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, c.getTimeInMillis());
                                    c.setTime(eventInteraction.getEvent().getEndtime());
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, c.getTimeInMillis());
                                    getContext().startActivity(intent);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

        holder.shareBTN.setTag(eventInteractiontmp);
        holder.shareBTN.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventInteraction eventInteraction = (EventInteraction) v.getTag();

                        shareWithBranch(eventInteraction);
//                    String link = eventInteraction.getEvent().getPicture();
//                    if(context instanceof MainActivity){
//                        ((MainActivity)context).shareEventToFacebook(eventInteraction.getEvent().getTitle(),
//                                eventInteraction.getEvent().getDescription(),
//                                link);
//                    }

//                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                    sharingIntent.setType("text/plain");
//                    String shareBody = eventInteraction.getEvent().getTitle()
//                            + "\nStarts @" + eventInteraction.getFormattedDate(eventInteraction.getEvent().getStarttime())
//                            + "\nLocation: " + eventInteraction.getEvent().getMaison() + "\n\n";
//                            shareBody = shareBody + eventInteraction.getEvent().getPicture();
//                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, eventInteraction.getEvent().getTitle());
//                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//                    getContext().startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

         holder.eventImage.setTag(eventInteractiontmp);
         holder.eventImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventInteraction eventInteraction = (EventInteraction) v.getTag();
                    Intent eventDetailIntent = new Intent(getContext(), EventDetailActivity.class);

                    eventDetailIntent.putExtra("Title", eventInteraction.getEvent().getTitle());
                    eventDetailIntent.putExtra("Location", eventInteraction.getEvent().getMaison());

                    eventDetailIntent.putExtra("CalendarStart", String.valueOf(eventInteraction.getEvent().getStarttime()));
                    eventDetailIntent.putExtra("CalendarEnd", String.valueOf(eventInteraction.getEvent().getEndtime()));

                    Calendar cal = new GregorianCalendar();
                    cal.setTime(eventInteraction.getEvent().getStarttime());
                    String buzzstart = android.text.format.DateFormat.format("dd MMM yyyy @ HH:mm",cal.getTimeInMillis()).toString();
                    eventDetailIntent.putExtra("Starttime", buzzstart);
                    cal.setTime(eventInteraction.getEvent().getEndtime());
                    String buzzend = android.text.format.DateFormat.format("dd MMM yyyy @ HH:mm",cal.getTimeInMillis()).toString();
                    eventDetailIntent.putExtra("Endtime", buzzend);

                    BackendlessUser backendlessUser = Backendless.UserService.CurrentUser() ;
                    if(backendlessUser.getProperty("maison").toString().equals(eventInteraction.getEvent().getMaison()))
                        eventDetailIntent.putExtra("Price", eventInteraction.getEvent().getPriceInt());
                    else
                        eventDetailIntent.putExtra("Price", eventInteraction.getEvent().getPriceExt());

                    eventDetailIntent.putExtra("Description", eventInteraction.getEvent().getDescription());
                    eventDetailIntent.putExtra("Image", eventInteraction.getEvent().getPicture());
                    eventDetailIntent.putExtra("EventID", eventInteraction.getEvent().getObjectId());
                    eventDetailIntent.putExtra("Image",eventInteraction.getEvent().getPicture());
                    eventDetailIntent.putExtra("isGoing", eventInteraction.getIsParticipating());

                    getContext().startActivity(eventDetailIntent);
                    ((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);

                }
            });

            setupItem(holder, eventInteractiontmp);

        return convertView;
    }
    private void setupItem(final EventHolder holder, final EventInteraction eventInteraction) {
        holder.eventTitle.setText(eventInteraction.getEvent().getTitle());
        Picasso.with(getContext()).load(eventInteraction.getEvent().getPicture()).into(holder.eventImage);
        holder.eventLocation.setText(eventInteraction.getEvent().getMaison());
//        if(eventInteraction.getEvent().getStarttime().getTime()<= Calendar.getInstance().getTimeInMillis()
//                && Calendar.getInstance().getTimeInMillis()<= eventInteraction.getEvent().getEndtime().getTime())
//            holder.eventDate.setText("Happening now!");

        holder.eventDay.setText(eventInteraction.getFormattedDay(eventInteraction.getEvent().getStarttime()));
        holder.eventMonth.setText(eventInteraction.getFormattedMonth(eventInteraction.getEvent().getStarttime()));

        holder.eventImage.setTag(eventInteraction);
        holder.goingBTN.setTag(eventInteraction);

        holder.ownerName.setText(eventInteraction.getEvent().getOwnerName());
        if (eventInteraction.getEvent().getOwnerImage() == null
                || TextUtils.equals(eventInteraction.getEvent().getOwnerImage(),"pas_de_token"))
            holder.ownerImage.setImageResource(R.drawable.default_user_icon);
        else {
            String pp = "https://graph.facebook.com/" +eventInteraction.getEvent().getOwnerImage() + "/picture?type=large";
            Picasso.with(getContext())
                    .load(pp)
                    .placeholder(R.drawable.default_user_icon)
                    .into(holder.ownerImage);
        }
        if(eventInteraction.getEvent().getPrivate()){
            holder.shareBTN.setAlpha(0.4f);
            holder.shareBTN.setEnabled(false);
        }
        else {

            holder.shareBTN.setAlpha(1f);
            holder.shareBTN.setEnabled(true);
        }
        holder.reportBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

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
                        .load(eventInteraction.getEvent().getPicture())
                        .into(imageReport);
                ImageView userImage = dialog.findViewById(R.id.userReport);
                if (eventInteraction.getEvent().getOwnerImage() == null  || TextUtils.equals(eventInteraction.getEvent().getOwnerImage(),"pas_de_token"))
                    userImage.setImageResource(R.drawable.default_user_icon);
                else {
                    String pp = "https://graph.facebook.com/" + eventInteraction.getEvent().getOwnerImage() + "/picture?type=large";
                    Picasso.with(getContext()).load(pp).placeholder(R.drawable.default_user_icon).into(userImage);
                }
                submitReport.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
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
                                reportEvent reportEvent= new reportEvent();
                                reportEvent.setNameReported(eventInteraction.getEvent().getOwnerName());
                                reportEvent.setEventReported(eventInteraction.getEvent().getObjectId());
                                reportEvent.setReason(textReport.getText().toString());
                                // Report item
                                Backendless.Persistence.save(reportEvent, new AsyncCallback<reportEvent>() {
                                    @Override
                                    public void handleResponse(reportEvent response) {
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
                                userReport.setUserReported(eventInteraction.getEvent().getOwnerImage());
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

        setGoingBTNsetTextView(eventInteraction);
        this.notifyDataSetChanged();
        //////////////////////////////////////////////////////////////////////////////////

    }
    private static class EventHolder {
        TextView ownerName,eventTitle,eventLocation,tvParticipating, eventDay, eventMonth;
        ImageButton goingBTN, calendarBTN, shareBTN, reportBTN;
        ImageView ownerImage, eventImage;
    }

    private String setGoingBTNsetTextView(EventInteraction eventInteraction) {
        String areGoing= " "+context.getString(R.string.are_going);
        if(eventInteraction.getEvent().getParticipation()==0){areGoing="";}

        String you = context.getString(R.string.you)+" "+context.getString(R.string.and);
        int numberOfParticipants= eventInteraction.getEvent().getParticipation();

        if(numberOfParticipants==0){
            holder.tvParticipating.setText("");
            holder.goingBTN.setBackgroundResource(R.drawable.ic_star_notgoing);
        }
        else if(eventInteraction.getIsParticipating()){
            // I am going to the event
            holder.goingBTN.setBackgroundResource(R.drawable.ic_star_going);
            if(numberOfParticipants==1)
                holder.tvParticipating.setText(context.getString(R.string.you) +" "+ context.getString(R.string.are_going_with_me)); // You alone
            else
                holder.tvParticipating.setText(you + " " + (numberOfParticipants-1) + " " + context.getString(R.string.are_going_with_me)); // You and (x-1) are going
        }
        else{
            // I am not going to the event
            you="";
            holder.goingBTN.setBackgroundResource(R.drawable.ic_star_notgoing);
            holder.tvParticipating.setText("");
            if(numberOfParticipants>=2)
                holder.tvParticipating.setText(numberOfParticipants + " " + areGoing);
        }
        return holder.tvParticipating.getText().toString();
    }


    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
// We implement here the filter logic
                if (constraint == null || constraint.length() == 0) {
// No filter implemented we return all the list
                    results.values = events;
                    results.count = events.size();
                } else {
// We perform filtering operation
                    List<EventInteraction> eventInteractions = new ArrayList<>();

                    for (EventInteraction p : events) {
                        System.out.println(p);
                        if (p.getEvent().getTitle().equals(constraint) || p.getEvent().getDescription().equals(constraint))
                            eventInteractions.add(p);
                    }

                    results.values = eventInteractions;
                    results.count = eventInteractions.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

// Now we have to inform the adapter about the new list filtered
                if (results.count == 0) {
                    notifyDataSetInvalidated();
                }                else {
                    filteredEvents = ( List<EventInteraction> ) results.values;
                    notifyDataSetChanged();
                }

            }
        };
    }

    private void shareWithBranch(EventInteraction eventInteraction){

        Calendar cal = new GregorianCalendar();
        cal.setTime(eventInteraction.getEvent().getStarttime());
        String calstart = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm",cal.getTimeInMillis()).toString();
        cal.setTime(eventInteraction.getEvent().getEndtime());
        String calend = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm",cal.getTimeInMillis()).toString();

        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("event/"+eventInteraction.getEvent().getObjectId())
                .setTitle(eventInteraction.getEvent().getTitle())
                .setContentDescription(eventInteraction.getEvent().getDescription())
                .setContentImageUrl(eventInteraction.getEvent().getPicture())
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .addContentMetadata("Title", eventInteraction.getEvent().getTitle())
                .addContentMetadata("Image", eventInteraction.getEvent().getPicture())
                .addContentMetadata("Location", eventInteraction.getEvent().getMaison())
                .addContentMetadata("CalendarStart", calstart)
                .addContentMetadata("CalendarEnd", calend)
                .addContentMetadata("PriceInt", eventInteraction.getEvent().getPriceInt())
                .addContentMetadata("PriceExt", eventInteraction.getEvent().getPriceExt())
                .addContentMetadata("Description", eventInteraction.getEvent().getDescription());

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing");

        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(context, "Check this out!", "Check out this event ")
                .setCopyUrlStyle(context.getResources().getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                .setMoreOptionStyle(context.getResources().getDrawable(android.R.drawable.ic_menu_search), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                .setAsFullWidthStyle(true)
                .setSharingTitle(context.getResources().getString(R.string.share));

        branchUniversalObject.showShareSheet((AppCompatActivity)context,
                linkProperties,
                shareSheetStyle,
                new Branch.BranchLinkShareListener() {
                    @Override
                    public void onShareLinkDialogLaunched() {}
                    @Override
                    public void onShareLinkDialogDismissed() {}
                    @Override
                    public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {}
                    @Override
                    public void onChannelSelected(String channelName) {}
                });
    }
}