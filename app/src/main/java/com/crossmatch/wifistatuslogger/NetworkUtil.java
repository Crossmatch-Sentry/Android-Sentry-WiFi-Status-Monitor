package com.crossmatch.wifistatuslogger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.NonNull;


public class NetworkUtil {
    public static void checkNetworkInfo(Context context, final OnConnectionStatusChange onConnectionStatusChange){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null){
                onConnectionStatusChange.onChange(false);
            }
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    onConnectionStatusChange.onChange(true);
                }
                @Override
                public void onLost(@NonNull Network network) {
                    onConnectionStatusChange.onChange(false);
                }
            });

        }
        //for android version below Nougat api 24
        else {

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest,new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    onConnectionStatusChange.onChange(true);
                }
                @Override
                public void onLost(@NonNull Network network) {
                    onConnectionStatusChange.onChange(false);
                }
            });
            //onConnectionStatusChange.onChange(networkInfo!= null && networkInfo.isConnectedOrConnecting());
        }
    }

    interface OnConnectionStatusChange{

        void onChange(boolean type);
    }
}