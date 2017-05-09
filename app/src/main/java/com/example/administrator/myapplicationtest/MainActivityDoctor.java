package com.example.administrator.myapplicationtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivityDoctor extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener{

    //监听器
    private View.OnClickListener patientInformationClickListener;

    //-----tab选项卡-------------------//
    private int page = 0;
    private ViewPager mPager;//页卡内容
    private List<View> listViews; // Tab页面列表
    private ImageView cursor;// 动画图片
    private TextView t1, t2, t3 ,t4;// 页卡头标
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private ListView listView_all;
    private ListView listView_Didnt;
    private ListView listView_refused;
    private ListView listView_auscultated;
    public static BaseAdapter baseAdapterTabAll;
    private BaseAdapter baseAdapterTabRefused;
    private BaseAdapter baseAdapterTabAuscultated;
    private BaseAdapter baseAdapterTabDidnt;
    public Comparator<OrderInformation> order_number_comparator;
    //-----菜单工具栏-------------------//
    private Toolbar toolbarMainActivity;

    //-------广播-----------------------//
    BroadcastReceiver mReceiver;
    private IntentFilter intentFilterBroadcast;
    private IntentFilter intentFilterMp3Service;
    private Intent intentNetworkService;
    private Intent intentMp3Service;
    public String main_url = "http://116.57.86.220/au/audio/";
    public String voice_saved_path;

    //------个人信息抽屉----------//
    private Drawer result;
    private String doctorId;

    //------数据库----------------//
    private SQLiteDatabase order_database;
    public static String order_database_name = "doctor.db3";
    //@8.这里只需要存all的订单就行了，从数据库取出来的时候再按照订单状态分发到其他的几个list。
    private String name_order_all_in_db = "order_all";
    public static OrderInformationDatabaseHelper orderInformationDatabaseHelper;

    //---------------------------广播信号定义--------------------------------------
    private static String orderAccepted =
            "com.example.administrator.myapplicationtest.orderAccepted";
    private static String orderRefused =
            "com.example.administrator.myapplicationtest.orderRefused";
    private static String ORDER_COMMIT =
            "com.example.administrator.myapplicationtest.ORDER_COMMIT";
    private static String orderNumber =
            "com.example.administrator.myapplicationtest.orderNumber";
    private static String contentTag =
            "com.example.administrator.myapplicationtest.contentTag";
    public static String newOrder =
            "com.example.administrator.myapplicationtest.newOrder";
    public static String FILE_EXISTING_STATE =
            "com.example.administrator.myapplicationtest.FILE_EXIST_STATE";
    public static String SUBMIT_RESULT_FEEDBACK =
            "com.example.administrator.myapplicationtest.SUBMIT_RESULT_FEEDBACK";
    public static String QUERY_INFORMATION_FEEDBACK =
            "com.example.administrator.myapplicationtest.QUERY_INFORMATION_FEEDBACK";
    public static String UPDATE_INFORMATION_FEEDBACK =
            "com.example.administrator.myapplicationtest.UPDATE_INFORMATION_FEEDBACK";
    public static String QUERY_DOCTOR_INFORMATION_FEEDBACK =
            "com.example.administrator.myapplicationtest.QUERY_DOCTOR_INFORMATION_FEEDBACK";

    LayoutInflater mInflater;
    View tab_layout_all;
    View tab_layout_refused;
    View tab_layout_auscultated;
    View tab_layout_didnt_auscultated;

    //用户信息保存。因为订单和用户信息分开两个几接口了，那么我们只能退而求其次。
    public static Map<String,String> userInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化用户信息为空
        userInformation = new HashMap<>();

        //检查权限
        checkPermissions();

        //获取医生的账号信息。
        SharedPreferences preferences = getSharedPreferences("info", MODE_APPEND);
        doctorId = preferences.getString("name","医生账号获取失败");

        //同步服务器端医生的信息
        Intent intentToGetDoctorInformation = new Intent();
        intentToGetDoctorInformation.setAction(NetWorkService.QUERY_DOCTOR_INFORMATION);
        intentToGetDoctorInformation.putExtra("doctor_id",doctorId);
        sendBroadcast(intentToGetDoctorInformation);

        functionInitSlideTab();
        functionToolbarInitialize();
        functionInitSelfInformationDrawer();
        listView_all = (ListView)tab_layout_all.findViewById(R.id.listView_all);



        listView_Didnt = (ListView)tab_layout_didnt_auscultated.findViewById(R.id.listView_Didnt);
        listView_refused = (ListView)tab_layout_refused.findViewById(R.id.listView_Refused);
        listView_auscultated = (ListView)tab_layout_auscultated.findViewById(R.id.listView_auscultated);
        //functionFillListView(tempNames,tempDates,tempOrderNumbers,tempMoneyAmounts,listView_all);

        functionCreateZombieOrders(); //先填充订单信息
        functionFillListViewAllWithBaseAdapter(); //填充ALL
        functionFillListViewDidntWithBaseAdapter(); //填充didn
        functionFillListViewRefusedWithBaseAdapter(); //填充refused
        functionFillListViewAuscultatedWithBaseAdapter();
        //baseAdapterTabAll.notifyDataSetChanged(); //提示listview数据已经更新了。
        order_number_comparator = new Comparator<OrderInformation>() {
            @Override
            public int compare(OrderInformation o1, OrderInformation o2) {
                if (Integer.valueOf(o1.getOrderNumber()) > Integer.valueOf(o2.getOrderNumber()))
                    return 1;
                else
                    return -1;
            }
        };



        mReceiver = new mReceiver();
        initIntentFilter();
        registerReceiver(mReceiver,intentFilterBroadcast);


//        intentNetworkService = new Intent(MainActivityDoctor.this,NetWorkService.class);
//        startService(intentNetworkService);
        intentMp3Service = new Intent(MainActivityDoctor.this,Mp3PlayerService.class);
        startService(intentMp3Service);
        //这里需要继续完善从服务器端获取数据的方法。
        //创建本地的录音文件。
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/HS_voice/");
        if (!file.exists()){
            file.mkdir();
            Log.d("voice saved path","文件目录不存在，新建目录。");
        }
        voice_saved_path = file.getPath();
        Log.d("voice saved path",voice_saved_path);

        //定义listview往下滚动到末尾会获取新订单回来。
        listView_all.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                Log.d("scroll","scrollState");
                if (scrollState == SCROLL_STATE_IDLE && !NetWorkService.isAccessingData){
//                    Log.d("scroll","SCROLL_STATE_IDLE");
//                    Log.d("scroll",String.valueOf(view.getLastVisiblePosition()));
//                    Log.d("scroll",String.valueOf(view.getAdapter().getCount()));
                    //这position是从0开始的
                    if (view.getLastVisiblePosition()+1 == view.getAdapter().getCount()){
//                        Log.d("scroll","prepare to send intent");
                        Intent intent = new Intent();
                        intent.setAction(NetWorkService.REQUIRE_DATA);
                        try{
                            //Log.d("menu",zombieListAll.get(zombieListAll.size() - 1).getOrderNumber());
                            intent.putExtra("local_latest_ordernumber",zombieListAll.get(zombieListAll.size() - 1).getOrderNumber());
                            intent.putExtra("local_oldest_ordernumber",zombieListAll.get(0).getOrderNumber());
                            intent.putExtra("doctorId",doctorId);
                            intent.putExtra("page",page);
                            sendBroadcast(intent);
                            page++;
                            Log.d("scroll page",String.valueOf(page));

                        }catch (ArrayIndexOutOfBoundsException ae){
                            ae.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //当滑到下面的时候直接加载新的数据。
//                Log.d("scroll","in the listener");

            }
        });

        patientInformationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivityDoctor.this);
                View patientInformationView = getLayoutInflater().inflate(R.layout.personal_details_patient,null);
                alertDialog.setView(patientInformationView);

                String str = MainActivityDoctor.userInformation.get("birthday");
                String year = str.substring(0,4);
                String month = str.substring(5,7);
                String day = str.substring(8,10);

                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_name)).
                        setText(MainActivityDoctor.userInformation.get("name"));
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_gender)).
                        setText(MainActivityDoctor.userInformation.get("gender").equals("1")?"男":"女");
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_birthday_year)).
                        setText(year);
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_birthday_month)).
                        setText(month);
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_birthday_day)).
                        setText(day);
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_phone_number)).
                        setText(MainActivityDoctor.userInformation.get("patient_id"));
                ((TextView)patientInformationView.
                        findViewById(R.id.patient_personal_information_address)).
                        setText(MainActivityDoctor.userInformation.get("address"));

                alertDialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        };
    }

    @Override
    public void finish(){
        if (intentMp3Service != null)
            stopService(intentMp3Service);
        if (intentNetworkService != null)
            stopService(intentNetworkService);
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        //unregisterReceiver(mReceiver);
    }

    //--------------------Tab 滑动效果实现代码 start------------------------------//
    /**
     * 初始化头标
     */
    private void InitTextView() {
        t1 = (TextView) findViewById(R.id.text1);
        t2 = (TextView) findViewById(R.id.text2);
        t3 = (TextView) findViewById(R.id.text3);
        t4 = (TextView) findViewById(R.id.text4);
        t1.setBackgroundColor(Color.parseColor("#bfbfbf"));

        t1.setOnClickListener(new MyOnClickListener(0));
        t2.setOnClickListener(new MyOnClickListener(1));
        t3.setOnClickListener(new MyOnClickListener(2));
        t4.setOnClickListener(new MyOnClickListener(3));
    }
    /**
     * 头标点击监听
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }
    /**
     * 初始化ViewPager
     */
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        //@1.设置缓存页面数量，减轻滑动卡顿现象。
        mPager.setOffscreenPageLimit(6);
        listViews = new ArrayList<View>();
        mInflater = getLayoutInflater();
        tab_layout_all = mInflater.inflate(R.layout.tab_layout_all, null);
        tab_layout_auscultated = mInflater.inflate(R.layout.tab_layout_auscultated, null);
        tab_layout_didnt_auscultated = mInflater.inflate(R.layout.tab_layout_didnt_auscultate, null);
        tab_layout_refused = mInflater.inflate(R.layout.tab_layout_refused, null);
        listViews.add(tab_layout_all);
        listViews.add(tab_layout_auscultated);
        listViews.add(tab_layout_didnt_auscultated);
        listViews.add(tab_layout_refused);
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }
    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        final int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        final int two = one * 2;// 页卡1 -> 页卡3 偏移量
        final int three = one + two; //页卡1-> 页卡4 偏移量
        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;

            switch (arg0) {
                case 0:
                    t1.setBackgroundColor(getResources().getColor(R.color.colorThinking3));
                    t2.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t3.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t4.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    } else if (currIndex == 3) {
                        animation = new TranslateAnimation(three, 0, 0, 0);
                    }
                    break;
                case 1:
                    t2.setBackgroundColor(getResources().getColor(R.color.colorThinking3));
                    t1.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t3.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t4.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    } else if (currIndex == 3) {
                        animation = new TranslateAnimation(three, one, 0, 0);
                    }
                    break;
                case 2:
                    t3.setBackgroundColor(getResources().getColor(R.color.colorThinking3));
                    t2.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t1.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t4.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    } else if (currIndex == 3) {
                        animation = new TranslateAnimation(three, two, 0, 0);
                    }
                    break;
                case 3:
                    t4.setBackgroundColor(getResources().getColor(R.color.colorThinking3));
                    t2.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t3.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    t1.setBackgroundColor(getResources().getColor(R.color.colorThinking2));
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, three, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, three, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, three, 0, 0);
                    }
                    break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            cursor.startAnimation(animation);
            //@7.试图在滑动的时候添加线程来解决卡顿问题。
            //并不能解决问题，感觉是内存问题？
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /**
     * 初始化动画
     */
    private void InitImageView() {
        cursor = (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.buzhihuowu)
                .getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursor.setImageMatrix(matrix);// 设置动画初始位置
    }

    /**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    /**
     * 一次性完成三个初始化
     */
    public void functionInitSlideTab(){
        InitTextView();
        InitViewPager();
        InitImageView();
    }
    //--------------------Tab 滑动效果实现代码 end------------------------------//



    //--------------------Toolbar功能实现代码 start------------------------------//
    /**
     * 一次性完成toolbar 的初始化功能设置
     */
    public void functionToolbarInitialize(){
        toolbarMainActivity = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbarMainActivity.setTitle(this.getTitle());
        toolbarMainActivity.setSubtitle("听诊服务");
        //MenuBuilder menuBuilder = new MenuBuilder(this);
        toolbarMainActivity.setNavigationIcon(R.drawable.handsome);


        //监听器要放到后面。。。暂时不知为何。
        setSupportActionBar(toolbarMainActivity);

        toolbarMainActivity.setOnMenuItemClickListener(MainActivityDoctor.this);
        //@2.添加导航图标点击事件。引出drawer layout（个人详情页面）.
        toolbarMainActivity.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.openDrawer();
                        Log.d("toolbar","setNavigationOnClickListener");
                    }
                }
        );
        //toolbarMainActivity.setOnCreateContextMenuListener(this);
    }
    /**
     * 复写设置的菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return  true;
    }

    //--------------------Toolbar功能实现代码 end------------------------------//


    //--------------------个人信息抽屉实现代码 start------------------------------//
    public void functionInitSelfInformationDrawer(){
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(doctorId).withIcon(getResources().getDrawable(R.drawable.xiake))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("个人信息").withIcon(R.drawable.person);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("账户余额:￥998.00").withIcon(R.drawable.money);
        //create the drawer and remember the `Drawer` result object
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(true)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("切换账户").withIdentifier(3).withIcon(R.drawable.exchange),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("安全退出").withIdentifier(4).withIcon(R.drawable.exit)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        if (drawerItem.getIdentifier()==3){
                            finish();
                        }else if (drawerItem.getIdentifier()==4){
                            finish();
                        }else if (drawerItem.getIdentifier() == 1){
                            final View doctorInformationView = getLayoutInflater().inflate(R.layout.personal_details,null);
                            //从数据库中取出事先保存好的个人信息进行填充。

                            android.app.AlertDialog.Builder customizeDialog =
                                    new android.app.AlertDialog.Builder(MainActivityDoctor.this);

                            customizeDialog.setView(doctorInformationView);
                            SharedPreferences sharedPreferences = getSharedPreferences("info", MODE_APPEND);

                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_name)).setText(
                                    sharedPreferences.getString(doctorId + "doctorName"," ")
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_hospital)).setText(
                                    sharedPreferences.getString(doctorId + "hospitalName"," ")
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_phone_number)).setText(
                                    doctorId
                            );
                            ((RadioGroup)doctorInformationView.findViewById(R.id.personal_information_gender)).check(
                                    sharedPreferences.getString(doctorId + "gender"," ").equals("1")?R.id.male:R.id.female
                            );

                            //这里读出年月日
                            String birthday = sharedPreferences.getString(doctorId + "age"," ");
                            String[] date = birthday.split("-");
                            Log.d("birthday",birthday);
                            Log.d("year",date[0]);
                            Log.d("month",date[1]);
                            Log.d("day",date[2]);
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_year)).setText(
                                    date[0]
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_month)).setText(
                                    date[1]
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_day)).setText(
                                    date[2]
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_subject)).setText(
                                    sharedPreferences.getString(doctorId + "subject"," ")
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_doctor_title)).setText(
                                    sharedPreferences.getString(doctorId + "doctor_title"," ")
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_address)).setText(
                                    sharedPreferences.getString(doctorId + "personal_information_address"," ")
                            );
                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_doctor_charge_way)).setText(
                                    sharedPreferences.getString(doctorId + "doctor_charge_way"," ")
                            );
                            customizeDialog.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String year = ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_year)).getText().toString();
                                    String month = ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_month)).getText().toString();
                                    String day = ((EditText)doctorInformationView.findViewById(R.id.personal_information_birthday_day)).getText().toString();

                                    if (month.length() == 1){
                                        month = "0" + month;
                                    }
                                    if (day.length() == 1){
                                        day = "0" + day;
                                    }

                                    SharedPreferences.Editor editor = getSharedPreferences("info", MODE_APPEND).edit();
                                    editor.putString(doctorId + "gender",((RadioButton)doctorInformationView.
                                                findViewById(((RadioGroup)doctorInformationView.findViewById(R.id.personal_information_gender)).getCheckedRadioButtonId()))
                                                .getText()
                                                .toString().equals("男")?"1":"2");
                                    editor.putString(doctorId + "age",year + "-" + month + "-" + day);
                                    editor.putString(doctorId + "subject",((EditText)doctorInformationView.findViewById(R.id.personal_information_subject)).getText().toString());
                                    editor.putString(doctorId + "doctor_title",((EditText)doctorInformationView.findViewById(R.id.personal_information_doctor_title)).getText().toString());
                                    editor.putString(doctorId + "personal_information_address",((EditText)doctorInformationView.findViewById(R.id.personal_information_address)).getText().toString());
                                    editor.putString(doctorId + "doctor_charge_way",((EditText)doctorInformationView.findViewById(R.id.personal_information_doctor_charge_way)).getText().toString());
                                    editor.putString(doctorId + "name",((EditText)doctorInformationView.findViewById(R.id.personal_information_name)).getText().toString());
                                    editor.putString(doctorId + "hospital",((EditText)doctorInformationView.findViewById(R.id.personal_information_hospital)).getText().toString());
                                    editor.apply();
                                    Toast.makeText(MainActivityDoctor.this,"修改成功",Toast.LENGTH_SHORT).show();

                                    Intent intentToUpdateDoctorInformation = new Intent();
                                    intentToUpdateDoctorInformation.setAction(NetWorkService.UPDATE_DOCTOR_INFORMATION);
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_id",
                                            doctorId
                                    );
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_name",
                                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_name)).getText().toString()
                                    );
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_gender",
                                            ((RadioButton)doctorInformationView.
                                                    findViewById(((RadioGroup)doctorInformationView.findViewById(R.id.personal_information_gender)).getCheckedRadioButtonId()))
                                                    .getText()
                                                    .toString().equals("男")?"1":"2"
                                    );

                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_birthday",
                                            year + "-" + month + "-" + day
                                    );
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_address",
                                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_address)).getText().toString()
                                    );
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_title",
                                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_doctor_title)).getText().toString()
                                    );
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_hospital",
                                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_hospital)).getText().toString()
                                    );
                                    Log.d("hospital",((EditText)doctorInformationView.findViewById(R.id.personal_information_hospital)).getText().toString());
                                    intentToUpdateDoctorInformation.putExtra(
                                            "doctor_department",
                                            ((EditText)doctorInformationView.findViewById(R.id.personal_information_subject)).getText().toString()
                                    );

                                    sendBroadcast(intentToUpdateDoctorInformation);

                                }
                            });
                            customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivityDoctor.this,"取消修改",Toast.LENGTH_SHORT).show();
                                }
                            });
                            customizeDialog.show();
                        }
                        return true;
                    }
                })
                .build();
//        item1.withName("mmp").
//                withBadge("19").
//                withBadgeStyle(new BadgeStyle().
//                        withTextColor(Color.WHITE).
//                        withColorRes(R.color.md_red_700));
        //notify the drawer about the updated element. it will take care about everything else
        result.updateItem(item1);
        result.addStickyFooterItem(new PrimaryDrawerItem().withName("@陈锦泉"));

    }


    //--------------------个人信息抽屉实现代码 end--------------------------------//

    //--------------------用Adapter填充服务信息实现代码 start---------------------//
    //先来填充all_tab界面的list。
    //我们使用的数据应该是来自服务器端的，需要填充多少个就从服务器拿多少个。
    //现在我们先在本地用虚拟数据填充。后面应该要弄一个数据库来存放本地想要保存的记录。
    //需要填充的单元：
    //    date_swipe      时间日期
    //    number_swipe     流水单号
    //    name_swipe     患者姓名
    //    money_amount_swipe     订单金额
    /**
     * 用来填充每个Tab页面的listView的函数。
     */
