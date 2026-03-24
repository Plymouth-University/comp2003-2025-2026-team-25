package com.example.qtrobot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CardModel> cardList;

    public CardAdapter(List<CardModel> cardList) {
        this.cardList = cardList;
    }

    @Override
    public int getItemViewType(int position) {
        return cardList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CardModel.TYPE_VIDEO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_card_video, parent, false);
            return new VideoCardViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CardModel card = cardList.get(position);
        if (holder instanceof VideoCardViewHolder) {
            VideoCardViewHolder videoHolder = (VideoCardViewHolder) holder;
            videoHolder.title.setText(card.getTitle());
            videoHolder.description.setText(card.getDescription());
        } else if (holder instanceof CardViewHolder) {
            CardViewHolder cardHolder = (CardViewHolder) holder;
            cardHolder.title.setText(card.getTitle());
            cardHolder.description.setText(card.getDescription());
            cardHolder.image.setImageResource(card.getImageResId());
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // Existing image card ViewHolder
    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
        }
    }

    // Video placeholder card ViewHolder
    static class VideoCardViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;

        public VideoCardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_video_title);
            description = itemView.findViewById(R.id.card_video_description);
        }
    }
}
