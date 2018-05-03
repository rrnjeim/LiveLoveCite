package fr.livelovecite.activities;

import com.backendless.BackendlessUser;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class Activity
{
  private String Location;
  private String ownerId;
  private String FbLink;
  private java.util.Date created;
  private String Description;
  private String Image;
  private java.util.Date updated;
  private String Occurrence;
  private String Title;
  private String objectId;
  private String Category;
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

  public String getFbLink()
  {
    return FbLink;
  }

  public void setFbLink( String FbLink )
  {
    this.FbLink = FbLink;
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

  public String getImage()
  {
    return Image;
  }

  public void setImage( String Image )
  {
    this.Image = Image;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getOccurrence()
  {
    return Occurrence;
  }

  public void setOccurrence( String Occurrence )
  {
    this.Occurrence = Occurrence;
  }

  public String getTitle()
  {
    return Title;
  }

  public void setTitle( String Title )
  {
    this.Title = Title;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getCategory()
  {
    return Category;
  }

  public void setCategory( String Category )
  {
    this.Category = Category;
  }


  public Activity save()
  {
    return Backendless.Data.of( Activity.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Activity> callback )
  {
    Backendless.Data.of( Activity.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Activity.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Activity.class ).remove( this, callback );
  }

  public static Activity findById( String id )
  {
    return Backendless.Data.of( Activity.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Activity> callback )
  {
    Backendless.Data.of( Activity.class ).findById( id, callback );
  }

  public static Activity findFirst()
  {
    return Backendless.Data.of( Activity.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Activity> callback )
  {
    Backendless.Data.of( Activity.class ).findFirst( callback );
  }

  public static Activity findLast()
  {
    return Backendless.Data.of( Activity.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Activity> callback )
  {
    Backendless.Data.of( Activity.class ).findLast( callback );
  }

  public static List<Activity> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( Activity.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Activity>> callback )
  {
    Backendless.Data.of( Activity.class ).find( queryBuilder, callback );
  }
}