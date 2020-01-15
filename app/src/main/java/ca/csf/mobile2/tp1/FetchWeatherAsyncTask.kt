package ca.csf.mobile2.tp1

import android.os.AsyncTask
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class FetchWeatherAsyncTask(private val fromCity: String, val onSuccess : (Weather) -> Unit, val onError : (NetworkError) -> Unit) :
    AsyncTask<Unit, Unit, Promise<Weather?, NetworkError?>>()
{

    override fun doInBackground(vararg params: Unit?): Promise<Weather?, NetworkError?> {

        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(WEB_SERVICE_URL + fromCity)
            .build()

        var response: Response? = null
        try {
            response = httpClient.newCall(request).execute()
            return when {
                response!!.code() == NOT_FOUND_ERROR_CODE -> Promise.err(NetworkError.NOT_FOUND)
                response!!.code() == FORBIDDEN_ERROR_CODE -> Promise.err(NetworkError.FORBIDDEN)
                response!!.code() == UNAUTHORIZED_ERROR_CODE -> Promise.err(NetworkError.UNAUTHORIZED)
                response.isSuccessful -> {
                    val responseBody = response.body()!!.string()
                    val mapper = jacksonObjectMapper()
                    val weather: Weather = mapper.readValue(responseBody)
                    Promise.ok(weather)
                }
                else -> Promise.err(NetworkError.SERVER)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return Promise.err(NetworkError.CONNECTIVITY)
        } finally {
            response?.close()
        }
    }

    override fun onPostExecute(promise: Promise<Weather?, NetworkError?>) {
        when {
            promise.isSuccessful -> onSuccess(promise.result!!)
            promise.error === NetworkError.NOT_FOUND -> onError(promise.error)
            promise.error === NetworkError.SERVER -> onError(promise.error)
            promise.error === NetworkError.CONNECTIVITY -> onError(promise.error)
            else -> throw RuntimeException("Unhandled error named " + promise.error!!.name)
        }
    }
}

private const val WEB_SERVICE_URL = "http://10.200.71.131:8080/api/v1/weather/"

private const val UNAUTHORIZED_ERROR_CODE = 401
private const val FORBIDDEN_ERROR_CODE = 403
private const val NOT_FOUND_ERROR_CODE = 404