package fr.livelovecite.ads;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.io.Serializable;
import java.util.List;

public class Ads implements Serializable{
    private String companyAddress;
    private String description;
    private String objectId;
    private java.util.Date updated;
    private String ownerId;
    private String companyDetails;
    private Boolean isValid;
    private String companyName;
    private String title;
    private String image;
    private String link;
    private int nbClick;
    private java.util.Date created;


    public String getCompanyAddress() {return companyAddress;}

    public void setCompanyAddress(String companyAddress) {this.companyAddress = companyAddress;}

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public java.util.Date getUpdated()
    {
        return updated;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public String getCompanyDetails()
    {
        return companyDetails;
    }

    public void setCompanyDetails( String companyDetails )
    {
        this.companyDetails = companyDetails;
    }

    public Boolean getIsValid()
    {
        return isValid;
    }

    public void setIsValid( Boolean isValid )
    {
        this.isValid = isValid;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName( String companyName )
    {
        this.companyName = companyName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage( String image )
    {
        this.image = image;
    }

    public java.util.Date getCreated()
    {
        return created;
    }

    public String getLink() {return link;}

    public void setLink(String link) {this.link = link;}

    public int getNbClick() {return nbClick;}

    public void setNbClick(int nbClick) {this.nbClick = nbClick;}

    public Ads save()
    {
        return Backendless.Data.of( Ads.class ).save( this );
    }

    public void saveAsync( AsyncCallback<Ads> callback )
    {
        Backendless.Data.of( Ads.class ).save( this, callback );
    }

    public Long remove()
    {
        return Backendless.Data.of( Ads.class ).remove( this );
    }

    public void removeAsync( AsyncCallback<Long> callback )
    {
        Backendless.Data.of( Ads.class ).remove( this, callback );
    }

    public static Ads findById( String id )
    {
        return Backendless.Data.of( Ads.class ).findById( id );
    }

    public static void findByIdAsync( String id, AsyncCallback<Ads> callback )
    {
        Backendless.Data.of( Ads.class ).findById( id, callback );
    }

    public static Ads findFirst()
    {
        return Backendless.Data.of( Ads.class ).findFirst();
    }

    public static void findFirstAsync( AsyncCallback<Ads> callback )
    {
        Backendless.Data.of( Ads.class ).findFirst( callback );
    }

    public static Ads findLast()
    {
        return Backendless.Data.of( Ads.class ).findLast();
    }

    public static void findLastAsync( AsyncCallback<Ads> callback )
    {
        Backendless.Data.of( Ads.class ).findLast( callback );
    }

    public static List<Ads> find( DataQueryBuilder queryBuilder )
    {
        return Backendless.Data.of( Ads.class ).find( queryBuilder );
    }

    public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Ads>> callback )
    {
        Backendless.Data.of( Ads.class ).find( queryBuilder, callback );
    }
}