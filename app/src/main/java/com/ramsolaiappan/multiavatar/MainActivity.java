package com.ramsolaiappan.multiavatar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    CheckBox isRandom;
    EditText avataridET;
    Button getBtn;
    ImageView avatar;

    String imageFileName = "";
    Bitmap bitmap = null;
    DownloadImage task;

    public class DownloadImage extends AsyncTask<String,Void,Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                Bitmap bitmap = ((BitmapDrawable) getDrawable(R.drawable.error)).getBitmap();
                return bitmap;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRandom = (CheckBox) findViewById(R.id.randomCheckBox);
        avataridET = (EditText) findViewById(R.id.avatarIdEditText);
        getBtn = (Button) findViewById(R.id.button);
        avatar = (ImageView) findViewById(R.id.imageView);

        downloadAndSetImage(generateRandomAvatarId());
        avataridET.setText(imageFileName.substring(14));

        isRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                avataridET.setText("");
                if(isChecked)
                    avataridET.setEnabled(false);
                else
                    avataridET.setEnabled(true);
            }
        });
    }

    protected void downloadAndSetImage(String avatarID)
    {
        task = new DownloadImage();
        try {
            bitmap = task.execute("https://api.multiavatar.com/"+avatarID+".png").get();
            avatar.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        imageFileName = "Multiavatar - "+avatarID;
    }

    public void getImage(View view)
    {
        ((Button) findViewById(R.id.button)).setEnabled(false);
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(),0);
        if(isRandom.isChecked())
        {
            String avatartId = generateRandomAvatarId();
            avataridET.setText(avatartId);
            downloadAndSetImage(avatartId);
        }
        else
        {
            if(avataridET.getText().toString().equals(""))
                Toast.makeText(MainActivity.this, "Enter some random text", Toast.LENGTH_SHORT).show();
            else
            {
                downloadAndSetImage(avataridET.getText().toString());
            }
        }
        ((Button) findViewById(R.id.button)).setEnabled(true);
    }

    public String generateRandomAvatarId()
    {
        Random random = new Random();
        String avatartId = "";
        for(int i = 0; i < 18; i++)
        {
            int alphaornum = random.nextInt(3); //0 - numeric ; 1 - Lower Alpha ; 2 - Upper Alpha
            if(alphaornum == 0)
                avatartId += String.valueOf(random.nextInt(10));
            else if(alphaornum == 1)
                avatartId += String.valueOf((char)(random.nextInt(26)+65));
            else if(alphaornum == 2)
                avatartId += String.valueOf((char)(random.nextInt(26)+97));
        }
        return avatartId;
    }
    public void downloadImage(View view)
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            saveImage();
        else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                saveImage();
            else
                Toast.makeText(MainActivity.this, "Allow permission to proceed", Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void saveImage()
    {
        BitmapDrawable drawable = (BitmapDrawable) avatar.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Multiavatar/");
            if(!dir.exists())
            {
                dir.mkdir();
            }
            File file = new File(dir,imageFileName+".png");
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(MainActivity.this, "Sucessfully Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void shareImage(View view)
    {
        saveImage();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Multiavatar/"+imageFileName+".png"));
        startActivity(Intent.createChooser(share,"Share Iamge"));
    }
    public void openWeb(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://multiavatar.com/"));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.website: openWeb(findViewById(R.id.website));break;
            case R.id.download: downloadImage(findViewById(R.id.download));break;
            case R.id.files:
                Snackbar.make(findViewById(R.id.fabDownload),Environment.getExternalStorageDirectory().getAbsolutePath()+"/Multiavatar/", BaseTransientBottomBar.LENGTH_LONG).show();
                break;
            case R.id.share:shareImage(findViewById(R.id.share));break;
        }
        return super.onOptionsItemSelected(item);
    }
}