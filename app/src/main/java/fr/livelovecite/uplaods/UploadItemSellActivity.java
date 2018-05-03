package fr.livelovecite.uplaods;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.util.Calendar;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;

public class UploadItemSellActivity extends AppCompatActivity {
    BackendlessUser user = new BackendlessUser();

    private ImageView itemImage;
    EditText itemTitle, itemPrice, itemTags, itemDescription;
    ImageButton uploadBTN, deleteBTN, selectImageBTN;
    Switch showNumberSwitch;
    Boolean imageChanged=false;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_item_sell);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_item));

        itemTitle = findViewById(R.id.addItemTitle);
        itemPrice = findViewById(R.id.addItemPrice);

        itemTags = findViewById(R.id.addItemTags);
        itemDescription = findViewById(R.id.addItemDescription);
        itemImage = findViewById(R.id.addItemImage);
        selectImageBTN = findViewById(R.id.addImageBTN);
        showNumberSwitch = findViewById(R.id.showNumberSwitch);

        uploadBTN = findViewById(R.id.doneBTN);
        deleteBTN = findViewById(R.id.deleteBTN);
        deleteBTN.setVisibility(View.GONE);

        Backendless.initApp(UploadItemSellActivity.this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        user = Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
        if(user.getProperty("mobile")==null || user.getProperty("mobile").toString().length()==0){
            showNumberSwitch.setChecked(false);
            showNumberSwitch.setEnabled(false);
        }
        if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES")) {
            itemTitle.setText(getIntent().getStringExtra("Title"));
            itemPrice.setText(getIntent().getStringExtra("Price"));
            itemTags.setText(getIntent().getStringExtra("Tags"));
            itemDescription.setText(getIntent().getStringExtra("Description"));
            deleteBTN.setVisibility(View.VISIBLE);
            itemImage.setBackgroundResource(android.R.color.transparent);
            showNumberSwitch.setChecked(getIntent().getBooleanExtra("ShowNumber",true));
            String pp = (getIntent().getStringExtra("Image"));
            if(pp!=null)
                Picasso.with(UploadItemSellActivity.this).load(pp).into(itemImage);
            getSupportActionBar().setTitle(getString(R.string.edit_item));

        }

        selectImageBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        itemImage.setOnClickListener(new View.OnClickListener() {

    @Override
    public void onClick(View v) {
        selectImage();
    }
});

        View.OnClickListener uploadButtonClickListener = createUploadButtonClickListener();
        uploadBTN.setOnClickListener( uploadButtonClickListener );

        View.OnClickListener deleteItemClickListener = createDeleteItemListener();
        deleteBTN.setOnClickListener(deleteItemClickListener);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadItemSellActivity.this);
        builder.setMessage(R.string.are_you_sure)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }

                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public View.OnClickListener createUploadButtonClickListener() {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES"))
                    UpdateItem();
                else if (CompletionValidator())
                    UploadNewItem();
            }
        };
    }
    public View.OnClickListener createDeleteItemListener(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {deleteItem();}
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }
    private boolean CompletionValidator() {
        if( itemTitle.getText().toString().trim().equals("")) {
            itemTitle.setError( "Title required!" );
            return false;}

        else if( itemPrice.getText().toString().trim().equals("")) {
            itemPrice.setError( "Price required!" );
            return false;}

        else if( itemTags.getText().toString().trim().equals("")) {
            itemTags.setError( "Tags required!" );
            return false;}

        else if(itemDescription.getText().toString().trim().equals("")) {
            itemDescription.setError( "Description required!" );
            return false;}

        else if(itemImage.getDrawable()==null) {
            Toast.makeText(UploadItemSellActivity.this,"Required item image!", Toast.LENGTH_LONG ).show();
            return false;}

        return true;
    }
    private void hasChanged(Market response) {
        // an item instance has been found by ObjectId
        response.setTitle(itemTitle.getText().toString());
        response.setPrice(itemPrice.getText().toString());
        response.settags(itemTags.getText().toString());
        response.setShowNumber(showNumberSwitch.isChecked());
        response.setDescription(itemDescription.getText().toString());

        if((boolean)user.getProperty("isAdmin")) response.setVerified(true);
        else response.setVerified(false);
    }

    private void UploadNewItem() {
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadItemSellActivity.this);
        builder.setMessage(getString(R.string.add_item_to_sell)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UploadItemSellActivity.this, getString(R.string.working),
                                getString(R.string.uploading_image), true);
                        progress.show();
                        final Calendar calendar = Calendar.getInstance();
                        final int thisHour = calendar.get(Calendar.HOUR);
                        final int thisMinute = calendar.get(Calendar.MINUTE);
                        final int thisSecond = calendar.get(Calendar.SECOND);
                        final String fileName =""+user.getUserId()+thisHour+thisMinute+thisSecond+".jpeg";

                        Bitmap finalImage = scaleBitmap( ((BitmapDrawable)itemImage.getDrawable()).getBitmap());
                        // Upload the file
                        Backendless.Files.Android.upload(finalImage,
                                Bitmap.CompressFormat.JPEG, 80, fileName, "myfiles",
                                new AsyncCallback<BackendlessFile>() {
                                    @Override
                                    public void handleResponse( final BackendlessFile backendlessFile ) {
                                        progress.setMessage(getString(R.string.uploading_info));

                                        Market item = new Market();
                                        item.setTitle(itemTitle.getText().toString());
                                        item.setPrice(itemPrice.getText().toString());
                                        item.settags(itemTags.getText().toString());
                                        item.setDescription(itemDescription.getText().toString());
                                        item.setShowNumber(showNumberSwitch.isChecked());


                                        item.setPicture("https://api.backendless.com/709E3602-AABE-41E9-FF47-48B4C07F4700/DC935617-7AC5-AAA8-FF45-EC35472FAA00/files/myfiles/"+fileName);
                                        item.setToken(user.getUserId());
                                        item.setOwnerId(user.getUserId());
                                        if((boolean)user.getProperty("isAdmin")) item.setVerified(true);
                                        item.setOwnerName(user.getProperty("name").toString());
                                        item.setOwnerImage(user.getProperty("facebookId").toString());
                                        item.setOwnerMaison(user.getProperty("maison").toString());

                                        // save object asynchronously
                                        Backendless.Persistence.save(item, new AsyncCallback<Market>() {

                                                    public void handleResponse(Market response) {
                                                        progress.dismiss();
                                                        if(!(boolean ) user.getProperty("isAdmin"))
                                                            Toast.makeText(UploadItemSellActivity.this, getString(R.string.waiting_approval) , Toast.LENGTH_LONG).show();
                                                        Intent resultIntent = new Intent();
                                                        setResult(Activity.RESULT_OK, resultIntent);
                                                        finish();

                                                    }

                                                    public void handleFault(BackendlessFault fault) {
                                                        progress.dismiss();
                                                        Toast.makeText(UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void handleFault( BackendlessFault backendlessFault ) {
                                        progress.dismiss();
                                        if(backendlessFault.getCode().equals("1168"))
                                            Toast.makeText(UploadItemSellActivity.this, "Please remove emoticons and smileys from texts and try again..", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void UpdateItem(){
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadItemSellActivity.this);
        builder.setMessage(getString(R.string.update_item).trim()+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UploadItemSellActivity.this, getString(R.string.working), getString(R.string.updating_info), true, false);
                        Backendless.Persistence.of( Market.class ).findById( getIntent().getStringExtra("ItemID"), new AsyncCallback<Market>() {
                @Override
                public void handleResponse( Market response )
                {
                    if(CompletionValidator()){
                        progress.show();
                        hasChanged(response);
                            Backendless.Persistence.save( response, new AsyncCallback<Market>() {
                                @Override
                                public void handleResponse( Market response )
                                {
                                    // Change image
                                    if(imageChanged) {
                                        Uri uri =  Uri.parse( response.getPicture() );
                                        progress.setMessage(getString(R.string.updating_image));
                                        String[] segments = uri.getPath().split("/");
                                        String idStr = segments[segments.length-1];
                                        Bitmap finalImage = scaleBitmap( ((BitmapDrawable)itemImage.getDrawable()).getBitmap());
                                        Backendless.Files.Android.upload(finalImage,
                                                Bitmap.CompressFormat.JPEG, 50, idStr, "myfiles", true, new AsyncCallback<BackendlessFile>() {
                                                    @Override
                                                    public void handleResponse(BackendlessFile response) {
                                                        progress.dismiss();
                                                        if(!(boolean ) user.getProperty("isAdmin"))
                                                            Toast.makeText(UploadItemSellActivity.this, R.string.waiting_approval , Toast.LENGTH_LONG).show();
                                                        Intent resultIntent = new Intent();
                                                        setResult(Activity.RESULT_OK, resultIntent);
                                                        finish();

                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        progress.dismiss();
                                                        Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                                    }
                                                });
                                    }
                                    ///////////////
                                    else{
                                        progress.dismiss();
                                        Intent resultIntent = new Intent();
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                }
                                @Override
                                public void handleFault( BackendlessFault fault )
                                {
                                    progress.dismiss();
                                    Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                }
                            } );
                        }
                    ////////////////////////////////////////////
                }
                            @Override
                            public void handleFault( BackendlessFault fault ) {
                                progress.dismiss();
                                Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void deleteItem() {
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadItemSellActivity.this);
        builder.setMessage(getString(R.string.delete_item)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UploadItemSellActivity.this, getString(R.string.working), getString(R.string.deleting_item), true, false);
                        progress.show();
        Backendless.Persistence.of( Market.class ).findById( getIntent().getStringExtra("ItemID"), new AsyncCallback<Market>() {
            @Override
            public void handleResponse(Market response) {
                final Uri uri =  Uri.parse( response.getPicture() );
                Backendless.Persistence.of( Market.class ).remove( response, new AsyncCallback<Long>() {
                    public void handleResponse( Long response ) {
                        progress.setMessage(getString(R.string.deleting_image));
                        String[] segments = uri.getPath().split("/");
                        String idStr = segments[segments.length-1];
                        Backendless.Files.remove("myfiles/"+idStr, new AsyncCallback<Void>() {
                            @Override
                            public void handleResponse(Void response) {
                                progress.dismiss();
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                progress.dismiss();
                                Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                            }
                        });

                    }
                    public void handleFault( BackendlessFault fault ) {
                        progress.dismiss();
                        Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();                    }
                } );
            }
            @Override
            public void handleFault( BackendlessFault fault ) {
                progress.dismiss();
                Toast.makeText( UploadItemSellActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
            }
        });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void selectImage() {
//        final boolean result= fr.livelovecite.Uplaods.Utility.checkPermission(UploadItemSellActivity.this);
//        if (!result)
//            return;
        final Dialog dialog = new Dialog(UploadItemSellActivity.this);
        dialog.setContentView(R.layout.imagepicker_layout);
        dialog.setCancelable(true);
        dialog.show();

        TextView cameraSelected = dialog.findViewById(R.id.ss);
        TextView gallerySelected = dialog.findViewById(R.id.ddd);
        cameraSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userChoosenTask ="Take Photo";
                    cameraIntent();
                dialog.dismiss();
            }
        });

        gallerySelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userChoosenTask ="Choose from Library";
                    galleryIntent();
                dialog.dismiss();
            }
        });
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case fr.livelovecite.Uplaods.Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if(userChoosenTask.equals("Take Photo"))
//                        cameraIntent();
//                    else if(userChoosenTask.equals("Choose from Library"))
//                        galleryIntent();
//                    else break;
//                }
//                break;
//        }
//    }
    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),SELECT_FILE);
    }
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    private void onCaptureImageResult(Intent data) {

        if (data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            itemImage.setBackgroundResource(android.R.color.transparent);
            itemImage.setImageBitmap(imageBitmap);
            imageChanged=true;
        }
    }
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
                Uri imageURI = data.getData();
                Picasso.with(UploadItemSellActivity.this).load(imageURI).into(itemImage);
                itemImage.setBackgroundResource(android.R.color.transparent);
                imageChanged=true;
        }
    }

    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int maxWidth= itemImage.getWidth();
        int maxHeight = itemImage.getWidth();

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        user= Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getBaseContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
    }

}
