package br.edu.iftm.workouttracker.ui.exercicio

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import br.edu.iftm.workouttracker.databinding.ActivityExercicioListBinding
import br.edu.iftm.workouttracker.ui.Aba
import br.edu.iftm.workouttracker.ui.NavegacaoAbas
import kotlinx.coroutines.launch

class ExercicioListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExercicioListBinding

    private val viewModel: ExercicioListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return ExercicioListViewModel(app.workoutRepository) as T
            }
        }
    }

    private val adapter = ExercicioAdapter { exercicio ->
        val intent = Intent(this, ExercicioDetailActivity::class.java)
        intent.putExtra(ExercicioDetailActivity.EXTRA_EXERCICIO_ID, exercicio.id)
        startActivity(intent)
    }

    private val adapterFiltros = FiltroChipAdapter { grupo -> viewModel.selecionarGrupo(grupo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExercicioListBinding.inflate(layoutInflater)
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

        binding.rvExercicios.layoutManager = LinearLayoutManager(this)
        binding.rvExercicios.adapter = adapter
        binding.rvFiltros.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFiltros.adapter = adapterFiltros

        NavegacaoAbas.configurar(this, binding.navTop, Aba.EXERCICIOS)

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NovoExercicioActivity::class.java))
        }

        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.atualizarBusca(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.exerciciosFiltrados.collect { lista ->
                        adapter.submitList(lista)
                        binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.grupos.collect { grupos ->
                        adapterFiltros.atualizar(grupos, viewModel.grupoSelecionado.value)
                    }
                }
                launch {
                    viewModel.grupoSelecionado.collect { grupo ->
                        adapterFiltros.atualizar(viewModel.grupos.value, grupo)
                    }
                }
            }
        }
    }
}
