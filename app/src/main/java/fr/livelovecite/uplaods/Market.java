package fr.livelovecite.uplaods;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Market {

  private String title;
  private String tags;
  private String ownerId;
  private java.util.Date updated;
  private String Price;
  private String description;
  private String token;
  private java.util.Date created;
  private String objectId;
  private String Picture;
  private Boolean Verified;
  private boolean showNumber;
  private String ownerName;
  private String ownerImage;
  private String ownerMaison;

  public String getTitle(){return title;}

  public void setTitle(String title){this.title=title;}

  public String gettags()
  {
    return tags;
  }

  public void settags( String tags ) {this.tags = tags;}

  public String getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {this.ownerId = ownerId;}

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getPrice() {return Price+" €";}

  public void setPrice( String Price )
  {
    this.Price = Price;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription( String description )
  {
    this.description = description;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken( String token )
  {
    this.token = token;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getCreatedFormatted(){
    Calendar calendar = new GregorianCalendar(Locale.US);
    calendar.setTime(this.getCreated());

    return calendar.get(Calendar.DAY_OF_MONTH)+" "+getMonthForInt(calendar.get(Calendar.MONTH))+" "+calendar.get(Calendar.YEAR)+" ∙ ";
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

  public String getObjectId()
  {
    return objectId;
  }

  public String getPicture()
  {
    return Picture;
  }

  public void setPicture( String Picture )
  {
    this.Picture = Picture;
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

  public String getOwnerMaison() {return ownerMaison;}

  public void setOwnerMaison(String ownerMaison) {this.ownerMaison = ownerMaison;}

  public Market save()
  {
    return Backendless.Data.of( Market.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Market> callback ) {
    Backendless.Data.of( Market.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Market.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback ) {
    Backendless.Data.of( Market.class ).remove( this, callback );
  }

  public static Market findById( String id ) {
    return Backendless.Data.of( Market.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Market> callback ) {
    Backendless.Data.of( Market.class ).findById( id, callback );
  }

  public static Market findFirst()
  {
    return Backendless.Data.of( Market.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Market> callback ) {
    Backendless.Data.of( Market.class ).findFirst( callback );
  }

  public static Market findLast()
  {
    return Backendless.Data.of( Market.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Market> callback ) {
    Backendless.Data.of( Market.class ).findLast( callback );
  }

  public static List<Market> find( DataQueryBuilder queryBuilder ) {
    return Backendless.Data.of( Market.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Market>> callback ) {
    Backendless.Data.of( Market.class ).find( queryBuilder, callback );
  }
}