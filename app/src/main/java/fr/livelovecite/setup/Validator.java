package fr.livelovecite.setup;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.Toast;

import fr.livelovecite.R;

/**
 * Provides static methods for different value validators.
 * Shows Toasts with warnings if validation fails.
 */
public class Validator {
    /**
     * Validates user's name: checks whether it is not empty and whether the first letter is in uppercase.
     * Shows Toast with a warning if validation fails.
     *
     * @param currentContext context, in which validation occurs
     * @param name           user's name to be validated
     * @return true if name is valid, false if validation failed
     */
    public static boolean isNameValid( Context currentContext, CharSequence name )
    {
        if( name.toString().isEmpty() )
        {
            Toast.makeText( currentContext, R.string.name_required, Toast.LENGTH_LONG ).show();
            return false;
        }

      /*  if( !Character.isUpperCase( name.charAt( 0 ) ) )
        {
            Toast.makeText( currentContext, "name in lowercase", Toast.LENGTH_LONG ).show();
            return false;
        }*/

        return true;
    }

    /**
     * Validates email: checks whether it is not empty and whether it matches Patterns.EMAIL_ADDRESS regex.
     * Shows Toast with a warning if validation fails.
     *
     * @param currentContext context, in which validation occurs
     * @param email          email to be validated
     * @return true if email is valid, false if validation failed
     */
    public static boolean isEmailValid( Context currentContext, CharSequence email )
    {
        if( email.toString().isEmpty() )
        {
            Toast.makeText( currentContext, R.string.email_required, Toast.LENGTH_LONG ).show();
            return false;
        }

        if( !Patterns.EMAIL_ADDRESS.matcher( email ).matches() )
        {
            Toast.makeText( currentContext, R.string.inavlid_email, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }

    /**
     * Validates password: checks whether it is not empty.
     * Shows Toast with a warning if validation fails.
     *
     * @param currentContext context, in which validation occurs
     * @param password       password to be validated
     * @return true if password is valid, false if validation failed
     */
    public static boolean isPasswordValid( Context currentContext, CharSequence password )
    {
        if( password.toString().isEmpty() )
        {
            Toast.makeText( currentContext, R.string.password_required, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }

    public static boolean isPhoneNumberValid( Context currentContext, CharSequence number )
    {
        if( number.toString().isEmpty() )
        {
            Toast.makeText( currentContext, R.string.number_required, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }


    /**
     * Select house: checks whether it is not empty.
     * Shows Toast with a warning if validation fails.
     *
     * @param currentContext context, in which validation occurs
     * @param maison       house to be selected
     * @return true if house is chosen, false if validation failed
     */
    public static boolean isHouseSelected( Context currentContext, CharSequence maison ) {
        if( TextUtils.equals( maison, currentContext.getString(R.string.maison_prompt) ) )
        {
            Toast.makeText( currentContext, R.string.house_required, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }

    public static boolean isNationalitySelected( Context currentContext, CharSequence nationality ) {
        if( TextUtils.equals( nationality,currentContext.getString(R.string.nationality_prompt) ) ) {
            Toast.makeText( currentContext, R.string.nationality_required, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }

    public static boolean hasAgreed (CheckBox checkBox) {
        if(!checkBox.isChecked()){
            checkBox.setError("Required !!");
            return false;
        }
        return true;
    }
}