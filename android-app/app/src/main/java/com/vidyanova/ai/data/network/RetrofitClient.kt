package com.vidyanova.ai.data.network

import com.vidyanova.ai.utils.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    private val httpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()

                val builder = request.newBuilder().apply {
                    AuthTokenStore.getToken()?.let { token ->
                        header("Authorization", "Bearer $token")
                    }
                }

                // Gzip compress push sync request bodies
                if (request.url.encodedPath.contains("/sync/push") && request.body != null) {
                    val originalBody = request.body!!
                    val buffer = Buffer()
                    originalBody.writeTo(buffer)

                    val gzipBuffer = Buffer()
                    val gzipSink = GzipSink(gzipBuffer).buffer()
                    gzipSink.writeAll(buffer)
                    gzipSink.close()

                    val compressedBody = object : RequestBody() {
                        override fun contentType() = originalBody.contentType()
                        override fun contentLength() = gzipBuffer.size
                        override fun writeTo(sink: BufferedSink) {
                            sink.write(gzipBuffer.snapshot())
                        }
                    }

                    builder.header("Content-Encoding", "gzip")
                    builder.post(compressedBody)
                }

                chain.proceed(builder.build())
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val apiService: ApiService =
        retrofit.create(ApiService::class.java)
}
