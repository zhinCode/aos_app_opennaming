/********************************************************************************************************************************************************************/
// MainActivity.class
// Create : 2015-01-10
// Modify : 2015-01-29
// Zingoo@Opennaming.com
// 스플래쉬 및 M.opennaming.com 로드 메인 웹뷰 및 GCM수신
/********************************************************************************************************************************************************************/
package com.opennaming.app;
import java.io.IOException;
import com.opennaming.app.util.PreferenceUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity
{
    private TextView _textStatus; // 정리할것
    private WebView webView_Main;
    /********************************************************************************************************************************************************************/
    private String ServerRootURL = "http://www.opennaming.com";

    /********************************************************************************************************************************************************************/
    //GCM 관련 설정 시작
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "152243536361"; //
    private GoogleCloudMessaging _gcm;
    private String _regId;
    private String PhoneNumber;
    private String GCM_Title;
    private String GCM_msg;
    private String GCM_Type;
    private String GCM_URL;
    private String GCM_Language;
    //GCM 관련 설정 끝
    /********************************************************************************************************************************************************************/
    /********************************************************************************************************************************************************************/
    // 렌덤 스플래쉬 배경 로딩 관련 설정 시작
    private ImageView imageView_Splash;
    private int index = (int) (Math.random() * 5);
    private int res = ran[index];
    private static final int ran[]= {
            R.drawable.bg_splash_1_off, R.drawable.bg_splash_2_off, R.drawable.bg_splash_3_off, R.drawable.bg_splash_4_off, R.drawable.bg_splash_5_off };
    // 렌덤 스플래쉬 배경 로딩 관련 설정 끝
    private boolean is_First_Time;
    /********************************************************************************************************************************************************************/
    /********************************************************************************************************************************************************************/
    // 백버튼 두번 클릭시 액션 관련 설정 시작
    private long backKeyClick=0;
    private long backKeyClickTime;
    private int duration = 2000;
    private String now_url;
    /********************************************************************************************************************************************************************/
    private Handler mHandler;
    private Runnable mRunnable;

    ProgressBar progressBar;

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE=1;
    private final static int KITKAT_RESULTCODE = 2;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }
    public static boolean isNetworkStat( Context context ) {
        ConnectivityManager manager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo lte_4g = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        boolean blte_4g = false;
        if(lte_4g != null)
            blte_4g = lte_4g.isConnected();
        if( mobile != null ) {
            if (mobile.isConnected() || wifi.isConnected() || blte_4g)
                return true;
        } else {
            if ( wifi.isConnected() || blte_4g )
                return true;
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setTitle("네트워크 오류");
        dlg.setMessage("네트워크 상태를 확인해 주십시요.");
        dlg.setIcon(R.drawable.ic_launcher);
        dlg.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dlg.show();
        return false;
    }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    getWindow().requestFeature(Window.FEATURE_PROGRESS); // 프로그레스
    is_First_Time = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

      progressBar = (ProgressBar) this.findViewById(R.id.pro);
    /********************************************************************************************************************************************************************/
    // 스플래쉬 랜덤 배경 세팅
    imageView_Splash = (ImageView)findViewById(R.id.imageView_Splash);
    imageView_Splash.setImageResource(res);
    //스플래쉬, 메인 레이아웃 세팅
    final View layout_Splash = (View)  findViewById(R.id.layout_Splash);
    final View layout_Main = (View)  findViewById(R.id.layout_Main);
    /********************************************************************************************************************************************************************/
    //_textStatus = (TextView) findViewById(R.id.textView_Main_GCM_MSG); // GCM 수신시 출력할 텍스트 뷰 - 현재 버전에서는 GCM 수신시 앱이 실행중이던 아니던 얼럿으로만 뜬다
    /********************************************************************************************************************************************************************/
      // 모바일 번호 추출 관련 시작
    //TelephonyManager systemService = (TelephonyManager)getSystemService    (Context.TELEPHONY_SERVICE);
    //PhoneNumber = systemService.getLine1Number();
    //PhoneNumber = PhoneNumber.substring(PhoneNumber.length()-10,PhoneNumber.length());
    //PhoneNumber = "0" + PhoneNumber;
    /********************************************************************************************************************************************************************/
      // GCM 수신 관련 엑스트라 설정
      /*
      String GCM_Title = getIntent().getStringExtra("GCM_Title");
      String GCM_msg = getIntent().getStringExtra("GCM_msg");
      String GCM_Type = getIntent().getStringExtra("GCM_Type");
      String GCM_Language = getIntent().getStringExtra("GCM_Language");
      */
      /********************************************************************************************************************************************************************/
      // 메인 웹뷰 설정
      webView_Main = (WebView)findViewById(R.id.webView_Main);
      webView_Main.getSettings().setJavaScriptEnabled(true); //자바 스크립트 enable
      webView_Main.setVerticalScrollbarOverlay(true);   //스크롤 영역 웹뷰에 오버레이
      webView_Main.getSettings().setSupportMultipleWindows(true);
//      webView_Main.setWebChromeClient(new WebChromeClient());






      /********************************************************************************************************************************************************************/
      // 웹뷰 클라이언트 오버라이딩
      webView_Main.setWebViewClient(new WebViewClient() { //웹뷰 클라이언트(주소창 없애기 위해)
            // 첫 로딩에만 스플래쉬 이미지 보여주기
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
              Log.i("MainActivity.java | onIntent", "|" + url + "| GCM_Type : " + GCM_Type);
                  Log.i("MainActivity.java | onIntent - GCM_URL", "|" + GCM_URL + "|");
                  url = ServerRootURL + GCM_URL;
              //isNetworkStat(MainActivity.this);
              // 인터넷 에러일 경우
/*
              if(url.contains("NewIntent")){
                  Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                  startActivity(browserIntent);
                  return true;
              }
*/



              if ( is_First_Time ) { //앱 실행시 한번만 실행되게
                  // true 리턴 =  onPageStarted/onPageFinished 스킵ㅂ





                  view.loadUrl(url);
                  return true;
              } else {
                  // false 리턴 =  onPageStarted 호출
                  return false;
              }






          }
          @Override
          public void onPageStarted(WebView view, String url, Bitmap favicon) {
              super.onPageStarted(view, url, favicon);
                if (is_First_Time){
                  layout_Splash.setVisibility(View.VISIBLE);
                  layout_Main.setVisibility(View.INVISIBLE);
              }
              super.onPageStarted(view, url, favicon);
              progressBar.setVisibility(View.VISIBLE);
              is_First_Time = false;
          }
          @Override
          public void onPageFinished(WebView view, String url) {
              super.onPageFinished(view, url);


                  layout_Splash.setVisibility(View.INVISIBLE);
                  layout_Main.setVisibility(View.VISIBLE);
              progressBar.setVisibility(View.INVISIBLE);
          }
          @Override
          public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
              //Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
          try {
              view.stopLoading();
          } catch (Exception e) {
          }
              view.loadUrl("file:///android_asset/error.html");
          }
      }); //end of new WebViewClient(){}

      webView_Main.setWebChromeClient(new WebChromeClient() {
          @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
          {
              WebView newWebView = new WebView(view.getContext());
              WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
              transport.setWebView(newWebView);
              resultMsg.sendToTarget();
              return true;
          }
          @Override
          public void onProgressChanged(WebView view, int newProgress) {
              progressBar.setProgress(newProgress);
          }

      });
    /********************************************************************************************************************************************************************/

      GCM_Title = getIntent().getStringExtra("GCM_Title");
      GCM_msg = getIntent().getStringExtra("GCM_msg");
      GCM_Type = getIntent().getStringExtra("GCM_Type");
      GCM_URL = getIntent().getStringExtra("GCM_URL");
      GCM_Language = getIntent().getStringExtra("GCM_Language");
