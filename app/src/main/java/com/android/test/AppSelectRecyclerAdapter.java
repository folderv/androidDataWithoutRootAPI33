package com.android.test;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class AppSelectRecyclerAdapter extends RecyclerView.Adapter<AppSelectRecyclerAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<AppSelectDialogFragment.AppItem> appList;
    private OnAppClickListener onAppClickListener;


    interface OnAppClickListener {
        void onAppClicked(AppSelectDialogFragment.AppItem appItem);
    }

    public void setOnAppClickListener(OnAppClickListener onAppClickListener) {
        this.onAppClickListener = onAppClickListener;
    }

    public AppSelectRecyclerAdapter(List<AppSelectDialogFragment.AppItem> appList) {
        this.appList = appList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_select_app;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppSelectDialogFragment.AppItem app = getItem(position);
        String pkg = app.getPkg();
        holder.appItem.setOnClickListener(view -> {
            if (onAppClickListener != null) {
                onAppClickListener.onAppClicked(app);
            }
        });
        holder.tvName.setText(app.getName());
        holder.tvPkg.setText(pkg);
        holder.ivTag.setVisibility(app.getHasPermission() ? View.VISIBLE : View.GONE);

        try {
            Context ctx = holder.ivIcon.getContext();
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(pkg, 0);
            if (packageInfo != null) {
                GlideApp.with(holder.ivIcon)
                        .load(packageInfo)
                        .placeholder(R.drawable.file_icon_apk)
                        .into(holder.ivIcon);
                //Log.w(TAG, "load PackageInfo: $it")
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return appList == null ? 0 : appList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        AppSelectDialogFragment.AppItem app = getItem(position);
        String name = app.getName();
        if (!TextUtils.isEmpty(name)) {
            char firstChar = name.charAt(0);
            return String.valueOf(firstChar).toUpperCase();
        }
        return "";
    }

    @NonNull
    private AppSelectDialogFragment.AppItem getItem(int position) {
        return appList.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View appItem;
        public TextView tvName;
        public TextView tvPkg;
        public ImageView ivIcon;
        public ImageView ivTag;

        ViewHolder(View itemView) {
            super(itemView);

            appItem = itemView.findViewById(R.id.appItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvPkg = itemView.findViewById(R.id.tvPkg);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivTag = itemView.findViewById(R.id.ivTag);
        }
    }
}
