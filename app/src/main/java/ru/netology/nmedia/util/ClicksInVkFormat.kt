package ru.netology.nmedia.util

import java.math.RoundingMode


fun clicksInVkFormat(countClicks: Int): String {

    val solution = when (countClicks) {
        in 0..999 -> countClicks.toString()
        // количество лайков меньше 1000
        in 1000..9999 -> "${
            (countClicks.toDouble() / 1000).toBigDecimal().setScale(1, RoundingMode.DOWN)
        }К"
        // Если количество кликов перевалило за 999, должно отображаться 1K.
        // 1.1К отображается по достижении 1 100.
        in 10_000..999999 -> "${(countClicks / 1000)}К"
        // После 10К сотни перестают отображаться.
        else -> "${
            (countClicks.toDouble() / 1_000_000).toBigDecimal().setScale(1, RoundingMode.DOWN)
        }М"
        // После 1M сотни тысяч отображаются в формате 1.3M.
    }

    return solution.replace(".0", "")
}