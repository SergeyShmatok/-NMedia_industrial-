package ru.netology.nmedia.di
//
//import android.annotation.SuppressLint
//import android.content.Context
//import androidx.room.Room
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.create
//import ru.netology.nmedia.BuildConfig
//import ru.netology.nmedia.api.ApiService
//import ru.netology.nmedia.auth.AppAuth
//import ru.netology.nmedia.db.AppDb
//import ru.netology.nmedia.repository.PostRepository
//import ru.netology.nmedia.repository.PostRepositoryFun
//
//
//// Тут указываются все используемые зависимости
//
//class DependencyContainer(
//    private val _context: Context,
//) {
//
//
//    companion object {
//
//        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
//
//        @SuppressLint("StaticFieldLeak")
//        @Volatile
//        private var instance: DependencyContainer? = null // (*) - больше не нужен?
//
//        fun initApp(context: Context) {
//            instance = DependencyContainer(context) // (*) - больше не нужен?
//        }
//
//        fun getInstance(): DependencyContainer { // (*) - больше не нужен?
//            return instance!!
//            }
//
//        }
//
//        val applicationContext = _context // (1) ?
//
//        val appAuth = AppAuth(this.applicationContext) // (2) ?
//
//        private val logger = HttpLoggingInterceptor().apply {
//            if (BuildConfig.DEBUG)
//                level = HttpLoggingInterceptor.Level.BODY
//        }
//
//
//        private val okhttp = OkHttpClient.Builder()
//            .addInterceptor(logger) // Последовательность logger'ов имеет значение (!)
//            .addInterceptor { chain -> // Модификация сетевых запросов через интерцептор
//                chain.proceed(
//                    appAuth.authState.value?.token?.let {
//                        chain.request().newBuilder().addHeader("Authorization", it)
//                            .build()
//                    } ?: chain.request()) // Если нет токена, тогда обычный "запрос".
//            } // addHeader добавляет заголовок с именем и значением. Предпочитайте этот метод для многозначных заголовков, таких как «Cookie».
//            .build()
//
//
//        private val retrofit = Retrofit.Builder()
//            .client(okhttp)
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        private val appDb = Room.databaseBuilder(this.applicationContext, AppDb::class.java, "app.db")
//            .fallbackToDestructiveMigration(true) // для миграции (**)
//            // .allowMainThreadQueries() - чтобы можно было работать с Room
//            //  на главном потоке (больше не нужно).
//            .build()
//
//
//        private val postDao = appDb.postDao()
//
//        val apiService = retrofit.create<ApiService>()
//
//        val repository: PostRepositoryFun = PostRepository(
//            postDao,
//            apiService,
//        )
//
//    }
