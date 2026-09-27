package br.edu.iftm.workouttracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import br.edu.iftm.workouttracker.MainActivity
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.databinding.NavTopBinding
import br.edu.iftm.workouttracker.ui.corrida.CorridaActivity
import br.edu.iftm.workouttracker.ui.exercicio.ExercicioListActivity

enum class Aba { TREINOS, EXERCICIOS, CORRIDA }

object NavegacaoAbas {

    fun configurar(activity: AppCompatActivity, nav: NavTopBinding, atual: Aba) {
        val botoes = mapOf(
            Aba.TREINOS to nav.btnNavTreinos,
            Aba.EXERCICIOS to nav.btnNavExercicios,
            Aba.CORRIDA to nav.btnNavCorrida
        )

        botoes.forEach { (aba, botao) ->
            if (aba == atual) {
                botao.setBackgroundResource(R.drawable.bg_pill_selected)
                botao.setTextColor(activity.getColor(R.color.on_fill))
            }
            botao.setOnClickListener { abrir(activity, atual, aba) }
        }
    }

    private fun abrir(activity: AppCompatActivity, atual: Aba, destino: Aba) {
        if (atual == destino) return

        val intent = when (destino) {
            Aba.TREINOS -> Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            Aba.EXERCICIOS -> Intent(activity, ExercicioListActivity::class.java)
            Aba.CORRIDA -> Intent(activity, CorridaActivity::class.java)
        }
        activity.startActivity(intent)

        if (atual != Aba.TREINOS) {
            activity.finish()
        }
    }
}
