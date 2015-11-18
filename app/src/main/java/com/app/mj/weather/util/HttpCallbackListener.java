package com.app.mj.weather.util;

public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);
}
