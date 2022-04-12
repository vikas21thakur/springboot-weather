package com.weather.report.springbootweather.repository;

import com.weather.report.springbootweather.model.WeatherMapData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherMapDataRepository extends JpaRepository<WeatherMapData, Long> {

}
