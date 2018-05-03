package fr.livelovecite.uplaods;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Job {

  private String Titre;
  private java.util.Date updated;
  private String token;
  private String Location;
  private String ownerId;
  private String objectId;
  private String Description;
  private String Picture;
  private java.util.Date created;
  private String Remuneration;
  private Boolean Verified;
  private boolean showNumber;
  private String ownerName;
  private String ownerImage;
  private String emailContact;
  private String numberContact;


  public String getTitre()
  {
    return Titre;
  }

  public void setTitre( String Titre )
  {
    this.Titre = Titre;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken( String token )
  {
    this.token = token;
  }

  public String getLocation()
  {
    return Location;
  }

  public void setLocation( String Location )
  {
    this.Location = Location;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {this.ownerId = ownerId;}

  public String getObjectId()
  {
    return objectId;
  }

  public String getDescription()
  {
    return Description;
  }

  public void setDescription( String Description )
  {
    this.Description = Description;
  }

  public String getPicture()
  {
    return Picture;
  }

  public void setPicture( String Picture )
  {
    this.Picture = Picture;
  }

  public java.util.Date getCreated()
  {
    return created;
  }
  public String getCreatedFormatted(){
    Calendar calendar = new GregorianCalendar(Locale.US);
    calendar.setTime(this.getCreated());

    return ""+calendar.get(Calendar.DAY_OF_MONTH)+" "+getMonthForInt(calendar.get(Calendar.MONTH))+" "+calendar.get(Calendar.YEAR)+" ∙ ";
  }
  private String getMonthForInt(int num) {
    String month = "N/A";
    DateFormatSymbols dfs = new DateFormatSymbols();
    String[] months = dfs.getShortMonths();
    if (num >= 0 && num <= 11 ) {
      month = months[num];
    }
    return month;
  }
  public String getRemuneration()
  {
    return Remuneration;
  }

  public void setRemuneration( String Remuneration )
  {
    this.Remuneration = Remuneration;
  }

  public Boolean getVerified()
  {
    return Verified;
  }

  public void setVerified( Boolean Verified )
  {
    this.Verified = Verified;
  }

  public void setShowNumber(boolean showNumber) {this.showNumber = showNumber;}

  public boolean getShowNumber() {return showNumber;}


  public String getOwnerName() {return ownerName;}

  public void setOwnerName(String ownerName) {this.ownerName = ownerName;}

  public String getOwnerImage() {return ownerImage;}

  public void setOwnerImage(String ownerImage) {this.ownerImage = ownerImage;}

  public String getEmailContact() {return emailContact;}

  public void setEmailContact(String emailContact) {this.emailContact = emailContact;}

  public String getNumberContact() {return numberContact;}

  public void setNumberContact(String numberContact) {this.numberContact = numberContact;}

  public String toString(){
    return (getTitre()+" CREATED ON "+getCreated()+"\n\n");
  }

  public Job save()
  {
    return Backendless.Data.of( Job.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Job> callback )
  {
    Backendless.Data.of( Job.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Job.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Job.class ).remove( this, callback );
  }

  public static Job findById( String id )
  {
    return Backendless.Data.of( Job.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Job> callback )
  {
    Backendless.Data.of( Job.class ).findById( id, callback );
  }

  public static Job findFirst()
  {
    return Backendless.Data.of( Job.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Job> callback )
  {
    Backendless.Data.of( Job.class ).findFirst( callback );
  }

  public static Job findLast()
  {
    return Backendless.Data.of( Job.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Job> callback )
  {
    Backendless.Data.of( Job.class ).findLast( callback );
  }

  public static List<Job> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( Job.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Job>> callback )
  {
    Backendless.Data.of( Job.class ).find( queryBuilder, callback );
  }
}