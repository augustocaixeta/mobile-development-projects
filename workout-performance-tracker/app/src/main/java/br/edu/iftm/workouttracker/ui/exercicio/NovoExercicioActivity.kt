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
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.WorkoutApp
import br.edu.iftm.workouttracker.databinding.ActivityNovoExercicioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NovoExercicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovoExercicioBinding
    private var exercicioId: Long = 0
    private var camposPreenchidos = false

    private val viewModel: NovoExercicioViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return NovoExercicioViewModel(app.workoutRepository, exercicioId) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercicioId = intent.getLongExtra(EXTRA_EXERCICIO_ID, 0)
        val modoEdicao = exercicioId != 0L
        camposPreenchidos = savedInstanceState != null

        enableEdgeToEdge()
        binding = ActivityNovoExercicioBinding.inflate(layoutInflater)
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

        if (modoEdicao) {
            binding.tvTitulo.setText(R.string.title_editar_exercicio)
            binding.btnExcluir.visibility = View.VISIBLE
        }

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnExcluir.setOnClickListener { confirmarExclusao() }

        binding.btnSalvar.setOnClickListener {
            val nome = binding.etNome.text?.toString()?.trim().orEmpty()
            val grupo = binding.etGrupoMuscular.text?.toString()?.trim().orEmpty()
            if (nome.isEmpty()) {
                binding.etNome.error = getString(R.string.error_campo_obrigatorio)
                return@setOnClickListener
            }
            if (grupo.isEmpty()) {
                binding.etGrupoMuscular.error = getString(R.string.error_campo_obrigatorio)
                return@setOnClickListener
            }
            if (modoEdicao && viewModel.exercicioOriginal.value == null) {
                return@setOnClickListener
            }
            viewModel.salvar(nome, grupo) { finish() }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exercicioOriginal.collect { exercicio ->
                    if (exercicio == null || camposPreenchidos) return@collect
                    binding.etNome.setText(exercicio.nome)
                    binding.etGrupoMuscular.setText(exercicio.grupoMuscular)
                    camposPreenchidos = true
                }
            }
        }
    }

    private fun confirmarExclusao() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_excluir_exercicio_titulo)
            .setMessage(R.string.dialog_excluir_exercicio_mensagem)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_excluir) { _, _ ->
                viewModel.excluir {
                    val intent = Intent(this, ExercicioListActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_EXERCICIO_ID = "extra_exercicio_id"
    }
}
