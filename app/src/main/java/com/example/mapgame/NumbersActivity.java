package com.example.mapgame;

import static android.content.ContentValues.TAG;
import static kotlinx.coroutines.DelayKt.delay;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;

public class NumbersActivity extends AppCompatActivity implements Game.ResultsCallback, MyButton.MyOnClickListener{
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //
    // Метод onresult() отвечает за отправку данных на следующую страницу
    // 159 строка
    //
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static final int MATRIX_SIZE = 5;// можете ставить от 2 до 20))

    //ui
    private String nameAntagonist = "Кот учёный";
    private TextView mUpText, mLowText;
    GridLayout mGridLayout;
    private MyButton[][] mButtons;
    private VideoView videoDialog;
    private Button btnSkip;

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.numbers_activity);

        mGridLayout = (GridLayout) findViewById(R.id.my_grid);
        mGridLayout.setColumnCount(MATRIX_SIZE);
        mGridLayout.setRowCount(MATRIX_SIZE);
        mButtons = new MyButton[MATRIX_SIZE][MATRIX_SIZE];//5 строк и 5 рядов
        videoDialog = (VideoView) findViewById(R.id.videoDialog);
        btnSkip = (Button) findViewById(R.id.btnSkipQuest);
        TextView name_enemy = (TextView) findViewById(R.id.upper_scoreboard);
        name_enemy.setText(String.format("%s: %d",nameAntagonist, 0));
        ImageView btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, CardGameActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        //создаем кнопки для цифр
        for (int yPos = 0; yPos < MATRIX_SIZE; yPos++) {
            for (int xPos = 0; xPos < MATRIX_SIZE; xPos++) {
                MyButton mBut = new MyButton(this, xPos, yPos);

                mBut.setTextSize(30-MATRIX_SIZE);
                Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                mBut.setTypeface(boldTypeface);
                mBut.setTextColor(ContextCompat.getColor(this, R.color.white));
                mBut.setOnClickListener(this);
                mBut.setPadding(1, 1, 1, 1); //так цифры будут адаптироваться под размер

                mBut.setAlpha(1);
                mBut.setClickable(false);

                mBut.setBackgroundResource(R.drawable.bg_grey);

                mButtons[yPos][xPos] = mBut;
                mGridLayout.addView(mBut);
            }
        }

        mUpText = (TextView) findViewById(R.id.upper_scoreboard);
        mLowText = (TextView) findViewById(R.id.lower_scoreboard);

        //расположим кнопки с цифрами равномерно внутри mGridLayout
        mGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        setButtonsSize();
                        //нам больше не понадобится OnGlobalLayoutListener
                        mGridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        game = new Game(this, MATRIX_SIZE); //создаем класс игры

        btnSkip.setOnClickListener(v -> {
            try {
                game.onResult();
                btnSkip.setVisibility(View.INVISIBLE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            game.startGame(); //и запускаем ее
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }//onCreate

    private void setButtonsSize() {
        int pLength;
        final int MARGIN = 6;

        int pWidth = mGridLayout.getWidth();
        int pHeight = mGridLayout.getHeight();
        int numOfCol = MATRIX_SIZE;
        int numOfRow = MATRIX_SIZE;

        //сделаем mGridLayout квадратом
        if (pWidth >= pHeight) pLength = pHeight;
        else pLength = pWidth;
        ViewGroup.LayoutParams pParams = mGridLayout.getLayoutParams();
        pParams.width = pLength;
        pParams.height = pLength;
        mGridLayout.setLayoutParams(pParams);

        int w = pLength / numOfCol;
        int h = pLength / numOfRow;

        for (int yPos = 0; yPos < MATRIX_SIZE; yPos++) {
            for (int xPos = 0; xPos < MATRIX_SIZE; xPos++) {
                GridLayout.LayoutParams params = (GridLayout.LayoutParams)
                        mButtons[yPos][xPos].getLayoutParams();
                params.width = w - 2 * MARGIN;
                params.height = h - 2 * MARGIN;
                params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
                mButtons[yPos][xPos].setLayoutParams(params);
                //Log.w(TAG, "process goes in customizeMatrixSize");
            }
        }
    }

    //MyButton.MyOnClickListener интерфейс
    //*************************************************************************
    @Override
    public void OnTouchDigit(MyButton v) throws InterruptedException {
        game.OnUserTouchDigit(v.getIdY(), v.getIdX());
    }

    //Game.ResultsCallback интерфейс
    //*************************************************************************
    @Override
    public void changeLabel(boolean upLabel, int points) {
        if (upLabel) mUpText.setText(String.format("%s: %d",nameAntagonist, points));
        else mLowText.setText(String.valueOf(String.format("Вы: %d", points)));
    }

    @Override
    public void changeButtonBg(int y, int x, boolean row, boolean active) {

        if (active) {
            if (row) mButtons[y][x].setBackgroundResource(R.drawable.bg_protagon);
            else mButtons[y][x].setBackgroundResource(R.drawable.bg_dark);

        } else {
            mButtons[y][x].setBackgroundResource(R.drawable.bg_grey);
        }
    }

    @Override
    public void setButtonText(int y, int x, int text) {
        mButtons[y][x].setText(String.valueOf(text));
    }

    @Override
    public void changeButtonClickable(int y, int x, boolean clickable) {
        mButtons[y][x].setClickable(clickable);
    }

    @Override
    public void onResult(int playerOnePoints, int playerTwoPoints) throws InterruptedException {

        String text;
        int result = -999;
        String questText;
        Integer path;
        if (playerOnePoints > playerTwoPoints)
        {
            text = "Вы победили!";
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.kot_win);
            questText = "Теперь вы готовы идти дальше и сразиться с Кощеем";
            result = RESULT_OK;
            path = R.layout.dialog_enemy;
        }
        else if (playerOnePoints < playerTwoPoints)
        {
            text = "Кот-учёный победил!";
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.kot_lose);
            questText = "Не расстраивайся. В следующий раз повезёт!";
            result = RESULT_CANCELED;
            path = R.layout.dialog_lose;
        }
        else
        {
            text = "Ничья!";
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.kot_lose);
            questText = "Не расстраивайся. В следующий раз повезёт!";
            result = RESULT_CANCELED;
            path = R.layout.dialog_lose;
        }
        int finalResult = result;
        videoDialog.setOnPreparedListener( v-> {
            ViewGroup.LayoutParams layoutParams = videoDialog.getLayoutParams();
            View d = (View)findViewById(R.id.lay);
            layoutParams.width = d.getLayoutParams().width;
            layoutParams.height = d.getLayoutParams().height;
            videoDialog.setLayoutParams(layoutParams);
            videoDialog.setClickable(true);
            videoDialog.setFocusable(true);
        });
        videoDialog.start();
        videoDialog.setVisibility(View.VISIBLE);
        FadePressets fade = new FadePressets(this, this);
        ImageView btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            videoDialog.stopPlayback();
            fade.fadeOutView();
            videoDialog.setVisibility(View.INVISIBLE);
            btnClose.setVisibility(View.INVISIBLE);
            startDialog(text, finalResult, questText, path);
        });

        videoDialog.setOnClickListener(v -> {


                    Log.d(TAG, "Action Down Detected");
                    if (btnClose.getVisibility() == View.INVISIBLE) {
                        fade.fadeIn(btnClose);
                        Log.d("TouchEvent", "Fading In");
//                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                            try {
//                                // Симуляция длительной задачи
//                                Thread.sleep(3000);
//                                fade.fadeOut(btnClose);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        });
//                        future.join();
                    }
                    else {
                        fade.fadeOut(btnClose);
                        Log.d("TouchEvent", "Fading Out");
                    }

            }
        );

        //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        TimeUnit.SECONDS.sleep(2);

        videoDialog.setOnCompletionListener( v -> {
            videoDialog.stopPlayback();
            fade.fadeOutView();
            videoDialog.setVisibility(View.INVISIBLE);
            fade.fadeOut(btnClose);
            fade.fadeInDimmingView();
            startDialog(text, finalResult, questText, path);
        });

        // Завершаем игру, отправляем данные на след страницу

