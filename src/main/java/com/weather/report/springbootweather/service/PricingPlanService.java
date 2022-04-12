package com.weather.report.springbootweather.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PricingPlanService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        PricingPlan pricingPlan = PricingPlan.resolvePlanFromApiKey(apiKey);
        return bucket(pricingPlan.getLimit());
    }

    private Bucket bucket(Bandwidth limit) {
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    public static boolean isValidAPIKey(String apiKey) {
        boolean isValid = false;
        switch (apiKey) {
            case "181f996c74f767dbd25ffc6daa37fd38":
            case "370a8612c37c0a340559b3055c5bd435":
            case "9a448ce5e81d47ef5109f22bb4c7c80b":
            case "8d1514c3a5bc8b086f28a5361b178f9a":
            case "412983cd574f7872a75e1cec08d2ce26":
                isValid = true;
                break;
            default:
                isValid = false;
                break;
        }
        return isValid;
    }
}