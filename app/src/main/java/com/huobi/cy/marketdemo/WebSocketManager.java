package com.huobi.cy.marketdemo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author chenyan@huobi.com
 * @date 2019/8/21 18:07
 * @desp
 */
public class WebSocketManager {

    private static final String TAG = WebSocketManager.class.getSimpleName();

    private static volatile WebSocketManager instance;
    private OkHttpClient mOkHttpClient;
    private EchoWebSocketListener socketListener;
    private WebSocket mWebSocket;
    private OnWebSocketListener mWebSocketListener;
    private Map<String, String> symbolMap = new LinkedHashMap<>();
    private boolean isSockeOpen = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static WebSocketManager getInstance() {
        if(instance == null) {
            synchronized (WebSocketManager.class) {
                if(instance == null) {
                    instance = new WebSocketManager();
                }
            }
        }
        return instance;
    }

    public WebSocketManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        socketListener = new EchoWebSocketListener();
    }

    /**
     * 获取所有交易对
     */
    public void getAllSymbols(final OnCallbackListener callbackListener) {
        Request request = new Request.Builder()
                .url("https://api.huobi.pro/v1/common/symbols")
                .build();
        Call call= mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e(TAG, e.getMessage());
                if(callbackListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbackListener.onFailure("error", e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    if(callbackListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callbackListener.onSuccess(result);
                            }
                        });
                    }
                } else {
                    if(callbackListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callbackListener.onFailure(""+response.code(), response.message());
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * 连接websocket.
     */
    public void connectSocket(OnWebSocketListener onWebSocketListener) {
        if(isSockeOpen) return;

        if(mOkHttpClient != null) {
            mWebSocketListener = onWebSocketListener;
            if(mWebSocket != null) {
                mWebSocket.cancel();
            }
            Request request = new Request.Builder()
                    .url("wss://api.huobi.pro/ws")
                    .build();

            mOkHttpClient.newWebSocket(request, socketListener);
        }
    }

    /**
     * websocket是否连接成功
     * @return
     */
    public boolean isSockeOpen() {
        return isSockeOpen;
    }

    /**
     * 关闭连接
     */
    public void disconnectSocket() {
        if(mWebSocket != null) {
            mWebSocket.cancel();
        }
        if(mWebSocketListener != null) {
            mWebSocketListener = null;
        }
        if(!symbolMap.isEmpty()) {
            symbolMap.clear();
        }
    }

    /**
     * 订阅数据
     */
    public void subMarket1Day(String symbol) {
        if(mWebSocket != null) {
            String sub = "market." + symbol.toLowerCase() + ".kline.1day";
            symbolMap.put(sub, symbol);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("sub", sub);
                jsonObject.put("id", "id1");
                Log.e(TAG,"subMarket1Day: " + jsonObject.toString());
                mWebSocket.send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mWebSocket = webSocket;
            isSockeOpen = true;
            Log.e(TAG,"onOpen:" + response.toString());
            if(mWebSocketListener != null) {
                mWebSocketListener.onOpen();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            try {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer =new  byte[1024];
                int len = -1;
                while ((len = gzipInputStream.read(buffer))!=-1) {
                    outStream.write(buffer, 0, len);
                }
                byte[] data = outStream.toByteArray();
                outStream.close();
                gzipInputStream.close();

                String jsonStr = new String(data);
                Log.e(TAG, "onMessage: " + jsonStr);
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    if(jsonObject.has("ping")) {
                        jsonObject.put("pong", jsonObject.getInt("ping"));
                        webSocket.send(jsonObject.toString());
                    } else {
                        if(jsonObject.has("ch") && jsonObject.has("tick")) {
                            if (mWebSocketListener != null) {
                                jsonObject.put("symbol", symbolMap.get(jsonObject.getString("ch")));
                                mWebSocketListener.onMessage(jsonObject.toString());
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.e(TAG,"onMessage text：" + text);
            if(mWebSocketListener != null) {
                mWebSocketListener.onMessage(text);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            isSockeOpen = false;
            Log.e(TAG, "onClosed:" + reason);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            isSockeOpen = false;
            Log.e(TAG, "onClosing: " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            isSockeOpen = false;
            Log.e(TAG, "onClosing: " + t.getMessage());
        }
    }

    public interface OnWebSocketListener {
        void onOpen();

        void onMessage(String json);
    }

    public interface OnCallbackListener {
        void onSuccess(String result);

        void onFailure(String code, String msg);
    }
}
