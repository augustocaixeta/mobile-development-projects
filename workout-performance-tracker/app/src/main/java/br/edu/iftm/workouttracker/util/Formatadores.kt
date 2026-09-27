package br.edu.iftm.workouttracker.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

object Formatadores {

    private val formatoData = SimpleDateFormat("dd/MM, HH:mm", Locale("pt", "BR"))

    fun data(timestampMillis: Long): String = formatoData.format(timestampMillis)

    fun numero(valor: Double): String {
        val arredondado = BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        return arredondado.toPlainString()
    }

    fun tempo(segundosTotais: Long): String {
        val minutos = segundosTotais / 60
        val segundos = segundosTotais % 60
        return String.format(Locale("pt", "BR"), "%d:%02d", minutos, segundos)
    }

    fun pace(distanciaKm: Double, tempoSegundos: Long): String {
        if (distanciaKm <= 0.0) return "0:00"
        val segundosPorKm = (tempoSegundos / distanciaKm).toLong()
        return tempo(segundosPorKm)
    }
}