//    private void functionFillListView(String[] names,
//                                      String[] dates,
//                                      String[] orderNumbers,
//                                      String[] moneyAmounts,
//                                      ListView listView
//                                      ){
//        List<Map<String,Object>> listItems = new ArrayList<>();
//        for (int i = 0;i < names.length ;i++){
//            Map<String,Object> listItem = new HashMap<>();
//            listItem.put("date",dates[i]);
//            listItem.put("name",names[i]);
//            listItem.put("orderNumber",orderNumbers[i]);
//            listItem.put("moneyAmount",moneyAmounts[i]);
//            listItems.add(listItem);
//        }
//        SimpleAdapter simpleAdapter = new SimpleAdapter(
//                this,
//                listItems,
//                R.layout.layout_swipe,
//                new String[]{"date","name","orderNumber","moneyAmount"},
//                new int[]{R.id.date_swipe,R.id.name_swipe,R.id.number_swipe,R.id.money_amount_swipe}
//        );
//        listView.setAdapter(simpleAdapter);
//    }

    /**
     * 用来填充TabAll页面的listView的函数。(用baseAdapter来实现)
     */
    private void functionFillListViewAllWithBaseAdapter(){
        baseAdapterTabAll = new BaseAdapter() {
            @Override
            public int getCount() {
                return zombieListAll.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return zombieListAll.size() - position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (zombieListAll.size() > 0){
                    final View view = mInflater.inflate(R.layout.layout_swipe,null);
                    final int realPosition = zombieListAll.size() - position - 1; //按时间最新从上到下显示。

                    //@5.ui线程中尽量做少一点事情，耗时的事情要开别的线程去做。
                    //@6.但是，这里如果在这里另外开线程很危险，下面的view 有可能直接会变成空指针。
                    ((TextView)(view.findViewById(R.id.date_swipe))).setText(zombieListAll.get(realPosition).getDate());
                    ((TextView)(view.findViewById(R.id.name_swipe))).setText(zombieListAll.get(realPosition).getName());
                    ((TextView)(view.findViewById(R.id.number_swipe))).setText(zombieListAll.get(realPosition).getOrderNumber());
                    ((TextView)(view.findViewById(R.id.money_amount_swipe))).setText(zombieListAll.get(realPosition).getMoneyAmount());
                    //如果有已诊断的，直接开直通车到已接受、已诊断，如果没有
                    if (zombieListAll.get(realPosition).getHandledState() || zombieListAll.get(realPosition).getAuscultateState()){

                        (view.findViewById(R.id.unhandled_state_swipe)).setVisibility(View.INVISIBLE);
                        (view.findViewById(R.id.handled_state_swipe)).setVisibility(View.VISIBLE);
                        if (zombieListAll.get(realPosition).getAcceptState() || zombieListAll.get(realPosition).getAuscultateState()){
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已接受");
                            zombieListAll.get(realPosition).setHandledState(true);
//                            zombieListAuscultated.add(zombieListAll.get(realPosition));
//                            baseAdapterTabAuscultated.notifyDataSetChanged();
                            baseAdapterTabAll.notifyDataSetChanged();
                        }
                        else if (!zombieListAll.get(realPosition).getAcceptState()){
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已拒绝");
//                            zombieListRefused.add(zombieListAll.get(realPosition));
//                            baseAdapterTabRefused.notifyDataSetChanged();
                            baseAdapterTabAll.notifyDataSetChanged();
                        }

                    }
                    if (zombieListAll.get(realPosition).getAuscultateState())
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("已诊断");
                    else
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("未诊断");

                    if (zombieListAll.get(realPosition).getPaymentState())
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("已付款");
                    else
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("未付款");




                    (view.findViewById(R.id.play_pause_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }
                    );

                    (view.findViewById(R.id.stop_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                    sendBroadcast(intent);
                                }
                            }
                    );


                    (view.findViewById(R.id.specified_content_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListAll.get(realPosition).getName());

                                    Log.d("patient_id",zombieListAll.get(realPosition).getName());

                                    final View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

                                    ((EditText)(popupView.findViewById(R.id.result_details))).setText(zombieListAll.get(realPosition).getAuscultateResult());

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);


//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    customizeDialog.setPositiveButton("确定提交评价", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!zombieListAll.get(realPosition).getAuscultateState() &&
                                                    (zombieListAll.get(realPosition).getAcceptState() ||
                                                            !zombieListAll.get(realPosition).getHandledState()
                                                    )) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        zombieListAll.get(realPosition).setAuscultateState(true);
                                                        zombieListAll.get(realPosition).setHandledState(true);
                                                        zombieListAll.get(realPosition).setAcceptState(true);
                                                        zombieListAll.get(realPosition).setAuscultateResult(
                                                                ((EditText) popupView.findViewById(R.id.result_details)).getText().toString()
                                                        );
                                                        zombieListAuscultated.add(zombieListAll.get(realPosition));
                                                        if (zombieListDidntAuscultate.contains(zombieListAll.get(realPosition))) {
                                                            zombieListDidntAuscultate.remove(zombieListAll.get(realPosition));
                                                        }
//                                                        ContentValues contentValues = new ContentValues();
//                                                        contentValues.put("auscultateState", true);
//                                                        contentValues.put("handledState", true);
//                                                        contentValues.put("acceptState ", true);
//                                                        contentValues.put("auscultatedResult ", ((EditText) popupView.findViewById(R.id.result_details)).getText().toString());
//                                                        //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                        updateDatabase(contentValues, zombieListAll.get(realPosition).getOrderNumber());
                                                        Collections.sort(zombieListAuscultated, order_number_comparator);
                                                        runOnUiThread(
                                                                new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        baseAdapterTabAuscultated.notifyDataSetChanged();
                                                                        baseAdapterTabAll.notifyDataSetChanged();
                                                                        baseAdapterTabDidnt.notifyDataSetChanged();
                                                                        Toast.makeText(MainActivityDoctor.this,"提交成功",Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        );
                                                        Intent intent = new Intent();
                                                        intent.setAction(NetWorkService.SUBMIT_RESULT);
                                                        intent.putExtra("order_id",zombieListAll.get(realPosition).getOrderNumber());
                                                        intent.putExtra("is_diagnosed","1");
                                                        intent.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                        sendBroadcast(intent);

                                                    }
                                                }).start();
                                            }
                                        }
                                    });
