package com.alaai.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class MainActivity extends Activity {

    LinearLayout messages;
    EditText input;
    ScrollView scroll;

    int gold = Color.rgb(220,180,90);
    int green = Color.rgb(25,65,51);
    int background = Color.rgb(12,25,22);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);
        root.setPadding(dp(16),dp(12),dp(16),dp(12));

        TextView title = new TextView(this);
        title.setText("ALA AI");
        title.setTextColor(gold);
        title.setTextSize(32);
        title.setTypeface(null,Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("مساعدك الذكي، يفهمك ويتحدث معك");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(5),0,dp(12));
        root.addView(subtitle);

        TextView status = new TextView(this);
        status.setText("●  مساعدك الشخصي");
        status.setTextColor(gold);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,dp(5),0,dp(10));
        root.addView(status);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(3),dp(12),dp(3),dp(15));

        scroll.addView(messages);

        root.addView(scroll,new LinearLayout.LayoutParams(
            -1,0,1
        ));

        addMessage(
            "مرحباً بك في ALA AI 👋\n\nأنا مساعدك الذكي.\nيمكنك الكتابة لي أو استخدام زر الميكروفون لتحويل صوتك إلى نص.",
            false
        );

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0,dp(10),0,0);

        input = new EditText(this);
        input.setSingleLine(false);
        input.setMaxLines(3);
        input.setTextSize(16);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setHint("اكتب رسالتك هنا...");
        input.setPadding(dp(14),dp(12),dp(14),dp(12));
        input.setBackground(round(Color.rgb(27,43,38),dp(18)));

        bottom.addView(input,new LinearLayout.LayoutParams(
            0,dp(52),1
        ));

        TextView send = new TextView(this);
        send.setText("إرسال");
        send.setTextColor(Color.rgb(20,30,25));
        send.setTextSize(15);
        send.setTypeface(null,Typeface.BOLD);
        send.setGravity(Gravity.CENTER);
        send.setBackground(round(gold,dp(14)));

        LinearLayout.LayoutParams sendParams =
            new LinearLayout.LayoutParams(dp(75),dp(52));
        sendParams.setMargins(dp(7),0,0,0);
        bottom.addView(send,sendParams);

        TextView mic = new TextView(this);
        mic.setText("🎙");
        mic.setTextSize(22);
        mic.setGravity(Gravity.CENTER);
        mic.setTextColor(Color.WHITE);
        mic.setBackground(round(green,dp(14)));

        LinearLayout.LayoutParams micParams =
            new LinearLayout.LayoutParams(dp(52),dp(52));
        micParams.setMargins(dp(7),0,0,0);
        bottom.addView(mic,micParams);

        root.addView(bottom);

        send.setOnClickListener(v -> sendMessage());
        input.setOnEditorActionListener((v,action,event) -> {
            sendMessage();
            return true;
        });

        mic.setOnClickListener(v -> startVoice());

        setContentView(root);
    }

    void sendMessage() {
        String text = input.getText().toString().trim();

        if (text.isEmpty()) return;

        addMessage(text, true);
        input.setText("");

        InputMethodManager imm =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

        addMessage("جاري التفكير والاتصال...", false);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://127.0.0.1:5050/chat");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("message", text);

                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.toString().getBytes("UTF-8"));
                }

                int code = connection.getResponseCode();
                java.io.InputStream stream =
                    code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8")
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject response = new JSONObject(result.toString());
                String answer = code >= 200 && code < 300
                    ? response.optString("reply", "لم يصل رد من المساعد")
                    : response.optString("error", "تعذر الاتصال بالخدمة");

                runOnUiThread(() -> addMessage(answer, false));

            } catch (Exception e) {
                runOnUiThread(() ->
                    addMessage(
                        "تعذر الاتصال بالمساعد. تأكد من تشغيل خادم Termux والإنترنت.",
                        false
                    )
                );
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    void addMessage(String text, boolean user) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(user ? Gravity.END : Gravity.START);
        row.setPadding(0,dp(5),0,dp(5));

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(16);
        bubble.setGravity(Gravity.RIGHT);
        bubble.setTextDirection(View.TEXT_DIRECTION_RTL);
        bubble.setPadding(dp(15),dp(13),dp(15),dp(13));
        bubble.setBackground(round(
            user ? Color.rgb(34,85,67) : Color.rgb(29,48,41),
            dp(18)
        ));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.78),
                -2
            );

        bubble.setLayoutParams(params);
        row.addView(bubble);
        messages.addView(row);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    void startVoice() {
        if(checkSelfPermission("android.permission.RECORD_AUDIO")
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{"android.permission.RECORD_AUDIO"},100
            );
            return;
        }

        openVoice();
    }

    void openVoice() {
        try {
            Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,"ar"
            );

            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,"تحدث الآن"
            );

            startActivityForResult(intent,200);

        } catch(Exception e) {
            Toast.makeText(this,
                "التعرف على الصوت غير متاح على الجهاز",
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,String[] permissions,int[] results) {

        super.onRequestPermissionsResult(
            requestCode,permissions,results
        );

        if(requestCode == 100 && results.length > 0
            && results[0] == PackageManager.PERMISSION_GRANTED) {
            openVoice();
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 200 && resultCode == RESULT_OK
            && data != null) {

            ArrayList<String> results =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                );

            if(results != null && !results.isEmpty()) {
                input.setText(results.get(0));
                input.setSelection(input.length());
            }
        }
    }

    GradientDrawable round(int color,int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    int dp(int value) {
        return (int)(value *
            getResources().getDisplayMetrics().density);
    }
}
