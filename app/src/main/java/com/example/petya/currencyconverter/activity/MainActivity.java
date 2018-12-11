package com.example.petya.currencyconverter.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petya.currencyconverter.data.Controller;
import com.example.petya.currencyconverter.interfaces.Link;
import com.example.petya.currencyconverter.R;
import com.example.petya.currencyconverter.data.Model;
import com.example.petya.currencyconverter.data.ModelLab;
import com.google.gson.JsonObject;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;

import java.text.DecimalFormat;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Float.parseFloat;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_START = 0;
    private static final int REQUEST_END = 1;
    private static final String KEY_START = "START";
    private static final String KEY_END = "END";
    private static final String KEY_START_NAME = "START_NAME";
    private static final String KEY_END_NAME = "END_NAME";
    private static final String KEY_START_SYMBOL = "START_SYMBOL";
    private static final String KEY_END_SYMBOLE = "END_SYMBOL";
    private static final String KEY_ANSWER = "ANSWER";
    private static final String DATA = "database";

    public static boolean mFlag;

    private static SharedPreferences sData;
    private static SharedPreferences.Editor sEditorData;

    private static Link mLink;
    private ModelLab mModelLab;

    private TextView mCurrencyAnswer;
    private EditText mCurrencyQuestion;
    private TextView mCurrencyStart;
    private TextView mCurrencyEnd;
    private TextView mCurrencyNameStart;
    private TextView mCurrencyNameEnd;
    private TextView mCurrencySymbolStart;
    private TextView mCurrencySymbolEnd;
    private String mCurrencySend;
    private float mRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawableResource(R.drawable.app);

        setContentView(R.layout.activity_main);

        sData = getSharedPreferences(DATA, MODE_PRIVATE);

        mModelLab = ModelLab.get(getApplicationContext());

        mCurrencyAnswer = findViewById(R.id.currencyAnswer);
        mCurrencyQuestion = findViewById(R.id.currencyQuestion);
        mCurrencyStart = findViewById(R.id.currencyStart);
        mCurrencyEnd = findViewById(R.id.currencyEnd);
        mCurrencyNameStart = findViewById(R.id.currencyNameStart);
        mCurrencyNameEnd = findViewById(R.id.currencyNameEnd);
        mCurrencySymbolStart = findViewById(R.id.currencySymbolStart);
        mCurrencySymbolEnd = findViewById(R.id.currencySymbolEnd);
        mLink = Controller.getApi();

        if (savedInstanceState != null) {
            mCurrencyStart.setText(savedInstanceState.getString(KEY_START));
            mCurrencyEnd.setText(savedInstanceState.getString(KEY_END));
            mCurrencyNameStart.setText(savedInstanceState.getString(KEY_START_NAME));
            mCurrencyNameEnd.setText(savedInstanceState.getString(KEY_END_NAME));
            mCurrencyAnswer.setText(savedInstanceState.getString(KEY_ANSWER));
            mCurrencySymbolStart.setText(savedInstanceState.getString(KEY_START_SYMBOL));
            mCurrencySymbolEnd.setText(savedInstanceState.getString(KEY_END_SYMBOLE));
        }

        /**
         *  Shows guide
         */
        if(!sData.getBoolean("firstTime", false)) {

            SharedPreferences.Editor editor = sData.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();

            SimpleTarget simpleTarget = new SimpleTarget.Builder(this)
                    .setPoint(540f, 200f)
                    .setShape(new Circle(200f))
                    .setTitle("Подсказка")
                    .setDescription("Нажав сюда вы можете поменять валюту")
                    .build();

            SimpleTarget simpleTarget2 = new SimpleTarget.Builder(this)
                    .setPoint(540f, 1700f)
                    .setShape(new Circle(250f))
                    .setTitle("Подсказка")
                    .setDescription("а так же сюда")
                    .build();

            SimpleTarget simpleTarget3 = new SimpleTarget.Builder(this)
                    .setPoint(540f, 1000f)
                    .setShape(new Circle(400f))
                    .setTitle("Подсказка")
                    .setDescription("И главная кнопка! Приятного пользования :)")
                    .build();

            Spotlight.with(this)
                    .setOverlayColor(R.color.background)
                    .setDuration(1000L)
                    .setTargets(simpleTarget, simpleTarget2, simpleTarget3)
                    .setClosedOnTouchedOutside(true)
                    .setAnimation(new DecelerateInterpolator(1f))
                    .start();
        }
    }

    /**
     * Handles click on currency select buttons and conversion button
     * @return
     */
    public void onClick(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        switch (view.getId()) {
            case R.id.btn_scan_qr:
                String checkEmpty = mCurrencyQuestion.getText().toString();
                if (checkEmpty.matches("")){
                    Toast.makeText(this, R.string.empty_edit_text, Toast.LENGTH_SHORT).show();
                    break;
                }

                if (sData.contains(createRequest()) && !isOnline()){
                    mRate = sData.getFloat(createRequest(), 0);
                    result();
                    return;
                }

                if (isOnline()) {
                    mCurrencySend = createRequest();
                    mLink.getConvert(mCurrencySend, "ultra").enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject jo = response.body();
                            mRate = jo.get(mCurrencySend).getAsFloat();
                            sEditorData = sData.edit();
                            sEditorData.putFloat(createRequest(), mRate);
                            sEditorData.apply();

                            result();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Log.d("myLogs", t.toString());
                            Toast.makeText(getApplicationContext(), "Faile fetching currency rate", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.currencyNameStart:
                startActivityForResult(intent, REQUEST_START);
                break;
            case R.id.currencyNameEnd:
                startActivityForResult(intent, REQUEST_END);
                break;
        }
    }

    private void result() {
        float numberOne = parseFloat(String.valueOf(mCurrencyQuestion.getText()));
        float answer = numberOne * mRate;
        String formatAnswer = new DecimalFormat("#0.00").format(answer);
        mCurrencyAnswer.setText(formatAnswer);
    }

    private String createRequest() {
        return mCurrencyStart.getText() + "_" + mCurrencyEnd.getText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_START) {
            if (data == null) {
                return;
            }
            Model mModelStart = mModelLab.getModel((UUID) data.getSerializableExtra(ListActivity.SELECTED_MODEL));
            mCurrencyStart.setText(mModelStart.getCurrencyId());
            mCurrencyNameStart.setText(mModelStart.getCurrencyName());
            mCurrencySymbolStart.setText(mModelStart.getCurrencySymbol());
        }
        if (requestCode == REQUEST_END) {
            if (data == null) {
                return;
            }
            Model mModelEnd = mModelLab.getModel((UUID) data.getSerializableExtra(ListActivity.SELECTED_MODEL));
            mCurrencyEnd.setText(mModelEnd.getCurrencyId());
            mCurrencyNameEnd.setText(mModelEnd.getCurrencyName());
            mCurrencySymbolEnd.setText(mModelEnd.getCurrencySymbol());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_START_NAME ,mCurrencyNameStart.getText().toString());
        outState.putString(KEY_END_NAME ,mCurrencyNameEnd.getText().toString());
        outState.putString(KEY_START, mCurrencyStart.getText().toString());
        outState.putString(KEY_END, mCurrencyEnd.getText().toString());
        outState.putString(KEY_ANSWER, mCurrencyAnswer.getText().toString());
        outState.putString(KEY_START_SYMBOL, mCurrencySymbolStart.getText().toString());
        outState.putString(KEY_END_SYMBOLE, mCurrencySymbolEnd.getText().toString());
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (null != networkInfo) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                return true;
            }
            if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                return true;
            }
        }
        return false;
    }
}
