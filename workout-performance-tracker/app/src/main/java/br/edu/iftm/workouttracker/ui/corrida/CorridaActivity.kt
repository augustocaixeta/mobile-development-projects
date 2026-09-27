package br.edu.iftm.workouttracker.ui.corrida

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
import br.edu.iftm.workouttracker.databinding.ActivityCorridaBinding
import br.edu.iftm.workouttracker.ui.Aba
import br.edu.iftm.workouttracker.ui.NavegacaoAbas
import kotlinx.coroutines.launch

class CorridaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCorridaBinding

    private val viewModel: CorridaViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return CorridaViewModel(app.corridaRepository) as T
            }
        }
    }

    private val adapter = CorridaAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCorridaBinding.inflate(layoutInflater)
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

        binding.rvCorridas.layoutManager = LinearLayoutManager(this)
        binding.rvCorridas.adapter = adapter

        NavegacaoAbas.configurar(this, binding.navTop, Aba.CORRIDA)

        binding.btnRegistrar.setOnClickListener {
            val distancia = binding.etDistancia.text?.toString()?.trim().orEmpty()
                .replace(",", ".").toDoubleOrNull()
            val minutos = binding.etMinutos.text?.toString()?.trim().orEmpty().toLongOrNull()
            val segundos = binding.etSegundos.text?.toString()?.trim().orEmpty().toLongOrNull()

            if (distancia == null) {
                binding.etDistancia.error = getString(R.string.error_valor_invalido)
                return@setOnClickListener
            }
            if (minutos == null) {
                binding.etMinutos.error = getString(R.string.error_valor_invalido)
                return@setOnClickListener
            }
            if (segundos == null) {
                binding.etSegundos.error = getString(R.string.error_valor_invalido)
                return@setOnClickListener
            }

            val tempoTotal = minutos * 60 + segundos
            viewModel.registrar(distancia, tempoTotal)
            binding.etDistancia.text?.clear()
            binding.etMinutos.text?.clear()
            binding.etSegundos.text?.clear()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.corridas.collect { lista ->
                    adapter.submitList(lista)
                    binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
