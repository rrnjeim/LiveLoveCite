package fr.livelovecite.report;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class userReport
{
  private java.util.Date updated;
  private String objectId;
  private String ownerId;
  private String userReported;
  private java.util.Date created;
  private String reason;

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public String getUserReported()
  {
    return userReported;
  }

  public void setUserReported( String userReported )
  {
    this.userReported = userReported;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason( String reason )
  {
    this.reason = reason;
  }


  public userReport save()
  {
    return Backendless.Data.of( userReport.class ).save( this );
  }

  public void saveAsync( AsyncCallback<userReport> callback )
  {
    Backendless.Data.of( userReport.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( userReport.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( userReport.class ).remove( this, callback );
  }

  public static userReport findById( String id )
  {
    return Backendless.Data.of( userReport.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<userReport> callback )
  {
    Backendless.Data.of( userReport.class ).findById( id, callback );
  }

  public static userReport findFirst()
  {
    return Backendless.Data.of( userReport.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<userReport> callback )
  {
    Backendless.Data.of( userReport.class ).findFirst( callback );
  }

  public static userReport findLast()
  {
    return Backendless.Data.of( userReport.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<userReport> callback )
  {
    Backendless.Data.of( userReport.class ).findLast( callback );
  }

  public static List<userReport> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( userReport.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<userReport>> callback )
  {
    Backendless.Data.of( userReport.class ).find( queryBuilder, callback );
  }
}