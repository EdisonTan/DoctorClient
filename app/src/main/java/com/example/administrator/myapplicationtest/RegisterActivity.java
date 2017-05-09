package com.example.administrator.myapplicationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.lippi.hsrecorder.pcm.NetworkService;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends SuperActivity {

    EditText etName, etPwd, etEnsure;
    ImageView ivName, ivPwd, ivEnsure;
    String name, pwd, ensure;
    TextView register,identifying_code_question;
    String state;
    private EditText patientName, age, caseHistory, gender;
    private String mPatientName, mAge, mCaseHistory, mGender,mPhoneNumber,mAddress;
    public String username;
    private String identifyCodeQuestion;

    public static String REGIST_SUCCESSFULLY=
            "com.lippi.hsrecorder.ui.RegisterActivity.doctor.REGIST_SUCCESSFULLY";
    public static String REGIST_FAILED=
            "com.lippi.hsrecorder.ui.RegisterActivity.doctor.REGIST_FAILED";
    public static String REQUIRE_HOSPITAL=
            "com.lippi.hsrecorder.ui.RegisterActivity.doctor.REQUIRE_HOSPITAL";

    private IntentFilter register_intentfilter;
    private RegisterBroadcasrReceiver registerBroadcasrReceiver;
    public static List<Map<String,String>> hospitalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reg);

        hospitalList = new ArrayList<>();

        //简易验证码生成。
        identifyCodeQuestion = createRandomIdentifyCode();
        identifying_code_question = (TextView)findViewById(R.id.identifying_code_question);
        identifying_code_question.setText(identifyCodeQuestion);

        etName = (EditText) findViewById(R.id.name_etr);
        ivName = (ImageView) findViewById(R.id.name_ivr);
        initForm(etName, ivName);
        etPwd = (EditText) findViewById(R.id.pwd_etr);
        ivPwd = (ImageView) findViewById(R.id.pwd_ivr);
        initForm(etPwd, ivPwd);
        etEnsure = (EditText) findViewById(R.id.ensure_et);
        ivEnsure = (ImageView) findViewById(R.id.ensure_iv);
        initForm(etEnsure, ivEnsure);
//        patientName = (EditText) findViewById(R.id.name_of_patient_reg);
//        age = (EditText) findViewById(R.id.age_of_patient_reg);
//        caseHistory = (EditText) findViewById(R.id.case_history_reg);
//        gender = (EditText) findViewById(R.id.gender_reg);

        findViewById(R.id.register_cancle).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent activity_intent = new Intent(RegisterActivity.this,LoginActivityDoctor.class);
