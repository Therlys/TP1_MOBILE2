package ca.csf.mobile2.tp1.model

data class Weather(var city : String, var temperatureInCelsius : Int, var type : WeatherType)

enum class WeatherType{
    SUNNY,
    PARTLY_SUNNY,
    CLOUDY,
    RAIN,
    SNOW
}