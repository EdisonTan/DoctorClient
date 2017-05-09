package com.example.administrator.myapplicationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.lippi.hsrecorder.pcm.NetworkService;
//
//import org.apache.http.Header;
//import org.apache.http.HeaderIterator;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.ProtocolVersion;
//import org.apache.http.StatusLine;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;


public class LoginActivityDoctor extends SuperActivity {

    EditText etName, etPwd;
    ImageView ivName, ivPwd;
    public static String name, pwd;
    TextView log, gotoreg;
    String state;
    private CheckBox mRememberPassword, mLoginAuto;
    SharedPreferences sp;
    private LoginReceiver loginReceiver;

    public static String LOGIN_SUCCESSFULLY=
            "com.lippi.hsrecorder.ui.LoginActivityDoctor.LOGIN_SUCCESSFULLY";
    public static String LOGIN_FAILED=
            "com.lippi.hsrecorder.ui.LoginActivityDoctor.LOGIN_FAILED";
    public static String LOGIN_FAILED_CONNECTION_ERROR=
            "com.lippi.hsrecorder.ui.LoginActivityDoctor.LOGIN_FAILED_CONNECTION_ERROR";

    private IntentFilter login_intentfilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.login);

        etName = (EditText) findViewById(R.id.name_et);
        ivName = (ImageView) findViewById(R.id.name_iv);
        initForm(etName, ivName);
        etPwd = (EditText) findViewById(R.id.pwd_et);
        ivPwd = (ImageView) findViewById(R.id.pwd_iv);
        initForm(etPwd, ivPwd);
        mRememberPassword = (CheckBox)findViewById(R.id.remember_password);
        mLoginAuto = (CheckBox)findViewById(R.id.login_auto);

        sp = getSharedPreferences("info", MODE_APPEND);
        String username = sp.getString("name", "");
        String password = sp.getString("password", "");

        boolean isRemember = sp.getBoolean("remember", false);
        boolean isAuto = sp.getBoolean("loginauto", false);

        if (isRemember) {
            etName.setText(username);
            etPwd.setText(password);
            mRememberPassword.setChecked(true);
        }

        if (isAuto) {
            mLoginAuto.setChecked(true);
            etName.setText(username);
            etPwd.setText(password);
            name = etName.getText().toString();
//            pwd = etPwd.toString();
            Intent intent = new Intent();
            intent.putExtra("account",username);
            intent.putExtra("password",password);
            intent.setAction(NetWorkService.LOGIN);
            sendBroadcast(intent);
//            startActivity(new Intent(LoginActivityDoctor.this, MainActivityDoctor.class));
        }
        //在这里启动网络服务。
        Intent intent = new Intent(LoginActivityDoctor.this, NetWorkService.class);
        startService(intent);

        //初始化intentfilter
        initIntentFilter();

        //注册广播接收
        loginReceiver = new LoginReceiver();
        registerReceiver(loginReceiver,login_intentfilter);

        log = (TextView) findViewById(R.id.log);
        log.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences.Editor editor = sp.edit();

                name = etName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(LoginActivityDoctor.this, "请输入登录账号", Toast.LENGTH_SHORT).show();
                    return;
                }
                pwd = etPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivityDoctor.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("account",name);
                intent.putExtra("password",pwd);
                intent.setAction(NetWorkService.LOGIN);
                sendBroadcast(intent);
//                login();

                editor.putString("name", name);
                editor.putString("password", pwd);
                if (mRememberPassword.isChecked()) {
                    editor.putBoolean("remember", true);
                }else {
                    editor.putBoolean("remember", false);
                }
                if (mLoginAuto.isChecked()){
                    editor.putBoolean("loginauto",true);
                }else {
                    editor.putBoolean("loginauto", false);
                }
                editor.commit();

            }

        });

        gotoreg = (TextView) findViewById(R.id.gotoreg);
        gotoreg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(LoginActivityDoctor.this, RegisterActivity.class));
            }

        });

        /*//监听记住密码多选框按钮事件
        mRememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mRememberPassword.isChecked()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("username", name);
                    editor.putString("password", pwd);
                    editor.apply();
                    //etName.setText(sp.getString("USER_NAME", ""));
                    //etPwd.setText(sp.getString("PASSWORD", ""));
                    sp.edit().putBoolean("ISCHECK", true).apply();

                }else {
                    sp.edit().putBoolean("ISCHECK", false).apply();

                }

            }
        });

        //监听自动登录多选框事件
        mLoginAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (mLoginAuto.isChecked()) {
                    sp.edit().putBoolean("AUTO_ISCHECK", true).commit();

                } else {
                    sp.edit().putBoolean("AUTO_ISCHECK", false).commit();
                }
            }
        });*/
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(loginReceiver);
    }

