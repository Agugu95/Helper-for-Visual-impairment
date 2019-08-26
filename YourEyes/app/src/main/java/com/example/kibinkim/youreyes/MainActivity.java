package com.example.kibinkim.youreyes;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Camera mCam; //카메라 변수
    private final int REQ_CODE_SPEECH_INPUT = 100; //왜 100으로 초기화했는지??? ......구분하기 위한 숫자이므로 어떤 숫자를 대입해도 무방하다.
    //파라미터로 넘어가는 숫자를 코드를 처음 보는 사람은 어떤 의미인지 모를 수 있으므로 변수로 초기화한 모습
    private TextToSpeech tts; //TTS 변수
    GeoVariable geovariable = new GeoVariable();  // 클래스 변수 사용 위해
    Geocoder geocoder; // 역지오코딩 하기 위해
    double latitude, longitude; // 위도, 경도 전역변수
    List<Address> list = null;
    String urldisaster = "http://52.231.67.53:8080/request_disaster";
    String urldust = "http://52.231.67.53:8080/dust_state";

    //    TextView tv_outPut;
    //    TextView onWhere; // 현재위치 출력위해
    //    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setTitle("시각장애인을 위한 카메라 음성인식 및 기상특보 알리미"); //어플 상단에 보이는 제목

        geocoder = new Geocoder(this);  // 역지오코딩 하기 위해

