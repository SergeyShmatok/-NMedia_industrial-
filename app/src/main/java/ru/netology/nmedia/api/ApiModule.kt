package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
    }


    @Singleton
    @Provides
    fun provideLogger() = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG)
            level = HttpLoggingInterceptor.Level.BODY
    }


    @Singleton
    @Provides
    fun provideOkHttp(
        logger: HttpLoggingInterceptor,
        appAuth: AppAuth, // Эту зависимость в рамках Hilt тут никто не предоставляет, можно научить
        // его создавать объект этого класса (переход в класс AppAuth).
    ) = OkHttpClient.Builder()
        .addInterceptor(logger) // Последовательность logger'ов имеет значение (!)
        .addInterceptor { chain -> // Модификация сетевых запросов через интерцептор
            chain.proceed(
                appAuth.authState.value?.token?.let {
                    chain.request().newBuilder().addHeader("Authorization", it)
                        .build()
                } ?: chain.request()) // Если нет токена, тогда обычный "запрос".
        } // addHeader добавляет заголовок с именем и значением. Предпочитайте этот метод для
        // многозначных заголовков, таких как «Cookie».
        .build()





    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit) = retrofit.create<ApiService>()

    // Все объекты тут будут в одном экземпляре @Singleton

}