package br.edu.iftm.workouttracker.ui.exercicio

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.WorkoutApp
import br.edu.iftm.workouttracker.databinding.ActivityExercicioDetailBinding
import br.edu.iftm.workouttracker.util.Formatadores
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class ExercicioDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExercicioDetailBinding
    private var exercicioId: Long = 0

    private val viewModel: ExercicioDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return ExercicioDetailViewModel(app.workoutRepository, exercicioId) as T
            }
        }
    }

    private val adapter = RegistroCargaAdapter()
    private val formatoDiaMes = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercicioId = intent.getLongExtra(EXTRA_EXERCICIO_ID, 0)

        enableEdgeToEdge()
        binding = ActivityExercicioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenPadding = resources.getDimensionPixelSize(R.dimen.screen_padding)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + screenPadding,
                systemBars.top + screenPadding,
                systemBars.right + screenPadding,
                systemBars.bottom + screenPadding
            )
            insets
        }

        binding.btnVoltar.setOnClickListener { finish() }
        binding.rvHistorico.layoutManager = LinearLayoutManager(this)
        binding.rvHistorico.adapter = adapter

        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, NovoExercicioActivity::class.java)
            intent.putExtra(NovoExercicioActivity.EXTRA_EXERCICIO_ID, exercicioId)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.evolucao.collect { evolucao ->
                    if (evolucao == null) return@collect
                    exibir(evolucao)
                }
            }
        }
    }

    private fun exibir(evolucao: EvolucaoExercicio) {
        binding.tvTitulo.text = evolucao.exercicio.nome
        binding.tvGrupo.text = evolucao.exercicio.grupoMuscular

        val vazio = evolucao.cronologico.isEmpty()
        val visibilidadeDados = if (vazio) View.GONE else View.VISIBLE
        binding.grupoEstatisticas.visibility = visibilidadeDados
        binding.cardGrafico.visibility = visibilidadeDados
        binding.tvSecaoHistorico.visibility = visibilidadeDados
        binding.rvHistorico.visibility = visibilidadeDados
        binding.tvVazio.visibility = if (vazio) View.VISIBLE else View.GONE
        if (vazio) {
            adapter.submitList(emptyList())
            return
        }

        binding.tvUltimaCarga.text = kg(evolucao.ultimaCarga ?: 0.0)
        binding.tvMaiorCarga.text = kg(evolucao.maiorCarga ?: 0.0)

        val variacao = evolucao.variacao ?: 0.0
        val sinal = if (variacao > 0) "+" else ""
        binding.tvEvolucao.text = getString(R.string.label_kg_format, sinal + Formatadores.numero(variacao))
        binding.tvEvolucao.setTextColor(
            getColor(
                when {
                    variacao > 0 -> R.color.positive
                    variacao < 0 -> R.color.accent
                    else -> R.color.text_primary
                }
            )
        )

        binding.grafico.definirPontos(
            evolucao.cronologico.map {
                PontoGrafico(formatoDiaMes.format(it.registro.dataHora), it.registro.cargaKg)
            }
        )
        binding.grafico.contentDescription = getString(
            R.string.cd_grafico_carga_format,
            evolucao.cronologico.size,
            Formatadores.numero(evolucao.ultimaCarga ?: 0.0)
        )

        adapter.submitList(evolucao.historicoRecente)
    }

    private fun kg(valor: Double): String =
        getString(R.string.label_kg_format, Formatadores.numero(valor))

    companion object {
        const val EXTRA_EXERCICIO_ID = "extra_exercicio_id"
    }
}
