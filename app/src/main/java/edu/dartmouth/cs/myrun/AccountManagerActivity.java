package edu.dartmouth.cs.myrun;

import java.io.File;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;

import android.widget.Button;
import android.widget.RadioButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.soundcloud.android.crop.Crop;

public abstract class AccountManagerActivity extends AppCompatActivity {

    private static final String AVATAR_URI = "avatar_uri";

    private static final int CAMERA_REQUEST = 0;
    public static final int PICK_IMAGE = 1;

    protected FirebaseAuth mAuth;


    private Bitmap rotatedBitmap;
    protected Uri avatarUri;

    private Uri mImageCaptureUri;

    Button mChange;
    RadioButton mFemale;
    RadioButton mMale;
    ImageView mAvatar;
    EditText mEmail;
    EditText mName;
    EditText mPassword;
    EditText mPhone;
    EditText mMajor;
    EditText mDartmouthClass;
    RadioGroup mRadioGroup;
    private boolean isTakenFromCamera = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        checkPermissions();


        mChange = findViewById(R.id.change_button);
        mFemale = findViewById(R.id.female_button);
        mMale = findViewById(R.id.male_button);
        mAvatar = findViewById(R.id.imageView);
        mEmail = findViewById(R.id.reg_edit_email);
        mName = findViewById(R.id.reg_name);
        mPassword = findViewById(R.id.reg_password);
        mPhone = findViewById(R.id.phone);
        mMajor = findViewById(R.id.major);
        mDartmouthClass = findViewById(R.id.dartmouth_class);
        mRadioGroup = findViewById(R.id.radioGender);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("AvatarView", "activity on start");
        //check if user is signed in (non-null) and update UI accordingly

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("AvatarView", "activity on resume");
        if (avatarUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), avatarUri);
                mAvatar.setImageBitmap(imageOreintationValidator(bitmap, avatarUri.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean checkInputValidation() {
        boolean result = true;

        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();


        if (TextUtils.isEmpty(name)) {
            mName.setError("This field is required");
            result = false;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("This field is required");
            result = false;
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmail.setError("invalid input");
                result = false;
            }
        }

        if (password.length() < 6) {
            if (TextUtils.isEmpty(password)) {
                mPassword.setError("This field is required");
                result = false;
            } else {
                mPassword.setError("The password needs more than 6 characters");
                result = false;
            }
        }
        return result;
    }



    @Override
    public boolean onSupportNavigateUp() { //actionbar左上角的箭头
        finish();
        return super.onSupportNavigateUp();
    }


    public void changeAvatar(View view) {

        final String[] items = new String[] {
                getString(R.string.take_from_camera),
                getString(R.string.select_from_gallery)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.profile_pic_picker);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals(getString(R.string.take_from_camera))) {
                    // open camera
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

                    // Construct temporary image path and name to save the taken
                    // photo
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                    mImageCaptureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);//文件存储路径

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intent.putExtra("return-data", true);
                    isTakenFromCamera = true;

                    startActivityForResult(intent, CAMERA_REQUEST);
                }
                else {
                    // open gallary
                    isTakenFromCamera = false;
                    mImageCaptureUri = null;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            beginCrop(mImageCaptureUri);
            //mAvatar.setImageBitmap(photo);
        }
        else if (requestCode == PICK_IMAGE && resultCode ==
                RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            //InputStream inputStream = getContentResolver().openInputStream(data.getData());
            //BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            //Bitmap photo = BitmapFactory.decodeStream(bufferedInputStream);
            mImageCaptureUri = data.getData();
            beginCrop(mImageCaptureUri);
            //mAvatar.setImageBitmap(photo);

        }
        else if(requestCode == Crop.REQUEST_CROP){
            handleCrop(resultCode, data);

            // Delete temporary image taken by camera after crop.
            if (isTakenFromCamera) {
                File f = new File(mImageCaptureUri.getPath());
                if (f.exists())
                    f.delete();
            }
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            avatarUri = Crop.getOutput(result);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), avatarUri);
                mAvatar.setImageBitmap(imageOreintationValidator(bitmap, avatarUri.getPath()));
                String filename = getFilesDir().getPath() + File.separator + UUID.randomUUID().toString() + ".png";
                FileOutputStream out = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                avatarUri = Uri.fromFile(new File(filename));

            }catch (Exception e){
                Log.d("Error", "error");
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    // code to handle image orientation issue -- sometimes the orientation is not right on the imageview
    // https://github.com/jdamcd/android-crop/issues/258
    protected Bitmap imageOreintationValidator(Bitmap bitmap, String path) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            rotatedBitmap = null;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;

                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Code to check for runtime permissions.
     */
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        }else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)||shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important for the app.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);

                        }
                    });
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
                }else{
                    //Never ask again and handle your app without permission.
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.e("CheckAvatar", "activity onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        avatarUri = savedInstanceState.getParcelable(AVATAR_URI);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e("CheckAvatar", "activity onSaveInstanceState");
        outState.putParcelable(AVATAR_URI, avatarUri);
        super.onSaveInstanceState(outState);

    }

}