//        Intent intent = new Intent(this, CardGameActivity.class);
//        setResult(finalResult, intent);
//        finish();

    }

    private void startDialog(String text, int finalResult, String questText, Integer path) {
        View dialog = new View(this);
        dialog = LayoutInflater.from(this).inflate(path, null);
        Dialog myDialog = new Dialog(this);
        TextView textDialog =  (TextView) dialog.findViewById(R.id.locationQuest);
        textDialog.setText(text);
        myDialog.setContentView(dialog);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
        myDialog.show();
        TextView questTextView = (TextView) dialog.findViewById(R.id.questText);
        questTextView.setText(questText);
        Button button = (Button) dialog.findViewById(R.id.btnBattle);
        Button buttonReboot = (Button) dialog.findViewById(R.id.btnReboot);
        buttonReboot.setOnClickListener(v -> {
            myDialog.dismiss();
            Intent intent = new Intent(this, NumbersActivity.class);
            startActivity(intent);
            finish();
        });
        button.setText("Вернуться на карту");
        button.setOnClickListener(v -> {
            myDialog.dismiss();
            finishGame(finalResult);
        });
    }

    private void finishGame(int resultCode) {
        Intent intent = new Intent(this, MainActivity.class);
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onClick(final int y, final int x, final boolean flyDown) {

        final Button currentBut = mButtons[y][x];

        currentBut.setAlpha(0.7f);
        currentBut.setClickable(false);

        AnimationSet sets = new AnimationSet(false);
        int direction = flyDown ? 400 : -400;
        TranslateAnimation animTr = new TranslateAnimation(0, 0, 0, direction);
        animTr.setDuration(810);
        AlphaAnimation animAl = new AlphaAnimation(0.4f, 0f);
        animAl.setDuration(810);
        sets.addAnimation(animTr);
        sets.addAnimation(animAl);
        currentBut.startAnimation(sets);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                currentBut.clearAnimation();
                currentBut.setAlpha(0);
            }
        }, 800);
    }
}
