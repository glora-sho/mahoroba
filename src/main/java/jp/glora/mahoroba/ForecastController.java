package jp.glora.mahoroba;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Controller
public class ForecastController {
    //private final String url = "https://glora.work/mahoroba/forecast.json";
    private final String endpoint = "https://api.openweathermap.org/data/2.5/forecast";
    private final String lat = "35.5854698853935";
    private final String lon = "139.57623659779637";
    private final String key = "396630025cd4f292536f653d0190589b";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        String url = endpoint + "?" + "lat=" + lat + "&lon=" + lon + "&appid=" + key;
        String json = getJson(url);
        Gson gson = new Gson();
        System.out.println(json);

        Forecast forecast = gson.fromJson(json, Forecast.class);
        java.util.List<jp.glora.mahoroba.List> lists = forecast.getList();

        List<Map<String, String>> forecasts = new ArrayList<Map<String, String>>();
        for(jp.glora.mahoroba.List list : lists){
            Map<String, String> map = new LinkedHashMap<String,String>();

            //予報日時の取得
            long dt = list.getDt();
            Date date = new java.util.Date(dt*1000);
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("M月d日 H時");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Tokyo"));
            String formattedDate = sdf.format(date);
            map.put("date",formattedDate);

            //天気予報出力要素の作成
            List<Weather> weathers = list.getWeather();
            Weather weather = weathers.get(0);
            map.put("weather",getJaForecast(weather.getId(),weather.getDescription()));
            map.put("icon","https://openweathermap.org/img/wn/" + weather.getIcon() + "@2x.png");
            forecasts.add(map);
        }
        model.addAttribute("forecasts", forecasts);
        return "index";
    }

    /**
     * JSONをエンドポイントから取得する
     * @param urlString
     * @return
     */
    private String getJson(String urlString){
        String json = "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                json += line;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * JSONから日本語で天気予報を返却
     * See https://openweathermap.org/weather-conditions
     * @param id
     * @param description
     * @return
     */
    private String getJaForecast(int id,String description){
        switch(id){
            case 500:
            case 501:
                return "晴 時々 雨";
            case 800:
                return "快晴";
            case 801:
                return "晴 時々 曇";
            case 802:
            case 803:
            case 804:
                return "曇";
            default:
                return description;
        }
    }
}
