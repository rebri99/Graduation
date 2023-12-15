package kunsan.khs.recycleinfo;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class cctvActivity extends AppCompatActivity {
    final String TAG = "TAG+CCTVFragment";
    private static final int REQUEST_PERMISSION = 2;

    public static final byte HEADER_TEXT = 0x01;
    public static final byte HEADER_IMAGE = 0x02;

    private static final int HANDLER_MSG_EXIT = 999;
    private static final int HANDLER_MSG_CONNECT = 100;

    private static final String serverIP = "125.139.250.60";
    private static final int serverPort = 9999;

    WebView webView;
    WebSettings webSettings;

    Socket socket;
    OutputStream os;

    private Button connectButton;
    private Button screenshot;
    private Button sendimage;

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);

        // 외부 저장소 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
        }

        init();

        Log.d(TAG, "Create CCTV Fragment");

        webView = (WebView) findViewById(R.id.cctvWeb);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} " +
                        "img{width:100%25;} div{overflow: hidden;} </style></head>" +
                        "<body><div><img src='http://125.139.250.60:8081/'/></div></body></html>",
                "text/html", "UTF-8");
        // WebView 에 CCTV 화면 띄움
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    webView.reload();
                }
                return true;
            }
        }); // WebView 터치 시 새로고침
    }
    private void init(){
        connectButton = findViewById(R.id.connect_server_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectServer();
            }
        });
        screenshot = findViewById(R.id.screenshotButton);
        screenshot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { captureWebView(); }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "외부 저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void connectServer() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    socket = new Socket();
                    SocketAddress addr = new InetSocketAddress(serverIP, serverPort);
                    socket.connect(addr);
                    os = socket.getOutputStream();
                    handler.sendEmptyMessage(HANDLER_MSG_CONNECT);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("connectServer", "Connection error: " + e.getMessage());
                }
            }
        }).start();
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == HANDLER_MSG_CONNECT) {
                completeConnect();
            } else if (what == HANDLER_MSG_EXIT) {
                connectButton.setEnabled(true);
            }
        }
    };
    private void completeConnect() {
        connectButton.setEnabled(false);
        Toast.makeText(this, "서버 접속 성공", Toast.LENGTH_SHORT).show();
        sendimage = findViewById(R.id.sendimage);
        sendimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txt = findViewById(R.id.textView);
                txt.setText("결과값을 불러오는 중입니다.");
                sendImage();
            }
        });
    }
    private void sendImage() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pictures/screenshot.png";
        File file = new File(path);

        final byte[] imageByteArr = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(imageByteArr);
            fis.close();
            Toast.makeText(this, "값을 받아오는데 약 2분 정도 소요됩니다.", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = objectToByte(HEADER_IMAGE, imageByteArr);
                        os.write(data);

                        byte[] buf = new byte[4096];
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        while(true){
                            int len = in.read(buf);
                            if(len == -1){
                                break;
                            }else{
                                if(buf[0]==HEADER_TEXT){
                                    int dataLen = getLen(buf[1], buf[2], buf[3], buf[4]);
                                    byte[] strData = new byte[dataLen];
                                    System.arraycopy(buf, 5, strData, 0 , dataLen);

                                    String msg = new String(strData, "utf-8");
                                    TextView receivedMessageTextview = findViewById(R.id.ODtext);
                                    receivedMessageTextview.setText(msg);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // infoActivity에서 msg값을 사용하기 위해 저장
                                            Intent intent = new Intent(cctvActivity.this, infoActivity.class);
                                            intent.putExtra("key", msg);
                                            startActivity(intent);
                                        }
                                    });

                                    os.close();
                                    socket.close();
                                    handler.sendEmptyMessage(HANDLER_MSG_EXIT);
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void captureWebView() {
        // WebView의 현재 내용을 비트맵으로 렌더링
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        webView.draw(new Canvas(bitmap));

        // 이미지를 저장할 파일 경로 설정
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = "screenshot.png";
        File imageFile = new File(storageDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //fos.flush();
            fos.close();
            Toast.makeText(this, "WebView 캡처 완료", Toast.LENGTH_SHORT).show();
            // 미디어 스캐닝
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"+storageDir.getPath()+"/"+"screenshot.png")));

            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.beep);
            mediaPlayer.start(); // beep음 발생
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "WebView 캡처 실패", Toast.LENGTH_SHORT).show();
        }
    } // screenshot func
    private byte[] getDataLength(int length) {
        byte[] blen = new byte[4];

        blen[0] |= (byte) ((length & 0xFF000000) >> 24);
        blen[1] |= (byte) ((length & 0xFF0000) >> 16);
        blen[2] |= (byte) ((length & 0xFF00) >> 8);
        blen[3] |= (byte) (length & 0xFF);

        return blen;
    } // int형 변수를 4바이트로 변환
    private byte[] objectToByte(byte header, byte[] data) {
        byte[] result = null;
        int size = 1 + 4 + data.length;
        result = new byte[size];
        result[0] = header;
        byte[] dataLen = getDataLength(data.length);
        System.arraycopy(dataLen, 0, result, 1, dataLen.length);
        System.arraycopy(data, 0, result, 5, data.length);

        return result;
    } // 매개변수로 메시지 타입의 헤더와 데이터를 받아서 byte를 합치는 작업
    private static int getLen(byte b, byte c, byte d, byte e) {
        int s1 = b & 0xff;
        int s2 = c & 0xff;
        int s3 = d & 0xff;
        int s4 = e & 0xff;

        return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
    } // 4바이트를 int형으로 변환, 데이터의 길이를 받는 부분
}
