package fr.livelovecite.report;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;


public class reportEvent
{
  private String ownerId;
  private java.util.Date created;
  private String nameReported;
  private String reason;
  private String objectId;
  private java.util.Date updated;
  private String eventReported;
  public String getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getNameReported()
  {
    return nameReported;
  }

  public void setNameReported( String nameReported )
  {
    this.nameReported = nameReported;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason( String reason )
  {
    this.reason = reason;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getEventReported()
  {
    return eventReported;
  }

  public void setEventReported( String eventReported )
  {
    this.eventReported = eventReported;
  }


  public reportEvent save()
  {
    return Backendless.Data.of( reportEvent.class ).save( this );
  }

  public void saveAsync( AsyncCallback<reportEvent> callback )
  {
    Backendless.Data.of( reportEvent.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( reportEvent.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( reportEvent.class ).remove( this, callback );
  }

  public static reportEvent findById( String id )
  {
    return Backendless.Data.of( reportEvent.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<reportEvent> callback )
  {
    Backendless.Data.of( reportEvent.class ).findById( id, callback );
  }

  public static reportEvent findFirst()
  {
    return Backendless.Data.of( reportEvent.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<reportEvent> callback )
  {
    Backendless.Data.of( reportEvent.class ).findFirst( callback );
  }

  public static reportEvent findLast()
  {
    return Backendless.Data.of( reportEvent.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<reportEvent> callback )
  {
    Backendless.Data.of( reportEvent.class ).findLast( callback );
  }

  public static List<reportEvent> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( reportEvent.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<reportEvent>> callback )
  {
    Backendless.Data.of( reportEvent.class ).find( queryBuilder, callback );
  }
}