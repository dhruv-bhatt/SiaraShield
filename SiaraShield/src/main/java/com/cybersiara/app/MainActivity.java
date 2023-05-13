package com.cybersiara.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    SwipeButton swipeButton;
    FrameLayout frame, frame_without_active;
    ImageView img_gif, captcha_image, img_refresh;
    EditText ed_captcha;
    AlertDialog alert;
    ProgressBar progress;
    String str_ip = "", str_id = "", str_width = "", str_height = "", str_name = "", RequestId = "", Visiter_Id = "",
            str_auth = "";
    CardView card_captcha;
    LinearLayout l_submit;
    TextView txt_privacy, txt_terms;
    String MASTER_URL = "";
    String AUTH_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cybersiara);

        swipeButton = findViewById(R.id.swipe_btn_1);
        frame = findViewById(R.id.frame_active);
        img_gif = findViewById(R.id.img_gif);
        card_captcha = findViewById(R.id.card_captcha);
        frame_without_active = findViewById(R.id.frame_without_active);
        l_submit = findViewById(R.id.l_submit);
        txt_privacy = findViewById(R.id.txt_privacy);
        txt_terms = findViewById(R.id.txt_terms);

        Intent intent = getIntent();
        MASTER_URL = intent.getStringExtra("master_key");
        AUTH_KEY = intent.getStringExtra("auth_key");


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        str_ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        str_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        str_width = String.valueOf(screenWidth);
        str_height = String.valueOf(screenHeight);

        str_name = Build.MODEL;

        if(MASTER_URL != null && AUTH_KEY != null){
            if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                new Login().execute();
            } else {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Master Key and Auth Key Not Found", Toast.LENGTH_LONG).show();
        }


        l_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                    new Validatetoken().execute();
                } else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
            }
        });

        txt_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.cybersiara.com/privacy"));
                startActivity(browserIntent);
            }
        });

        txt_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.cybersiara.com/terms"));
                startActivity(browserIntent);
            }
        });

        swipeButton.setOnActiveListener(new OnActiveListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onActive() {
                swipeButton.setVisibility(View.GONE);
                l_submit.setVisibility(View.GONE);
                frame_without_active.setVisibility(View.VISIBLE);
                if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                    new SubmitCaptcha().execute();
                } else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
//                swipeButton.setButtonBackgroundTint(getApplicationContext().getColor(R.color.green));
//                swipeButton.setButtonBackgroundImage(getApplicationContext().getDrawable(R.drawable.checkmark_white));
//                swipeButton.setActivated(false);
//                swipeButton.setInnerText(getApplicationContext().getString(R.string.verified));
//                swipeButton.setInnerTextColor(getApplicationContext().getColor(R.color.white));
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
        final View customLayout = getLayoutInflater().inflate(R.layout.popup_layout, null);

        alertDialog.setView(customLayout);
        alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

//        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 1000;
//        lp.height = 1300;
//        alert.getWindow().setAttributes(lp);

        ImageView img_close = (ImageView) customLayout.findViewById(R.id.img_close);
        captcha_image = (ImageView) customLayout.findViewById(R.id.img_captcha);
        ed_captcha = (EditText) customLayout.findViewById(R.id.ed_captcha);
        progress = (ProgressBar) customLayout.findViewById(R.id.progress_circular);
        Spinner spinner = (Spinner) customLayout.findViewById(R.id.spinner2);
        img_refresh = (ImageView) customLayout.findViewById(R.id.img_refresh);
        TextView txt_privacy = (TextView) customLayout.findViewById(R.id.txt_privacy);
        TextView txt_terms = (TextView) customLayout.findViewById(R.id.txt_terms);
        ImageView img_visibility = customLayout.findViewById(R.id.img_visiblity);
        ImageView img_menu = customLayout.findViewById(R.id.img_menu);

        img_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.cybersiara.com/accessibility"));
                startActivity(browserIntent);
            }
        });

        img_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, img_menu);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.accessibility) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                            browserIntent.setData(Uri.parse("https://www.cybersiara.com/accessibility"));
                            startActivity(browserIntent);
                            return true;
                        } else if (id == R.id.report_image) {
                            Intent browserIntent1 = new Intent(Intent.ACTION_VIEW);
                            browserIntent1.setData(Uri.parse("https://mycybersiara.com/ReportBug"));
                            startActivity(browserIntent1);
                            return true;
                        } else if (id == R.id.report_bug) {
                            Intent browserIntent2 = new Intent(Intent.ACTION_VIEW);
                            browserIntent2.setData(Uri.parse("https://mycybersiara.com/ReportBug"));
                            startActivity(browserIntent2);
                            return true;
                        }
                        return false;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        txt_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.cybersiara.com/privacy"));
                startActivity(browserIntent);
            }
        });

        txt_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.cybersiara.com/terms"));
                startActivity(browserIntent);
            }
        });

        img_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                    new GenerateCaptcha().execute();
                } else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
            new GenerateCaptcha().execute();
        } else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }

        String[] years = {"English","Hindi","Gujarati","Marathi"};
        ArrayAdapter<CharSequence> langAdapter = new ArrayAdapter<CharSequence>(MainActivity.this, R.layout.spinner_text, years );
        langAdapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(langAdapter);

        spinner.setSelection(0, true);
        View view = spinner.getSelectedView();
        ((TextView) view).setTextColor(Color.parseColor("#FFFFFF"));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)view).setTextColor(Color.parseColor("#FFFFFF"));
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                swipeButton.setVisibility(View.VISIBLE);
                l_submit.setVisibility(View.GONE);
                frame.setVisibility(View.GONE);
                frame_without_active.setVisibility(View.GONE);

            }
        });

        ed_captcha.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
               if (s.length() == 4){
                   if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                       new CaptchaVerify().execute();
                   } else {
                       Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                   }
               }
            }
        });
    }



    @SuppressLint("StaticFieldLeak")
    public class Login extends AsyncTask<String, Void, String> {

        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            Dialog.show();
            Dialog.setMessage(getResources().getString(R.string.please_wait));
            Dialog.setCancelable(false);
        }

        @SuppressLint("WrongThread")
        protected String doInBackground(String... arg0) {
            try {

                URL url = new URL(ApiConfig.Login_URL);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("MasterUrlId", MASTER_URL);
                postDataParams.put("RequestUrl", "com.cybersiara.app");
                postDataParams.put("BrowserIdentity", str_id);
                postDataParams.put("DeviceIp", str_ip);
                postDataParams.put("DeviceType", "Android");
                postDataParams.put("DeviceBrowser", "Chrome");
                postDataParams.put("DeviceName", str_name);
                postDataParams.put("DeviceHeight", str_height);
                postDataParams.put("DeviceWidth", str_width);

                Log.e("PostDataLogin", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Login Response", result);
            Dialog.dismiss();
            LoginResponse(result);

        }
    }

    private void LoginResponse(String response) {

        if (!response.equals("")) {
            try {
                JSONObject jsobjectcategory = new JSONObject(response);

                String message = jsobjectcategory.getString("Message");
                RequestId = jsobjectcategory.getString("RequestId");
                Visiter_Id = jsobjectcategory.getString("Visiter_Id");
                if (message.equals("success")){
                }else{
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class SubmitCaptcha extends AsyncTask<String, Void, String> {

        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        protected String doInBackground(String... arg0) {
            try {

                URL url = new URL(ApiConfig.SUBMIT_CAPTCHA_URL);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("MasterUrl", MASTER_URL);
                postDataParams.put("BrowserIdentity", str_id);
                postDataParams.put("DeviceIp", str_ip);
                postDataParams.put("DeviceName", str_name);
                postDataParams.put("VisiterId", Visiter_Id);
                postDataParams.put("RequestID", RequestId);
                postDataParams.put("second", "5");
                postDataParams.put("Protocol", "http");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Submit Captcha", result);
            SubmitCaptchaResponse(result);
        }
    }

    private void SubmitCaptchaResponse(String response) {

        if (!response.equals("")) {
            try {
                JSONObject jsobjectcategory = new JSONObject(response);

                String message = jsobjectcategory.getString("Message");
                str_auth = jsobjectcategory.getString("data");
                if (message.equals("success")){
                    swipeButton.setVisibility(View.GONE);
                    l_submit.setVisibility(View.VISIBLE);
                    frame_without_active.setVisibility(View.GONE);
                frame.setVisibility(View.VISIBLE);
                Glide.with(MainActivity.this)
                        .load(R.drawable.veri)
                        .apply(new RequestOptions()
                                .override(Target.SIZE_ORIGINAL)
                                .format(DecodeFormat.PREFER_ARGB_8888))
                        .into(new GifDrawableImageViewTarget(img_gif, 1));
                }else if (message.equals("fail")){
                    showAlertDialog();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class GenerateCaptcha extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            captcha_image.setVisibility(View.GONE);
        }

        @SuppressLint("WrongThread")
        protected String doInBackground(String... arg0) {
            try {

                URL url = new URL(ApiConfig.GENERATE_CAPTCHA_URL);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("MasterUrlId", MASTER_URL);
                postDataParams.put("RequestUrl", "com.cybersiara.app");
                postDataParams.put("BrowserIdentity", str_id);
                postDataParams.put("DeviceIp", str_ip);
                postDataParams.put("DeviceType", "Android");
                postDataParams.put("DeviceBrowser", "Chrome");
                postDataParams.put("DeviceName", str_name);
                postDataParams.put("DeviceHeight", str_height);
                postDataParams.put("DeviceWidth", str_width);
                postDataParams.put("VisiterId", Visiter_Id);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            GenerateCaptchaResponse(result);

        }
    }

    private void GenerateCaptchaResponse(String response) {

        if (!response.equals("")) {
            try {
                JSONObject jsobjectcategory = new JSONObject(response);

                String message = jsobjectcategory.getString("Message");
                String gif = jsobjectcategory.getString("HtmlFormate");
                if (message.equals("success")){
                    progress.setVisibility(View.GONE);
                    captcha_image.setVisibility(View.VISIBLE);
                    Glide.with(this).load(gif).into(captcha_image);
                }else{
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class CaptchaVerify extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {
        }

        @SuppressLint("WrongThread")
        protected String doInBackground(String... arg0) {
            try {

                URL url = new URL(ApiConfig.CAPTCHA_VERIFY_URL);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("MasterUrl", MASTER_URL);
                postDataParams.put("BrowserIdentity", str_id);
                postDataParams.put("DeviceIp", str_ip);
                postDataParams.put("DeviceType", "Android");
                postDataParams.put("DeviceName", str_name);
                postDataParams.put("VisiterId", Visiter_Id);
                postDataParams.put("UserCaptcha", ed_captcha.getText().toString());
                postDataParams.put("ByPass", "Netural");
                postDataParams.put("Timespent", "24");
                postDataParams.put("Protocol", "http");
                postDataParams.put("Flag", "1");
                postDataParams.put("second", "2");
                postDataParams.put("RequestID", RequestId);
                postDataParams.put("fillupsecond", "8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            CaptchaVerifyResponse(result);

        }
    }

    private void CaptchaVerifyResponse(String response) {

        if (!response.equals("")) {
            try {
                JSONObject jsobjectcategory = new JSONObject(response);

                String message = jsobjectcategory.getString("Message");
                String gif = jsobjectcategory.getString("HtmlFormate");
                str_auth = jsobjectcategory.getString("data");
                if (message.equals("success")){
                    alert.dismiss();
                   swipeButton.setVisibility(View.GONE);
                   frame_without_active.setVisibility(View.GONE);
                   frame.setVisibility(View.VISIBLE);
                   l_submit.setVisibility(View.VISIBLE);
                   Glide.with(MainActivity.this)
                           .load(R.drawable.veri)
                           .apply(new RequestOptions()
                                   .override(Target.SIZE_ORIGINAL)
                                   .format(DecodeFormat.PREFER_ARGB_8888))
                           .into(new GifDrawableImageViewTarget(img_gif, 1));
                    l_submit.setVisibility(View.VISIBLE);
                }else{
                    ed_captcha.setText("");

                    if (ConnectivityDetector.isConnectingToInternet(MainActivity.this)) {
                        new GenerateCaptcha().execute();
                    } else {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Validatetoken extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                URL url = new URL(ApiConfig.VALIDATE_TOKEN_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("ip", str_ip);
                urlConnection.setRequestProperty("Authorization", "Bearer "+str_auth);
                urlConnection.setRequestProperty("key", AUTH_KEY);

                int code = urlConnection.getResponseCode();

                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null)
                            result += line;
                    }
                    in.close();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                urlConnection.disconnect();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Check Get", result);
            ValidatetokenResponse(result);
            super.onPostExecute(result);
        }
    }

        private void ValidatetokenResponse(String response) {

        if (!response.equals("")) {
            try {
                JSONObject jsobjectcategory = new JSONObject(response);

                String message = jsobjectcategory.getString("Message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (message.equals("Verified")){

                }else{

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}