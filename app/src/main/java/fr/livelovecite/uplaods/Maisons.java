package fr.livelovecite.uplaods;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class Maisons
{
  private String Email;
  private String objectId;
  private String Picture;
  private String Adresse;
  private String ownerId;
  private java.util.Date updated;
  private String Maison;
  private String Phone;
  private String Password;
  private java.util.Date created;
  public String getEmail()
  {
    return Email;
  }

  public void setEmail( String Email )
  {
    this.Email = Email;
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

  public String getAdresse()
  {
    return Adresse;
  }

  public void setAdresse( String Adresse )
  {
    this.Adresse = Adresse;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getMaison()
  {
    return Maison;
  }

  public void setMaison( String Maison )
  {
    this.Maison = Maison;
  }

  public String getPhone()
  {
    return Phone;
  }

  public String getPassword(){return Password;}

  public void setPhone( String Phone )
  {
    this.Phone = Phone;
  }

  public java.util.Date getCreated()
  {
    return created;
  }


  public Maisons save()
  {
    return Backendless.Data.of( Maisons.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Maisons> callback )
  {
    Backendless.Data.of( Maisons.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Maisons.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Maisons.class ).remove( this, callback );
  }

  public static Maisons findById( String id )
  {
    return Backendless.Data.of( Maisons.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Maisons> callback )
  {
    Backendless.Data.of( Maisons.class ).findById( id, callback );
  }

  public static Maisons findFirst()
  {
    return Backendless.Data.of( Maisons.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Maisons> callback )
  {
    Backendless.Data.of( Maisons.class ).findFirst( callback );
  }

  public static Maisons findLast()
  {
    return Backendless.Data.of( Maisons.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Maisons> callback )
  {
    Backendless.Data.of( Maisons.class ).findLast( callback );
  }

  public static List<Maisons> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( Maisons.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Maisons>> callback )
  {
    Backendless.Data.of( Maisons.class ).find( queryBuilder, callback );
  }
}