package com.nal2nal2.recipe;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // 사진 속 글자를 폰 안에서 읽어주는 기능을 웹 화면에 연결해요
        registerPlugin(TextReaderPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