//        tv_outPut = (TextView) findViewById(R.id.tv_outPut);
//        onWhere = (TextView) findViewById(R.id.onWhere);
//        tv = (TextView) findViewById(R.id.textView);
//        tv.setText("위치정보 미수신중");

        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != -1) { //초기화 시 상태가 Error가 아니라면
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        final Button btnVoice = (Button) findViewById(R.id.btnVoice); //findViewById() -> xml의 버튼코드를 가져와 UI에 보여주기 위한 코드
        final String btnVoiceText = btnVoice.getText().toString(); //버튼 내부 텍스트
        Button btnDisaster = (Button) findViewById(R.id.btnDisaster);
        final String btnDisasterText = btnDisaster.getText().toString();
        Button btnDust = (Button) findViewById(R.id.btnDust);
        final String btnDustText = btnDust.getText().toString();



        // LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        final Switch TTSswitch = (Switch) findViewById(R.id.TTSswitch); // TTS 객체

        btnVoice.setOnClickListener(new View.OnClickListener() { //음성인식 버튼을 눌렀을 때 실행되는 동작
            @Override
            public void onClick(View v) { //(구글 음성인식이 실행되고 어떻게 결과를 받게 되는지 과정)http://m.blog.daum.net/mailss/20?tp_nil_a=2
                tts.speak(btnVoiceText+"이 실행되었습니다.", TextToSpeech.QUEUE_FLUSH, null); //음성인식 버튼을 누르면 음성을 출력.
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //intent생성, ACTION_RECOGNIZE_SPEECH : 사용자의 음성을 인식할 수 있는 액티비티의 호출을 요구하는 액션
                //ACTION_WEB_SEARCH : 음성 인식 액티비티를 실행시키고, 결과로 웹 검색을 추가로 수행하고 반환하는 기능을 하는 액션이다
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //음성 인식 액티비티를 실행할 때 음성 인식에 사용하는 언어 모델을 명시하는 엑스트라. 이 엑스트라는 다음과 추가적인 상수를 사용하여 음성 인식 액티비티에 전달해 주어야 한다
                //LANGUAGE_MODEL_FREE_FORM : 시스템에서 알아서 처리한다.
                //LANGUAGE_MODEL_WEB_SEARCH : 웹 검색 조건을 기반으로 언어 모델을 선택한다.
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); //지정된 카테고리의 Java 가상 머신에 의해 설정된 디폴트의 Locale를 돌려준다. 단말기에 설정된 환경 정보를 사용하여 음성을 인식한다.
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"말씀 해주세요"); //구글 음성인식에 표시되는 텍스트 설정.
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT); //구글 음성인식 실행, 음성인식결과를 가져오기 위해 startActivityForResult메소드를 사용한다.
                    //onActivityResult에서 결과 값을 받기 위해 intent와 REQ_CODE_SPEECH_INPUT를 같이 보낸다. REQ_CODE_SPEECH_INPUT은 onActivityResult에서 요청한 코드를 구분하기 위해 넘겨준다.
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Not Supported", Toast.LENGTH_SHORT).show(); //LENGTH_SHORT와 LENGTH_LONG. Toast메시지를 얼마나 길게 표시되는지의 차이.
                }
            }
        });
        //정리)
        //버튼을 눌렀을 때 실행되는 동작이 들어가있는 onClick(), 사용자의 음성을 인식할 수 있는 액티비티의 호출을 요구하는 액션을 파라미터로 받는 intent 객체 생성(intent.putExtra사용)
        //intent.putExtra를 사용해 언어 설정하고 추가적으로 EXTRA_PROMPT를 사용해 텍스트 설정. 음성인식결과를 가져오기 위해 startActivityForResult메소드를 사용



        TTSswitch.setOnClickListener(new View.OnClickListener() { //스위치를 눌렀을 때 실행되는 동작
            @Override
            public void onClick(View v) {
                if (TTSswitch.isChecked() == true) { //스위치를 킬 때
                    //tts.setPitch(2.0f); tts.setSpeechRate(1.0f);//음성 톤조절, 음성 속도조절
//                    try {
//                        whereami();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    tts.speak(btnVoiceText+"버튼은 우측에 있습니다.", TextToSpeech.QUEUE_FLUSH, null); //FLUSH는 초기값, 음성을 추가하려면 FLUSH 대신 ADD로 추가값
                    tts.speak(btnDisasterText+"버튼은 좌측상단에 있습니다.", TextToSpeech.QUEUE_ADD, null);
                    tts.speak(btnDustText+"버튼은 좌측하단에 있습니다.", TextToSpeech.QUEUE_ADD, null);
                } else { //스위치를 끌 때
                    tts.speak(TTSswitch.getText().toString()+"음성 듣기가 종료되었습니다.", TextToSpeech.QUEUE_FLUSH, null); //.getText() -> TTSswitch를 말그대로 받고, .toString() -> Charsequence로 되어있는 데이터 형태를 String으로 바꿔준다. 삭제하면 알 수 없는 소리가 나옴.
                    moveTaskToBack(true);
                }
                // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록
//                tv.setText("수신중..");

            }
        });

        btnDisaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tts.speak(btnDisasterText+"가 실행되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                try {
                    reverseCoding();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnDust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tts.speak(btnDustText+"가 실행되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                dustAlarm();
            }
        });
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
//            tv.setText("\n위도 : " + longitude + "\n경도 : " + latitude);
            geovariable.setLatitude(latitude); // 클래스 변수에 위도 대입
            geovariable.setLongitude(longitude);  // 클래스 변수에 경도 대입
        }


        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };



    public void reverseCoding() throws Exception { // 위도 경도 넣어가지구 역지오코딩 주소값 뽑아낸다
        try {
            latitude = geovariable.getLatitude(); // 위도 경도 클래스변수에서 가져옴
            longitude = geovariable.getLongitude();
            list = geocoder.getFromLocation(latitude, longitude, 10); // 위도, 경도, 얻어올 값의 개수
            //timer.schedule(task,3000);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size() == 0) {
//                onWhere.setText("해당되는 주소 정보는 없습니다");
                tts.speak("위도 경도가 잡히지 않았습니다.", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                //onWhere.setText(list.get(0).toString()); //원래 통으로 나오는 주소값 문자열

                // 문자열을 자르자!
                String cut[] = list.get(0).toString().split(" ");
                for (int i = 0; i < cut.length; i++) {
                    System.out.println("cut[" + i + "] : " + cut[i]);
                } // cut[0] : Address[addressLines=[0:"대한민국
                // cut[1] : 서울특별시  cut[2] : 송파구  cut[3] : 오금동
                // cut[4] : cut[4] : 41-26"],feature=41-26,admin=null ~~~~
                String city = cut[1];
//                onWhere.setText(city.substring(0, 2));
                String bigCity = city.substring(0, 2); // 내가 원하는 구의 값을 뽑아내 출력

                ContentValues val = new ContentValues();
                val.put("fileAjax", bigCity);
                // AsyncTask를 통해 HttpURLConnection 수행.
                NetworkTask networkTask = new NetworkTask(urldisaster, val);
                networkTask.execute();
            }
        }
    }

