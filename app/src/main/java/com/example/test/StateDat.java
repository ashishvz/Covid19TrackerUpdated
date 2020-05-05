package com.example.test;

public class StateDat {
    private String countryName;
    private int active,recovered,deaths,confirmed;

    public StateDat(String countryName, int active, int confirmed,int deaths, int recovered){
        this.countryName=countryName;
        this.active=active;
        this.confirmed=confirmed;
        this.deaths=deaths;
        this.recovered=recovered;
    }

    public String getCountryName() {
        return countryName;
    }

    public int getRecovered() {
        return recovered;
    }

    public int getActive() {
        return active;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public int getDeaths() {
        return deaths;
    }
}
