package com.soapgu.sntptest;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.util.SntpClient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private final CompositeDisposable disposables = new CompositeDisposable();

    @SuppressLint("SimpleDateFormat")
    private static transient final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private TextView tv_show_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tv_show_time = this.findViewById(R.id.tv_show_time);
        this.findViewById(R.id.btn_test).setOnClickListener( v -> {
            disposables.add( this.getSntpTime( "172.16.30.240" )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( t -> {
                        Date data = new Date(t);
                        Logger.i( "sntp ms:%s",t );
                        tv_show_time.setText( format.format(data) );
                    }, throwable -> {
                        tv_show_time.setText(throwable.getMessage());
                        Logger.e( "getSntpTime error",throwable );
                    }) );
        } );
        //"172.16.30.240"
    }

    @OptIn(markerClass = UnstableApi.class)
    private Single<Long> getSntpTime(String ntpHost){
        return Single.create( emitter -> {
            SntpClient.setNtpHost(ntpHost);
            SntpClient.initialize(null, new SntpClient.InitializationCallback() {
                @Override
                public void onInitialized() {
                    Logger.i(" elapsedRealtime:%s, get offset ms:%s" , SystemClock.elapsedRealtime(),SntpClient.getElapsedRealtimeOffsetMs());
                    emitter.onSuccess( SystemClock.elapsedRealtime() + SntpClient.getElapsedRealtimeOffsetMs() );
                }

                @Override
                public void onInitializationFailed(IOException error) {
                    emitter.onError(error);
                }
            });
        } );

    }
}