//    public void whereami() throws IOException {
//        latitude = geovariable.getLatitude(); // 위도 경도 클래스변수에서 가져옴
//        longitude = geovariable.getLongitude();
//        list = geocoder.getFromLocation(latitude, longitude, 10); // 위도, 경도, 얻어올 값의 개수
//
//        if (list != null) {
//            if (list.size() == 0) {
//                onWhere.setText("해당되는 주소 정보는 없습니다");
//                tts.speak("위도 경도가 잡히지 않았습니다.", TextToSpeech.QUEUE_FLUSH, null);
//            } else {
//                //onWhere.setText(list.get(0).toString()); //원래 통으로 나오는 주소값 문자열
//
//                // 문자열을 자르자!
//                String cut[] = list.get(0).toString().split(" ");
//                for (int i = 0; i < cut.length; i++) {
//                    System.out.println("cut[" + i + "] : " + cut[i]);
//                } // cut[0] : Address[addressLines=[0:"대한민국
//                // cut[1] : 서울특별시  cut[2] : 송파구  cut[3] : 오금동
//                // cut[4] : cut[4] : 41-26"],feature=41-26,admin=null ~~~~
//                String city = cut[1] + cut[2] + cut[3];
//                tts.speak("나의 현재 위치는 " + city + "입니다.", TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }
//    }

    public void dustAlarm() {
        ContentValues val1 = new ContentValues();
        val1.put("fileAjax", 1);
//        NetworkDustTask dustNetwork = new NetworkDustTask(urldust, val1);
//        dustNetwork.execute();
        NetworkTask networkTask = new NetworkTask(urldust, val1);
        networkTask.execute();
    }


    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            RequestHttpUrlConnection requestHttpURLConnection = new RequestHttpUrlConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tts.setSpeechRate(1.0f);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
