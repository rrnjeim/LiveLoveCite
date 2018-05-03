package fr.livelovecite.report;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class reportMarket
{
  private String productReported;
  private String reason;
  private String ownerId;
  private java.util.Date created;
  private java.util.Date updated;
  private String objectId;
  private String nameReported;
  public String getProductReported()
  {
    return productReported;
  }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setProductReported(String productReported )
  {
    this.productReported = productReported;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason( String reason )
  {
    this.reason = reason;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getNameReported()
  {
    return nameReported;
  }

  public void setNameReported( String nameReported )
  {
    this.nameReported = nameReported;
  }


  public reportMarket save()
  {
    return Backendless.Data.of( reportMarket.class ).save( this );
  }

  public void saveAsync( AsyncCallback<reportMarket> callback )
  {
    Backendless.Data.of( reportMarket.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( reportMarket.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( reportMarket.class ).remove( this, callback );
  }

  public static reportMarket findById( String id )
  {
    return Backendless.Data.of( reportMarket.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<reportMarket> callback )
  {
    Backendless.Data.of( reportMarket.class ).findById( id, callback );
  }

  public static reportMarket findFirst()
  {
    return Backendless.Data.of( reportMarket.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<reportMarket> callback )
  {
    Backendless.Data.of( reportMarket.class ).findFirst( callback );
  }

  public static reportMarket findLast()
  {
    return Backendless.Data.of( reportMarket.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<reportMarket> callback )
  {
    Backendless.Data.of( reportMarket.class ).findLast( callback );
  }

  public static List<reportMarket> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( reportMarket.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<reportMarket>> callback )
  {
    Backendless.Data.of( reportMarket.class ).find( queryBuilder, callback );
  }
}