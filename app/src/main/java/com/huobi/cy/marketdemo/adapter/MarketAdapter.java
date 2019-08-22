package com.huobi.cy.marketdemo.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.huobi.cy.marketdemo.R;
import com.huobi.cy.marketdemo.databinding.ItemTickerBinding;
import com.huobi.cy.marketdemo.model.TickerBean;

import java.util.List;

/**
 * @author chenyan@huobi.com
 * @date 2019/8/21 14:00
 * @desp
 */
public class MarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<TickerBean> mList;

    public MarketAdapter(Context context, List<TickerBean> datas) {
        mContext = context;
        mList = datas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ItemTickerBinding tickerBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_ticker, viewGroup, false);
        return new VH(tickerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof VH) {
            VH vh = (VH) viewHolder;
            TickerBean tickerBean = mList.get(position);
            vh.itemTickerBinding.tvLeftCoin.setText(tickerBean.getLeftCoin().toUpperCase());
            vh.itemTickerBinding.tvRightCoin.setText("/" + tickerBean.getRightCoin().toUpperCase());
            vh.itemTickerBinding.tvCloseCny.setText(String.format("¥%.2f", tickerBean.getClose() * 7.0f));
            String close_price = "";
            if(tickerBean.getClose() < 1) {
                close_price = String.format("%f", tickerBean.getClose());
            } else if (tickerBean.getClose() < 100) {
                close_price = String.format("%.4f", tickerBean.getClose());
            } else {
                close_price = String.format("%.2f", tickerBean.getClose());
            }
            vh.itemTickerBinding.tvClosePrice.setText(close_price);
            vh.itemTickerBinding.tvDayAmountValue.setText(String.valueOf((int)tickerBean.getAmount()));
            double rate = (tickerBean.getClose()-tickerBean.getOpen())/tickerBean.getOpen() * 100;
            if(rate > 0) {
                vh.itemTickerBinding.tvRate.setText(String.format("+%.2f%%", rate));
                vh.itemTickerBinding.tvRate.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_bg_corner_green));
            } else if(rate < 0) {
                vh.itemTickerBinding.tvRate.setText(String.format("%.2f%%", rate));
                vh.itemTickerBinding.tvRate.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_bg_corner_red));
            } else {
                vh.itemTickerBinding.tvRate.setText(String.format("%.2f%%", 0f));
                vh.itemTickerBinding.tvRate.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_bg_corner_gray));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public class VH extends RecyclerView.ViewHolder {
        ItemTickerBinding itemTickerBinding;

        public VH(ItemTickerBinding itemTickerBinding) {
            super(itemTickerBinding.getRoot());
            this.itemTickerBinding = itemTickerBinding;
        }
    }
}
