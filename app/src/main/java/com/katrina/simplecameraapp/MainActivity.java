package com.katrina.simplecameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button mTakePictureButton;
    ImageView mCameraPicture;

    private static int TAKE_PICTURE = 0;
    private String mImageFilename = "temp_photo_file";
    private Uri mImageFileUri;

    private static final String PICTURE_TO_DISPLAY = "there is a picture to display";
    private boolean mIsPictureToDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mIsPictureToDisplay = savedInstanceState.getBoolean(PICTURE_TO_DISPLAY, false);
        }

        mCameraPicture = (ImageView) findViewById(R.id.camera_picture);
        mTakePictureButton = (Button) findViewById(R.id.take_picture_button);
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //Check to see if there is a camera on this device
                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                    //Create a File object from the specified filename
                    File imageFile = new File(Environment.getExternalStorageDirectory(), mImageFilename);
                    //And a URI that Android will use to find this file resource
                    //remember a URI specifies how to find a particular resource (in this case, on the file system)
                    //and where to find it (the file path)
                    mImageFileUri = Uri.fromFile(imageFile);
                    //Include this URI as an extra
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageFileUri);
                    //and then request the camera is launched
                    startActivityForResult(pictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(MainActivity.this, "Your device does not have a camera",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE) {
            mIsPictureToDisplay = true;
        } else {
            mIsPictureToDisplay = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && mIsPictureToDisplay) {
            Bitmap image = scaleBitmap();
            mCameraPicture.setImageBitmap(image);
            MediaStore.Images.Media.insertImage(getContentResolver(), image,
                    "SimpleCameraApp", "Photo taken by SimpleCameraApp");
        }
    }

    private Bitmap scaleBitmap() {
        //Step 1
        int imageViewHeight = mCameraPicture.getHeight();
        int imageViewWidth = mCameraPicture.getWidth();

        //Step 2
        BitmapFactory.Options bOptions = new BitmapFactory.Options();
        bOptions.inJustDecodeBounds = true;

        File file = new File(Environment.getExternalStorageDirectory(), mImageFilename);
        Uri imageFileUri = Uri.fromFile(file);
        String photoFilePath = imageFileUri.getPath();
        BitmapFactory.decodeFile(photoFilePath, bOptions);
        int pictureHeight = bOptions.outHeight;
        int pictureWidth = bOptions.outWidth;

        //Step 3
        int scaleFactor = Math.min(pictureHeight / imageViewHeight, pictureWidth / imageViewWidth);

        //Step 4
        bOptions.inJustDecodeBounds = false;
        bOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath, bOptions);
        return bitmap;
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putBoolean(PICTURE_TO_DISPLAY, mIsPictureToDisplay);
    }
}