//            tv_outPut.setText(s);
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //주석에 관한 내용출처: http://ilililililililililili.blogspot.com/2013/07/android.html
        super.onActivityResult(requestCode, resultCode, data); //호출하는 액티비티가 여러개 있을 경우 구분하기 위한 requestCode.
        // REQ_CODE_SPEECH_INPUT의 값을 requestCode로 받는다.
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: { //startActivityResult에서 전달된 REQ_CODE_SPEECH_INPUT
                if (resultCode == RESULT_OK && null != data) { //처리된 결과 코드(resultCode)가 RESULT_OK이고 데이터가 정상적으로 전달이 되었다면 requestCode를 판별해 결과 처리를 진행
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //음성 인식의 결과인 문자열의 리스트를 얻는다.
                    Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_LONG).show(); //사용자가 말한 음성을 Toast메시지에 띄워준다.

                    if (result.get(0).equals("종료")) { //음성인식 버튼 누른 후 종료라고 말했을 때
                        tts.speak("종료 합니다.", TextToSpeech.QUEUE_ADD, null);
                        moveTaskToBack(true);
                        //finish(); //moveTaskToBack()과 같은 기능이지만 종료되는 속도가 빨라 '종료합니다'음성이 들리지 않는다.
                        //timer 변수와 timertask변수 선언 후 timer.schedule(mTask, 6000)를 사용해 종료되는 시간은 딜레이 시키려했는데 앱이 중지되었다는 오류가 발생.
                    }
                    else if (result.get(0).equals("촬영")) { //음성인식 버튼 누른 후 촬영이라고 말했을 때
                        try {
                            SurfaceView surfaceView = new SurfaceView(getApplicationContext()); //(SurfaceView란?) https://k1rha.tistory.com/entry/android-%ED%8E%8C-SurfaceView-%EC%9D%98-%EC%9D%B4%ED%95%B4
                            //정리: 카메라 미리보기. 컨텐츠를 표시하는 View중 하나인데, 일반적인 뷰는 뷰를 화면에 표시하는 처리가 하나의 쓰레드에 처리가 되는데, 이것 때문에 ANR현상이 일어난다. 그에 비해, SurfaceView는 백그라운드 쓰레드를 수행하여 정상적인 동작을 하게 한다.
                            //ANR이란? Application Not Responding. 어플리케이션이 일정 시간동안 아무런 동작이 없을 경우 발생하는 현상.
                            //+) SurfaceView는 미리보기일 뿐, 카메라 디스플레이는 입혀지지 않은 상태인데 setPreviewDisplay와 startPreview메소드를 거침으로써 카메라 디스플레이가 씌워진다.
                            mCam = Camera.open(); //카메라 실행
                            mCam.setPreviewDisplay(surfaceView.getHolder()); //카메라에게 서피스뷰를 알려주는 메서드
                            Camera.Parameters p = mCam.getParameters(); //플래시를 사용하기 위해 p객체 선언
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); //android.hardware.camera의 플래시 기능을 사용
                            mCam.setParameters(p); //플래시 실행
                            mCam.startPreview(); //카메라 미리보기에 시작

                            mCam.takePicture(null, null, mCall); //Raw데이터: 가공하지 않았다는 뜻으로 기록된 원본 데이터를 그대로 저장한다는 뜻.
                            Thread.sleep(1000); //일정 기간동안 쓰레드를 중지시킨다. 해당 어플리케이션 내에 있는 다른 어플리케이션의 쓰레드들에게 프로세서를 이용 가능하도록 만들기 위한 효율적인 방법

                            SurfaceHolder surfaceHolder = surfaceView.getHolder();
                            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //카메라가 SurfaceView를 독점하기 위해선 SurfaceHolder의 타입은 항상 SURFACE_TYPE_PUSH_BUFFERS로 설정한다.
                            tts.speak("사진이 촬영되었습니다. 기다려 주십쇼.", TextToSpeech.QUEUE_FLUSH, null);

                            //sendPicture();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                break;
            }
        }
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) { //사진을 찍고나서 사진데이터가 메모리에 적재되면 어떻게 처리할것인가. byte[] data: 사진 데이터
            FileOutputStream outStream = null;
            try{
                File sdFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());  //저장되는 외부저장소 파일 경로를 sdFile에 저장
                // Environment.getExternalStorageDirectory(), "A"
                if(!sdFile.exists()) sdFile.mkdirs();

                //저장되는 사진의 이름을 현재 연월일시분초로 저장.
                Calendar cal = Calendar.getInstance(); //cal객체 선언
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String strFileName = (simpleDateFormat.format(cal.getTime())); //현재 연월일시분초를 strFileName 저장

                outStream = new FileOutputStream(sdFile + "/Camera/" + strFileName + ".jpg"); //sdFile/Camera/ 디렉터리에 jpg형식으로 저장하라고 알려주는 outStream객체 생성
                outStream.write(data); //데이터를 객체 형식에 맞게 저장
                outStream.close(); //종료

                mCam.startPreview(); //사진 데이터가 저장된 후 미리보기를 다시 띄워준다.
                mCam.stopPreview(); //미리보기 중지
                //메모리 해제
                mCam.release();
                mCam = null;

                Toast.makeText(getApplicationContext(), sdFile.getPath(), Toast.LENGTH_LONG).show(); //사진을 찍은 후 저장 된 경로를 Toast메시지로 띄워준다.
            } catch (FileNotFoundException e){
                Log.d("CAM", e.getMessage());
            } catch (IOException e){
                Log.d("CAM", e.getMessage());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCam != null) {
            this.mCam.stopPreview();
            this.mCam.release();
        }
        if (tts != null) {  //TTS객체 파괴를 위한 함수
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