//    public void login() {
//        new Thread() {
//            public void run() {
//                HttpClient client = new DefaultHttpClient();
//                HttpPost post = new HttpPost("http://116.57.86.220/au/ci/index.php/Plogin");
//                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("patient_id", name));
//                params.add(new BasicNameValuePair("psword", pwd));
//
//                try {
//                    HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    post.setEntity(entity);
//                    HttpResponse response = new HttpResponse() {
//                        @Override
//                        public StatusLine getStatusLine() {
//                            return null;
//                        }
//
//                        @Override
//                        public void setStatusLine(StatusLine statusLine) {
//
//                        }
//
//                        @Override
//                        public void setStatusLine(ProtocolVersion protocolVersion, int i) {
//
//                        }
//
//                        @Override
//                        public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
//
//                        }
//
//                        @Override
//                        public void setStatusCode(int i) throws IllegalStateException {
//
//                        }
//
//                        @Override
//                        public void setReasonPhrase(String s) throws IllegalStateException {
//
//                        }
//
//                        @Override
//                        public HttpEntity getEntity() {
//                            return null;
//                        }
//
//                        @Override
//                        public void setEntity(HttpEntity httpEntity) {
//
//                        }
//
//                        @Override
//                        public Locale getLocale() {
//                            return null;
//                        }
//
//                        @Override
//                        public void setLocale(Locale locale) {
//
//                        }
//
//                        @Override
//                        public ProtocolVersion getProtocolVersion() {
//                            return null;
//                        }
//
//                        @Override
//                        public boolean containsHeader(String s) {
//                            return false;
//                        }
//
//                        @Override
//                        public Header[] getHeaders(String s) {
//                            return new Header[0];
//                        }
//
//                        @Override
//                        public Header getFirstHeader(String s) {
//                            return null;
//                        }
//
//                        @Override
//                        public Header getLastHeader(String s) {
//                            return null;
//                        }
//
//                        @Override
//                        public Header[] getAllHeaders() {
//                            return new Header[0];
//                        }
//
//                        @Override
//                        public void addHeader(Header header) {
//
//                        }
//
//                        @Override
//                        public void addHeader(String s, String s1) {
//
//                        }
//
//                        @Override
//                        public void setHeader(Header header) {
//
//                        }
//
//                        @Override
//                        public void setHeader(String s, String s1) {
//
//                        }
//
//                        @Override
//                        public void setHeaders(Header[] headers) {
//
//                        }
//
//                        @Override
//                        public void removeHeader(Header header) {
//
//                        }
//
//                        @Override
//                        public void removeHeaders(String s) {
//
//                        }
//
//                        @Override
//                        public HeaderIterator headerIterator() {
//                            return null;
//                        }
//
//                        @Override
//                        public HeaderIterator headerIterator(String s) {
//                            return null;
//                        }
//
//                        @Override
//                        public HttpParams getParams() {
//                            return null;
//                        }
//
//                        @Override
//                        public void setParams(HttpParams httpParams) {
//
//                        }
//                    };
//
//                    try{
//                        response = client.execute(post);
//                        Log.d("http","executed");
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        runOnUiThread(
//                                new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(LoginActivityDoctor.this,"连接超时", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                        );
//
//                    }
//                    Log.d("http","get a response");
//                    if (response.getStatusLine().getStatusCode() == 200) {
//                        StringBuilder sb = new StringBuilder();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//                        String s = br.readLine();
//                        for (; s != null; s = br.readLine()) {
//                            sb.append(s);
//                        }
////                        JSONObject json = new JSONObject(sb.toString());
////                        state = json.getString("state");
//
//                        final String msg = sb.toString();
//                        Log.d("Login",msg);
//                        //简直迷幻，这个应该是   登录
//                        if (msg.equals("登陆成功")){
////                            runOnUiThread(new Runnable() {
////                                public void run() {
////                                    Toast.makeText(LoginActivityDoctor.this, msg.toString(), Toast.LENGTH_SHORT).show();
////                                }
////                            });
//                            Intent intent = new Intent();
//                            intent.setAction(LoginActivityDoctor.LOGIN_SUCCESSFULLY);
//                            sendBroadcast(intent);
//                        }else{
//                            Intent intent = new Intent();
//                            intent.setAction(LoginActivityDoctor.LOGIN_FAILED);
//                            sendBroadcast(intent);
//                        }
//
//                    }
//                    else {
//                        Log.d("register", String.valueOf(response.getStatusLine().getStatusCode()));
//                    }
//                } catch (UnsupportedEncodingException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (ClientProtocolException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(LoginActivityDoctor.LOGIN_FAILED)){
                Toast.makeText(LoginActivityDoctor.this,"登录失败，用户名或密码错误", Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(LoginActivityDoctor.LOGIN_SUCCESSFULLY)){
                Toast.makeText(LoginActivityDoctor.this,"登录成功", Toast.LENGTH_SHORT).show();
                Intent intentToMain = new Intent(LoginActivityDoctor.this, MainActivityDoctor.class);
                intentToMain.putExtra("doctorId", LoginActivityDoctor.name);
                startActivity(intentToMain);
            }else if (intent.getAction().equals(LoginActivityDoctor.LOGIN_FAILED_CONNECTION_ERROR)){
                Toast.makeText(LoginActivityDoctor.this,"登录失败，请检查网络连接。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initIntentFilter(){
        login_intentfilter = new IntentFilter();
        login_intentfilter.addAction(LoginActivityDoctor.LOGIN_FAILED);
        login_intentfilter.addAction(LoginActivityDoctor.LOGIN_SUCCESSFULLY);
        login_intentfilter.addAction(LoginActivityDoctor.LOGIN_FAILED_CONNECTION_ERROR);
    }
}