//
//                                    popupView.findViewById(R.id.commit_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                    //未诊断的是前提，并且一定要被接受或者未被处理过的。
//                                                    //这样不会造成在已诊断中多次添加。
//                                                    if (!zombieListAll.get(realPosition).getAuscultateState() &&
//                                                            (zombieListAll.get(realPosition).getAcceptState() ||
//                                                                    !zombieListAll.get(realPosition).getHandledState()
//                                                            )){
//                                                        new Thread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                zombieListAll.get(realPosition).setAuscultateState(true);
//                                                                zombieListAll.get(realPosition).setHandledState(true);
//                                                                zombieListAll.get(realPosition).setAcceptState(true);
//                                                                zombieListAll.get(realPosition).setAuscultateResult(
//                                                                        ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
//                                                                );
//                                                                zombieListAuscultated.add(zombieListAll.get(realPosition));
//                                                                if (zombieListDidntAuscultate.contains(zombieListAll.get(realPosition))){
//                                                                    zombieListDidntAuscultate.remove(zombieListAll.get(realPosition));
//                                                                }
//                                                                ContentValues contentValues = new ContentValues();
//                                                                contentValues.put("auscultateState",true);
//                                                                contentValues.put("handledState",true);
//                                                                contentValues.put("acceptState ",true);
//                                                                contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                                //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                                updateDatabase(contentValues,zombieListAll.get(realPosition).getOrderNumber());
//                                                                Collections.sort(zombieListAuscultated,order_number_comparator);
//                                                                runOnUiThread(
//                                                                        new Runnable() {
//                                                                            @Override
//                                                                            public void run() {
//                                                                                baseAdapterTabAuscultated.notifyDataSetChanged();
//                                                                                baseAdapterTabAll.notifyDataSetChanged();
//                                                                                baseAdapterTabDidnt.notifyDataSetChanged();
//                                                                            }
//                                                                        }
//                                                                );
//                                                                Intent network_intent = new Intent();
//                                                                network_intent.setAction(NetWorkService.SUBMIT_RESULT);
//                                                                sendBroadcast(network_intent);
//                                                            }
//                                                        }).start();
//
//                                                    }
//                                                }
//                                            }
//                                    );

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListAll.get(realPosition).getName() + "/" + zombieListAll.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListAll.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );
                                                    Log.d("download",main_url + zombieListAll.get(realPosition).getName() + "/" + zombieListAll.get(realPosition).getMediaName());
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);

