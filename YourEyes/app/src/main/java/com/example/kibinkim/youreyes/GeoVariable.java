package com.example.kibinkim.youreyes;

public class GeoVariable {
    public static double latitude; // static 클래스 변수 위도
    public static double longitude; // static 클래스 변수 경도
    public static String searchedGu; // static 클래스 변수 역지오해서 자른 구


    public static String getSearchedGu() {
        return searchedGu;
    }
    public static void setSearchedGu(String searchedGu) {
        GeoVariable.searchedGu = searchedGu;
    }
    public static double getLatitude() {
        return latitude;
    }
    public static void setLatitude(double latitude) {
        GeoVariable.latitude = latitude;
    }
    public static double getLongitude() {
        return longitude;
    }
    public static void setLongitude(double longitude) {
        GeoVariable.longitude = longitude;
    }
}
