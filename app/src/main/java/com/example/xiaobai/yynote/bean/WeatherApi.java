package com.example.xiaobai.yynote.bean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public  class WeatherApi {
    private  String apiURL;
    private  String apiIP;
    public  static WeatherInfo weatherInfo= new WeatherInfo();
    public  WeatherApi(String apiURL,String apiIP){
        this.apiURL =apiURL;
        this.apiIP =apiIP;
    }
    public  boolean JsonWeather() {
        try {
            String city = JsonCity();
            city = java.net.URLEncoder.encode(city, "UTF-8");
            String apiUrl = String.format("%s?city=%s", apiURL, city);
            URL url = new URL(apiUrl);
            URLConnection open = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(open.getInputStream(), "UTF-8"));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null)
                str.append(line);
            br.close();

            String date = str.toString();
            JSONObject dataOfJson = JSONObject.fromObject(date);

            //创建WeatherInfo对象，提取所需的天气信息


            //从json数据中提取数据
            String data = dataOfJson.getString("data");
//			System.out.println(data);
            dataOfJson = JSONObject.fromObject(data);
            String weather = dataOfJson.getString("weather");
            JSONObject weatherOfJson = JSONObject.fromObject(weather);
            weatherInfo.setCityname(dataOfJson.getString("city"));
            ;
            weatherInfo.setAirquality(weatherOfJson.getString("aqi"));
            weatherInfo.settemperatureNow(weatherOfJson.getString("current_temperature"));
            //获取预测的天气预报信息
            JSONArray forecast = weatherOfJson.getJSONArray("forecast_list");
            //取得当天的
            JSONObject result = forecast.getJSONObject(1);

            weatherInfo.setDate(result.getString("date"));

            String high = result.getString("high_temperature");
            String low = result.getString("low_temperature");

            weatherInfo.setTemperature(low + "~" + high);
            weatherInfo.setWeather(result.getString("condition"));
//        System.out.println(weatherInfo.toString());
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    private  String JsonCity() throws Exception{
//		String apiIp=String.format("%s?ip=%s", apiIP,getIP());
        URL url=new URL(apiIP);
        URLConnection open=url.openConnection();
        BufferedReader br=new BufferedReader(new InputStreamReader(open.getInputStream(),"gb2312"));
        StringBuilder str = new StringBuilder();
        String line=null;
        while((line=br.readLine())!=null)
            str.append(line);
        br.close();
        String date=str.toString();
        date=parse(date);
//		System.out.println(date.toString());
//		JSONObject dataOfJson = JSONObject.fromObject(date);
//		//从json数据中提取数据
//		String data = dataOfJson.getString("data");
//
//		dataOfJson = JSONObject.fromObject(data);
//		String citys=dataOfJson.getString("city");
        return date;
    }
    private  String parse(String s)
    {
        Pattern pattern =Pattern.compile("位置：(.+?) ");
        Matcher matcher=pattern.matcher(s);

        while(matcher.find())

        {
            s=matcher.group(1);
        }

//Pattern pattern1 =Pattern.compile("省(.+?)市");
//Matcher matcher1=pattern.matcher(s);
//while(matcher1.find())
//
//{
//
// s=matcher1.group(1);
//
//}
        return s;

    }


}
