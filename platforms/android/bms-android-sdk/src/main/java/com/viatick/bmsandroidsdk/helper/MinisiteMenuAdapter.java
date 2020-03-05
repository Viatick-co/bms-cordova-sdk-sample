package com.viatick.bmsandroidsdk.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaMinisite;
import com.viatick.bmsandroidsdk.view.MinisiteActivity;

import java.util.ArrayList;
import java.util.List;

// To be modified

public class MinisiteMenuAdapter extends RecyclerView.Adapter<MinisiteMenuAdapter.ViewHolder> {
    private static final String TAG = "[VIATICK]";
    private final Context context;
    private ArrayList<ViaMinisite> values;
    private String API_KEY;
    private int customerId;

    public MinisiteMenuAdapter(Context context, ArrayList<ViaMinisite> values, String API_KEY, int customerId) {
        super();
        this.context = context;
        this.values = values;
        this.API_KEY = API_KEY;
        this.customerId = customerId;
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

            // Use Picasso library to load the photo from server
            Picasso.get()
                    .load(viaMinisite.getCoverUrl().replace(" ", "%20"))
                    .into(cover);

            // Go to the Minisite on event click
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent minisiteIntent = new Intent(context.getApplicationContext(), MinisiteActivity.class);
                    ViaBmsUtil.MinisiteType type = viaMinisite.getType();

                    if (type != ViaBmsUtil.MinisiteType.DEEP_LINK) {
                        minisiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        minisiteIntent.putExtra("minisiteId", viaMinisite.getId());
                        minisiteIntent.putExtra("url", viaMinisite.getUrl());
                        minisiteIntent.putExtra("title", viaMinisite.getTitle());
                        minisiteIntent.putExtra("type", type);
                        minisiteIntent.putExtra("customerId", customerId);

                        context.startActivity(minisiteIntent);
                    } else {
                        String link ="http://www.google.com";
//                        String link = viaMinisite.getDeepLink();
                        Uri uri = Uri.parse(link);

                        String host = uri.getHost();
                        String scheme = uri.getScheme();

                        if (!host.isEmpty() && (scheme.equals("http") || scheme.equals("https"))) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                            context.startActivity(browserIntent);
                        } else {
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

//                            Verify if app XXX has this screen path
                            PackageManager packageManager = context.getPackageManager();
                            List<ResolveInfo> activities =
                                    packageManager.queryIntentActivities(mapIntent, 0);
                            boolean isIntentSafe = activities.size() > 0;

                            //Start HomeActivity of app XXX because it's existed
                            if (isIntentSafe) {
                                context.startActivity(mapIntent);
                            } else {
                                Toast.makeText(context, "No application can handle this request.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
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
        values = viaMinisites;
    }
}
