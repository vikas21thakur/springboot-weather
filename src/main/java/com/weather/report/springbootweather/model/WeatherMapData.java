package com.weather.report.springbootweather.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "current_weather")
public class WeatherMapData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "citycd")
    private String citycd;

    @Column(name = "countrycd")
    private String countrycd;

    @Column(name = "apikey")
    private String apikey;

    @Column(name = "dateCreated")
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "apiresponse")
    @Lob
    private String apiresponse;

    public WeatherMapData(String citycd, String countrycd, String apikey, String apiresponse) {
        this.citycd = citycd;
        this.countrycd = countrycd;
        this.apikey = apikey;
        this.apiresponse = apiresponse;
    }

    @Override
    public String toString() {
        return "WeatherMapData{" +
                "id=" + id +
                ", citycd='" + citycd + '\'' +
                ", countrycd='" + countrycd + '\'' +
                ", apikey='" + apikey + '\'' +
                ", dateCreated=" + dateCreated +
                ", apiresponse='" + apiresponse + '\'' +
                '}';
    }
}
