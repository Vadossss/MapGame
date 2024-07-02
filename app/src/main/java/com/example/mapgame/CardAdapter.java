package com.example.mapgame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private List<Card> cardList;
    private OnCardClickListener onCardClickListener;

    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    public CardAdapter(List<Card> cardList, OnCardClickListener onCardClickListener) {
        this.cardList = cardList;
        this.onCardClickListener = onCardClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView cardBack;
        public ImageView cardFront;

        public ViewHolder(View itemView) {
            super(itemView);
            cardBack = itemView.findViewById(R.id.card_back);
            cardFront = itemView.findViewById(R.id.card_front);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.cardFront.setImageResource(card.getImageResId());

        if (card.isFaceUp() || card.isMatched()) {
            holder.cardFront.setVisibility(View.VISIBLE);
            holder.cardBack.setVisibility(View.GONE);
        } else {
            holder.cardFront.setVisibility(View.GONE);
            holder.cardBack.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!card.isFaceUp() && !card.isMatched()) {
                onCardClickListener.onCardClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
