package com.viatick.bmsandroidsdk.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaMinisite;
import com.viatick.bmsandroidsdk.view.MinisiteActivity;

import java.util.ArrayList;

// To be modified

public class MinisiteMenuAdapter extends RecyclerView.Adapter<MinisiteMenuAdapter.ViewHolder> {
    private static final String TAG = "[VIATICK]";
    private final Context context;
    private ArrayList<ViaMinisite> values;
    private String API_KEY;

    public MinisiteMenuAdapter(Context context, ArrayList<ViaMinisite> values, String API_KEY) {
        super();
        this.context = context;
        this.values = values;
        this.API_KEY = API_KEY;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View rowView;
        public ViewHolder(View v) {
            super(v);
            rowView = v;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(context.getResources().getIdentifier("minisite_menu_each", "layout", context.getPackageName()), parent, false);

        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            TextView title = (TextView) holder.rowView.findViewById(
                    context.getResources().getIdentifier("title", "id", context.getPackageName()));
            TextView text = (TextView) holder.rowView.findViewById(
                    context.getResources().getIdentifier("text", "id", context.getPackageName()));

            final FrameLayout mFrame = (FrameLayout) holder.rowView.findViewById(
                    context.getResources().getIdentifier("frame", "id", context.getPackageName()));

            mFrame.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams mParams;
                    mParams = (LinearLayout.LayoutParams) mFrame.getLayoutParams();
//                    mParams.height = mFrame.getWidth();
                    mFrame.setLayoutParams(mParams);
                    mFrame.postInvalidate();
                }
            });

            final ImageView cover = (ImageView) holder.rowView.findViewById(
                    context.getResources().getIdentifier("cover", "id", context.getPackageName()));
            final ViaMinisite viaMinisite = values.get(position);

            title.setText(viaMinisite.getTitle());
            text.setText(viaMinisite.getDescription());

//            Log.i(TAG, viaMinisite);

            // Use Picasso library to load the photo from server
            Picasso.with(context)
                    .load(viaMinisite.getCoverUrl().replace(" ", "%20"))
                    .into(cover, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.i("WIDTH", String.valueOf(cover.getMeasuredWidth()));
                            Log.i("HEIGHT", String.valueOf(cover.getMeasuredHeight()));
//                            cover.getLayoutParams().width = cover.getMeasuredHeight();
                        }

                        @Override
                        public void onError() {

                        }
                    });

            // Go to the Minisite on event click
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked");
                    Intent minisiteIntent = new Intent(context.getApplicationContext(), MinisiteActivity.class);
                    minisiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    minisiteIntent.putExtra("url", viaMinisite.getUrl().toString());
                    minisiteIntent.putExtra("title", viaMinisite.getTitle());
                    minisiteIntent.putExtra("type", viaMinisite.getType());
                    minisiteIntent.putExtra("customerId", ViaBmsUtil.ViaCustomer.customerId);
                    minisiteIntent.putExtra("API_KEY", API_KEY);

                    context.startActivity(minisiteIntent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void updateValues (ArrayList<ViaMinisite> viaMinisites) {
        Log.i("UPDATE_VALUE",viaMinisites.toString());
        values = viaMinisites;
    }
}
