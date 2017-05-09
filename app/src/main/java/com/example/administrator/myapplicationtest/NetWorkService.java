package com.example.administrator.myapplicationtest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static com.example.administrator.myapplicationtest.MainActivityDoctor.userInformation;

/**
 * Created by Administrator on 2017/4/4 0004.
 */

public class NetWorkService extends Service {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private JSONArray array;
    public static Boolean isAccessingData;
//    private final String server_url = "http://116.57.86.220/au/ci/index.php/QueryOrder";

    private final String doctorQueryOrderInterface = "http://116.57.86.220/au/ci/index.php/QueryOrderByDoc";
    private final String doctorLoginInterface = "http://116.57.86.220/au/ci/index.php/Dlogin";
    private final String doctorRegisterInterface = "http://116.57.86.220/au/ci/index.php/DoctorRegister";
    private final String queryHospitalInterface = "http://116.57.86.220/au/ci/index.php/QueryHospital";
    private final String doctorSubmitResultInterface = "http://116.57.86.220/au/ci/index.php/UploadResult";
    private final String doctorQueryPatientInformationInterface = "http://116.57.86.220/au/ci/index.php/QueryPatientInf";
    private final String doctorUpdateInformationInterface = "http://116.57.86.220/au/ci/index.php/UpdataDoctor";
    private final String queryDoctorInformationInterface = "http://116.57.86.220/au/ci/index.php/QueryDoctorInf";


    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    public static String REQUIRE_DATA=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.REQUIRE_DATA";
    public static String SUBMIT_RESULT=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.SUBMIT_RESULT";
    public static String DOWNLOAD_MEDIA=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.DOWNLOAD_MEDIA";
    public static String MEDIA_HTTP_URL=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.MEDIA_HTTP_URL";
    public static String MEDIA_SAVED_AS_NAME=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.MEDIA_SAVED_AS_NAME";
    public static String MEDIA_SAVED_AS_FILE_PATH=
            "com.example.administrator.myapplicationtest.NetWorkService.doctor.MEDIA_SAVED_AS_FILE_PATH";
    public static String LOGIN=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.LOGIN";
    public static String REGIST=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.REGIST";
    public static String REQUIRE_HOSPITAL=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.REQUIRE_HOSPITAL";
    public static String UPLOAD_RESULT=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.UPLOAD_RESULT";
    public static String QUERY_INFORMATION=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.QUERY_INFORMATION";
    public static String UPDATE_DOCTOR_INFORMATION=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.UPDATE_DOCTOR_INFORMATION";
    public static String QUERY_DOCTOR_INFORMATION=
            "com.lippi.hsrecorder.pcm.NetWorkService.doctor.QUERY_DOCTOR_INFORMATION";

    @Override
    public IBinder onBind(Intent arg0){
        Log.d("Network Service","Service has been bind.");
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("Network Service","Service has been created.");
        functionAddIntentFilter();
        broadcastReceiver = new NetWorkService.networkReceiver();
        registerReceiver(broadcastReceiver,intentFilter);
        //mediaPlayer = new MediaPlayer();
        isAccessingData = false;

    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("Network Service","Service is started.");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        Log.d("Network Service","Service has been destroyed.");
    }

