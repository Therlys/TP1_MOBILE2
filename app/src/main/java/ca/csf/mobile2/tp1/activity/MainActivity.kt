package ca.csf.mobile2.tp1.activity

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
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import ca.csf.mobile2.tp1.*
import ca.csf.mobile2.tp1.model.*
import ca.csf.mobile2.tp1.view.DisplayState


class MainActivity : AppCompatActivity() {

    private lateinit var networkErrorGroup: Group
    private lateinit var weatherGroup: Group
    private lateinit var searchEditText: EditText
    private lateinit var loadingView: View
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherTypeImageView: ImageView
    private lateinit var cityTextView: TextView
    private lateinit var errorTextView: TextView
    private lateinit var retryButton: Button

    private var showedDisplayState: DisplayState = DisplayState.HOME
    private var showedWeatherType: WeatherType = WeatherType.SUNNY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkErrorGroup = findViewById(R.id.network_error_group)
        weatherGroup = findViewById(R.id.weather_group)
        searchEditText = findViewById(R.id.search_edit_text)
        loadingView = findViewById(R.id.loading_view)
        temperatureTextView = findViewById(R.id.temperature_text_view)
        cityTextView = findViewById(R.id.city_text_view)
        errorTextView = findViewById(R.id.error_text_view)
        retryButton = findViewById(R.id.retry_button)
        weatherTypeImageView = findViewById(R.id.weather_type_image_view)

        retryButton.setOnClickListener { searchWeather() }
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                if(searchEditText.text.toString() == "") {
                    true
                }else{
                    searchWeather()
                    hideKeyboardFrom(searchEditText)
                    false
                }
            }
            else{
                hideKeyboardFrom(searchEditText)
                false
            }
        }

        hideShowedStates()
    }

    private fun Context.hideKeyboardFrom(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SHOWED_TEMPERATURE_IN_CELSIUS_TAG, temperatureTextView.text.toString())
        outState.putString(SHOWED_CITY_TAG, cityTextView.text.toString())
        outState.putSerializable(SHOWED_WEATHER_TYPE_TAG, showedWeatherType)
        outState.putSerializable(SHOWED_STATE_TAG, showedDisplayState)
        outState.putString(SHOWED_NETWORK_ERROR_TAG, errorTextView.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        temperatureTextView.text = savedInstanceState.getString(SHOWED_TEMPERATURE_IN_CELSIUS_TAG)
        cityTextView.text = savedInstanceState.getString(SHOWED_CITY_TAG)
        errorTextView.text = savedInstanceState.getString(SHOWED_NETWORK_ERROR_TAG)
        setWeatherType(savedInstanceState.getSerializable(SHOWED_WEATHER_TYPE_TAG) as WeatherType)
        val displayState: DisplayState = savedInstanceState.getSerializable(SHOWED_STATE_TAG) as DisplayState
        if(displayState == DisplayState.LOADING) searchWeather()
        setDisplayState(displayState)
    }

    private fun searchWeather(){
        setDisplayState(DisplayState.LOADING)
        FetchWeatherAsyncTask(
            searchEditText.text.toString(),
            this::onWeatherFetched,
            this::onNetworkError
        )
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
        setDisplayState(DisplayState.ERROR)
    }


    private fun setDisplayState(displayState: DisplayState) {
        hideShowedStates()
        when (displayState){
            DisplayState.HOME -> return
            DisplayState.LOADING -> loadingView.visibility = View.VISIBLE
            DisplayState.WEATHER -> weatherGroup.visibility = Group.VISIBLE
            DisplayState.ERROR -> networkErrorGroup.visibility = Group.VISIBLE
        }
        showedDisplayState = displayState
    }

    private fun hideShowedStates(){
        loadingView.visibility = View.GONE
        weatherGroup.visibility = View.GONE
        networkErrorGroup.visibility = View.GONE
    }

    private fun setWeather(weather: Weather){
        setWeatherCity(weather.city)
        setWeatherTemperatureInCelsius(weather.temperatureInCelsius)
        setWeatherType(weather.type)
        setDisplayState(DisplayState.WEATHER)
    }

    private fun setWeatherTemperatureInCelsius(temperatureInCelsius: Int){
        temperatureTextView.text = (temperatureInCelsius.toString() + DEGREE_SYMBOL)
    }

    private fun setWeatherCity(city: String){
        cityTextView.text = city
    }

    private fun setWeatherType(type: WeatherType){
        when (type) {
            WeatherType.CLOUDY -> weatherTypeImageView.background = getDrawable(R.drawable.ic_cloudy)
            WeatherType.PARTLY_SUNNY -> weatherTypeImageView.background = getDrawable(R.drawable.ic_partly_sunny)
            WeatherType.RAIN -> weatherTypeImageView.background = getDrawable(R.drawable.ic_rain)
            WeatherType.SNOW -> weatherTypeImageView.background = getDrawable(R.drawable.ic_snow)
            WeatherType.SUNNY -> weatherTypeImageView.background = getDrawable(R.drawable.ic_sunny)
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