//                                                    Intent mIntent = new Intent();
//                                                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
//                                                    Uri uri = Uri.fromFile(new File(voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName()));
//                                                    mIntent.setDataAndType(uri , "audio/x-wav");
//                                                    startActivity(mIntent);

//                                                    Intent mIntent = new Intent();
//                                                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
//                                                    Uri uri = Uri.parse(voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName());
//                                                    mIntent.setDataAndType(uri , "audio/x-wav");
//                                                    startActivity(mIntent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListAll.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListAll.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListAll.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListAll.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListAll.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListAll.get(realPosition).getAuscultateResult());

//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//                                    popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);

                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
                                }
                            }

                    );



                    (view.findViewById(R.id.details_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListAll.get(realPosition).getName());

                                    final View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

                                    ((EditText)(popupView.findViewById(R.id.result_details))).setText(zombieListAll.get(realPosition).getAuscultateResult());

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);


//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    customizeDialog.setPositiveButton("确定提交评价", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!zombieListAll.get(realPosition).getAuscultateState() &&
                                                    (zombieListAll.get(realPosition).getAcceptState() ||
                                                            !zombieListAll.get(realPosition).getHandledState()
                                                    )) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        zombieListAll.get(realPosition).setAuscultateState(true);
                                                        zombieListAll.get(realPosition).setHandledState(true);
                                                        zombieListAll.get(realPosition).setAcceptState(true);
                                                        zombieListAll.get(realPosition).setAuscultateResult(
                                                                ((EditText) popupView.findViewById(R.id.result_details)).getText().toString()
                                                        );
                                                        zombieListAuscultated.add(zombieListAll.get(realPosition));
                                                        if (zombieListDidntAuscultate.contains(zombieListAll.get(realPosition))) {
                                                            zombieListDidntAuscultate.remove(zombieListAll.get(realPosition));
                                                        }
//                                                        ContentValues contentValues = new ContentValues();
//                                                        contentValues.put("auscultateState", true);
//                                                        contentValues.put("handledState", true);
//                                                        contentValues.put("acceptState ", true);
//                                                        contentValues.put("auscultatedResult ", ((EditText) popupView.findViewById(R.id.result_details)).getText().toString());
//                                                        //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                        updateDatabase(contentValues, zombieListAll.get(realPosition).getOrderNumber());
                                                        Collections.sort(zombieListAuscultated, order_number_comparator);
                                                        runOnUiThread(
                                                                new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        baseAdapterTabAuscultated.notifyDataSetChanged();
                                                                        baseAdapterTabAll.notifyDataSetChanged();
                                                                        baseAdapterTabDidnt.notifyDataSetChanged();
                                                                        Toast.makeText(MainActivityDoctor.this,"提交成功",Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        );
                                                        Intent intent = new Intent();
                                                        intent.setAction(NetWorkService.SUBMIT_RESULT);
                                                        intent.putExtra("order_id",zombieListAll.get(realPosition).getOrderNumber());
                                                        intent.putExtra("is_diagnosed","1");
                                                        intent.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                        sendBroadcast(intent);

                                                    }
                                                }).start();
                                            }
                                        }
                                    });
//
//                                    popupView.findViewById(R.id.commit_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                    //未诊断的是前提，并且一定要被接受或者未被处理过的。
//                                                    //这样不会造成在已诊断中多次添加。
//                                                    if (!zombieListAll.get(realPosition).getAuscultateState() &&
//                                                            (zombieListAll.get(realPosition).getAcceptState() ||
//                                                                    !zombieListAll.get(realPosition).getHandledState()
//                                                            )){
//                                                        new Thread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                zombieListAll.get(realPosition).setAuscultateState(true);
//                                                                zombieListAll.get(realPosition).setHandledState(true);
//                                                                zombieListAll.get(realPosition).setAcceptState(true);
//                                                                zombieListAll.get(realPosition).setAuscultateResult(
//                                                                        ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
//                                                                );
//                                                                zombieListAuscultated.add(zombieListAll.get(realPosition));
//                                                                if (zombieListDidntAuscultate.contains(zombieListAll.get(realPosition))){
//                                                                    zombieListDidntAuscultate.remove(zombieListAll.get(realPosition));
//                                                                }
//                                                                ContentValues contentValues = new ContentValues();
//                                                                contentValues.put("auscultateState",true);
//                                                                contentValues.put("handledState",true);
//                                                                contentValues.put("acceptState ",true);
//                                                                contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                                //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                                updateDatabase(contentValues,zombieListAll.get(realPosition).getOrderNumber());
//                                                                Collections.sort(zombieListAuscultated,order_number_comparator);
//                                                                runOnUiThread(
//                                                                        new Runnable() {
//                                                                            @Override
//                                                                            public void run() {
//                                                                                baseAdapterTabAuscultated.notifyDataSetChanged();
//                                                                                baseAdapterTabAll.notifyDataSetChanged();
//                                                                                baseAdapterTabDidnt.notifyDataSetChanged();
//                                                                            }
//                                                                        }
//                                                                );
//                                                                Intent network_intent = new Intent();
//                                                                network_intent.setAction(NetWorkService.SUBMIT_RESULT);
//                                                                sendBroadcast(network_intent);
//                                                            }
//                                                        }).start();
//
//                                                    }
//                                                }
//                                            }
//                                    );

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListAll.get(realPosition).getName() + "/" + zombieListAll.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListAll.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );
                                                    Log.d("download",main_url + zombieListAll.get(realPosition).getName() + "/" + zombieListAll.get(realPosition).getMediaName());
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);

//                                                    Intent mIntent = new Intent();
//                                                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
//                                                    Uri uri = Uri.fromFile(new File(voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName()));
//                                                    mIntent.setDataAndType(uri , "audio/x-wav");
//                                                    startActivity(mIntent);

//                                                    Intent mIntent = new Intent();
//                                                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
//                                                    Uri uri = Uri.parse(voice_saved_path + "/" + zombieListAll.get(realPosition).getMediaName());
//                                                    mIntent.setDataAndType(uri , "audio/x-wav");
//                                                    startActivity(mIntent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListAll.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListAll.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListAll.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListAll.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListAll.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListAll.get(realPosition).getAuscultateResult());

