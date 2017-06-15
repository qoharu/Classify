package id.innovable.classify;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.attr.type;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    private static final String IMAGE_DIRECTORY_NAME = "classify";
    private Uri fileUri;
    private ImageView imgPreview;
    private TextView txt_res;
    private VisualRecognition service;
    private View blah;
    private SendMagic magic;
    private String hasilnyo;
    private JSONObject jsonres;
    Bitmap compressedimg;
    TextView textView;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    RecyclerView.Adapter recyclerViewAdapter;
    RecyclerView.LayoutManager recylerViewLayoutManager;
    Context context;
    List<Classification> contents;
    Intent intent;
    File filenya;
    Uri ret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestRuntimePermission();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
        service.setApiKey("510fba8044c2fd4720188107e7e1ee9e393439b0");

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        imgPreview = (ImageView) findViewById(R.id.imageViewplaces);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
                blah = new View(MainActivity.this);
                blah = view;

            }
        });
        contents = new ArrayList<Classification>();
        context = getApplicationContext();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_content);
        recylerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recylerViewLayoutManager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void captureImage() {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, 100);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    public Uri getOutputMediaFileUri(int type) {
        filenya = getOutputMediaFile(type);
        if (Build.VERSION.SDK_INT >= 24) {
            ret = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileProvider",
                    filenya);
        }else{
            ret = Uri.fromFile(getOutputMediaFile(type));
        }

        return ret;

    }
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            try {
                mediaStorageDir.mkdirs();
            }catch (Exception e){
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                e.printStackTrace();
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }
    public void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("asd","Permission is granted");
            } else {

                Log.v("asd","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

        private void previewCapturedImage() {
            try {
                Bitmap asdb;

                imgPreview.setVisibility(View.VISIBLE);
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 8;

                asdb = BitmapFactory.decodeFile(filenya.getPath(),
                        options);
//                }
                final Bitmap bitmap = asdb;

                compressedimg = getResizedBitmap(bitmap,1080);
                imgPreview.setImageBitmap(bitmap);
                magic = new SendMagic();
                magic.execute();

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }



    public class SendMagic extends AsyncTask<Void, String, Void> {
        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
        private ClassifyImagesOptions opsi;
        private VisualClassification hasil;

        @Override
        protected void onPreExecute() {
            txt_res = (TextView) findViewById(R.id.txt_res);
            mProgressDialog.setMessage("Applying spell words...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressNumberFormat(null);
            mProgressDialog.setProgressPercentFormat(null);
            mProgressDialog.show();
            if (!contents.isEmpty()){
                contents.clear();
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            //create a file to write bitmap data
            File f = new File(context.getCacheDir(), "cache.png");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

//Convert bitmap to byte array
            Bitmap bitmap = compressedimg;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


//            opsi = new ClassifyImagesOptions.Builder()
//                    .images(new File(fileUri.getPath()))
//                    .build();
            opsi = new ClassifyImagesOptions.Builder()
                    .images(f)
                    .build();
            try {
                hasil = service.classify(opsi).execute();
            }catch (Exception e){
                Snackbar.make( blah, e.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.e("watson error: ",e.toString());
            }finally {
                System.out.println(hasil);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            try {
                System.out.println(hasil);

                jsonres = new JSONObject(String.valueOf(hasil));
                JSONArray res_images = jsonres.getJSONArray("images");
                JSONObject res_img = res_images.getJSONObject(0);
                JSONArray res_classifiers = res_img.getJSONArray("classifiers");
                JSONObject res_class = res_classifiers.getJSONObject(0);
                JSONArray res_classrray = res_class.getJSONArray("classes");

                hasilnyo = "";

                for (int i = 0; i < res_classrray.length(); i++) {
                    JSONObject d = res_classrray.getJSONObject(i);
//                    hasilnyo = hasilnyo+d.getString("class")+" ("+d.getString("score")+")\n";
                    Classification hasil = new Classification(d.getString("class"),d.getDouble("score"));
                    contents.add(hasil);
                }
                recyclerViewAdapter = new ClassiAdapter(context, contents);
                recyclerView.setAdapter(recyclerViewAdapter);
//                txt_res.setText(hasilnyo);

            }catch (Exception e){
                Log.d("Watson:",e.toString());
            }

        }
    }
}
