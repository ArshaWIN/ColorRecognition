package com.mihailenko.ilya.colorrecognizer2016.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.activities.viewmodels.ColorItemViewModel;
import com.mihailenko.ilya.colorrecognizer2016.databinding.ColorItemBinding;
import com.mihailenko.ilya.colorrecognizer2016.models.MyColor;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.OnDeleteItemClickListener;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.OnItemClickListener;

import java.util.ArrayList;

/**
 * Created by ILYA on 13.08.2016.
 */

public class ColorHistoryListAdapter extends RecyclerView.Adapter<ColorHistoryListAdapter.ViewHolder>
        implements OnDeleteItemClickListener {

    private ArrayList<MyColor> colors;
    private LayoutInflater layoutInflater;
    private OnItemClickListener<MyColor> onItemClickListener;

    public ColorHistoryListAdapter(ArrayList<MyColor> colors, OnItemClickListener<MyColor> onItemClickListener) {
        this.colors = colors;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        ColorItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.color_item, parent, false);

        return new ViewHolder(binding, onItemClickListener, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (colors == null) {
            return;
        }

        holder.setColor(colors.get(position));
        holder.setColorInfo();

    }

    @Override
    public int getItemCount() {
        return colors == null ? 0 : colors.size();
    }

    @Override
    public void onItemDelete(int position) {
        colors.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ColorItemViewModel {

        private MyColor color;
        private ColorItemBinding itemBinding;
        private final OnItemClickListener<MyColor> onItemClickListener;
        private final OnDeleteItemClickListener onDeleteItemClickListener;

        private MaterialDialog agreeDialog;

        public ViewHolder(ColorItemBinding itemBinding, OnItemClickListener<MyColor> onItemClickListener, OnDeleteItemClickListener onDeleteItemClickListener) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
            this.onItemClickListener = onItemClickListener;
            this.onDeleteItemClickListener = onDeleteItemClickListener;

            itemBinding.setViewModel(this);
        }

        private void setColor(MyColor color) {
            this.color = color;
        }

        private void setColorInfo() {
            String colorInfo = color.getColorHEX() + " " + color.getColorName();
            itemBinding.colorInfo.setText(colorInfo);
            itemBinding.currentColor.setBackgroundColor(Color.parseColor(color.getColorHEX()));
        }

        @Override
        public void onDeleteClick(View view) {
            createDialog(color);
            agreeDialog.show();

        }

        private void createDialog(final MyColor color) {
            agreeDialog = new MaterialDialog.Builder(itemView.getContext())
                    .title(R.string.dialog_tittle_delete)
                    .content(R.string.dialog_delete_content)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            onDeleteItemClickListener.onItemDelete(getAdapterPosition());
                            onItemClickListener.onItemClick(color);
                        }
                    }).build();

        }
    }
}
