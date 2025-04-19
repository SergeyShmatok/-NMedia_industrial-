package ru.netology.nmedia.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


// Тут инициализация AppAuth с созданием собственной реализации Application.

@HiltAndroidApp
class App: Application()

// "Вы можете предоставить собственную реализацию, создав подкласс
// и указав полное имя этого подкласса в качестве "android:name"атрибута
// в теге AndroidManifest.xml <application>. Класс Application или
// ваш подкласс класса Application создается до любого другого класса
// при создании процесса для вашего приложения/пакета."
