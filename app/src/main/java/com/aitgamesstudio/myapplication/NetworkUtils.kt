package com.aitgamesstudio.myapplication

import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


object NetworkUtils {
    private val LOG_TAG = NetworkUtils::class.java.simpleName


    // Base endpoint URL
    private const val BASE_URL =
        "https://efs5i1ube5.execute-api.eu-central-1.amazonaws.com/prod"


    /**
     * method to make the actual query.
     *
     * @return the JSON response string from the query.
     */
    fun getInfo(): String? {

        // Set up variables for the try block that need to be closed in the
        // finally block.
        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        var requestJSONString: String? = null
        try {

            val builtURI =
                Uri.parse(BASE_URL).buildUpon()
                    .build()

            // Convert the URI to a URL.
            val requestURL = URL(builtURI.toString())

            // Open the network connection.
            urlConnection = requestURL.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection!!.connect()

            // Get the InputStream.
            val inputStream = urlConnection.inputStream

            // Create a buffered reader from that input stream.
            reader = BufferedReader(InputStreamReader(inputStream))

            // Use a StringBuilder to hold the incoming response.
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                // Add the current line to the string.
                builder.append(line)

                // Since this is JSON, adding a newline isn't necessary (it won't
                // affect parsing) but it does make debugging a *lot* easier
                // if you print out the completed buffer for debugging.
                builder.append("\n")
            }
            if (builder.isEmpty()) {
                // Stream was empty.  Exit without parsing.
                return null
            }
            requestJSONString = builder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // Close the connection and the buffered reader.
            urlConnection?.disconnect()
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        // Write the final JSON response to the log
        Log.d(LOG_TAG, requestJSONString)
        return requestJSONString
    }
}