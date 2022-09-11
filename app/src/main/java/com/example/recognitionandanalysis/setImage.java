package com.example.recognitionandanalysis;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.internal.Intrinsics;

public class setImage extends AppCompatActivity {

    public Button selectImg;
    public ImageView recognizeImgBtn;
    public ImageView imageView;
    public TextView textView;
    private final int Selected_Picture = 200;
    public String rText;
    public InputImage inputImage;
    public Uri imageUri;


    TextRecognizer recognizer =
            TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());


    private final String API_URL = "http://aiopen.etri.re.kr:8000/WiseNLU";
    private final String API_KEY = "b5307d84-0969-4a2c-a53f-f46af682f6d9";
    private final String analysis_code = "ner";
    //private final EntityRecognizer recognizer;

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


    private String show_img_or_text;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_image);

        selectImg = findViewById(R.id.selectImg);

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
            String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
            String accessKey = "b5307d84-0969-4a2c-a53f-f46af682f6d9";
            String analysisCode = "ner";
            String text = rText;
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

                responseCode = con.getResponseCode();
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

                for( Map<String, Object> sentence : sentences ){

                    List<Map<String, Object>> nameEntityRecognitionResult = (List<Map<String, Object>>)sentence.get("NE");
                    for( Map<String, Object> nameEntityInfo : nameEntityRecognitionResult ) {
                        String name = (String) nameEntityInfo.get("text");
                        NameEntity nameEntity = nameEntitiesMap.get(name);
                        System.out.println("개체명 인식 결과"+nameEntityRecognitionResult);
                        if ( nameEntity == null ) {
                            nameEntity = new NameEntity(name, (String) nameEntityInfo.get("type"), 1);
                            nameEntitiesMap.put(name, nameEntity);

                        } else {
                            nameEntity.count = nameEntity.count + 1;
                        }
                    }
                    System.out.println("");

                }
            }


             catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    } // end using NLPAPI


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


        private final void convertImagetoText(Uri imageUri) {
            try {

                inputImage = InputImage.fromFilePath(this, imageUri);

                Task<Text> result =
                        recognizer.process(inputImage)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {

                                        rText = visionText.getText();

                                        System.out.println("성공");
                                        System.out.println(rText);

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
