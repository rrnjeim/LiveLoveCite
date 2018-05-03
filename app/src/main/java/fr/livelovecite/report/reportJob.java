package fr.livelovecite.report;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class reportJob
{
  private java.util.Date created;
  private String reason;
  private String nameReported;
  private String objectId;
  private String ownerId;
  private String jobReported;
  private java.util.Date updated;

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
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

  public String getNameReported()
  {
    return nameReported;
  }

  public void setNameReported( String nameReported )
  {
    this.nameReported = nameReported;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public String getJobReported()
  {
    return jobReported;
  }

  public void setJobReported( String jobReported )
  {
    this.jobReported = jobReported;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }


  public reportJob save()
  {
    return Backendless.Data.of( reportJob.class ).save( this );
  }

  public void saveAsync( AsyncCallback<reportJob> callback )
  {
    Backendless.Data.of( reportJob.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( reportJob.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( reportJob.class ).remove( this, callback );
  }

  public static reportJob findById( String id )
  {
    return Backendless.Data.of( reportJob.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<reportJob> callback )
  {
    Backendless.Data.of( reportJob.class ).findById( id, callback );
  }

  public static reportJob findFirst()
  {
    return Backendless.Data.of( reportJob.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<reportJob> callback )
  {
    Backendless.Data.of( reportJob.class ).findFirst( callback );
  }

  public static reportJob findLast()
  {
    return Backendless.Data.of( reportJob.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<reportJob> callback )
  {
    Backendless.Data.of( reportJob.class ).findLast( callback );
  }

  public static List<reportJob> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( reportJob.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<reportJob>> callback )
  {
    Backendless.Data.of( reportJob.class ).find( queryBuilder, callback );
  }
}