//                        startActivity(activity_intent);
                        finish();
                    }
                }
        );

        Intent queryHospitalIntent = new Intent();
        queryHospitalIntent.setAction(NetWorkService.REQUIRE_HOSPITAL);
        sendBroadcast(queryHospitalIntent);

        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                name = etName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Long.parseLong(name);
                    if (name.length() != 11)
                        throw new Exception("号码长度有误");
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "请输入正确手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }


                pwd = etPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    Long.parseLong(pwd);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "暂时仅接受纯数字的密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                ensure = etEnsure.getText().toString();
                if (TextUtils.isEmpty(ensure)) {
                    Toast.makeText(RegisterActivity.this, "请再次确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pwd.equals(ensure)) {
                    Toast.makeText(RegisterActivity.this, "确认密码和输入密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!((EditText)findViewById(R.id.identifying_code_answer)).getText().toString().equals(identifyCodeQuestion)){
                    Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    //同时更新验证码
                    identifyCodeQuestion = createRandomIdentifyCode();
                    identifying_code_question.setText(identifyCodeQuestion);
                    return;
                }

                String doctorName = ((EditText)findViewById(R.id.doctorName)).getText().toString();
                if (TextUtils.isEmpty(doctorName)){
                    Toast.makeText(RegisterActivity.this, "请填写您的姓名", Toast.LENGTH_SHORT).show();
                    return;
                }
                String hospitalName = ((Spinner)findViewById(R.id.hospitalSpinner)).getSelectedItem().toString();
                if (hospitalName.equals("请选择所属医院")){
                    Toast.makeText(RegisterActivity.this, "请选择您所属的医院单位", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Map<String,String> hospitalMap:
                        RegisterActivity.hospitalList
                     ) {
                    if (hospitalMap.containsValue(hospitalName)){
                        Intent intent = new Intent();
                        intent.setAction(NetWorkService.REGIST);
                        intent.putExtra("doctor_id",name);
                        intent.putExtra("psword",pwd);
                        intent.putExtra("name",doctorName);
                        intent.putExtra("hospital_id",hospitalMap.get("hospital_id"));
                        sendBroadcast(intent);
                        Log.d("register","所有注册信息都合法");
                    }
                }

                SharedPreferences.Editor editor = getSharedPreferences("info", MODE_APPEND).edit();

                editor.putString(name + "doctorName",doctorName);
                editor.putString(name + "hospitalName",hospitalName);
                editor.apply();
//                if (((EditText)findViewById(R.id.name_of_patient_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入姓名", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (((EditText)findViewById(R.id.age_of_patient_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入年龄", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (((EditText)findViewById(R.id.case_history_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入病历号", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (((EditText)findViewById(R.id.gender_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入性别", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (((EditText)findViewById(R.id.phone_number_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (((EditText)findViewById(R.id.address_reg)).getText().toString().isEmpty()){
//                    Toast.makeText(RegisterActivity.this, "请输入联系地址", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                mPatientName = patientName.getText().toString();
//                /*if (TextUtils.isEmpty(mPatientName)) {
//                    Toast.makeText(RegisterActivity.this, "请输入患者姓名", Toast.LENGTH_SHORT).show();
//                    goback;
//                }*/
//                mAge = age.getText().toString();
//                /*if (TextUtils.isEmpty(mAge)) {
//                    Toast.makeText(RegisterActivity.this, "请输入患者年龄", Toast.LENGTH_SHORT).show();
//                    goback;
//                }*/
//                mCaseHistory = caseHistory.getText().toString();
//                /*if (TextUtils.isEmpty(mCaseHistory)) {
//                    Toast.makeText(RegisterActivity.this, "请输入病历号", Toast.LENGTH_SHORT).show();
//                    goback;
//                }*/
//                mGender = gender.getText().toString();
//                /*if (TextUtils.isEmpty(mGender)) {
//                    Toast.makeText(RegisterActivity.this, "请输入患者性别", Toast.LENGTH_SHORT).show();
//                    goback;
//                }*/
//                //register();
//
//                mPhoneNumber = ((EditText)findViewById(R.id.phone_number_reg)).getText().toString();
//                mAddress = ((EditText)findViewById(R.id.address_reg)).getText().toString();


                //根据不同用户名来构建数据库。
//                UserInformationDatabase userInformationDatabase =
//                        new UserInformationDatabase(RegisterActivity.this,name + MainActivityDoctor.USER_DATABASE_NAME,1);
//
//                Log.d("information",name + MainActivityDoctor.USER_DATABASE_NAME);
//
//                ContentValues values = new ContentValues();
//                values.put("name",mPatientName);
//                values.put("gender",mGender);
//                values.put("age",mAge);
//                values.put("phoneNumber",mPhoneNumber);
//                values.put("address",mAddress);
//                if (userInformationDatabase.
//                        getWritableDatabase().
//                        insert("personal_information",null,values) != -1);{
//                    Log.d("register","建立个人数据库完成（但未初始化）");
//                }


//                UserInformationDatabase userInformationDatabase =
//                        new UserInformationDatabase(RegisterActivity.this,name + MainActivityDoctor.USER_DATABASE_NAME,1);
//
//                Log.d("information",name + MainActivityDoctor.USER_DATABASE_NAME);
//
//                ContentValues values = new ContentValues();
//                values.put("name","");
//                values.put("gender","");
//                values.put("age","");
//                values.put("phoneNumber",name);
//                values.put("address","");
//                if (userInformationDatabase.
//                        getWritableDatabase().
//                        insert("personal_information",null,values) != -1);{
//                    Log.d("register","建立个人数据库完成（但未初始化）");
//                }


            }
        });
        saveLoginInfo(getApplicationContext(), name, pwd);

        //init the receiver at here.
        registerBroadcasrReceiver = new RegisterBroadcasrReceiver();
        init_intentfilter();
        registerReceiver(registerBroadcasrReceiver,register_intentfilter);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(registerBroadcasrReceiver);
    }

