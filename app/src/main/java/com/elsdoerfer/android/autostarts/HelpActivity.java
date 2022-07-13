package com.elsdoerfer.android.autostarts;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.elsdoerfer.android.autostarts.opt.RootFeatures;

import java.util.Objects;

public class HelpActivity extends Activity {
    private static final int[] DefaultFaq = {
            R.array.faq1,
            R.array.faq2,
            R.array.faq3,
            R.array.faq4,
            R.array.faq5,
            R.array.faq6,
    };

    // Does not include questions about root features
    private static final int[] NoRootFaq = {
            R.array.faq1,
            R.array.faq3,
            R.array.faq4
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);

        // Build the FAQ text.
        int[] faqConfig = RootFeatures.Enabled ? DefaultFaq : NoRootFaq;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        boolean isQuestion = false;
        for (int k : faqConfig) {
            // TODO: The string-array items use HTML formatting, which
            // is lost here. There doesn't seem to be a way to read the
            // string-array resource as a Spannable?
            CharSequence[] question = getResources().getTextArray(k);

            for (int j = 0; j <= 1; j++) {
                // The array contains alternating questions and answers
                isQuestion = !isQuestion;

                if (question[j] instanceof Spanned) {
                    builder.append(question[j]).append("\n\n");
                } else {
                    if (isQuestion) {
                        SpannableString q = new SpannableString(question[j]);
                        q.setSpan(new StyleSpan(Typeface.BOLD), 0, q.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.append(q).append("\n");
                    } else {
                        builder.append(question[j]).append("\n\n");
                    }
                }
            }
        }

        ((TextView) findViewById(R.id.faq_text)).setText(builder);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
