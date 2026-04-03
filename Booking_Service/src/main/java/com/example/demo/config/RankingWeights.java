package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ranking.weights")
public class RankingWeights {
    private double trust = 0.4;
    private double popularity = 0.2;
    private double distance = 0.2;
    private double priceMatch = 0.2;

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getPriceMatch() {
        return priceMatch;
    }

    public void setPriceMatch(double priceMatch) {
        this.priceMatch = priceMatch;
    }
}
