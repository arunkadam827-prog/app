package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;

public class HelpSupportActivity extends AppCompatActivity {

        private Button faqButton;
        private Button contactButton;
        private Button reportButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_help_support);

                faqButton = findViewById(R.id.faq_button);
                contactButton = findViewById(R.id.contact_support_button);
                reportButton = findViewById(R.id.report_problem_button);

                findViewById(R.id.back_button).setOnClickListener(v -> finish());

                // Kisan AI — live AI customer support / chatbot
                findViewById(R.id.ai_support_button).setOnClickListener(v ->
                                startActivity(new Intent(this, AiChatActivity.class)));

                faqButton.setOnClickListener(v -> Toast.makeText(
                                this,
                                "Frequently Asked Questions coming soon",
                                Toast.LENGTH_SHORT).show());

                contactButton.setOnClickListener(v -> Toast.makeText(
                                this,
                                "Contact support: support@farmerapp.com",
                                Toast.LENGTH_LONG).show());

                reportButton.setOnClickListener(v -> Toast.makeText(
                                this,
                                "Report Problem feature coming soon",
                                Toast.LENGTH_SHORT).show());
        }
}