//      Log.i("MainActivity.java | onNewIntent", "|" + GCM_msg + "|");
      Log.i("MainActivity.java | onIntent", "|" + GCM_Type + "| GCM_URL : " + GCM_URL);



    // GCM 관련 메서드 시작
      if (checkPlayServices()) //google play service가 사용가능한지 분기
      {
          _gcm = GoogleCloudMessaging.getInstance(this);
          _regId = getRegistrationId();
          if (TextUtils.isEmpty(_regId)){// regid 가 없을경우
              registerInBackground();//구글에서 regid 값을 받아오기.
          }else{ //regid 가 있을경우

          }


          if (!TextUtils.isEmpty(GCM_URL)) {// GCM 수신했을경우 (엑스트라 값 수신했을경우)
              //_textStatus.append("\n" + msg + "\n");

                  webView_Main.loadUrl(GCM_URL);
                  Log.i("MainActivity.java | onIntent", " GCM_URL : " + GCM_URL + "|");
          }else{ // GCM_Msg 가 없으면? - > 앱을 그냥 실행한 경우 -> 처음 실행 두번째 실행에 따른 분기 설정해야함

              new Handler().postDelayed(new Runnable()
              {
                  @Override
                  public void run()
                  {
                      webView_Main.loadUrl(ServerRootURL + "/?regid=" + _regId.toString() + "&PhoneNumber=" + /*PhoneNumber.toString()*/ "&DeviceLanguage=" + getResources().getConfiguration().locale.getDefault().getLanguage());
                      Log.i("MainActivity.java | onIntent", "|" + "No Gcm" + "|");
                  }
              }, 2000);






          }



          /*
              if (GCM_type == "01"){
                  webView_Main.loadUrl(ServerRootURL + "/Quiz/?L=" + GCM_Language );
              }else if (GCM_type == "02"){
                  webView_Main.loadUrl(ServerRootURL + "/Result/Result.asp?L=" + GCM_Language );
              }else if (GCM_type == "03"){
                  webView_Main.loadUrl(ServerRootURL + "/Eval/Preference.asp?L=" + GCM_Language);
              }else {
                  webView_Main.loadUrl(ServerRootURL + "/?regid=" + _regId.toString() + "&PhoneNumber=" + PhoneNumber.toString() );
              }
              */





      }else{ // google play service 가 사용가능 하지 않은경우
          webView_Main.loadUrl(ServerRootURL + "/?regid=&PhoneNumber=" + /*PhoneNumber.toString()*/  "&DeviceLanguage=" + getResources().getConfiguration().locale.getDefault().getLanguage());
          Log.i("MainActivity.java | onIntent", "|" + "No play service" + "|");
      }

      //GCM 관련 메서드 끝
      /********************************************************************************************************************************************************************/

  } // end of onCreate();

    /********************************************************************************************************************************************************************/
    // 스플래쉬 화면 로고 애니메이션 관련 시작
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub

        ImageView imageView_Logo = (ImageView) findViewById(R.id.imageView_Logo);
        imageView_Logo.setVisibility(ImageView.VISIBLE);
        imageView_Logo.setBackgroundResource(R.drawable.animation_logo);
        AnimationDrawable frameAnimation = (AnimationDrawable) imageView_Logo.getBackground();
        frameAnimation.start();
        super.onWindowFocusChanged(hasFocus);
    }
    // 스플래쉬 화면 로고 애니메이션 관련 끝
    /********************************************************************************************************************************************************************/
    // 백버튼 관련 액션을 위한 오버라이딩 시작
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        now_url = webView_Main.getOriginalUrl().toString();
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView_Main.canGoBack()) {
            // URL 에 DE Dead End 가 있을경우는 백버튼 종료 시작
            if (now_url.contains("DE")) {
                long currentTime = System.currentTimeMillis();
                backKeyClick ++;
                if (backKeyClick == 1) // -->
                {
                    backKeyClickTime = System.currentTimeMillis();
                    Toast t =  Toast.makeText(MainActivity.this, getString(R.string.toast_text1),Toast.LENGTH_SHORT);
                    //LinearLayout toastLayout = (LinearLayout) t.getView();
                    //TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    //toastTV.setTextSize(11);
                    //뒤로가기 버튼을 한번 더 누르면 앱이 종료 됩니다.
                    t.setDuration(Toast.LENGTH_SHORT);
                    t.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(duration);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            backKeyClick=0;
                        }
                    }).start();
                }
                else if(backKeyClick == 2) // -->
                {
                    if(currentTime - backKeyClickTime <= duration  ){
                        return super.onKeyDown(keyCode, event);
                    }
                    backKeyClick = 0;
                }
                return true;
            } else {
                webView_Main.goBack();
                return true;
            }
        }else {
            long currentTime = System.currentTimeMillis();
            backKeyClick ++;
            if (backKeyClick == 1) // 백버튼이 한번만 눌린경우
            {
                backKeyClickTime = System.currentTimeMillis();
                Toast t =  Toast.makeText(MainActivity.this, getString(R.string.toast_text1),Toast.LENGTH_SHORT);
                //뒤로가기 버튼을 한번 더 누르면 앱이 종료 됩니다.
                //LinearLayout toastLayout = (LinearLayout) t.getView();
                //TextView toastTV = (TextView) toastLayout.getChildAt(0);
                //toastTV.setTextSize(11);
                //뒤로가기 버튼을 한번 더 누르면 앱이 종료 됩니다.
                t.setDuration(Toast.LENGTH_SHORT);
                t.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(duration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        backKeyClick=0;
                    }
                }).start();
            }
            else if(backKeyClick == 2) // 백버튼이 두번 눌린경우
            {
                if(currentTime - backKeyClickTime <= duration  ){
                    return super.onKeyDown(keyCode, event);
                }
                backKeyClick = 0;
            }
            return true;
        }
        //return super.onKeyDown(keyCode, event);
    } // end of Override.onKeyDown()
    // 백버튼 관련 액션을 위한 오버라이딩 끝
    /********************************************************************************************************************************************************************/
  @Override
  protected void onNewIntent(Intent intent) // GCM 받았을때 액션 설정 -> 정리 해야함
  {
    super.onNewIntent(intent);
      setIntent(intent);
      // display received msg
      GCM_Title = getIntent().getStringExtra("GCM_Title");
      GCM_msg = getIntent().getStringExtra("GCM_msg");
      GCM_Type = getIntent().getStringExtra("GCM_Type");
      GCM_URL = getIntent().getStringExtra("GCM_URL");
      GCM_Language = getIntent().getStringExtra("GCM_Language");
//      Log.i("MainActivity.java | onNewIntent", "|" + GCM_msg + "|");
      Log.i("MainActivity.java | onNewIntent", "|" + GCM_Type + "| GCM_URL : " + GCM_URL);
      webView_Main.loadUrl(GCM_URL );
  } // end of onNewIntent()

  /********************************************************************************************************************************************************************/
  // google play service가 사용가능한지 알아내는 매서드 시작
  private boolean checkPlayServices()
  {
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS)
    {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
      {
        //GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
      }
      else
      {
        //Log.i("MainActivity.java | checkPlayService", "|This device is not supported.|");
        //_textStatus.append("\n This device is not supported.\n");
        //finish();
      }
      return false;
    }
    return true;
  }
  /********************************************************************************************************************************************************************/
  // registration id를 가져온다.
  private String getRegistrationId()
  {
    String registrationId = PreferenceUtil.instance(getApplicationContext()).regId();
    if (TextUtils.isEmpty(registrationId))
    {
      Log.i("MainActivity.java | getRegistrationId", "|Registration not found.|");
      //_textStatus.append("\n Registration not found.\n");
      return "";
    }
    int registeredVersion = PreferenceUtil.instance(getApplicationContext()).appVersion();
    int currentVersion = getAppVersion();
    if (registeredVersion != currentVersion)
    {
      Log.i("MainActivity.java | getRegistrationId", "|App version changed.|");
      //_textStatus.append("\n App version changed.\n");
      return "";
    }
    return registrationId;
  }
  /********************************************************************************************************************************************************************/
  // app version을 가져온다. 정리 필요
  private int getAppVersion()
  {
    try
    {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      return packageInfo.versionCode;
    }
    catch (NameNotFoundException e)
    {
      // should never happen
      throw new RuntimeException("Could not get package name: " + e);
    }
  }
