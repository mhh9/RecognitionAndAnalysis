package com.example.recognitionandanalysis;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class setImage extends AppCompatActivity {

    public Button selectImg;
    public ImageView recognizeImgBtn;
    public ImageView imageView;
    public TextView textView;
    private final int Selected_Picture = 200;
    public String tempText;
    public String[] resultText;
    public InputImage inputImage;
    public Uri imageUri;

    TextRecognizer recognizer =
            TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

    public ArrayList<String> item = new ArrayList<String>();
    public ArrayList<String> typeitem = new ArrayList<String>();
    public ArrayList<MainList> mainLists = new ArrayList<MainList>();

    //나오는 결과 edittext
    public EditText name;
    public EditText cpName; // 사업자 명
    public EditText enName;  // 근로자 이름
    public EditText number; // 전화번호
    public EditText address; // 사업자 주소
    public EditText start; // 근무 시작일
    public EditText salary; // 돈
    public EditText hours; // 근무시간

    String eName; // editText에 들어갈 상호명
    String rCpName; // editText에 들어갈 사업자
    String rEnName; // editText에 들어갈 근로자 주소
    String rNumber ; // editText에 들어갈 사업자 번호
    String rAddress; // editText에 들어갈 사업자 주소
    String rStart; // editText에 들어갈 근무 시작일
    String rSalary; // editText에 들어갈 돈
    String rHours; // editText에 들어갈 근무시간

    // 근로시간용 체크박스
    CheckBox monCheckbox;
    CheckBox tueCheckbox;
    CheckBox wedCheckbox;
    CheckBox thuCheckbox;
    CheckBox friCheckbox;
    CheckBox satCheckbox;
    CheckBox sunCheckbox;

    // 근로요일을 저장할 변수
    int wDays;

    // 일하는 시간
    int wHours;

    static public class NameEntity {
        final String text;
        final String type;
        Integer count;

        public NameEntity(String text, String type, Integer count) {
            this.text = text;
            this.type = type;
            this.count = count;
        }
    }

    public class MainList{
        public String mName;
        public String mCategory;
        public MainList(String name, String category){
            this.mName = name;
            this.mCategory = category;
        }
        public String getmName() { return mName; }
        public String getmCategory() { return mCategory; }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_image);

        selectImg = findViewById(R.id.selectImg);

        name = (EditText)findViewById(R.id.add_edittext_name);
        cpName = (EditText)findViewById(R.id.add_edittext_cpName);  // 사업자명
        enName = (EditText)findViewById(R.id.add_edittext_enName);
        number = (EditText)findViewById(R.id.add_edittext_number); // 사업자 전화번호
        address = (EditText)findViewById(R.id.add_edittext_address); // 사업자 주소
        start = (EditText)findViewById(R.id.add_edittext_start); // 근무 시작일
        salary = (EditText)findViewById(R.id.add_edittext_salary); // 돈
        hours = (EditText)findViewById(R.id.add_edittext_hours); // 근무시간

        //체크박스
        monCheckbox = (CheckBox)findViewById(R.id.monday);
        tueCheckbox =  (CheckBox)findViewById(R.id.tuesday);
        wedCheckbox =  (CheckBox)findViewById(R.id.wednesday);
        thuCheckbox =  (CheckBox)findViewById(R.id.thursday);
        friCheckbox =  (CheckBox)findViewById(R.id.friday);
        satCheckbox =  (CheckBox)findViewById(R.id.saturday);
        sunCheckbox =  (CheckBox)findViewById(R.id.sunday);

        if(monCheckbox.isChecked()){
            wDays++;
        }
        else if (tueCheckbox.isChecked()){
            wDays++;
        }
        else if (wedCheckbox.isChecked()){
            wDays++;
        }
        else if (thuCheckbox.isChecked()){
            wDays++;
        }
        else if (friCheckbox.isChecked()){
            wDays++;
        }
        else if (satCheckbox.isChecked()){
            wDays++;
        }
        else if (sunCheckbox.isChecked()){
            wDays++;
        }


        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImg();
            }

        });




    } // end onCreate

    public class usingNLPAPI extends AsyncTask<String, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override

        protected String doInBackground(String... strings) {

            for (int i = 0; i < resultText.length; i++) {

                String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
                String accessKey = "b5307d84-0969-4a2c-a53f-f46af682f6d9";
                String analysisCode = "ner";
                // 인식된 텍스트 저장
                String text = resultText[i];
                Gson gson = new Gson();

                Map<String, Object> request = new HashMap<>();
                Map<String, String> argument = new HashMap<>();

                argument.put("analysis_code", analysisCode);
                argument.put("text", text);

                request.put("access_key", accessKey);
                request.put("argument", argument);

                URL url;
                Integer responseCode = null;
                String responBodyJson = null;
                Map<String, Object> responeBody = null;

                try {
                    url = new URL(openApiURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.write(gson.toJson(request).getBytes("UTF-8"));
                    wr.flush();
                    wr.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                    String line;
                    String page = "";

                    while ((line = reader.readLine()) != null) {
                        page += line;
                    }

                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(page);


                    String check = jsonElement.getAsJsonObject().get("return_object").getAsJsonObject().get("sentence").getAsJsonArray().get(0).getAsJsonObject().get("NE").getAsJsonArray().toString();
                    System.out.println("에러검사: "+check);
                    if(!check.equals("[]"))
                    {
                        String type = jsonElement.getAsJsonObject().get("return_object").getAsJsonObject().get("sentence").getAsJsonArray().get(0).getAsJsonObject().get("NE").getAsJsonArray().get(0).getAsJsonObject().get("type").toString();


                        if(type.equals("\"LCP_PROVINCE\"") || type.equals("\"LCP_COUNTY\"") || type.equals("\"LCP_CITY\"")||type.equals("\"DT_DURATION \"")||type.equals("\"TI_HOUR \"")||type.equals("\"TI_MINUTE\"")||type.equals("\"QT_PRICE\""))
                        {

                            String item1 = jsonElement.getAsJsonObject().get("return_object").getAsJsonObject().get("sentence").getAsJsonArray().get(0).getAsJsonObject().get("NE").getAsJsonArray().get(0).getAsJsonObject().get("text").toString();

                            item.add(jsonElement.getAsJsonObject().get("return_object").getAsJsonObject().get("sentence").getAsJsonArray().get(0).getAsJsonObject().get("NE").getAsJsonArray().get(0).getAsJsonObject().get("text").toString());
                           //System.out.println(item.get(i) + "아이템 배열 확인");
                            typeitem.add(type);
                            mainLists.add(new MainList(jsonElement.getAsJsonObject().get("return_object").getAsJsonObject().get("sentence").getAsJsonArray().get(0).getAsJsonObject().get("NE").getAsJsonArray().get(0).getAsJsonObject().get("text").toString(),type));
                        }

                    }


                   /* responseCode = con.getResponseCode();
                    InputStream is = con.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuffer sb = new StringBuffer();

                    String inputLine = "";
                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    responBodyJson = sb.toString();

                    // http 요청 오류 시 처리
                    if (responseCode != 200) {
                        // 오류 내용 출력
                        System.out.println("[error] " + responBodyJson);
                    }

                    responeBody = gson.fromJson(responBodyJson, Map.class);
                    Integer result = ((Double) responeBody.get("result")).intValue();
                    Map<String, Object> returnObject;
                    List<Map> sentences;

                    // 분석 요청 오류 시 처리
                    if (result != 0) {

                        // 오류 내용 출력
                        System.out.println("[error] " + responeBody.get("result"));
                    }

                    returnObject = (Map<String, Object>) responeBody.get("return_object");
                    sentences = (List<Map>) returnObject.get("sentence");
                    Map<String, NameEntity> nameEntitiesMap = new HashMap<String, NameEntity>();
                    List<NameEntity> nameEntities = null;

                    for (Map<String, Object> sentence : sentences) {

                        List<Map<String, Object>> nameEntityRecognitionResult = (List<Map<String, Object>>) sentence.get("NE");
                        for (Map<String, Object> nameEntityInfo : nameEntityRecognitionResult) {
                            String name = (String) nameEntityInfo.get("text");
                            NameEntity nameEntity = nameEntitiesMap.get(name);
                            System.out.println("개체명 인식 결과" + nameEntityRecognitionResult);
                            if (nameEntity == null) {
                                nameEntity = new NameEntity(name, (String) nameEntityInfo.get("type"), 1);
                                nameEntitiesMap.put(name, nameEntity);

                            } else {
                                nameEntity.count = nameEntity.count + 1;
                            }
                        }
                        System.out.println("");

                    }*/
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                return null;
            }

    } // end using NLPAPI

    // 인식된 텍스트를 잘라줌
    public void splitResult(String string){
        resultText = string.split("\n");
        for (int i = 0; i< resultText.length; i++) {
            if(resultText[i].contains("CS")){
                eName = resultText[i];
                System.out.println("값"+eName);
                name.setText(resultText[i]);
            }else if(resultText[i].contains("김덕성")){
                rCpName = resultText[i];
                cpName.setText(resultText[i]);
            }else if(resultText[i].contains("민혜")){
                rEnName = resultText[i];
                enName.setText(resultText[i]);
            }else if(resultText[i].contains("연락처")){
                rNumber = resultText[i];
                number.setText(resultText[i]);
            }else if(resultText[i].contains("서울")){
                rAddress = resultText[i];
                address.setText(resultText[i]);
            }else if(resultText[i].contains("계약기간")){
                rStart = resultText[i];
                start.setText(resultText[i]);
            }else if(resultText[i].contains("임 금")){
                rSalary = resultText[i];
                salary.setText(resultText[i]);
            }

            System.out.println("나눠진 값"+resultText[i]);
        }
        hours.setText(wHours); //일하는 시간
    } // end splitResult


        private ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public final void onActivityResult(ActivityResult it) {
                        Intent data = it.getData();
                        imageUri = data.getData();
                        convertImagetoText(imageUri);
                    }
                });


    // 인식된 이미지를 텍스트로 바꿈
        private final void convertImagetoText(Uri imageUri) {
            try {

                inputImage = InputImage.fromFilePath(this, imageUri);

                Task<Text> result =
                        recognizer.process(inputImage)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {

                                        tempText = visionText.getText();

                                        System.out.println("성공");
                                        System.out.println(tempText);

                                        splitResult(tempText);

                                        usingNLPAPI Async2 = new usingNLPAPI();
                                        Async2.execute();

                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });
            } catch (Exception e) {

            }

        }

        private final void chooseImg() {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction("android.intent.action.GET_CONTENT");
            ActivityResultLauncher imageView = this.intentActivityResultLauncher;
            if (imageView != null) {
                imageView.launch(i);
            }
        } // end chooseImg


    }