//    public void register() {
//        new Thread() {
//            public void run() {
//                HttpClient client = new DefaultHttpClient();
//                HttpPost post = new HttpPost("http://116.57.86.220/au/ci/index.php/Register");
//                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("patient_id", name));
//                params.add(new BasicNameValuePair("psword", pwd));
//                params.add(new BasicNameValuePair("psword_confirm", ensure));
//                //params.add(new BasicNameValuePair("mAge", mAge));
//                //params.add(new BasicNameValuePair("mCaseHistory", mCaseHistory));
//                //params.add(new BasicNameValuePair("mGender", mGender));
//                try {
//
//                    HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    post.setEntity(entity);
//                    HttpResponse response = client.execute(post);
//                    if (response.getStatusLine().getStatusCode() == 200) {
//
//                        StringBuilder sb = new StringBuilder();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//                        String s = br.readLine();
//                        for (; s != null; s = br.readLine()) {
//                            sb.append(s);
//                        }
//                        String js = sb.toString();
//                        JSONObject json = new JSONObject(js);
//                        state = json.getString("state");
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                Toast.makeText(RegisterActivity.this, state, Toast.LENGTH_SHORT).show();
//                            }
//                        });
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
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    /*private boolean register(String newusername, String newpassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "insert into User(username, password) values(?,?)";
        Object obj[] = {newusername, newpassword};
        db.execSQL(sql, obj);
        goback true;
    }*/
    public static void saveLoginInfo(Context context, String username, String password) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", context.MODE_APPEND);
        SharedPreferences.Editor editor = sharedPre.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
    }


    private void init_intentfilter(){
        register_intentfilter = new IntentFilter();
        register_intentfilter.addAction(RegisterActivity.REGIST_SUCCESSFULLY);
        register_intentfilter.addAction(RegisterActivity.REGIST_FAILED);
        register_intentfilter.addAction(RegisterActivity.REQUIRE_HOSPITAL);
    }

    public class RegisterBroadcasrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(RegisterActivity.REGIST_FAILED)){
                if (intent.getStringExtra("feedback").equals("-1")){
                    Toast.makeText(RegisterActivity.this,"连接服务器失败，请检查网络连接。",Toast.LENGTH_LONG).show();
                }else if (intent.getStringExtra("feedback").equals("2")){
                    Toast.makeText(RegisterActivity.this,"注册失败：" + "该账号已被注册",Toast.LENGTH_LONG).show();
                }


            }else if (intent.getAction().equals(RegisterActivity.REGIST_SUCCESSFULLY)){
                Toast.makeText(RegisterActivity.this,"注册成功", Toast.LENGTH_SHORT).show();
//                Intent activity_intent = new Intent(RegisterActivity.this,LoginActivityDoctor.class);
//                startActivity(activity_intent);
                //结束这个activity，其实应该不用finish也可以的，我们的login是singletask，召唤他会把这个清掉。
                finish();
            }else if (intent.getAction().equals(RegisterActivity.REQUIRE_HOSPITAL)){
                //在这里填充spinner
                ((Spinner)findViewById(R.id.hospitalSpinner)).setAdapter(fillDoctorSpinner());
                Log.d("spinner","医院名称填充完毕");
            }
        }
    }

    private String createRandomIdentifyCode(){
        Random random = new Random();
        return String.valueOf(random.nextInt(10)) +
                String.valueOf(random.nextInt(10)) +
                String.valueOf(random.nextInt(10)) +
                String.valueOf(random.nextInt(10));
    }

    private ArrayAdapter fillDoctorSpinner(){
        //这个adapter还要绑定view的
        List<String> hospitalList = new ArrayList<>();
        hospitalList.add("请选择所属医院");
        for (Map<String,String> hospitalMap:
                RegisterActivity.hospitalList
             ) {
            hospitalList.add(hospitalMap.get("hospital_name"));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this,R.layout.hospital_list,R.id.hospitalTextView,hospitalList);
        return adapter;
    }
}