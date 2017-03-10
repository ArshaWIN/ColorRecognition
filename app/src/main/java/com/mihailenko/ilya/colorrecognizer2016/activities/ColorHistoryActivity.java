package com.mihailenko.ilya.colorrecognizer2016.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.adapters.ColorHistoryListAdapter;
import com.mihailenko.ilya.colorrecognizer2016.databinding.ActivityHistoryBinding;
import com.mihailenko.ilya.colorrecognizer2016.models.MyColor;
import com.mihailenko.ilya.colorrecognizer2016.utils.DividerItemDecoration;
import com.mihailenko.ilya.colorrecognizer2016.utils.SQLHelper;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.OnItemClickListener;

import java.util.ArrayList;

/**
 * Created by ILYA on 13.08.2016.
 */

public class ColorHistoryActivity extends BaseActivity implements OnItemClickListener<MyColor> {

    private ArrayList<MyColor> colors;
    private SQLHelper sql_helper;

    private MaterialDialog agreeDialog;

    private ColorHistoryListAdapter adapter;

    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history);

        setupActionBar();
        sql_helper = new SQLHelper(this);
        colors = sql_helper.getAllColor();
        createColorList();
    }

    private void createColorList() {
        adapter = new ColorHistoryListAdapter(colors, this);
        binding.colorsList.setAdapter(adapter);
        binding.colorsList.addItemDecoration(new DividerItemDecoration(this,R.drawable.item_divider));
        binding.colorsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(MyColor color) {
        sql_helper.deleteColor(color);
    }



    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(R.string.color_history);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
