package com.lweiss01.paydirt.di

import android.content.Context
import androidx.room.Room
import com.lweiss01.paydirt.data.local.PayDirtDatabase
import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.LinkedAccountDao
import com.lweiss01.paydirt.data.local.dao.PaymentDao
import com.lweiss01.paydirt.data.remote.PlaidApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PayDirtDatabase =
        Room.databaseBuilder(
            context,
            PayDirtDatabase::class.java,
            PayDirtDatabase.DATABASE_NAME,
        )
            .addMigrations(PayDirtDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideCardDao(db: PayDirtDatabase): CardDao = db.cardDao()

    @Provides
    fun providePaymentDao(db: PayDirtDatabase): PaymentDao = db.paymentDao()

    @Provides
    fun provideLinkedAccountDao(db: PayDirtDatabase): LinkedAccountDao = db.linkedAccountDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL_DEBUG = "https://paydirt-api.newsthread.workers.dev/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-PayDirt-Version", "1.0.0")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_DEBUG)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePlaidApiService(retrofit: Retrofit): PlaidApiService =
        retrofit.create(PlaidApiService::class.java)
}
