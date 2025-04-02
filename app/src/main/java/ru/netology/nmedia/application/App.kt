package ru.netology.nmedia.application

import android.app.Application
import ru.netology.nmedia.auth.AppAuth


// Тут инициализация AppAuth с созданием собственной реализации Application.

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp(this)

    }
}


// "Вы можете предоставить собственную реализацию, создав подкласс
// и указав полное имя этого подкласса в качестве "android:name"атрибута
// в теге AndroidManifest.xml <application>. Класс Application или
// ваш подкласс класса Application создается до любого другого класса
// при создании процесса для вашего приложения/пакета."