//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//                                    popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
                                }
                            }
                    );




                    ((view.findViewById(R.id.accept_swipe))).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("swipe","Clicked");
                                    String number = String.
                                            valueOf(((TextView)(view.findViewById(R.id.number_swipe))).
                                                    getText());
                                    Intent intent = new Intent();
                                    intent.setAction(MainActivityDoctor.orderAccepted);
                                    intent.putExtra(MainActivityDoctor.orderNumber,number);
                                    sendBroadcast(intent);

                                    Intent intentToUpdateOrderState = new Intent();
                                    intentToUpdateOrderState.setAction(NetWorkService.SUBMIT_RESULT);
                                    intentToUpdateOrderState.putExtra("order_id",zombieListAll.get(realPosition).getOrderNumber());
                                    intentToUpdateOrderState.putExtra("is_diagnosed","3");
                                    intentToUpdateOrderState.putExtra("result","");
                                    sendBroadcast(intentToUpdateOrderState);
                                }
                            }
                    );
                    ((view.findViewById(R.id.refuse_swipe))).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String number = String.
                                            valueOf(((TextView)(view.findViewById(R.id.number_swipe))).
                                                    getText());
                                    Intent intent = new Intent();
                                    intent.setAction(MainActivityDoctor.orderRefused);
                                    intent.putExtra(MainActivityDoctor.orderNumber,number);
                                    sendBroadcast(intent);

                                    Intent intentToUpdateOrderState = new Intent();
                                    intentToUpdateOrderState.setAction(NetWorkService.SUBMIT_RESULT);
                                    intentToUpdateOrderState.putExtra("order_id",zombieListAll.get(realPosition).getOrderNumber());
                                    intentToUpdateOrderState.putExtra("is_diagnosed","2");
                                    intentToUpdateOrderState.putExtra("result","");
                                    sendBroadcast(intentToUpdateOrderState);
                                }
                            }
                    );



                    return view;
                }else
                    return null;

            }


        };

        listView_all.setAdapter(baseAdapterTabAll);
    }

    private void functionFillListViewAuscultatedWithBaseAdapter(){
        baseAdapterTabAuscultated = new BaseAdapter() {
            @Override
            public int getCount() {
                return zombieListAuscultated.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return zombieListAuscultated.size() - position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (zombieListAuscultated.size() > 0){
                    final View view = mInflater.inflate(R.layout.layout_swipe,null);
                    final int realPosition = zombieListAuscultated.size() - position - 1; //按时间最新从上到下显示。
                    ((TextView)(view.findViewById(R.id.date_swipe))).setText(zombieListAuscultated.get(realPosition).getDate());
                    ((TextView)(view.findViewById(R.id.name_swipe))).setText(zombieListAuscultated.get(realPosition).getName());
                    ((TextView)(view.findViewById(R.id.number_swipe))).setText(zombieListAuscultated.get(realPosition).getOrderNumber());
                    ((TextView)(view.findViewById(R.id.money_amount_swipe))).setText(zombieListAuscultated.get(realPosition).getMoneyAmount());

                    if (zombieListAuscultated.get(realPosition).getHandledState()){
                        (view.findViewById(R.id.unhandled_state_swipe)).setVisibility(View.INVISIBLE);
                        (view.findViewById(R.id.handled_state_swipe)).setVisibility(View.VISIBLE);
                        if (zombieListAuscultated.get(realPosition).getAcceptState())
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已接受");
                        else
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已拒绝");
                    }
                    if (zombieListAuscultated.get(realPosition).getAuscultateState())
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("已诊断");
                    else
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("未诊断");

                    if (zombieListAuscultated.get(realPosition).getPaymentState())
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("已付款");
                    else
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("未付款");

                    (view.findViewById(R.id.play_pause_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }
                    );

                    (view.findViewById(R.id.stop_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                    sendBroadcast(intent);
                                }
                            }
                    );

                    (view.findViewById(R.id.details_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListAuscultated.get(realPosition).getName());

                                    View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);
                                    ((EditText)(popupView.findViewById(R.id.result_details))).setText(zombieListAuscultated.get(realPosition).getAuscultateResult());
//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);


//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListAuscultated.get(realPosition).getName() + "/" + zombieListAuscultated.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListAuscultated.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListAuscultated.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );


                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListAuscultated.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListAuscultated.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListAuscultated.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListAuscultated.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListAuscultated.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListAuscultated.get(realPosition).getAuscultateResult());


                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );

                    (view.findViewById(R.id.specified_content_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListAuscultated.get(realPosition).getName());

                                    View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);
                                    ((EditText)(popupView.findViewById(R.id.result_details))).setText(zombieListAuscultated.get(realPosition).getAuscultateResult());
//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);


//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListAuscultated.get(realPosition).getName() + "/" + zombieListAuscultated.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListAuscultated.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListAuscultated.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );


                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListAuscultated.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListAuscultated.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListAuscultated.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListAuscultated.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListAuscultated.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListAuscultated.get(realPosition).getAuscultateResult());


                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );




                    return view;
                }else
                    return null;

            }


        };

        listView_auscultated.setAdapter(baseAdapterTabAuscultated);
    }

    //其实都可以用一个函数来解决这些问题。不过写都写了。算求了。
    /**
     * 用来填充TabdidntAuscultate页面的listView的函数。(用baseAdapter来实现)
     */
    private void functionFillListViewDidntWithBaseAdapter(){
        baseAdapterTabDidnt = new BaseAdapter() {
            @Override
            public int getCount() {
                return zombieListDidntAuscultate.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return zombieListDidntAuscultate.size() - position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (zombieListDidntAuscultate.size() > 0){
                    final View view = mInflater.inflate(R.layout.layout_swipe,null);
                    final int realPosition = zombieListDidntAuscultate.size() - position - 1; //按时间最新从上到下显示。

                    ((TextView)(view.findViewById(R.id.date_swipe))).setText(zombieListDidntAuscultate.get(realPosition).getDate());
                    ((TextView)(view.findViewById(R.id.name_swipe))).setText(zombieListDidntAuscultate.get(realPosition).getName());
                    ((TextView)(view.findViewById(R.id.number_swipe))).setText(zombieListDidntAuscultate.get(realPosition).getOrderNumber());
                    ((TextView)(view.findViewById(R.id.money_amount_swipe))).setText(zombieListDidntAuscultate.get(realPosition).getMoneyAmount());
                   if (zombieListDidntAuscultate.get(realPosition).getHandledState()){
                        (view.findViewById(R.id.unhandled_state_swipe)).setVisibility(View.INVISIBLE);
                        (view.findViewById(R.id.handled_state_swipe)).setVisibility(View.VISIBLE);
                        if (zombieListDidntAuscultate.get(realPosition).getAcceptState())
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已接受");
                        else
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已拒绝");
                    }
                    if (zombieListDidntAuscultate.get(realPosition).getAuscultateState())
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("已诊断");
                    else
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("未诊断");

                    if (zombieListDidntAuscultate.get(realPosition).getPaymentState())
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("已付款");
                    else
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("未付款");


                    (view.findViewById(R.id.play_pause_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }
                    );

                    (view.findViewById(R.id.stop_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                    sendBroadcast(intent);
                                }
                            }
                    );

                    (view.findViewById(R.id.details_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListDidntAuscultate.get(realPosition).getName());

                                    final View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListDidntAuscultate.get(realPosition).getName() + "/" + zombieListDidntAuscultate.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListDidntAuscultate.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListDidntAuscultate.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

//                                    popupView.findViewById(R.id.commit_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setHandledState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setAcceptState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateResult(
//                                                            ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
//                                                    );
//                                                    zombieListAuscultated.add(zombieListDidntAuscultate.get(realPosition));
//
//                                                    //数据库更新。
//
//                                                    ContentValues contentValues = new ContentValues();
//                                                    contentValues.put("auscultateState",true);
//                                                    contentValues.put("handledState",true);
//                                                    contentValues.put("acceptState ",true);
//                                                    contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                    //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                    updateDatabase(contentValues,zombieListDidntAuscultate.get(realPosition).getOrderNumber());
//                                                    zombieListDidntAuscultate.remove(realPosition);
//
//
//                                                    new Thread(
//                                                            new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    Collections.sort(zombieListAuscultated,order_number_comparator);
//                                                                    runOnUiThread(
//                                                                            new Runnable() {
//                                                                                @Override
//                                                                                public void run() {
//                                                                                    baseAdapterTabDidnt.notifyDataSetChanged();
//                                                                                    baseAdapterTabAuscultated.notifyDataSetChanged();
//                                                                                }
//                                                                            }
//                                                                    );
//                                                                    Intent intent = new Intent();
//                                                                    intent.setAction(NetWorkService.SUBMIT_RESULT);
//                                                                    sendBroadcast(intent);
//                                                                }
//                                                            }
//                                                    ).start();
//
//
//                                                }
//                                            }
//                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListDidntAuscultate.get(realPosition).getAuscultateResult());

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    customizeDialog.setPositiveButton("确认提交评价",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
//                                                    popupWindow.dismiss();
                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setHandledState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setAcceptState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateResult(
                                                            ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
                                                    );



                                                    zombieListAuscultated.add(zombieListDidntAuscultate.get(realPosition));

                                                    //数据库更新。

                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put("auscultateState",true);
                                                    contentValues.put("handledState",true);
                                                    contentValues.put("acceptState ",true);
                                                    contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                    //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
                                                    updateDatabase(contentValues,zombieListDidntAuscultate.get(realPosition).getOrderNumber());



                                                    new Thread(
                                                            new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Collections.sort(zombieListAuscultated,order_number_comparator);
                                                                    runOnUiThread(
                                                                            new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    baseAdapterTabDidnt.notifyDataSetChanged();
                                                                                    baseAdapterTabAuscultated.notifyDataSetChanged();
                                                                                    Toast.makeText(MainActivityDoctor.this,"提交成功",Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                    );
                                                                    Intent intent = new Intent();
                                                                    intent.setAction(NetWorkService.SUBMIT_RESULT);
                                                                    intent.putExtra("order_id",zombieListDidntAuscultate.get(realPosition).getOrderNumber());
                                                                    intent.putExtra("is_diagnosed","1");
                                                                    intent.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                                    sendBroadcast(intent);
                                                                    //remove要放在处理完之后，否则会空指针异常。
                                                                    zombieListDidntAuscultate.remove(realPosition);

//                                                                    Intent intentUploadResult = new Intent();
//                                                                    intentUploadResult.setAction(NetWorkService.UPLOAD_RESULT);
//                                                                    intentUploadResult.putExtra("order_id",zombieListDidntAuscultate.get(realPosition).getOrderNumber());
//                                                                    intentUploadResult.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                                    sendBroadcast(intentUploadResult);
                                                                }
                                                            }
                                                    ).start();


                                                }
                                            }

                                    );
                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );
                    (view.findViewById(R.id.specified_content_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListDidntAuscultate.get(realPosition).getName());

                                    final View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListDidntAuscultate.get(realPosition).getName() + "/" + zombieListDidntAuscultate.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListDidntAuscultate.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListDidntAuscultate.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

//                                    popupView.findViewById(R.id.commit_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setHandledState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setAcceptState(true);
//                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateResult(
//                                                            ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
//                                                    );
//                                                    zombieListAuscultated.add(zombieListDidntAuscultate.get(realPosition));
//
//                                                    //数据库更新。
//
//                                                    ContentValues contentValues = new ContentValues();
//                                                    contentValues.put("auscultateState",true);
//                                                    contentValues.put("handledState",true);
//                                                    contentValues.put("acceptState ",true);
//                                                    contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                    //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
//                                                    updateDatabase(contentValues,zombieListDidntAuscultate.get(realPosition).getOrderNumber());
//                                                    zombieListDidntAuscultate.remove(realPosition);
//
//
//                                                    new Thread(
//                                                            new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    Collections.sort(zombieListAuscultated,order_number_comparator);
//                                                                    runOnUiThread(
//                                                                            new Runnable() {
//                                                                                @Override
//                                                                                public void run() {
//                                                                                    baseAdapterTabDidnt.notifyDataSetChanged();
//                                                                                    baseAdapterTabAuscultated.notifyDataSetChanged();
//                                                                                }
//                                                                            }
//                                                                    );
//                                                                    Intent intent = new Intent();
//                                                                    intent.setAction(NetWorkService.SUBMIT_RESULT);
//                                                                    sendBroadcast(intent);
//                                                                }
//                                                            }
//                                                    ).start();
//
//
//                                                }
//                                            }
//                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListDidntAuscultate.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListDidntAuscultate.get(realPosition).getAuscultateResult());

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    customizeDialog.setPositiveButton("确认提交评价",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
//                                                    popupWindow.dismiss();
                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setHandledState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setAcceptState(true);
                                                    zombieListDidntAuscultate.get(realPosition).setAuscultateResult(
                                                            ((EditText)popupView.findViewById(R.id.result_details)).getText().toString()
                                                    );



                                                    zombieListAuscultated.add(zombieListDidntAuscultate.get(realPosition));

                                                    //数据库更新。

                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put("auscultateState",true);
                                                    contentValues.put("handledState",true);
                                                    contentValues.put("acceptState ",true);
                                                    contentValues.put("auscultatedResult ",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                    //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
                                                    updateDatabase(contentValues,zombieListDidntAuscultate.get(realPosition).getOrderNumber());



                                                    new Thread(
                                                            new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Collections.sort(zombieListAuscultated,order_number_comparator);
                                                                    runOnUiThread(
                                                                            new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    baseAdapterTabDidnt.notifyDataSetChanged();
                                                                                    baseAdapterTabAuscultated.notifyDataSetChanged();
                                                                                    Toast.makeText(MainActivityDoctor.this,"提交成功",Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                    );
                                                                    Intent intent = new Intent();
                                                                    intent.setAction(NetWorkService.SUBMIT_RESULT);
                                                                    intent.putExtra("order_id",zombieListDidntAuscultate.get(realPosition).getOrderNumber());
                                                                    intent.putExtra("is_diagnosed","1");
                                                                    intent.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
                                                                    sendBroadcast(intent);
                                                                    //remove要放在处理完之后，否则会空指针异常。
                                                                    zombieListDidntAuscultate.remove(realPosition);

//                                                                    Intent intentUploadResult = new Intent();
//                                                                    intentUploadResult.setAction(NetWorkService.UPLOAD_RESULT);
//                                                                    intentUploadResult.putExtra("order_id",zombieListDidntAuscultate.get(realPosition).getOrderNumber());
//                                                                    intentUploadResult.putExtra("result",((EditText)popupView.findViewById(R.id.result_details)).getText().toString());
//                                                                    sendBroadcast(intentUploadResult);
                                                                }
                                                            }
                                                    ).start();


                                                }
                                            }

                                    );
                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );

                    return view;
                } else
                    return null;

            }
        };

        listView_Didnt.setAdapter(baseAdapterTabDidnt);
    }


    /**
     * 用来填充RefusedAuscultate页面的listView的函数。(用baseAdapter来实现)
     */
    private void functionFillListViewRefusedWithBaseAdapter(){
        baseAdapterTabRefused = new BaseAdapter() {
            @Override
            public int getCount() {
                return zombieListRefused.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return zombieListRefused.size() - position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (zombieListRefused.size() > 0){
                    final View view = mInflater.inflate(R.layout.layout_swipe,null);
                    final int realPosition = zombieListRefused.size() - position - 1; //按时间最新从上到下显示。

                    ((TextView)(view.findViewById(R.id.date_swipe))).setText(zombieListRefused.get(realPosition).getDate());
                    ((TextView)(view.findViewById(R.id.name_swipe))).setText(zombieListRefused.get(realPosition).getName());
                    ((TextView)(view.findViewById(R.id.number_swipe))).setText(zombieListRefused.get(realPosition).getOrderNumber());
                    ((TextView)(view.findViewById(R.id.money_amount_swipe))).setText(zombieListRefused.get(realPosition).getMoneyAmount());

                    if (zombieListRefused.get(realPosition).getHandledState()){
                        (view.findViewById(R.id.unhandled_state_swipe)).setVisibility(View.INVISIBLE);
                        (view.findViewById(R.id.handled_state_swipe)).setVisibility(View.VISIBLE);
                        if (zombieListRefused.get(realPosition).getAcceptState())
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已接受");
                        else
                            ((TextView)(view.findViewById(R.id.choice_swipe))).setText("已拒绝");
                    }
                    if (zombieListRefused.get(realPosition).getAuscultateState())
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("已诊断");
                    else
                        ((TextView)(view.findViewById(R.id.auscultation_statement_all_layout))).setText("未诊断");

                    if (zombieListRefused.get(realPosition).getPaymentState())
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("已付款");
                    else
                        ((TextView)(view.findViewById(R.id.payment_statement_all_layout))).setText("未付款");

                    (view.findViewById(R.id.play_pause_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }
                    );

                    (view.findViewById(R.id.stop_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                    sendBroadcast(intent);
                                }
                            }
                    );

                    (view.findViewById(R.id.details_bottom_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListRefused.get(realPosition).getName());

                                    View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListRefused.get(realPosition).getName() + "/" + zombieListRefused.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListRefused.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListRefused.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListRefused.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListRefused.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListRefused.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListRefused.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListRefused.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListRefused.get(realPosition).getAuscultateResult());

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);

                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );

                    (view.findViewById(R.id.specified_content_swipe)).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //只要一进入订单详情页面查看，我们先将静态的用户个人信息先清掉。
                                    //后面可以加上判断，如果是id相同的话，那么就不用再次去请求。
                                    setIntentToGetPatientInformation(zombieListRefused.get(realPosition).getName());

                                    View popupView = MainActivityDoctor.this.getLayoutInflater().inflate(R.layout.order_detials_layout,null);

//                                    final PopupWindow popupWindow = new PopupWindow(
//                                            popupView,
//                                            getWindowManager().getDefaultDisplay().getWidth()*4/5,
//                                            getWindowManager().getDefaultDisplay().getHeight()*4/5);

                                    popupView.findViewById(R.id.download_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent  = new Intent();
                                                    intent.setAction(NetWorkService.DOWNLOAD_MEDIA);
                                                    intent
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_HTTP_URL,
                                                                    main_url + zombieListRefused.get(realPosition).getName() + "/" + zombieListRefused.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_NAME,
                                                                    zombieListRefused.get(realPosition).getMediaName())
                                                            .putExtra(
                                                                    NetWorkService.MEDIA_SAVED_AS_FILE_PATH,
                                                                    voice_saved_path
                                                            );

                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.play_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.PLAY_MEDIA);
                                                    intent.putExtra(Mp3PlayerService.MEDIA_URL,
                                                            voice_saved_path + "/" + zombieListRefused.get(realPosition).getMediaName()
                                                    );
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

                                    popupView.findViewById(R.id.stop_details).setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Mp3PlayerService.STOP_MEDIA);
                                                    sendBroadcast(intent);
                                                }
                                            }
                                    );

