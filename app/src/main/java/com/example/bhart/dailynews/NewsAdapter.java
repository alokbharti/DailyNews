package com.example.bhart.dailynews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Context context;
    private List<News> list;

    public NewsAdapter(Context context, List<News> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {

        News item = list.get(position);
        holder.heading.setText(item.getHeading());
        holder.desc.setText(item.getDetails());
        holder.link.setText(item.getNews_link());
        Picasso.with(context).load(item.getImage()).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public TextView heading;
        public TextView desc;
        public ImageView image;
        public TextView link;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            heading = (TextView)itemView.findViewById(R.id.topic);
            desc = (TextView)itemView.findViewById(R.id.details);
            link=(TextView)itemView.findViewById(R.id.image_link);
            image = (ImageView)itemView.findViewById(R.id.imageView);
            cardView = (CardView)itemView.findViewById(R.id.news_card);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Url = link.getText().toString();
                    Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(Url));
                    v.getContext().startActivity(intent);
                }
            });

        }
    }
}

