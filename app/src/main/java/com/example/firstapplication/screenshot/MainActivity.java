package com.example.firstapplication.screenshot;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.graphics.PixelFormat.RGBA_8888;



public class MainActivity extends AppCompatActivity {
    Button btn;
    ConstraintLayout container;
    ImageView image;

    String[] required_permissions = new String[]{
            android.Manifest.permission.READ_MEDIA_IMAGES
};

    boolean is_storage_image_permitted=false;

    String TAG="Permission";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn=findViewById(R.id.btn);
        container=findViewById(R.id.container);
        image=findViewById(R.id.image);
// for permission to android 13
        if(!is_storage_image_permitted)
        {
            requestPermissionStorageImages();

        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap screershot=captureScreenShot(container);
                image.setImageBitmap(screershot);

                //
                //
                storeImageAsJPEGandShare(screershot);

            }
        });

    }
public Bitmap captureScreenShot(View view)
{
        Bitmap returnBitmap=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);

    Canvas canvas=new Canvas(returnBitmap);
    Drawable bgdrawable=view.getBackground();
    if(bgdrawable!=null)
        bgdrawable.draw(canvas);
    else
        canvas.drawColor(Color.WHITE);
    view.draw(canvas);
        return returnBitmap;

}

//== step 2
    public void storeImageAsJPEGandShare(Bitmap bitmap)
    {
        OutputStream outst;

        try {
            // scooped storage
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            {
                ContentResolver contentResolver=getContentResolver();
                ContentValues contentValues=new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Image_"+".jpg");

                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES+File.separator+"TestFolder");

                Uri imageUri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);


                outst=contentResolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outst);
                Objects.requireNonNull(outst);


                Toast.makeText(this,"Image is Save",Toast.LENGTH_LONG).show();


                // intent
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                share.putExtra(Intent.EXTRA_STREAM,imageUri);
                startActivity(Intent.createChooser(share,"Share Image"));

            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }











    public void  requestPermissionStorageImages()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, required_permissions[0]) == PackageManager.PERMISSION_GRANTED)
         {
            Log.d(TAG,required_permissions[0]+"Granted");
            is_storage_image_permitted=true;

        }else {
            required_permissions_launcher_storage_images.launch(required_permissions[0]);
        }
    }

    private ActivityResultLauncher<String> required_permissions_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted->{
                if (isGranted){
                    Log.d(TAG,required_permissions[0]+"Not Granted");
                    is_storage_image_permitted=true;

                }else {
                    Log.d(TAG,required_permissions[0]+"Not Granted");

                    is_storage_image_permitted=false;
                }
                    });

}
