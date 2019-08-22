package com.huobi.cy.marketdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.huobi.cy.marketdemo.databinding.ActivityMainBinding;
import com.huobi.cy.marketdemo.model.EventBusMessage;

import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    ActivityMainBinding activityMainBinding = null;
    private List<Fragment> fragmentList = new ArrayList<>(10);
    private List<String> titleList = new ArrayList<>(10);
    private ProgressDialog loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initView();
        initData();
    }

    private void initMagicIndicator(final ViewPager mViewPager) {
        activityMainBinding.tabVp.setBackgroundColor(Color.WHITE);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(false);
        commonNavigator.setScrollPivotX(0.8f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mViewPager.getAdapter().getCount();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setText(mViewPager.getAdapter().getPageTitle(index));
                simplePagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                simplePagerTitleView.getPaint().setFakeBoldText(true);
                simplePagerTitleView.setNormalColor(getResources().getColor(R.color.color_999999));
                simplePagerTitleView.setSelectedColor(getResources().getColor(R.color.colorAccent));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setLineHeight(UIUtil.dip2px(context, 2));
//                indicator.setLineWidth(UIUtil.dip2px(context, 20));
//                indicator.setRoundRadius(UIUtil.dip2px(context, 1.5));
//                indicator.setStartInterpolator(new AccelerateInterpolator());
//                indicator.setEndInterpolator(new DecelerateInterpolator(2f));
                indicator.setColors(getResources().getColor(R.color.colorAccent));
                return indicator;
            }
        });
        activityMainBinding.tabVp.setNavigator(commonNavigator);
        ViewPagerHelper.bind(activityMainBinding.tabVp, mViewPager);
    }

    private void initView() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("USDT", "USDT");
        map.put("HUSD", "HUSD");
        map.put("BTC", "BTC");
        map.put("ETH", "ETH");
        map.put("HT", "HT");
        map.put("ALTS", "TRX");
        for(String key : map.keySet()) {
            Fragment fragment = new FragmentCommonCoins();
            Bundle bundle = new Bundle();
            bundle.putString("title", map.get(key));
            fragment.setArguments(bundle);
            titleList.add(key);
            fragmentList.add(fragment);
        }
        activityMainBinding.vpMain.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragmentList.get(i);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }
        });
        initMagicIndicator(activityMainBinding.vpMain);
    }

    private void initData() {
        connectWebSocket();
        loadingProgress = new ProgressDialog(this);
        loadingProgress.setMessage("loading...");
        loadingProgress.show();
        WebSocketManager.getInstance().getAllSymbols(new WebSocketManager.OnCallbackListener() {
            @Override
            public void onSuccess(String result) {
                loadingProgress.dismiss();
                if(!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if("ok".equals(object.getString("status"))) {
                            JSONArray data = object.getJSONArray("data");
                            EventBus.getDefault().postSticky(new EventBusMessage(EventBusMessage.EVENT_GET_SYMBOLS, data));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(String code, final String msg) {
                loadingProgress.dismiss();
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectWebSocket() {
        WebSocketManager.getInstance().connectSocket(new WebSocketManager.OnWebSocketListener() {
            @Override
            public void onOpen() {
                Log.e(TAG,"onOpen");
            }

            @Override
            public void onMessage(String json) {
                EventBus.getDefault().post(new EventBusMessage<>(EventBusMessage.EVENT_RECV_MESSAGE, json));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().disconnectSocket();
    }
}
