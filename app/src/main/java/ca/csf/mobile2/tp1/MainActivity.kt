package ca.csf.mobile2.tp1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import java.lang.StringBuilder
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.inputmethod.EditorInfo


class MainActivity : AppCompatActivity() {

    private lateinit var networkErrorGroup: Group
    private lateinit var weatherGroup: Group
    private lateinit var searchEditText: EditText
    private lateinit var loadingView: View
    private lateinit var temperatureTextView: TextView
    private lateinit var typeImageView: ImageView
    private lateinit var cityTextView: TextView
    private lateinit var errorTextView: TextView
    private lateinit var retryButton: Button

    private var showedState: State? = null
    private var showedWeatherType: WeatherType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkErrorGroup = findViewById(R.id.errors_group)
        weatherGroup = findViewById(R.id.weather_group)
        searchEditText = findViewById(R.id.search_word_edit_text)
        loadingView = findViewById(R.id.loading_view)
        temperatureTextView = findViewById(R.id.temperature_text_view)
        cityTextView = findViewById(R.id.city_text_view)
        errorTextView = findViewById(R.id.error_text_view)
        retryButton = findViewById(R.id.error_retry_button)
        typeImageView = findViewById(R.id.meteo_image_text_view)

        setState(State.HOME)

        retryButton.setOnClickListener { searchWeather() }
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                if(searchEditText.text.toString() == "") {
                    true
                }else{
                    searchWeather()
                    false
                }
            }
            else{
                false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putString(SHOWED_TEMPERATURE_IN_CELSIUS_TAG, temperatureTextView.text.toString())
        outState!!.putString(SHOWED_CITY_TAG, cityTextView.text.toString())
        if(showedWeatherType != null) outState!!.putString(SHOWED_WEATHER_TYPE_TAG, showedWeatherType.toString())
        if(showedState != null) outState!!.putString(SHOWED_STATE_TAG, showedState.toString())
        outState!!.putString(SHOWED_NETWORK_ERROR_TAG, errorTextView.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            temperatureTextView.text = savedInstanceState.getString(SHOWED_TEMPERATURE_IN_CELSIUS_TAG)
            cityTextView.text = savedInstanceState.getString(SHOWED_CITY_TAG)
            errorTextView.text = savedInstanceState.getString(SHOWED_NETWORK_ERROR_TAG)

            val weatherTypeString: String? = savedInstanceState.getString(SHOWED_WEATHER_TYPE_TAG)
            if(weatherTypeString != null) {
                val weatherType: WeatherType? = WeatherType.valueOf(weatherTypeString)
                if (weatherType != null) setWeatherType(weatherType)
            }

            val stateString: String? = savedInstanceState.getString(SHOWED_WEATHER_TYPE_TAG)
            if(stateString != null){
                val state: State? = State.valueOf(stateString)
                if(state == State.LOADING) searchWeather()
                else if (state != null) setState(state)
            }
        }
    }

    private fun searchWeather(){
        setState(State.LOADING)
        FetchWeatherAsyncTask(searchEditText.text.toString(), this::onWeatherFetched, this::onNetworkError)
            .execute()
    }

    private fun onWeatherFetched(weather: Weather){
        setWeather(weather)
    }

    private fun onNetworkError(networkError: NetworkError){
        val errorMessage = StringBuilder(getString(R.string.error_message))
        when (networkError){
            NetworkError.UNAUTHORIZED -> errorMessage.append(getString(R.string.unauthorized_error_message))
            NetworkError.FORBIDDEN -> errorMessage.append(getString(R.string.forbidden_error_message))
            NetworkError.NOT_FOUND -> errorMessage.append(getString(R.string.not_found_error_message))
            NetworkError.SERVER -> errorMessage.append(getString(R.string.server_error_message))
            NetworkError.CONNECTIVITY -> errorMessage.append(getString(R.string.connectivity_error_message))
        }
        errorTextView.text = errorMessage
        setState(State.ERROR)
    }


    private fun setState(state: State) {
        loadingView.visibility = View.GONE
        weatherGroup.visibility = View.GONE
        networkErrorGroup.visibility = View.GONE
        when (state){
            State.HOME -> return
            State.LOADING -> loadingView.visibility = View.VISIBLE
            State.WEATHER -> weatherGroup.visibility = Group.VISIBLE
            State.ERROR -> networkErrorGroup.visibility = Group.VISIBLE
        }
        showedState = state
    }

    private fun setWeather(weather: Weather){
        setWeatherCity(weather.city)
        setWeatherTemperatureInCelsius(weather.temperatureInCelsius)
        setWeatherType(weather.type)
        setState(State.WEATHER)
    }

    private fun setWeatherTemperatureInCelsius(temperatureInCelsius: Int){
        temperatureTextView.text = temperatureInCelsius.toString() + DEGREE_SYMBOL
    }

    private fun setWeatherCity(city: String){
        cityTextView.text = city
    }

    private fun setWeatherType(type: WeatherType){
        when (type) {
            WeatherType.CLOUDY -> typeImageView.background = getDrawable(R.drawable.ic_cloudy)
            WeatherType.PARTLY_SUNNY -> typeImageView.background = getDrawable(R.drawable.ic_partly_sunny)
            WeatherType.RAIN -> typeImageView.background = getDrawable(R.drawable.ic_rain)
            WeatherType.SNOW -> typeImageView.background = getDrawable(R.drawable.ic_snow)
            WeatherType.SUNNY -> typeImageView.background = getDrawable(R.drawable.ic_sunny)
        }
        showedWeatherType = type
    }
}

private const val SHOWED_CITY_TAG = "SHOWED_CITY_TAG"
private const val SHOWED_TEMPERATURE_IN_CELSIUS_TAG = "SHOWED_TEMPERATURE_IN_CELSIUS_TAG"
private const val SHOWED_NETWORK_ERROR_TAG = "SHOWED_NETWORK_ERROR_TAG"
private const val SHOWED_WEATHER_TYPE_TAG = "SHOWED_WEATHER_TYPE_TAG"
private const val SHOWED_STATE_TAG = "SHOWED_STATE_TAG"

private const val DEGREE_SYMBOL = "°"