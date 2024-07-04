package com.example.mapgame;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CardGameActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public TextView trsCounter;
    public CardAdapter cardAdapter;
    public List<Card> cardList;
    private int selectedCardPosition = -1;
    private boolean isProcessingTurn = false;
    public int countHelper = 0;
    public int moveCount = 20; // Инициализация счетчика ходов
    private VideoView videoDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_matching_game);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        trsCounter = (TextView)findViewById(R.id.scoreboard);
        trsCounter.setText("Ходы: "+ moveCount);

        videoDialog = (VideoView) findViewById(R.id.videoDialogView);

        cardList = generateCards();
        cardAdapter = new CardAdapter(cardList, this::onCardClick);
        recyclerView.setAdapter(cardAdapter);

        ImageView btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, CardGameActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        });
    }

    private List<Card> generateCards() {
        List<Card> cards = new ArrayList<>();
        int[] images = {R.drawable.imag1, R.drawable.image2, R.drawable.image3, R.drawable.image4,
                R.drawable.image5, R.drawable.image6, R.drawable.image7, R.drawable.image8};
        for (int image : images) {
            cards.add(new Card(image));
            cards.add(new Card(image)); // Добавляем пары карточек
        }
        Collections.shuffle(cards); // Перемешиваем карточки
        return cards;
    }

    private void setVideo() {
        videoDialog.setOnPreparedListener( v-> {
            ViewGroup.LayoutParams layoutParams = videoDialog.getLayoutParams();
            View d = (View)findViewById(R.id.main);
            layoutParams.width = d.getLayoutParams().width;
            layoutParams.height = d.getLayoutParams().height;
            videoDialog.setLayoutParams(layoutParams);
            videoDialog.setClickable(true);
            videoDialog.setFocusable(true);
        });
        videoDialog.start();
        videoDialog.setVisibility(View.VISIBLE);
    }

    private void onCardClick(int position) {
        if (isProcessingTurn)
        {
            return;
        }
        FadePressets fade = new FadePressets(this, this);

        countHelper++; // Увеличиваем счетчик открытых карт
        if (countHelper % 2 == 0) {
            // Уменьшаем счетчик ходов только при каждой второй открытой карте
            moveCount--;
            updateMoveCountUI();
        }

        Card clickedCard = cardList.get(position);
        clickedCard.setFaceUp(true);
        cardAdapter.notifyItemChanged(position);

        if (selectedCardPosition == -1) {
            selectedCardPosition = position;
        } else {
            isProcessingTurn = true;
            Card selectedCard = cardList.get(selectedCardPosition);

            if (selectedCard.getImageResId() == clickedCard.getImageResId()) {
                selectedCard.setMatched(true);
                clickedCard.setMatched(true);
                if (allCardsMatched()) {
                    fade.fadeInDimmingView();
                    videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.solovei_win1);
                    setVideo();
                    videoDialog.setOnCompletionListener( v -> {
                        videoDialog.stopPlayback();
                        fade.fadeOutView();
                        videoDialog.setVisibility(View.INVISIBLE);
                        fade.fadeInDimmingView();
                        View dialog = new View(this);
                        dialog = LayoutInflater.from(this).inflate(R.layout.dialog_enemy, null);
                        Dialog myDialog = new Dialog(this);
                        TextView text = (TextView) dialog.findViewById(R.id.locationQuest);
                        TextView questText = (TextView) dialog.findViewById(R.id.questText);
                        questText.setText("Вы смогли победить Соловья, но это не всё. В следующий раз его уловки на вас не сработают!");
                        text.setText("Вы выиграли!");
                        myDialog.setContentView(dialog);
                        myDialog.setCancelable(false);
                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
                        myDialog.show();


                        Button button = (Button) dialog.findViewById(R.id.btnBattle);
                        button.setText("Выйти на карту");
                        button.setOnClickListener(s -> {
                            myDialog.dismiss();
                            finishGame(RESULT_OK);
                        });

                    });
                }
                else {
                    resetTurn();
                }
            } else {
                new Handler().postDelayed(() -> {
                    selectedCard.setFaceUp(false);
                    clickedCard.setFaceUp(false);
                    cardAdapter.notifyItemChanged(selectedCardPosition);
                    cardAdapter.notifyItemChanged(position);
                    resetTurn();
                }, 1000);
            }
        }

        if (moveCount == 0 && !allCardsMatched()) {
            fade.fadeInDimmingView();
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.solovei_lose1);
            setVideo();

            videoDialog.setOnCompletionListener( s -> {

                View dialog = new View(this);
                dialog = LayoutInflater.from(this).inflate(R.layout.dialog_lose, null);
                Dialog myDialog = new Dialog(this);
                TextView text = (TextView) dialog.findViewById(R.id.locationQuest);
                text.setText("Вы проиграли!");
                myDialog.setContentView(dialog);
                myDialog.setCancelable(false);
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
                myDialog.show();

                Button buttonReboot = (Button) dialog.findViewById(R.id.btnReboot);
                buttonReboot.setOnClickListener(v -> {
                    myDialog.dismiss();
                    Intent intent = new Intent(this, CardGameActivity.class);
                    startActivity(intent);
                    finish();
                });

                Button button = (Button) dialog.findViewById(R.id.btnBattle);
                button.setOnClickListener(v -> {
                    myDialog.dismiss();
                    finishGame(RESULT_CANCELED);
                });
            });

        }
    }

    private boolean allCardsMatched() {
        for (Card card : cardList) {
            if (!card.isMatched()) {
                return false;
            }
        }
        return true;
    }

    private void finishGame(int resultCode) {
        Intent intent = new Intent(this, MainActivity.class);
        setResult(resultCode, intent);
        finish();
    }

    private void resetTurn() {
        selectedCardPosition = -1;
        isProcessingTurn = false;
    }

    private void updateMoveCountUI() {
        trsCounter.setText("Ходы: "+ moveCount);
    }
}