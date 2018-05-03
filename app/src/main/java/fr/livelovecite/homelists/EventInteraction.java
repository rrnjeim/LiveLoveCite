package fr.livelovecite.homelists;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import fr.livelovecite.uplaods.Event;

public class EventInteraction{
    private Event event;
    private Boolean isParticipating=false;

    public Event getEvent() {return event;}
    public void setEvent( Event event ) {this.event = event;}

    public Boolean getIsParticipating(){return isParticipating;}
    public void setIsParticipating (Boolean isParticipating){this.isParticipating=isParticipating;}


    public String getFormattedDate(Date olddate){
        String date = String.valueOf(olddate);
        SimpleDateFormat input = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy @ HH:mm", Locale.getDefault());
        try {
            date = output.format((input.parse(date)));
        } catch (ParseException e) {e.printStackTrace();}

        return date;
    }
    public String getFormattedDay(Date olddate){
        Calendar cal = new GregorianCalendar();
        cal.setTime(olddate);
        return DateFormat.format("d",cal.getTimeInMillis()).toString();
    }
    public String getFormattedMonth(Date olddate){
        Calendar cal = new GregorianCalendar();
        cal.setTime(olddate);
        return DateFormat.format("MMM",cal.getTimeInMillis()).toString();
    }



    public static class Comparators {

        public static Comparator<EventInteraction> StartDate = new Comparator<EventInteraction>() {
            @Override
            public int compare(EventInteraction o1, EventInteraction o2) {
                return o1.event.getStarttime().compareTo(o2.event.getStarttime());
            }
        };
        public static Comparator<EventInteraction> StartDateDESC = new Comparator<EventInteraction>() {
            @Override
            public int compare(EventInteraction o1, EventInteraction o2) {
                return o2.event.getStarttime().compareTo(o1.event.getStarttime());
            }
        };
        public static Comparator<EventInteraction> createdDate = new Comparator<EventInteraction>() {
            @Override
            public int compare(EventInteraction o1, EventInteraction o2) {
                return o2.event.getCreated().compareTo(o1.event.getCreated());
            }
        };
        public static Comparator<EventInteraction> Popularity = new Comparator<EventInteraction>() {
            @Override
            public int compare(EventInteraction o1, EventInteraction o2) {
                return o2.event.getParticipation() - o1.event.getParticipation();
            }
        };

    }
}

