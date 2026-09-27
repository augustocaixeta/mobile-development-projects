package br.edu.iftm.workouttracker.ui.rotina

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
import br.edu.iftm.workouttracker.databinding.ActivityRotinaDetailBinding
import br.edu.iftm.workouttracker.util.Formatadores
import kotlinx.coroutines.launch

class RotinaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRotinaDetailBinding
    private var rotinaId: Long = 0

    private val viewModel: RotinaDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return RotinaDetailViewModel(app.workoutRepository, rotinaId) as T
            }
        }
    }

    private val adapter = ExercicioResumoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rotinaId = intent.getLongExtra(EXTRA_ROTINA_ID, 0)

        enableEdgeToEdge()
        binding = ActivityRotinaDetailBinding.inflate(layoutInflater)
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
        binding.rvExercicios.layoutManager = LinearLayoutManager(this)
        binding.rvExercicios.adapter = adapter

        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, NovaRotinaActivity::class.java)
            intent.putExtra(NovaRotinaActivity.EXTRA_ROTINA_ID, rotinaId)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rotina.collect { dados ->
                    if (dados == null) return@collect
                    val rotina = dados.rotina
                    val exercicios = dados.exercicios.sortedBy { it.registro.id }

                    binding.tvGrupoMuscular.text =
                        getString(R.string.label_treino_grupo_format, rotina.grupoMuscular)
                    binding.tvNome.text = rotina.nome
                    binding.tvData.text = Formatadores.data(rotina.realizadoEm)

                    val volume = exercicios.sumOf {
                        it.registro.cargaKg * it.registro.series * it.registro.repeticoes
                    }
                    binding.tvVolume.text = getString(R.string.label_kg_format, Formatadores.numero(volume))

                    adapter.submitList(exercicios)
                    binding.tvVazio.visibility = if (exercicios.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    companion object {
        const val EXTRA_ROTINA_ID = "extra_rotina_id"
    }
}
