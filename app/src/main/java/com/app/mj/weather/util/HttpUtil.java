package com.app.mj.weather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);//URL对象
					connection = (HttpURLConnection) url.openConnection();//HttpURLConnection实例
					connection.setRequestMethod("GET");//会用GET方式
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();//获得输入流
                    //对输入流进行读取，存入response中
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					if (listener != null) {listener.onFinish(response.toString());}
				} catch (Exception e) {
					if (listener != null) {listener.onError(e);}
				}
                //当然最后要把连接关闭
                finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

}