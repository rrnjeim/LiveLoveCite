package fr.livelovecite.uplaods;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class Event {
  private String Title;
  private String PriceExt;
  private String ownerId;
  private java.util.Date Endtime;
  private String Maison;
  private String PriceInt;
  private String objectId;
  private java.util.Date Starttime;
  private java.util.Date created;
  private String Description;
  private java.util.Date updated;
  private String Picture;
  private String token;
  private int Participation;
  private Boolean Private;
  private Boolean Verified;
  private String ownerName;
  private String ownerImage;

  public String getTitle()
  {
    return Title;
  }

  public void setTitle( String Title )
  {
    this.Title = Title;
  }

  public String getPriceExt()
  {
    return PriceExt;
  }

  public void setPriceExt( String PriceExt )
  {
    this.PriceExt = PriceExt;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {this.ownerId = ownerId;}

  public java.util.Date getEndtime()
  {
    return Endtime;
  }

  public void setEndtime( java.util.Date Endtime )
  {
    this.Endtime = Endtime;
  }

  public String getMaison()
  {
    return Maison;
  }

  public void setMaison( String Maison )
  {
    this.Maison = Maison;
  }

  public String getPriceInt()
  {
    return PriceInt;
  }

  public void setPriceInt( String PriceInt )
  {
    this.PriceInt = PriceInt;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public java.util.Date getStarttime() {return Starttime;}

  public void setStarttime( java.util.Date Starttime )
  {
    this.Starttime = Starttime;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getDescription()
  {
    return Description;
  }

  public void setDescription( String Description )
  {
    this.Description = Description;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getPicture()
  {
    return Picture;
  }

  public void setPicture( String Picture )
  {
    this.Picture = Picture;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken( String token )
  {
    this.token = token;
  }

  public int getParticipation(){return Participation;}

  public void setParticipation(int Participation){this.Participation=Participation;}

  public Boolean getPrivate()
  {
    return Private;
  }

  public void setPrivate( Boolean Private )
  {
    this.Private = Private;
  }

  public Boolean getVerified()
  {
    return Verified;
  }

  public void setVerified( Boolean Verified )
  {
    this.Verified = Verified;
  }

  public String getOwnerName() {return ownerName;}

  public void setOwnerName(String ownerName) {this.ownerName = ownerName;}

  public String getOwnerImage() {return ownerImage;}

  public void setOwnerImage(String ownerImage) {this.ownerImage = ownerImage;}

  @Override
  public String toString(){
    return "Title: "+getTitle()
            +"\nLocation: "+getMaison()
            +"\nDate Start: "+getStarttime()
            +"\nDate End: "+getEndtime()
            +"\nDescription: "+getDescription()
            +"\nParticipating "+getParticipation();
  }

  public Event save()
  {
    return Backendless.Data.of( Event.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Event> callback )
  {
    Backendless.Data.of( Event.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Event.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Event.class ).remove( this, callback );
  }

  public static Event findById( String id )
  {
    return Backendless.Data.of( Event.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Event> callback )
  {
    Backendless.Data.of( Event.class ).findById( id, callback );
  }

  public static Event findFirst()
  {
    return Backendless.Data.of( Event.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Event> callback )
  {
    Backendless.Data.of( Event.class ).findFirst( callback );
  }

  public static Event findLast()
  {
    return Backendless.Data.of( Event.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Event> callback )
  {
    Backendless.Data.of( Event.class ).findLast( callback );
  }

  public static List<Event> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( Event.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Event>> callback )
  {
    Backendless.Data.of( Event.class ).find( queryBuilder, callback );
  }
}