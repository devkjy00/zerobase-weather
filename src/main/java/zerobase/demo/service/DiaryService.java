package zerobase.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.demo.WeatherApplication;
import zerobase.demo.domain.DateWeather;
import zerobase.demo.domain.Diary;
import zerobase.demo.error.InvalidDate;
import zerobase.demo.repository.DateWeatherRepository;
import zerobase.demo.repository.DiaryRepository;

@Service
@Transactional(readOnly = true)
public class DiaryService {

    @Value("${open_weather_map.key}")
    private String API_KEY;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository,
        DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void createDiary(LocalDate date, String text) {
        logger.info("createDiary");

        DateWeather dateWeather = getDateWeather(date);

        Diary nowDiary = Diary.builder()
            .weather(dateWeather.getWeather())
            .icon(dateWeather.getIcon())
            .temperature(dateWeather.getTemperature())
            .text(text)
            .date(date)
            .build();


        diaryRepository.save(nowDiary);
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeathersFromDB = dateWeatherRepository.findAllByDate(date);

        if (dateWeathersFromDB.isEmpty()) {
            logger.info("getDateWeather from API");
            return getWeatherFromApi();
        }
        return dateWeathersFromDB.get(0);
    }

    @Transactional
    @Scheduled(cron = "0/5 * * * * *")
    public void saveWeatherDate(){
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi(){
       String weather = getWeatherString();
       Map<String, Object> parsedWeather = parseWeather(weather);
       DateWeather dateWeather = DateWeather.builder()
           .date(LocalDate.now())
           .weather(parsedWeather.get("main").toString())
           .icon(parsedWeather.get("icon").toString())
           .temperature((double) parsedWeather.get("temp"))
           .build();

       return dateWeather;
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + API_KEY;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private Map<String, Object> parseWeather(String weatherString) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) parser.parse(weatherString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArr = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArr.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }

    public List<Diary> readDiary(LocalDate date) {
//        if (date.isAfter(LocalDate.ofYearDay(3050, 1))){
//            throw new InvalidDate();
//        }

        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional()
    public void updateDiary(LocalDate date, String text) {
        Diary diary = diaryRepository.getFirstByDate(date)
            .orElseThrow(RuntimeException::new);
        diary.setText(text);
        diaryRepository.save(diary);
    }

    @Transactional()
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
