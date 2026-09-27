package br.edu.iftm.workouttracker

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
import br.edu.iftm.workouttracker.databinding.ActivityMainBinding
import br.edu.iftm.workouttracker.ui.Aba
import br.edu.iftm.workouttracker.ui.NavegacaoAbas
import br.edu.iftm.workouttracker.ui.rotina.NovaRotinaActivity
import br.edu.iftm.workouttracker.ui.rotina.RotinaAdapter
import br.edu.iftm.workouttracker.ui.rotina.RotinaDetailActivity
import br.edu.iftm.workouttracker.ui.rotina.RotinaListViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: RotinaListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as WorkoutApp
                return RotinaListViewModel(app.workoutRepository) as T
            }
        }
    }

    private val adapter = RotinaAdapter { rotina ->
        val intent = Intent(this, RotinaDetailActivity::class.java)
        intent.putExtra(RotinaDetailActivity.EXTRA_ROTINA_ID, rotina.rotina.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        binding.rvRotinas.layoutManager = LinearLayoutManager(this)
        binding.rvRotinas.adapter = adapter

        NavegacaoAbas.configurar(this, binding.navTop, Aba.TREINOS)

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NovaRotinaActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rotinas.collect { lista ->
                    adapter.submitList(lista)
                    binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
