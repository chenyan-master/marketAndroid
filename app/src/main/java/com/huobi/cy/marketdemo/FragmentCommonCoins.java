package com.huobi.cy.marketdemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huobi.cy.marketdemo.adapter.MarketAdapter;
import com.huobi.cy.marketdemo.databinding.FragmentCommonCoinsBinding;
import com.huobi.cy.marketdemo.model.EventBusMessage;
import com.huobi.cy.marketdemo.model.TickerBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * @author chenyan@huobi.com
 * @date 2019/8/22 10:44
 * @desp
 */
public class FragmentCommonCoins extends Fragment {

    FragmentCommonCoinsBinding fragmentCommonCoinsBinding;

    private String mTitle;
    private MarketAdapter marketAdapter;
    private List<TickerBean> tickerBeanList = new ArrayList<>();
    private Map<String, TickerBean> symbolMap = new LinkedHashMap<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentCommonCoinsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_common_coins, container, false);
        initView();
        initData();
        return fragmentCommonCoinsBinding.getRoot();
    }

    private void initView() {
        fragmentCommonCoinsBinding.rcvMain.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        marketAdapter = new MarketAdapter(getActivity(), tickerBeanList);
        fragmentCommonCoinsBinding.rcvMain.setAdapter(marketAdapter);
    }

    private void initData() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            mTitle = bundle.getString("title");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(EventBusMessage message) {
        if (EventBusMessage.EVENT_GET_SYMBOLS == message.code) {
            try {
                JSONArray data = (JSONArray) message.data;
                for(int i = 0;i < data.length();i ++) {
                    JSONObject symbol = data.getJSONObject(i);
                    if (!TextUtils.isEmpty(mTitle) && mTitle.equalsIgnoreCase(symbol.getString("quote-currency"))) {
                        if (!"online".equalsIgnoreCase(symbol.getString("state"))) {
                            continue;
                        }
                        String leftCoin = symbol.getString("base-currency");
                        TickerBean tickerBean = new TickerBean();
                        tickerBean.setLeftCoin(leftCoin);
                        tickerBean.setRightCoin(mTitle);
                        tickerBean.setSymbol(symbol.getString("symbol"));
                        tickerBeanList.add(tickerBean);
                        Collections.sort(tickerBeanList);
                        symbolMap.put(tickerBean.getSymbol(), tickerBean);
                    }
                }
                getActivity().getWindow().getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        marketAdapter.notifyDataSetChanged();
                    }
                });
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if(WebSocketManager.getInstance().isSockeOpen()) {
                                subMarkets();
                                break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(EventBusMessage.EVENT_RECV_MESSAGE == message.code) {
            handleMessage((String) message.data);
        }
    }

    private void subMarkets() {
        if(tickerBeanList != null) {
            for(int i = 0;i < tickerBeanList.size();i ++) {
                WebSocketManager.getInstance().subMarket1Day(tickerBeanList.get(i).getSymbol());
            }
        }
    }

    private void handleMessage(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has("symbol")) {
                String symbol = jsonObject.getString("symbol");
                if(!TextUtils.isEmpty(symbol)) {
                    TickerBean tickerBean = symbolMap.get(symbol);
                    if(tickerBean == null) return;
                    JSONObject tick = jsonObject.getJSONObject("tick");
                    tickerBean.setOpen(tick.getDouble("open"));
                    tickerBean.setClose(tick.getDouble("close"));
                    tickerBean.setAmount(tick.getDouble("amount"));
                    tickerBean.setClose(tick.getDouble("close"));

                    if(fragmentCommonCoinsBinding.rcvMain.getScrollState() != SCROLL_STATE_IDLE) {
                        return;
                    }
                    getActivity().getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            marketAdapter.notifyDataSetChanged();

                        }
                    }, 200);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().removeAllStickyEvents();
            EventBus.getDefault().unregister(this);
        }
    }
}