/********************************************************************************************************************************************************************/
//gcm 서버에 접속해서 registration id를 발급받는다.
  private void registerInBackground()
  {
    new AsyncTask<Void, Void, String>()
    {
      @Override
      protected String doInBackground(Void... params)
      {
        String msg = "";
        try
        {
          if (_gcm == null)
          {
            _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
          }
          _regId = _gcm.register(SENDER_ID);
          msg = "Device registered, registration ID=" + _regId;
          storeRegistrationId(_regId);
        }
        catch (IOException ex)
        {
          msg = "Error :" + ex.getMessage();
        }
        return msg;
      }
      @Override
      protected void onPostExecute(String msg)
      {
        Log.i("MainActivity.java | onPostExecute", "|" + msg + "|");
        //_textStatus.append(msg);
      }
    }.execute(null, null, null);
  }
  /********************************************************************************************************************************************************************/
    //registraion id를 preference에 저장.
  private void storeRegistrationId(String regId)
  {
    int appVersion = getAppVersion();
    Log.i("MainActivity.java | storeRegistrationId", "|" + "Saving regId on app version " + appVersion + "|");
    PreferenceUtil.instance(getApplicationContext()).putRedId(regId);
    PreferenceUtil.instance(getApplicationContext()).putAppVersion(appVersion);
  }
  /********************************************************************************************************************************************************************/
}


