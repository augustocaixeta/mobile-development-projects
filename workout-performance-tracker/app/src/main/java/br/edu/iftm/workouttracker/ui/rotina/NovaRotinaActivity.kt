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
import br.edu.iftm.workouttracker.MainActivity
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.WorkoutApp
import br.edu.iftm.workouttracker.databinding.ActivityNovaRotinaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NovaRotinaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovaRotinaBinding
    private var rotinaId: Long = 0
    private var camposPreenchidos = false

    private val viewModel: NovaRotinaViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return NovaRotinaViewModel(app.workoutRepository, rotinaId) as T
            }
        }
    }

    private lateinit var adapter: ExercicioSelecaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rotinaId = intent.getLongExtra(EXTRA_ROTINA_ID, 0)
        val modoEdicao = rotinaId != 0L
        camposPreenchidos = savedInstanceState != null

        enableEdgeToEdge()
        binding = ActivityNovaRotinaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenPadding = resources.getDimensionPixelSize(R.dimen.screen_padding)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left + screenPadding,
                systemBars.top + screenPadding,
                systemBars.right + screenPadding,
                maxOf(systemBars.bottom, teclado.bottom) + screenPadding
            )
            insets
        }

        if (modoEdicao) {
            binding.tvTitulo.setText(R.string.title_editar_treino)
            binding.btnSalvar.setText(R.string.action_salvar_alteracoes)
            binding.btnExcluir.visibility = View.VISIBLE
        } else {
            binding.btnSalvar.setText(R.string.action_concluir_treino)
        }

        adapter = ExercicioSelecaoAdapter(
            aoAlternar = { exercicio -> viewModel.alternar(exercicio.id) },
            valoresDe = { id -> viewModel.valoresDe(id) }
        )
        binding.rvExercicios.layoutManager = LinearLayoutManager(this)
        binding.rvExercicios.adapter = adapter

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
            if (modoEdicao && viewModel.rotinaOriginal.value == null) {
                return@setOnClickListener
            }

            when (val resultado = viewModel.salvar(nome, grupo) { finish() }) {
                ResultadoValidacao.Valido -> Unit
                ResultadoValidacao.SemExercicios -> avisar(getString(R.string.error_sem_exercicios))
                is ResultadoValidacao.ValoresInvalidos -> {
                    binding.rvExercicios.smoothScrollToPosition(resultado.posicao)
                    avisar(getString(R.string.error_valores_exercicio_format, resultado.nomeExercicio))
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.itens.collect { lista ->
                        if (lista == null) return@collect
                        adapter.submitList(lista)
                        binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                        binding.tvDicaSelecao.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.rotinaOriginal.collect { rotina ->
                        if (rotina == null || camposPreenchidos) return@collect
                        binding.etNome.setText(rotina.nome)
                        binding.etGrupoMuscular.setText(rotina.grupoMuscular)
                        camposPreenchidos = true
                    }
                }
            }
        }
    }

    private fun avisar(mensagem: String) {
        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_LONG).show()
    }

    private fun confirmarExclusao() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_excluir_treino_titulo)
            .setMessage(R.string.dialog_excluir_treino_mensagem)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_excluir) { _, _ ->
                viewModel.excluir {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_ROTINA_ID = "extra_rotina_id"
    }
}
