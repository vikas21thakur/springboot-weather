package com.weather.report.springbootweather.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weather.report.springbootweather.model.WeatherMapData;
import com.weather.report.springbootweather.repository.WeatherMapDataRepository;
import com.weather.report.springbootweather.service.PricingPlanService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WelcomeController {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    PricingPlanService pricingPlanService;

    @Autowired
    WeatherMapDataRepository weatherMapDataRepository;

    @GetMapping("/welcome")
    public String getString() {
        return "Welcome Sir..";
    }

    @GetMapping("/currentweather")
    public ResponseEntity<String> getOpenWeatherMap(@RequestHeader(value = "X-api-key") String apiKey,
                                                    @RequestParam(required = true) String q) {
        String countryCD = null;
        String cityCD = null;
        String description ="Invalid Request : ";
        String resp =null;
        ResponseEntity<String> responseString = null;
        try {
            if(isValidString(q) && isValidString(apiKey) ){
                String[] strParams = q.split(",");
                if(2 != strParams.length)
                    description += " q parameter should have city code,country code..";
                else if(!PricingPlanService.isValidAPIKey(apiKey))
                    description += " Invalid API key..";
                else if(2 == strParams.length) {
                    cityCD = strParams[0];
                    countryCD = strParams[1];
                    Bucket bucket = pricingPlanService.resolveBucket(apiKey);
                    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
                    if (probe.isConsumed()) {
                        String url = "http://api.openweathermap.org/data/2.5/weather?q="+q+"&APPID="+apiKey;
                        responseString = restTemplate.getForEntity(url,String.class);
                        resp = responseString.getBody().toString();
                        description = getDescriptionfromResponse(resp);
                        weatherMapDataRepository.save(new WeatherMapData(cityCD,countryCD,apiKey,resp));
                        return ResponseEntity.ok()
                                .header("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()))
                                .body(description);
                    }
                    description = "hourly limit has been exceeded for apiKey =   "+apiKey;
                    weatherMapDataRepository.save(new WeatherMapData(cityCD,countryCD,apiKey,resp));
                    long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill))
                            .body(description);
                }
            }
            return new ResponseEntity<>(description, HttpStatus.OK);
        } catch (Exception e) {
            description = e.getMessage();
            return new ResponseEntity<>(description, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    String getDescriptionfromResponse(String resp){
        String description =resp;
        JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject();
        if(null != jsonObject){
            if(jsonObject.has("weather")){
                JsonArray arr = jsonObject.getAsJsonArray("weather");
                for(JsonElement j : arr){
                    if(arr.size() > 1)
                        description += "," + String.valueOf(j.getAsJsonObject().get("description"));
                    else
                        description = String.valueOf(j.getAsJsonObject().get("description"));
                }

            }
        }
        return description;
    }
    boolean isValidString(String str){
        if(StringUtils.isBlank(str))
            return  false;
        else if("null".equalsIgnoreCase(str.trim()))
            return false;

        return true ;
    }
}
