package com.home.myweather.testing;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Простая заглушка Retrofit Call без Mockito.
 * Mockito 5 не инициализируется на устройстве (нужен JVM-агент), поэтому
 * в инструментальных тестах используются ручные фейки.
 */
public final class FakeCall<T> implements Call<T> {

    private final Response<T> response;
    private final Throwable failure;
    private boolean canceled;
    private boolean executed;

    private FakeCall(Response<T> response, Throwable failure) {
        this.response = response;
        this.failure = failure;
    }

    public static <T> FakeCall<T> ok(T body) {
        return new FakeCall<>(Response.success(body), null);
    }

    public static <T> FakeCall<T> failing(Throwable throwable) {
        return new FakeCall<>(null, throwable);
    }

    @Override
    public void enqueue(Callback<T> callback) {
        executed = true;
        if (canceled) return;
        if (failure != null) {
            callback.onFailure(this, failure);
        } else {
            callback.onResponse(this, response);
        }
    }

    @Override
    public Response<T> execute() throws IOException {
        executed = true;
        if (failure != null) throw new IOException(failure);
        return response;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Call<T> clone() {
        return new FakeCall<>(response, failure);
    }

    public Request request() {
        return null;
    }

    public Timeout timeout() {
        return Timeout.NONE;
    }
}
