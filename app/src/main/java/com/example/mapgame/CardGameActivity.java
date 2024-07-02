package com.example.mapgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardGameActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public TextView trsCounter;
    public CardAdapter cardAdapter;
    public List<Card> cardList;
    private int selectedCardPosition = -1;
    private boolean isProcessingTurn = false;
    public int countHelper = 0;
    public int moveCount = 20; // Инициализация счетчика ходов


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_matching_game);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        trsCounter = (TextView)findViewById(R.id.trsCounter);
        trsCounter.setText(String.valueOf(moveCount));

        cardList = generateCards();
        cardAdapter = new CardAdapter(cardList, this::onCardClick);
        recyclerView.setAdapter(cardAdapter);
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

    private void onCardClick(int position) {
        if (isProcessingTurn)
        {
            return;
        }

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
                    finishGame(RESULT_OK);
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
            finishGame(RESULT_CANCELED);
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
        trsCounter.setText(String.valueOf(moveCount));
    }
}