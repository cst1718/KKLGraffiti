package com.kkl.graffiti;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author cst1718 on 2018/12/21 11:14
 * @explain
 */
public class WebActivity extends BaseActivity{
    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_webtest;
    }

    @Override
    public void initViewsAndListeners() {
        WebView webview = (WebView) findViewById(R.id.webView);
        WebSettings wv_setttig = webview.getSettings();
        wv_setttig.setJavaScriptEnabled(true);
        // wv_setttig.setRenderPriority(RenderPriority.HIGH);
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        String url = "http://h.4399.com/play/202578.htm";
        webview.loadUrl(url);
    }
}
