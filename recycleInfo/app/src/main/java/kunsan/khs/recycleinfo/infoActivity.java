package kunsan.khs.recycleinfo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class infoActivity extends AppCompatActivity {
    final String TAG = "TAG+InfoFragment";

    private ArrayAdapter adapter1,adapter2;
    private Spinner spinner1, spinner2;
    private Button searchButton;
    String choice_do=""; //지역
    String choice_se=""; //행정구역
    String data;

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView cont = findViewById(R.id.content);
        TextView reg = findViewById(R.id.region);

        Intent intent = getIntent();
        if(intent != null){
            data = intent.getStringExtra("key");
        } // cctvActivity에서 받은 값 가져오기

        spinner1 = (Spinner)findViewById(R.id.spinner1);
        spinner2 = (Spinner)findViewById(R.id.spinner2);

        adapter1 = ArrayAdapter.createFromResource(this, R.array.array_지역, android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                if (adapter1.getItem(i).equals("전라북도")){ //첫번째 스피너가 전라북도라면
                    choice_do = "전라북도"; // 출력을 위해 "전라북도"를 저장
                    adapter2 = ArrayAdapter.createFromResource(infoActivity.this, R.array.array_전라북도, android.R.layout.simple_spinner_dropdown_item);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner2.setAdapter(adapter2); // spinner2에는 전북의 행정구역 array 할당
                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                            choice_se = adapter2.getItem(i).toString(); // spinner2에서 선택한 행정구역 저장
                            if(adapter2.getItem(i).equals("군산시")){
                                reg.setText("군산시 배출 및 수거 주기\n" +
                                        "월~금 : 일몰 후 오후 6시 ~ 다음날 새벽 6시 배출\n" +
                                        "토 : 배출금지\n" +
                                        "일 : 오후6시 이후 배출");
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // 아무 것도 선택 안할 시
                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //아무 것도 선택 안할 시
            }
        });
        searchButton = findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(data != null){
                    switch(data){
                        case "can":
                            cont.setText("철캔, 알루미늄캔 : \n" +
                                    " - 내용물을 비우고 물로 헹군 후 압착\n" +
                                    " - 겉 또는 속에 플라스틱 뚜껑이 있는 것은 플라스틱 제거 후 투명봉투에 넣어서 배출\n\n" +
                                    "부탄가스, 살충제용기 : \n" +
                                    " - 구멍을 뚫어 내용물을 비운 후 배출\n\n" +
                                    "* 재활용 안 되는 품목 : \n" +
                                    " - 페인트통, 폐유통, 이물질이 묻어있는 제품");
                            break;
                        case "glass":
                            cont.setText("음료수병, 기타병류 : \n" +
                                    " - 병뚜껑을 제거한 후 내용물을 비우고 물로 헹굼\n" +
                                    " - 담배꽁초 등 이물질을 넣지 말 것\n\n" +
                                    "* 재활용 안 되는 품목 : \n" +
                                    " - 이물질이 들어 있는 병, 거울, 도자기류, 폐타일");
                            break;
                        case "metal":
                            cont.setText("고철류 : \n" +
                                    " - 철사, 못, 공구류, 국자, 맥주병뚜껑, 분유깡통, 열쇠, 후라이팬, 주전자, 철판(가정용), 옷걸이 등\n\n" +
                                    "비철금속 : \n" +
                                    " - 구리, 알루미늄, 스텐 등\n\n" +
                                    "* 두 경우 모두 이물질이 섞이지 않도록 끈으로 묶거나 한 곳에 넣어 배출\n" +
                                    "* 재활용 안 되는 품목 : \n" +
                                    " - 이물질이 묻어있는 제품");
                            break;
                        case "paper":
                            cont.setText("신문지 : \n" +
                                    " - 물기에 젖지 않게 함\n" +
                                    " - 반듯하게 펴서 차곡차곡 쌓아서 묶음\n\n" +
                                    "책자, 노트, 달력, 종이쇼핑백, 포장지 : \n" +
                                    " - 비닐코팅표지, 공책의 스프링 등을 제거함\n" +
                                    " - 비닐포장지는 제외\n\n" +
                                    "종이팩 (우유팩, 음료팩 등) : \n" +
                                    " - 내용물을 비우고 물로 한번 헹군 후 압착하여 다른 종이류와 섞이지 않도록 별도 봉투에 넣거나 한 데 묶음\n\n" +
                                    "종이상자 : \n" +
                                    " - 비닐코팅부분, 테이프, 철핀 등 제거 후 압착하여 운반이 용이하도록 묶음\n\n" +
                                    "* 재활용 안 되는 품목 : \n" +
                                    " - 비닐코팅 종이류, 1회용 기저귀, 화장실 사용 휴지");
                            break;
                        case "plastic":
                            cont.setText("페트병 : \n" +
                                    " - 병뚜껑을 제거한 후 내용물을 비우고 물로 헹굼\n\n" +
                                    "합성수지 용기류 : \n" +
                                    " - 가능한 한 압착하여 부피 축소(폐유 용기류는 제외)\n\n" +
                                    "기타 플라스틱류 : \n" +
                                    " - 이물질 및 부착상표 등을 제거한 후 투명봉투 속에 넣거나, 묶어서 배출\n\n" +
                                    "폐스티로폼 : \n" +
                                    " - 과일 생선상자 등은 잔재물을 완전히 비우고 물로 깨끗이 헹구어 배출\n" +
                                    "(이물질이 묻어 있는 것은 종량제봉투에 담아 배출)\n\n" +
                                    "* 재활용 안 되는 품목 : \n" +
                                    " - 이물질이 들어 있거나 부착상표가 붙어 있는 것");
                            break;
                        case "trash":
                            cont.setText("과자봉지, 라면봉지 등 : \n" +
                                    " - 이물질이 섞이지 않도록 한 뒤 흩날리지 않도록 배출\n" +
                                    "(이물질이 묻어 있는 것은 종량제봉투에 담아 배출)\n\n" +
                                    "그 외 일반쓰레기 : \n" +
                                    " - 종량제봉투에 담아 배출");
                            break;
                        default:
                            cont.setText("쓰레기 종류 인식에 실패했거나 존재하지 않는 클래스입니다.");
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "쓰레기 종류 인식값이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
