package study.iot.tb.demo_client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;
import study.iot.tb.demo_client.R;

public class RestFragment extends TabFragments {
    private static final String TAG = "RestFragment";
    private EditText mUsername;
    private EditText mPassword;
    private EditText mServer_address;
    private EditText mDevice_id_edittext;
    private EditText mRequest_body_edittext;
    private TextView mResponse;
    private Button mLogin;
    private Button mCreateDevice;
    private String loginToken="";
    public String mDeviceId;
    public String mDeivceToken;
    private String response;
    private String tokeninfo;
    public static final int UPDATE_TOKEN=1;
    public static final int UPDATE_RESPONSE=2;

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.rest_layout,container,false);
        mUsername = view.findViewById(R.id.username);
        mPassword = view.findViewById(R.id.password);
        mServer_address = view.findViewById(R.id.server);
        mLogin = view.findViewById(R.id.login_button);
        mCreateDevice=view.findViewById(R.id.create_device);
        mResponse = view.findViewById(R.id.response_info);
        mDevice_id_edittext=view.findViewById(R.id.device_id_edittext);
        mRequest_body_edittext=view.findViewById(R.id.request_body_edittext);
        String data1="{ \"Date\":\"today\",\"Time\":\"{\\\"Hour\\\":12" +
                ",\\\"Minutes\\\":56"+"}\"}";
        mRequest_body_edittext.setText(data1);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loginToken = doLogin();
                        Message message=new Message();
                        message.what=UPDATE_TOKEN;
                        handler.sendMessage(message);
                    }
                }).start();

            }
        });

        mCreateDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createDevice();
                        Message message=new Message();
                        message.what=UPDATE_RESPONSE;
                        handler.sendMessage(message);
                    }
                }).start();
            }
        });
        return view;
    }

    private Handler handler= new Handler(){
      public void handleMessage(Message msg){
          switch(msg.what){
              case UPDATE_TOKEN:
                  mResponse.setText(tokeninfo);
                  break;
              case UPDATE_RESPONSE:
                  mResponse.setText(response);
                  break;
              default:
                  break;
          }
      }
    };

    private String doLogin(){
        String username= String.valueOf(mUsername.getText());
        String password= String.valueOf(mPassword.getText());
        String server_address=String.valueOf(mServer_address.getText());
        try {
            URL url = new URL(server_address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String account = "{\"username\":" + "\""+username +"\""+ ",\"password\":"+"\""+password+"\"}";
            con.setRequestMethod("POST");
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoInput(true);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(account.getBytes("utf-8"));
            os.flush();
            os.close();
            int responseCode = con.getResponseCode();
            Log.i(TAG, "doLogin: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int length = -1;
                while ((length = is.read(buf)) != -1) {
                    baos.write(buf, 0, length);
                }
                String response = new String(baos.toByteArray(), "utf-8");
                Log.i(TAG, "getLoginToken response:" + response);
                int index= response.indexOf("refreshToken");
                Log.i(TAG, "getLoginToken: i============="+index);
                String response1= response.substring(10,index-3);
                Log.i(TAG, "response1: "+response1);
                loginToken=response1;
                SharedPreferences.Editor editor = this.getContext().getSharedPreferences("tokenInfo", MODE_PRIVATE).edit();
                editor.putString("login_token", loginToken);
                editor.commit();
                tokeninfo="Login successfully:LoginToken="+loginToken;
            }
            else if(responseCode==HttpURLConnection.HTTP_UNAUTHORIZED){
                tokeninfo="login info wrong!";
            }
            else{
                return loginToken="";
            }
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loginToken;
    }

    private String createDevice(){
        SharedPreferences token_info = this.getContext().getSharedPreferences("tokenInfo", MODE_PRIVATE);
        loginToken=token_info.getString("login_token", "");
        String server_address=String.valueOf(mServer_address.getText());
        String link_address= server_address.substring(0,25)+"/api/device";
        if(loginToken.equals("")){
            response="No Login Token!";
        }
        else{
            try {
                URL url = new URL(link_address);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String name = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                String devicedata = "{\"name\":" + "\""+name +"\""+ ",\"type\":\"default\"}";
                Log.i(TAG, "saveDevice: "+devicedata);
                con.setRequestMethod("POST");
                con.setConnectTimeout(60000);
                con.setReadTimeout(60000);
                String token="Bearer "+loginToken;
                con.setRequestProperty("X-Authorization",token);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(devicedata.getBytes("utf-8"));
                os.flush();
                os.close();
                int responseCode = con.getResponseCode();
                Log.i(TAG, "saveDevice: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = con.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int length = -1;
                    while ((length = is.read(buf)) != -1) {
                        baos.write(buf, 0, length);
                    }
                    response = new String(baos.toByteArray(), "utf-8");
                    Log.i(TAG, "response:" + response);
                    JSONObject content = new JSONObject(response);
                    JSONObject content_id = new JSONObject(content.getString("id"));
                    mDeviceId = content_id.getString("id");
                    Log.i(TAG, "saveDevice: mDeviceId="+mDeviceId);
                    mDeivceToken= getDeviceToken(mDeviceId,loginToken);
                    response = response+"Device_Token="+mDeivceToken;
                    Log.i(TAG, "saveDevice: mDeviceToken="+mDeivceToken);
                    SharedPreferences.Editor editor = this.getContext().getSharedPreferences("deviceInfo", MODE_PRIVATE).edit();
                    editor.putString("device_id", mDeviceId);
                    editor.putString("device_token", mDeivceToken);
                    editor.commit();
                }
                else if(responseCode==HttpURLConnection.HTTP_BAD_REQUEST){
                    response="Device already exist!\n";
                    SharedPreferences info = this.getContext().getSharedPreferences("deviceInfo", MODE_PRIVATE);
                    mDeviceId=info.getString("device_id", "");
                    mDeivceToken= getDeviceToken(mDeviceId,loginToken);
                    response= response+ "Device_id="+mDeviceId+"\n"+"Device_Token="+mDeivceToken;
                    Log.i(TAG, "ExsitDevice: mDeviceToken="+mDeivceToken);
                    SharedPreferences.Editor editor = this.getContext().getSharedPreferences("deviceInfo", MODE_PRIVATE).edit();
                    editor.putString("device_id", mDeviceId);
                    editor.putString("device_token", mDeivceToken);
                    editor.commit();
                }
                else if(responseCode==HttpURLConnection.HTTP_UNAUTHORIZED){
                    response="login token out of date,pls login again.";
                }
            }catch (JSONException e) {
                Log.i(TAG, "saveDevice error:" + e.getMessage());
                e.printStackTrace();}
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public String getDeviceToken(String mDeviceId,String loginToken) {
        String server_address=String.valueOf(mServer_address.getText());
        String link_address= server_address.substring(0,25)+"/api/device/"+mDeviceId+"/credentials";
        try {
            URL url = new URL(link_address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String token="Bearer "+loginToken;
            con.setRequestMethod("GET");
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.setRequestProperty("X-Authorization",token);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoInput(true);
            int responseCode = con.getResponseCode();
            Log.i(TAG, "getDeiceToken: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int length = -1;
                while ((length = is.read(buf)) != -1) {
                    baos.write(buf, 0, length);
                }
                String response = new String(baos.toByteArray(), "utf-8");
                Log.i(TAG, "getDeviceToken response:" + response);
                JSONObject content = new JSONObject(response);
                mDeivceToken = content.getString("credentialsId");
                Log.i(TAG, "device token =" + mDeivceToken);


            }
        }catch (JSONException e) {
            Log.i(TAG, "getDeviceToken error:" + e.getMessage());
            e.printStackTrace();}
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mDeivceToken;
    }


    @Override
    public void updateStatus(Intent intent) {
        Log.i(TAG, "updateStatus: Rest+++++++++");
    }
}