//                                    popupView.findViewById(R.id.close_details).setOnClickListener(
//                                            new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    popupWindow.dismiss();
//                                                }
//                                            }
//                                    );

                                    ((TextView)popupView.findViewById(R.id.name_details)).
                                            setText(zombieListRefused.get(realPosition).getName());
                                    ((TextView)popupView.findViewById(R.id.gender_details)).
                                            setText(zombieListRefused.get(realPosition).getGender());
                                    ((TextView)popupView.findViewById(R.id.description_details)).
                                            setText(zombieListRefused.get(realPosition).getDescription());
                                    ((TextView)popupView.findViewById(R.id.regular_details)).
                                            setText(zombieListRefused.get(realPosition).getRegularCheck());
                                    ((TextView)popupView.findViewById(R.id.media_name_details)).
                                            setText(zombieListRefused.get(realPosition).getMediaName());
                                    ((TextView)(popupView.findViewById(R.id.result_details))).setText(zombieListRefused.get(realPosition).getAuscultateResult());

                                    AlertDialog.Builder customizeDialog =
                                            new AlertDialog.Builder(MainActivityDoctor.this);
                                    customizeDialog.setView(popupView);

                                    customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    //在这里调出新的dialog 显示信息。
                                    popupView.findViewById(R.id.details_details).setOnClickListener(patientInformationClickListener);
                                    customizeDialog.show();
