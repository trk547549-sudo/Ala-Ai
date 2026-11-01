package com.alaai.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout messages;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12, 25, 22));
        root.setPadding(18, 20, 18, 12);

        TextView title = new TextView(this);
        title.setText("ALA AI");
        title.setTextColor(Color.rgb(220, 180, 90));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 15, 0, 5);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("مساعدك الذكي، يفهمك ويتحدث معك");
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(5, 20, 5, 20);

        TextView welcome = new TextView(this);
        welcome.setText("مرحباً بك في ALA AI\n\nأنا مساعدك الذكي الجديد.\nقريباً سأتمكن من الإجابة عن أسئلتك ومساعدتك في التحكم بهاتفك.");
        welcome.setTextColor(Color.WHITE);
        welcome.setTextSize(17);
        welcome.setPadding(15, 20, 15, 20);
        welcome.setBackgroundColor(Color.rgb(25, 55, 45));
        messages.addView(welcome);

        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setHint("اكتب أمرك هنا...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setSingleLine(true);
        bottom.addView(input, new LinearLayout.LayoutParams(0, -2, 1));

        Button send = new Button(this);
        send.setText("إرسال");
        bottom.addView(send);

        Button voice = new Button(this);
        voice.setText("🎙");
        bottom.addView(voice);

        send.setOnClickListener(v -> sendMessage());

        voice.setOnClickListener(v ->
            Toast.makeText(this,
                "سيتم تفعيل المحادثة الصوتية في المرحلة القادمة",
                Toast.LENGTH_SHORT).show()
        );

        root.addView(bottom);
        setContentView(root);
    }

    void sendMessage() {
        String text = input.getText().toString().trim();

        if (text.isEmpty()) return;

        TextView message = new TextView(this);
        message.setText("أنت: " + text);
        message.setTextColor(Color.WHITE);
        message.setTextSize(16);
        message.setPadding(12, 15, 12, 15);

        messages.addView(message);
        input.setText("");
    }
}
