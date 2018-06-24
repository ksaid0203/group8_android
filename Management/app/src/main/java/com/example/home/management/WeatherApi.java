package com.example.home.management;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WeatherApi extends Thread {
    public interface EventListener {
        void onApiResult(String result);
        void onApiFailed();
    }

    private Handler handler; // 메인 쓰레드에 연결될 핸들러
    private GoogleRouteApi.EventListener listener; // LocationActivity에서 전달될 콜백 리스너를 저장할 변수
    private String apiKey; // apiKey저장
    private LatLng Point;

    public WeatherApi(Context context, LatLng Point, GoogleRouteApi.EventListener eventListener) {
        this.handler = new Handler(Looper.getMainLooper());
        this.listener=eventListener;
        this.apiKey = "1a9ef4f75e14723189048e639816314b";
        this.Point = Point;
    }

    @Override
    public void run() {
        HttpsURLConnection httpsURLConnection = null;
        try {

            //https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY

            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat="
                    + Point.latitude+"&lon=" + Point.longitude
                    + "&appid="
                    + apiKey
            );
            httpsURLConnection = (HttpsURLConnection) url.openConnection(); // 연결
            if(httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) { // 응답코드 확인
                InputStream inputStream = httpsURLConnection.getInputStream(); // 바이트 단위로 데이터 반환
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8"); // utf-8 문자로 데이터 반환
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // 한줄씩 문자열 데이터 반환

                String line; // 하나의 라인을 저장할 변수
                StringBuilder stringBuilder = new StringBuilder(); // 전체 문자열을 저장할 수 있는 객체
                while((line = bufferedReader.readLine())!=null) { // 더이상 읽을 라인이 없을때까지
                    stringBuilder.append(line); // stringbuilder에 라인단위로 추가
                }
                bufferedReader.close(); // 리소스 정리
                String result = stringBuilder.toString(); // string형으로 반환
                onApiResult(result); // 성공 결과 전달
            } else {
                onApiFailed(); // OK가 아닐때 요청 실패
            }
        } catch (Exception e) {
            e.printStackTrace();
            onApiFailed(); // 예외 발생시 실패 콜백
        } finally {
            if(httpsURLConnection != null) { // Connection이 있는지 확인
                httpsURLConnection.disconnect(); // 연결 종료
            }
        }
    }

    private void onApiResult(final String result) {
        if(listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onApiResult(result);
                }
            });
        }
    }

    private void onApiFailed() {
        if(listener!=null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onApiFailed();
                }
            });
        }
    }
}