    public class networkReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(final Context context, final Intent intent){
            if (intent.getAction().equals(NetWorkService.REQUIRE_DATA)){
                Log.d("Network Service","require command received.");
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if (intent.getStringExtra("local_latest_ordernumber")==null) {
                                        Log.d("fdsa","motherfucker");

                                        getOrders("0",
                                                "0",
                                                intent.getStringExtra("doctorId"),
                                                intent.getIntExtra("page", 0));
                                    }
                                    else {
                                        Log.d("fdsa","damn it");
                                        getOrders(intent.getStringExtra("local_latest_ordernumber"),
                                                intent.getStringExtra("local_oldest_ordernumber"),
                                                intent.getStringExtra("doctorId"),
                                                intent.getIntExtra("page", 0));
                                    }

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                ).start();

            }
            else if (intent.getAction().equals(NetWorkService.SUBMIT_RESULT)){
                    Log.d("Network Service","Submit");

                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Intent intentToReturnResultState = new Intent();
                                    intentToReturnResultState.setAction(MainActivityDoctor.SUBMIT_RESULT_FEEDBACK);
                                    if (submitResult(intent.getStringExtra("order_id"),intent.getStringExtra("is_diagnosed"),intent.getStringExtra("result"))){
                                        intentToReturnResultState.putExtra("feedbackState","1");
                                        sendBroadcast(intentToReturnResultState);
                                    } else {
                                        intentToReturnResultState.putExtra("feedbackState","0");
                                        sendBroadcast(intentToReturnResultState);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                ).start();




            }
            else if (intent.getAction().equals(NetWorkService.DOWNLOAD_MEDIA)){
                Log.d("Network Service","DOWNLOAD_MEDIA");

                File file1 = new File(intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_FILE_PATH));
                if (!file1.exists()){
                    Log.d("create dir",file1.getPath());
                    //路径不存在要创建路径。mkdirs
                    if (file1.mkdirs()){
                        File file = new File(
                                intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_FILE_PATH) + "/" +
                                        intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_NAME)
                        );
                        if (!file.exists()){
                            try{
                                okHttpDownload(
                                        intent.getStringExtra(NetWorkService.MEDIA_HTTP_URL),
                                        intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_FILE_PATH),
                                        intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_NAME)
                                );
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Intent file_intent = new Intent();
                            file_intent.setAction(MainActivityDoctor.FILE_EXISTING_STATE);
                            sendBroadcast(file_intent);
                        }
                    }else{
                        Log.d("create dir","创建路径失败");
                    }
                }else{
                    File file = new File(
                            intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_FILE_PATH) + "/" +
                                    intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_NAME)
                    );
                    //如果文件存在我们就不用去下载了。
                    if (!file.exists()){
                        try{
                            okHttpDownload(
                                    intent.getStringExtra(NetWorkService.MEDIA_HTTP_URL),
                                    intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_FILE_PATH),
                                    intent.getStringExtra(NetWorkService.MEDIA_SAVED_AS_NAME)
                            );
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Intent file_intent = new Intent();
                        file_intent.setAction(MainActivityDoctor.FILE_EXISTING_STATE);
                        sendBroadcast(file_intent);
                    }
                }







            }
            else if (intent.getAction().equals(NetWorkService.LOGIN)){
                final Intent intentForLogin = new Intent();

                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if (login(intent.getStringExtra("account"),intent.getStringExtra("password"))){
                                        intentForLogin.setAction(LoginActivityDoctor.LOGIN_SUCCESSFULLY);
                                        sendBroadcast(intentForLogin);
                                    }else{
                                        intentForLogin.setAction(LoginActivityDoctor.LOGIN_FAILED);
                                        sendBroadcast(intentForLogin);
                                    }

                                }catch (Exception e){
                                    e.printStackTrace();
                                    intentForLogin.setAction(LoginActivityDoctor.LOGIN_FAILED_CONNECTION_ERROR);
                                    sendBroadcast(intentForLogin);
                                }
                            }
                        }
                ).start();
            }
            else if (intent.getAction().equals(NetWorkService.REGIST)){

                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if (register(
                                            intent.getStringExtra("doctor_id"),
                                            intent.getStringExtra("psword"),
                                            intent.getStringExtra("name"),
                                            intent.getStringExtra("hospital_id")
                                    )){
                                        Intent registerIntent = new Intent();
                                        registerIntent.putExtra("feedback","1");
                                        registerIntent.setAction(RegisterActivity.REGIST_SUCCESSFULLY);
                                        sendBroadcast(registerIntent);
                                    }else {
                                        Intent registerIntent = new Intent();
                                        registerIntent.putExtra("feedback","2");
                                        registerIntent.setAction(RegisterActivity.REGIST_FAILED);
                                        sendBroadcast(registerIntent);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();

                                    Intent registerIntent = new Intent();
                                    registerIntent.putExtra("feedback","-1");
                                    registerIntent.setAction(RegisterActivity.REGIST_FAILED);
                                    sendBroadcast(registerIntent);

                                }
                            }
                        }
                ).start();
            }
            else if (intent.getAction().equals(NetWorkService.REQUIRE_HOSPITAL)) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                queryHospital();
                            }
                        }
                ).start();
            }
            else if (intent.getAction().equals(NetWorkService.QUERY_INFORMATION)) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    queryInformation(intent.getStringExtra("patient_id"));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                ).start();
            }
            else if (intent.getAction().equals(NetWorkService.UPDATE_DOCTOR_INFORMATION)) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    updateDoctorInformation(
                                            intent.getStringExtra("doctor_id"),
                                            intent.getStringExtra("doctor_name"),
                                            intent.getStringExtra("doctor_gender"),
                                            intent.getStringExtra("doctor_birthday"),
                                            intent.getStringExtra("doctor_address"),
                                            intent.getStringExtra("doctor_title"),
                                            intent.getStringExtra("doctor_hospital"),
                                            intent.getStringExtra("doctor_department")
                                    );
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                ).start();
            }
            else if (intent.getAction().equals(NetWorkService.QUERY_DOCTOR_INFORMATION)) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    queryDoctorInformation(intent.getStringExtra("doctor_id"));

                                }catch (Exception e){
                                    e.printStackTrace();
                                    Intent intent = new Intent();
                                    intent.setAction(MainActivityDoctor.QUERY_DOCTOR_INFORMATION_FEEDBACK);
                                    intent.putExtra("feedback","0");
                                    sendBroadcast(intent);
                                }

                            }
                        }
                ).start();
            }

        }
    }

    private void functionAddIntentFilter(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(NetWorkService.REQUIRE_DATA);
        intentFilter.addAction(NetWorkService.SUBMIT_RESULT);
        intentFilter.addAction(NetWorkService.DOWNLOAD_MEDIA);
        intentFilter.addAction(NetWorkService.LOGIN);
        intentFilter.addAction(NetWorkService.REGIST);
        intentFilter.addAction(NetWorkService.REQUIRE_HOSPITAL);
        intentFilter.addAction(NetWorkService.UPLOAD_RESULT);
        intentFilter.addAction(NetWorkService.QUERY_INFORMATION);
        intentFilter.addAction(NetWorkService.UPDATE_DOCTOR_INFORMATION);
        intentFilter.addAction(NetWorkService.QUERY_DOCTOR_INFORMATION);
    }

    private void queryDoctorInformation(String doctor_id)throws Exception{

        RequestBody formBody = new FormEncodingBuilder()
                .add("doctor_id", doctor_id)
                .build();
        final Request request = new Request.Builder()
                .url(queryDoctorInformationInterface)
                .post(formBody)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();

        Map<String,String> doctorInformaiton = gson.fromJson(response.body().string(),
                new TypeToken<Map<String,String>>() {
                }.getType());

        SharedPreferences.Editor editor = getSharedPreferences("info", MODE_APPEND).edit();
        editor.putString(doctor_id + "gender",doctorInformaiton.get("gender"));
        editor.putString(doctor_id + "age",doctorInformaiton.get("birthday"));
        editor.putString(doctor_id + "subject",doctorInformaiton.get("department"));
        editor.putString(doctor_id + "doctor_title",doctorInformaiton.get("title"));
        editor.putString(doctor_id + "personal_information_address",doctorInformaiton.get("address"));
        editor.putString(doctor_id + "doctor_charge_way","10元/次");
        editor.putString(doctor_id + "doctorName",doctorInformaiton.get("name"));
        editor.putString(doctor_id + "hospitalName",doctorInformaiton.get("hospital"));
        editor.apply();

        Intent intent = new Intent();
        intent.setAction(MainActivityDoctor.QUERY_DOCTOR_INFORMATION_FEEDBACK);
        intent.putExtra("feedback","1");
        sendBroadcast(intent);

        Log.d("information",doctorInformaiton.get("doctor_id"));
        Log.d("information",doctorInformaiton.get("name"));
        Log.d("information",doctorInformaiton.get("birthday"));
        Log.d("information",doctorInformaiton.get("gender"));
        Log.d("information",doctorInformaiton.get("address"));
    }

    private void updateDoctorInformation(String doctor_id,
                                       String doctor_name,
                                       String doctor_gender,
                                       String doctor_birthday,
                                       String doctor_address,
                                         String doctor_title,
                                         String doctor_hospital,
                                         String doctor_department)throws Exception{
        Log.d("net_hospital",doctor_hospital);
        RequestBody formBody = new FormEncodingBuilder()
                .add("doctor_id", doctor_id)
                .add("doctor_name", doctor_name)
                .add("doctor_gender", doctor_gender)
                .add("doctor_birthday", doctor_birthday)
                .add("doctor_address", doctor_address)
                .add("doctor_title", doctor_title)
                .add("doctor_department", doctor_department)
                .build();
        final Request request = new Request.Builder()
                .url(doctorUpdateInformationInterface)
                .post(formBody)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();

        Intent intent = new Intent();
        intent.setAction(MainActivityDoctor.UPDATE_INFORMATION_FEEDBACK);

        String str = response.body().string();

        if (str.equals("1")){
            intent.putExtra("feedback","1");
            //successful
        }else if (str.equals("0")){
            intent.putExtra("feedback","0");
            //failed
        }else {
            intent.putExtra("feedback","-1");
            //no such a user.
        }
        sendBroadcast(intent);

    }

    private void queryInformation(String patient_id)throws Exception{

        RequestBody formBody = new FormEncodingBuilder()
                .add("patient_id", patient_id)
                .build();
        final Request request = new Request.Builder()
                .url(doctorQueryPatientInformationInterface)
                .post(formBody)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();

        MainActivityDoctor.userInformation = gson.fromJson(response.body().string(),
                new TypeToken<Map<String,String>>() {
                }.getType());

        Log.d("information",userInformation.get("patient_id"));
        Log.d("information",userInformation.get("name"));
        Log.d("information",userInformation.get("birthday"));
        Log.d("information",userInformation.get("gender"));
        Log.d("information",userInformation.get("address"));
    }

    private void queryHospital(){
        Request request =new Request.Builder()
                .url(queryHospitalInterface)
                .build();
        try{
            Response response = new OkHttpClient().newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Gson gson = new Gson();
            RegisterActivity.hospitalList = gson.fromJson(response.body().charStream(),
                    new TypeToken<List<Map<String,String>>>() {
                    }.getType());
            Intent intent = new Intent();
            intent.setAction(RegisterActivity.REQUIRE_HOSPITAL);
            sendBroadcast(intent);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean register(String doctor_id,String psword,String name,String hospital_id)throws Exception{
        RequestBody formBody = new FormEncodingBuilder()
                .add("doctor_id", doctor_id)
                .add("psword", psword)
                .add("psword_confirm",psword)
                .add("name",name)
                .add("hospital_id",hospital_id)
                .build();
        Log.d("registerId",doctor_id);
        Log.d("registerPassword",psword);

        Request request =new Request.Builder()
                .url(doctorRegisterInterface)
                .post(formBody)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        if ("注册成功".equals(response.body().string()))
            return true;
        else
            return false;

    }

    private boolean submitResult (String orderId,String isDiagnosed,String result)throws Exception{
        RequestBody formBody = new FormEncodingBuilder()
                .add("order_id", orderId)
                .add("is_diagnosed",isDiagnosed)
                .add("result", result)
                .build();
        Log.d("feedback",String.valueOf(1));
        final Request request = new Request.Builder()
                .url(doctorSubmitResultInterface)
                .post(formBody)
                .build();
        Log.d("feedback",String.valueOf(2));
        Response response = new OkHttpClient().newCall(request).execute();
        Log.d("feedback",String.valueOf(3));
        String str = response.body().string();
        Log.d("feedback",String.valueOf(4));
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        Log.d("feedback",String.valueOf(5));

        Log.d("feedback",str);

        if ("1".equals(str))
            return true;
        else
            return false;
    }

    private boolean login(String account,String password) throws Exception{
        RequestBody formBody = new FormEncodingBuilder()
                .add("doctor_id", account)
                .add("psword", password)
                .build();
        Log.d("LoginId",account);
        Log.d("LoginPassword",password);
        final Request request = new Request.Builder()
                .url(doctorLoginInterface)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        if ("登陆成功".equals(response.body().string()))
            return true;
        else
            return false;
    }


    private void getOrders(String local_latest_order_number,String oldest_order_number,String doctorId,int page) throws Exception {
//        Log.d("fdsa","getOrders");
        isAccessingData = true;
        //在获取完一次数据之前不让他再进行数据获取。
        RequestBody formBody = new FormEncodingBuilder()
                .add("doctor_id", doctorId)
                .add("page",String.valueOf(page))
                .build();

        Request request = new Request.Builder()
                .url(doctorQueryOrderInterface)
                .post(formBody)
                .build();
//        Log.d("fdsa","1");
        Response response = new OkHttpClient().newCall(request).execute();
//        Log.d("fdsa","2");
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//        Log.d("fdsa","3");
        //array = new JSONArray(response.body().charStream());

        Gson gson = new Gson();
        List<Map<String,String>> list= gson.fromJson(response.body().charStream(),
                new TypeToken<List<Map<String,String>>>() {
                }.getType());
//        Log.d("fdsa","4");
        //Gist gist = gson.fromJson(response.body().charStream(), Gist.class);
//        for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue().content);
//        }

        //@4.对从服务器端口下载的订单按照编号进行排序。
        Collections.sort(list,
                new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        if (Integer.valueOf(o1.get("order_id")) > Integer.valueOf(o2.get("order_id")))
                            return 1;
                        else return -1;
                    }
                }
        );

        Log.d("list",String.valueOf(list.size()));

        SQLiteDatabase sqLiteDatabase = MainActivityDoctor.orderInformationDatabaseHelper.getWritableDatabase();
        Log.d("database",sqLiteDatabase.toString());

        int count = 0;
        for (Map<String,String> orderInformation:list
             ) {
            if (Integer.valueOf(local_latest_order_number) < Integer.valueOf(orderInformation.get("order_id"))){
                MainActivityDoctor.zombieListAll.add(new OrderInformation(
                        orderInformation.get("order_id"),
                        orderInformation.get("patient_id"),
                        orderInformation.get("create_time"),
                        String.valueOf(10),
                        orderInformation.get("file_name"),
                        orderInformation.get("description"),
                        "男",
                        orderInformation.get("is_routine"),
                        orderInformation.get("is_diagnosed"),
                        orderInformation.get("is_pay"),
                        orderInformation.get("result")
                ));
                int index = MainActivityDoctor.zombieListAll.size() - 1;
                Log.d("index",String.valueOf(index));

                //已接受已诊断
                if (MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListAuscultated.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }
                //已接受未诊断
                if (!MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState() &&
                        MainActivityDoctor.zombieListAll.get(index).getAcceptState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListDidntAuscultate.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }
                //已处理已拒绝
                if (!MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState() &&
                        !MainActivityDoctor.zombieListAll.get(index).getAcceptState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListRefused.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }

//                if (MainActivityDoctor.zombieListAll.get(index).getAuscultateState()){
//                    MainActivityDoctor.zombieListAll.get(index).setHandledState(true);
//                    MainActivityDoctor.zombieListAll.get(index).setAcceptState(true);
////                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
//                        MainActivityDoctor.zombieListAuscultated.add(MainActivityDoctor.zombieListAll.get(index));
////                    }
//                }



//                sqLiteDatabase.execSQL("insert into order_information values(null,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                            new Object[]{
//                                    orderInformation.get("patient_id"),
//                                    orderInformation.get("order_id"),
//                                    orderInformation.get("create_time"),
//                                    String.valueOf(10),
//                                    orderInformation.get("file_name"),
//                                    orderInformation.get("description"),
//                                    "男",
//                                    "是",
//                                    false,
//                                    false,
//                                    false,
//                                    false,
//                                    ""
////                private boolean acceptState;
////                private boolean handledState;
////                private boolean auscultateState;
////                private boolean paymentState;
//
//                            }
//                        );

//                Log.d("http",orderInformation.get("file_name"));
//                Intent intent = new Intent();
//                intent.setAction(MainActivityDoctor.newOrder);
//                sendBroadcast(intent);
            }else if (Integer.valueOf(oldest_order_number) > Integer.valueOf(orderInformation.get("order_id"))){
                //在末尾处加上

                MainActivityDoctor.zombieListAll.add(
                        count,
                        new OrderInformation(
                        orderInformation.get("order_id"),
                        orderInformation.get("patient_id"),
                        orderInformation.get("create_time"),
                        String.valueOf(10),
                        orderInformation.get("file_name"),
                        orderInformation.get("description"),
                        "男",
                        orderInformation.get("is_routine"),
                        orderInformation.get("is_diagnosed"),
                        orderInformation.get("is_pay"),
                        orderInformation.get("result")
                ));

                int index = count;
                Log.d("index",String.valueOf(index));

                //已接受已诊断
                if (MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListAuscultated.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }
                //已接受未诊断
                if (!MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState() &&
                        MainActivityDoctor.zombieListAll.get(index).getAcceptState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListDidntAuscultate.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }
                //已处理已拒绝
                if (!MainActivityDoctor.zombieListAll.get(index).getAuscultateState() &&
                        MainActivityDoctor.zombieListAll.get(index).getHandledState() &&
                        !MainActivityDoctor.zombieListAll.get(index).getAcceptState()){
//                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
                    MainActivityDoctor.zombieListRefused.add(MainActivityDoctor.zombieListAll.get(index));
//                    }
                }

//                if (MainActivityDoctor.zombieListAll.get(index).getAuscultateState()){
//                    MainActivityDoctor.zombieListAll.get(index).setHandledState(true);
//                    MainActivityDoctor.zombieListAll.get(index).setAcceptState(true);
//                    //直接采用全部洗牌的方式了，死猪不怕开水烫。
////                    if (!MainActivityDoctor.zombieListAuscultated.contains(MainActivityDoctor.zombieListAll.get(index))) {
//                        MainActivityDoctor.zombieListAuscultated.add(MainActivityDoctor.zombieListAll.get(index));
////                    }
//                }
                count++;
//                sqLiteDatabase.execSQL("insert into order_information values(null,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                        new Object[]{
//                                orderInformation.get("patient_id"),
//                                orderInformation.get("order_id"),
//                                orderInformation.get("create_time"),
//                                String.valueOf(10),
//                                orderInformation.get("file_name"),
//                                orderInformation.get("description"),
//                                "男",
//                                "是",
//                                false,
//                                false,
//                                false,
//                                false,
//                                ""
////                private boolean acceptState;
////                private boolean handledState;
////                private boolean auscultateState;
////                private boolean paymentState;
//                        });
            }
        }

        isAccessingData = false;
        //不在for中发广播，太快了，可能会发生迷之问题，搞完再发十分稳，暂时没有原来的报错。
        //报了一次，但是不是很影响使用的样子。。。
        //暂时先不管这个事情吧。
        Intent intent = new Intent();
        intent.setAction(MainActivityDoctor.newOrder);
        sendBroadcast(intent);
        //要直接把他notify，不然有可能报错，不知道是不是这样来解决。
        //并不是

    }

    static class Gist {
        List<Map<String, GistFile>> files;
    }

    static class GistFile {
        String content;
    }



    private void okHttpDownload(final String url,String voice_saved_path,String mediaName) {

        final String final_media_name = mediaName;
        final String final_voice_saved_path = voice_saved_path;

        final Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    double current = 0;
                    double total = response.body().contentLength();
                    //Log.d("http",response.body().string());
                    is = response.body().byteStream();
                    File file = new File(final_voice_saved_path, final_media_name);
                    Log.d("Http",final_voice_saved_path + "/" + final_media_name);
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                    }

                    fos.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径

                } catch (IOException e) {

                    try {
                        if (is != null) is.close();
                    } catch (IOException ei) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException ei) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
}
