package fr.livelovecite.uplaods;

        import com.backendless.Backendless;
        import com.backendless.async.callback.AsyncCallback;
        import com.backendless.persistence.BackendlessDataQuery;
        import com.backendless.persistence.DataQueryBuilder;

        import java.util.List;

public class Going
{
    private String ownerId;
    private java.util.Date created;
    private java.util.Date updated;
    private String objectId;
    private String eventID;
    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(String ownerId){this.ownerId=ownerId;}

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

    public String getEventID()
    {
        return eventID;
    }

    public void setEventID( String eventID )
    {
        this.eventID = eventID;
    }


    public Going save()
    {
        return Backendless.Data.of( Going.class ).save( this );
    }

    public void saveAsync( AsyncCallback<Going> callback )
    {
        Backendless.Data.of( Going.class ).save( this, callback );
    }

    public Long remove()
    {
        return Backendless.Data.of( Going.class ).remove( this );
    }

    public void removeAsync( AsyncCallback<Long> callback )
    {
        Backendless.Data.of( Going.class ).remove( this, callback );
    }

    public static Going findById( String id )
    {
        return Backendless.Data.of( Going.class ).findById( id );
    }

    public static void findByIdAsync( String id, AsyncCallback<Going> callback )
    {
        Backendless.Data.of( Going.class ).findById( id, callback );
    }

    public static Going findFirst()
    {
        return Backendless.Data.of( Going.class ).findFirst();
    }

    public static void findFirstAsync( AsyncCallback<Going> callback )
    {
        Backendless.Data.of( Going.class ).findFirst( callback );
    }

    public static Going findLast()
    {
        return Backendless.Data.of( Going.class ).findLast();
    }

    public static void findLastAsync( AsyncCallback<Going> callback )
    {
        Backendless.Data.of( Going.class ).findLast( callback );
    }

    public static List<Going> find( DataQueryBuilder queryBuilder )
    {
        return Backendless.Data.of( Going.class ).find( queryBuilder );
    }

    public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Going>> callback )
    {
        Backendless.Data.of( Going.class ).find( queryBuilder, callback );
    }
}