//                                    popupWindow.setFocusable(true);
//                                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
//                                    popupWindow.showAtLocation(findViewById(R.id.main_view),Gravity.CENTER,0,0);
                                }
                            }
                    );

//                    ((view.findViewById(R.id.accept_swipe))).setOnClickListener(
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Log.d("swipe","Clicked");
//                                    String number = String.
//                                            valueOf(((TextView)(view.findViewById(R.id.number_swipe))).
//                                                    getText());
//                                    Intent intent = new Intent();
//                                    intent.setAction(MainActivityDoctor.orderAccepted);
//                                    intent.putExtra(MainActivityDoctor.orderNumber,number);
//                                    sendBroadcast(intent);
//                                }
//                            }
//                    );
//                    ((view.findViewById(R.id.refuse_swipe))).setOnClickListener(
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    String number = String.
//                                            valueOf(((TextView)(view.findViewById(R.id.number_swipe))).
//                                                    getText());
//                                    Intent intent = new Intent();
//                                    intent.setAction(MainActivityDoctor.orderRefused);
//                                    intent.putExtra(MainActivityDoctor.orderNumber,number);
//                                    sendBroadcast(intent);
//                                }
//                            }
//                    );
                    return view;
                } else
                    return null;

            }
        };

        listView_refused.setAdapter(baseAdapterTabRefused);
    }


    //--------------------用Adapter填充服务信息实现代码 end-----------------------//


    //--------------------接收广播实现代码 start------------------------------------//


    public void initIntentFilter(){
        intentFilterBroadcast = new IntentFilter();
        intentFilterBroadcast.addAction(MainActivityDoctor.orderAccepted);
        intentFilterBroadcast.addAction(MainActivityDoctor.orderRefused);
        intentFilterBroadcast.addAction(MainActivityDoctor.newOrder);
        intentFilterBroadcast.addAction(MainActivityDoctor.FILE_EXISTING_STATE);
        intentFilterBroadcast.addAction(MainActivityDoctor.ORDER_COMMIT);
        intentFilterBroadcast.addAction(MainActivityDoctor.SUBMIT_RESULT_FEEDBACK);
        intentFilterBroadcast.addAction(MainActivityDoctor.QUERY_INFORMATION_FEEDBACK);
        intentFilterBroadcast.addAction(MainActivityDoctor.UPDATE_INFORMATION_FEEDBACK);
        intentFilterBroadcast.addAction(MainActivityDoctor.QUERY_DOCTOR_INFORMATION_FEEDBACK);
    }

    public class mReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(MainActivityDoctor.orderAccepted)){
                //简易查找。 这里的查找到后面的订单开始多起来的时候，可能会非常卡。要不以后另开线程？
                for (int i = 0;i < zombieListAll.size();i++){
                    if (zombieListAll.get(i).getOrderNumber().equals(intent.getStringExtra(MainActivityDoctor.orderNumber))){
                        zombieListAll.get(i).setHandledState(true);
                        zombieListAll.get(i).setAcceptState(true);
                        zombieListDidntAuscultate.add(zombieListAll.get(i));
                        //更新数据库
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("handledState",true);
                        contentValues.put("acceptState ",true);
                        //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
                        updateDatabase(contentValues,intent.getStringExtra(MainActivityDoctor.orderNumber));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Collections.sort(zombieListDidntAuscultate,order_number_comparator);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                baseAdapterTabDidnt.notifyDataSetChanged();
                                            }
                                        }
                                );
                            }
                        }).start();


                        break;
                    }
                }
                baseAdapterTabAll.notifyDataSetChanged();
                Log.d("broadcast",intent.getStringExtra(MainActivityDoctor.orderNumber) + "accepted");
            } else if (intent.getAction().equals(MainActivityDoctor.orderRefused)){
                for (int i = 0;i < zombieListAll.size();i++){
                    if (zombieListAll.get(i).getOrderNumber().equals(intent.getStringExtra(MainActivityDoctor.orderNumber))){
                        zombieListAll.get(i).setHandledState(true);
                        zombieListAll.get(i).setAcceptState(false);
                        zombieListRefused.add(zombieListAll.get(i));
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("handledState",true);
                        contentValues.put("acceptState ",false);
                        //文件读写操作，也许另开线程好的一点？虽然这个数据量很小。
                        updateDatabase(contentValues,intent.getStringExtra(MainActivityDoctor.orderNumber));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Collections.sort(zombieListRefused,order_number_comparator);

                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                baseAdapterTabRefused.notifyDataSetChanged();
                                            }
                                        }
                                );
                            }
                        }).start();
                        break;
                    }
                }
                baseAdapterTabAll.notifyDataSetChanged();
                Log.d("broadcast",intent.getStringExtra(MainActivityDoctor.orderNumber) + "refused");
            } else if (intent.getAction().equals(MainActivityDoctor.newOrder)){
                Log.d("broadcast","new order");
                //在这里开个线程？就算阻塞也会弄过去，不然的话，可能让某一次的信号流失。
                //无济于事。
                baseAdapterTabAll.notifyDataSetChanged();
                Collections.sort(zombieListAuscultated,order_number_comparator);
                Collections.sort(zombieListDidntAuscultate,order_number_comparator);
                Collections.sort(zombieListRefused,order_number_comparator);
                baseAdapterTabAuscultated.notifyDataSetChanged();
                baseAdapterTabDidnt.notifyDataSetChanged();
                baseAdapterTabRefused.notifyDataSetChanged();
            } else if (intent.getAction().equals(MainActivityDoctor.FILE_EXISTING_STATE)){
                Toast.makeText(MainActivityDoctor.this,"文件已存在，请直接播放。",Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(MainActivityDoctor.SUBMIT_RESULT_FEEDBACK)){
                if (intent.getStringExtra("feedbackState").equals("1")){
                    Toast.makeText(MainActivityDoctor.this,"提交诊断结果成功",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MainActivityDoctor.this,"提交诊断结果失败",Toast.LENGTH_SHORT).show();
            }   else if (intent.getAction().equals(MainActivityDoctor.UPDATE_INFORMATION_FEEDBACK)){
                //我们在这个地方反馈结果。
                    if (intent.getStringExtra("feedback").equals("1")){
                        Toast.makeText(MainActivityDoctor.this,"更新医生个人信息成功",Toast.LENGTH_SHORT).show();
                    } else if (intent.getStringExtra("feedback").equals("0")){
                        Toast.makeText(MainActivityDoctor.this,"更新医生个人信息失败",Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(MainActivityDoctor.this,"该医生不存在",Toast.LENGTH_SHORT).show();
                    }

            }   else if (intent.getAction().equals(MainActivityDoctor.QUERY_DOCTOR_INFORMATION_FEEDBACK)){
                //我们在这个地方反馈结果。
                if (intent.getStringExtra("feedback").equals("1")){
                    Toast.makeText(MainActivityDoctor.this,"同步云端医生信息成功",Toast.LENGTH_SHORT).show();
                } else if (intent.getStringExtra("feedback").equals("0")){
                    Toast.makeText(MainActivityDoctor.this,"同步云端医生信息失败",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    //--------------------接收广播实现代码 end--------------------------------------//


    //--------------------创建僵尸订单 start--------------------------------------//
    //这里我们创建了虚拟服务
    //用一个ArrayList来维护所有的订单信息。
    public static List<OrderInformation> zombieListAll;
    public static List<OrderInformation> zombieListDidntAuscultate;
    public static List<OrderInformation> zombieListRefused;
    public static List<OrderInformation> zombieListAuscultated;
    public void functionCreateZombieOrders(){
        zombieListAll = new ArrayList<>();
        zombieListDidntAuscultate = new ArrayList<>();
        zombieListRefused = new ArrayList<>();
        zombieListAuscultated = new ArrayList<>();
//        for ( int i = 0; i < tempNames.length;i++){
//            zombieListAll.add(new OrderInformation(
//                    tempOrderNumbers[i],
//                    tempNames[i],
//                    tempDates[i],
//                    tempMoneyAmounts[i],
//                    tempMediaName[i],
//                    tempDescription[i],
//                    tempGender[i],
//                    tempRegularCheck[i]
//                    ));
//        }

        //4.24 数据库暂时弃用。
        //在这个地方创建数据库。如果不存在则创建，存在则打开。
        orderInformationDatabaseHelper = new OrderInformationDatabaseHelper(
                this,
                doctorId + order_database_name,
                1);
        Log.d("database",String.valueOf(Environment.getExternalStorageDirectory().getPath() + "/" + doctorId + order_database_name));

        //数据库存在，我们取出其中的数据，并且填充到订单列表中去。
        SQLiteDatabase sqLiteDatabase = orderInformationDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from order_information",null);
        //cursor 在一开始的值应该是-1.
        try{
            while(cursor.moveToNext()){
                //全部信息填充到all list中。
//                zombieListAll.add(new OrderInformation(
//                        cursor.getString(cursor.getColumnIndex("orderNumber")),
//                        cursor.getString(cursor.getColumnIndex("name")),
//                        cursor.getString(cursor.getColumnIndex("date")),
//                        cursor.getString(cursor.getColumnIndex("moneyAmount")),
//                        cursor.getString(cursor.getColumnIndex("mediaName")),
//                        cursor.getString(cursor.getColumnIndex("description")),
//                        cursor.getString(cursor.getColumnIndex("gender")),
//                        cursor.getString(cursor.getColumnIndex("regularCheck")),
//                        cursor.getString(cursor.getColumnIndex("auscultatedResult")),
//                        cursor.getString(cursor.getColumnIndex("paymentState"))
//                ));
                zombieListAll.get(zombieListAll.size() - 1).setAuscultateResult(cursor.getString(cursor.getColumnIndex("auscultatedResult")));
                //从all list 中提取订单特征，分发到其他的几个list中去。填充好数据之后，要拿这些数据去填充list view。后面的操作会自动拿这些数据去填充。
                zombieListAll.get(zombieListAll.size() - 1).setAcceptState(cursor.getString(cursor.getColumnIndex("acceptState")).equals("1"));
                zombieListAll.get(zombieListAll.size() - 1).setHandledState(cursor.getString(cursor.getColumnIndex("handledState")).equals("1"));
                zombieListAll.get(zombieListAll.size() - 1).setAuscultateState(cursor.getString(cursor.getColumnIndex("auscultateState")).equals("1"));
                zombieListAll.get(zombieListAll.size() - 1).setPaymentState(cursor.getString(cursor.getColumnIndex("paymentState")).equals("1"));

                if (zombieListAll.get(zombieListAll.size() - 1).getHandledState()){
                    //若已经处理过了，还要再判断是处理到什么状态了。
                    if (zombieListAll.get(zombieListAll.size() - 1).getAuscultateState()){
                        //已诊断
                        zombieListAuscultated.add(zombieListAll.get(zombieListAll.size() - 1));
                    }
                    if (zombieListAll.get(zombieListAll.size() - 1).getAcceptState() &&
                            !zombieListAll.get(zombieListAll.size() - 1).getAuscultateState()){
                        //已接受，未诊断。
                        zombieListDidntAuscultate.add(zombieListAll.get(zombieListAll.size() - 1));
                    }
                    if (!zombieListAll.get(zombieListAll.size() - 1).getAcceptState()){
                        //已拒绝。
                        zombieListRefused.add(zombieListAll.get(zombieListAll.size() - 1));
                    }
                }

    //            Log.d("database",
    //                    cursor.getString(0) + " " +
    //                            cursor.getString(1) + " " +
    //                            cursor.getString(2) + " " +
    //                            cursor.getString(3) + " " +
    //                            cursor.getString(4) + " " +
    //                            cursor.getString(5) + " " +
    //                            cursor.getString(6) + " " +
    //                            cursor.getString(7) + " " +
    //                            cursor.getString(8) + " " +
    //                            cursor.getString(9) + " " +
    //                            cursor.getString(10) + " " +
    //                            cursor.getString(11) + " " +
    //                            cursor.getString(12)
    //            );
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //--------------------创建僵尸订单 end--------------------------------------//


    //-------------------------listener start-----------------------------------//

    @Override
    public boolean onMenuItemClick(MenuItem m){
        Log.d("Menu","clicked");
        if(m.getItemId() == R.id.require_new_data && !NetWorkService.isAccessingData){
            Intent intent = new Intent();
            intent.putExtra("doctorId",doctorId);
            intent.putExtra("page",0);
            //每次从头获取数据的时候要把 page 页清掉重新获取数据,否则在新订单数量太多的时候会丢失掉一部分
            //这样的话，我们本地就不要保存了，每次都直接从服务器上面通讯获取订单信息。
            //把listview的信息也重新载入一遍。
            page = 0;
//            MainActivityDoctor.zombieListAll = new ArrayList<>();
            MainActivityDoctor.zombieListAll.clear();
            MainActivityDoctor.zombieListAuscultated.clear();
            MainActivityDoctor.zombieListDidntAuscultate.clear();
            MainActivityDoctor.zombieListRefused.clear();
            baseAdapterTabAll.notifyDataSetChanged();
            baseAdapterTabAuscultated.notifyDataSetChanged();
            baseAdapterTabRefused.notifyDataSetChanged();
            baseAdapterTabDidnt.notifyDataSetChanged();
            intent.setAction(NetWorkService.REQUIRE_DATA);
            //@3.需要将本地最新的订单编号传过去，用以回去服务端的最新数据。
            //在没有订单的时候可能数组会越界，写个异常捕捉程序将其捕捉。
            try{
                //Log.d("menu",zombieListAll.get(zombieListAll.size() - 1).getOrderNumber());
                intent.putExtra("local_latest_ordernumber",zombieListAll.get(zombieListAll.size() - 1).getOrderNumber());
                intent.putExtra("local_oldest_ordernumber",zombieListAll.get(0).getOrderNumber());
                intent.putExtra("doctorId",doctorId);
            }catch (ArrayIndexOutOfBoundsException ae){
                ae.printStackTrace();
            }

            sendBroadcast(intent);
            return true;
        }
        return false;
    }

    //-------------------------listener end-----------------------------------//

    //--------------------------data_base_start------------------------------//

    public class OrderInformationDatabaseHelper extends SQLiteOpenHelper{
        final String CREATE_TABLE_SQL=
                "create table order_information(" +
                        "_id integer primary key autoincrement ," +
                        "name nvarchar(20) ," +
                        "orderNumber nvarchar(20) ," +
                        "date nvarchar(50) ," +
                        "moneyAmount nvarchar(20) ," +
                        "mediaName nvarchar(50) ," +
                        "description nvarchar(200) ," +
                        "gender nvarchar(10) ," +
                        "regularCheck nvarchar(10) ," +
                        "acceptState boolean ,"  +
                        "handledState boolean ," +
                        "auscultateState boolean ," +
                        "paymentState boolean ," +
                        "auscultatedResult nvarchar(200)" +
                        ")";

        public OrderInformationDatabaseHelper(Context context,String name,int version){

            super(context,name,null,version);
            Log.d("database","open data base");
            Log.d("database",context.getFilesDir().getPath());
            Log.d("database",String.valueOf(new File(context.getFilesDir().getPath() + "/" + order_database_name).exists()));
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_TABLE_SQL);
            Log.d("database","creating new data base");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,int newVersion){
        }
    }

    private void updateDatabase(ContentValues contentValues,String orderNumber){
        orderInformationDatabaseHelper.getWritableDatabase().
                update("order_information",
                        contentValues,
                        "orderNumber like ?",
                        new String[] {orderNumber});
    }

    //--------------------------------权限申请部分-----------------------------------------------

    private android.app.AlertDialog dialog;

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE};
    private void checkPermissions(){

            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permissions[0]) ||
                    PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permissions[1])){
                new android.app.AlertDialog.Builder(this)
                        .setTitle("（文件存储）权限不可用")
                        .setMessage("本软件需要开启（文件存储、媒体播放）权限。\n否则将无法正常使用。")
                        .setPositiveButton("立即开启",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivityDoctor.this, permissions, 333);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();

            }
        }

    //权限申请的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 333){
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                            grantResults[1] != PackageManager.PERMISSION_GRANTED){
                    startAccessPermissions();
                }else{
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
          }
    }

    private void startAccessPermissions(){
        dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("权限不可用")
                .setMessage("请在-应用设置-权限-中，允许 " + getApplicationInfo().name + " 使用存储、媒体权限来保证正常使用。")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    private void goToAppSetting(){
        Intent intent =new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permissions[0])||
                    PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permissions[1])){
                new android.app.AlertDialog.Builder(this)
                        .setTitle("权限不可用")
                        .setMessage("请在-应用设置-权限-中，允许 " + getApplicationInfo().name + " 使用存储、媒体权限来保存用户数据")
                        .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 跳转到应用设置界面
                                goToAppSetting();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else{
                if (dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setIntentToGetPatientInformation(String patient_id){
        MainActivityDoctor.userInformation = new HashMap<String, String>();
        Intent intentToGetUserInformation = new Intent();
        intentToGetUserInformation.setAction(NetWorkService.QUERY_INFORMATION);
        intentToGetUserInformation.putExtra("patient_id",patient_id);
        sendBroadcast(intentToGetUserInformation);
    }

}

