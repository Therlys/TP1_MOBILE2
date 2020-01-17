package ca.csf.mobile2.tp1.model

import android.os.AsyncTask
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class FetchWeatherAsyncTask(private val weatherCity: String, val onSuccess : (Weather) -> Unit, val onError : (NetworkError) -> Unit) :
    AsyncTask<Unit, Unit, Promise<Weather?, NetworkError?>>()
{

    override fun doInBackground(vararg params: Unit?): Promise<Weather?, NetworkError?> {

        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(WEB_SERVICE_URL + weatherCity)
            .build()

        var response: Response? = null
        try {
            response = httpClient.newCall(request).execute()
            return when {
                response.code() == NOT_FOUND_ERROR_CODE -> Promise.err(NetworkError.NOT_FOUND)
                response.code() == FORBIDDEN_ERROR_CODE -> Promise.err(NetworkError.FORBIDDEN)
                response.code() == UNAUTHORIZED_ERROR_CODE -> Promise.err(NetworkError.UNAUTHORIZED)
                response.isSuccessful -> {
                    val responseBody = response.body()!!.string()
                    val mapper = jacksonObjectMapper()
                    val weather: Weather = mapper.readValue(responseBody)
                    Promise.ok(weather)
                }
                else -> Promise.err(NetworkError.SERVER)
            }
        }
        catch (e: SocketTimeoutException) {
            return Promise.err(NetworkError.SERVER)
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
            promise.error == null -> throw RuntimeException(UNHANDLED_ERROR_MESSAGE)
            else -> onError(promise.error)
        }
    }
}

private const val WEB_SERVICE_URL = "http://10.200.86.157:8080/api/v1/weather/"
private const val UNHANDLED_ERROR_MESSAGE = "Unhandled error!"

private const val UNAUTHORIZED_ERROR_CODE = 401
private const val FORBIDDEN_ERROR_CODE = 403
private const val NOT_FOUND_ERROR_CODE = 404