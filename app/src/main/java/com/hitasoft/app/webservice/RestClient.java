package com.hitasoft.app.webservice;


import android.content.Context;
import android.util.Log;

import com.hitasoft.app.utils.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("ALL")
public class RestClient {

    private static Api api;
    private static Context mContext;
    private static final RestClient restClient = new RestClient(mContext);

    static {
        initRestClient();
    }

    public RestClient(Context context) {
        this.mContext = context;
    }

    private static void initRestClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder().method(original.method(), original.body()).build();
                Log.e("URL ", "" + " - " + request);
                return chain.proceed(request);
            }
        });

        httpClient.addInterceptor(interceptor);

        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        api = retrofit.create(Api.class);
    }

    public RestClient getInstance() {
        return restClient;
    }

    public Api get() {
        return api;
    }
}
