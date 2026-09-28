package br.edu.iftm.readingmanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.edu.iftm.readingmanager.ui.detail.DetailScreen
import br.edu.iftm.readingmanager.ui.form.FormScreen
import br.edu.iftm.readingmanager.ui.home.HomeScreen
import br.edu.iftm.readingmanager.ui.performance.PerformanceScreen
import br.edu.iftm.readingmanager.ui.session.SessionScreen
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Grafo de navegação com as cinco telas do Figma: Início, Detalhe, Novo registro,
 * Sessão ativa e Desempenho.
 *
 * @param navController controlador que guarda a pilha de telas.
 */
@Composable
fun ReadingNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
    ) {
        composable<Home> { entry ->
            HomeScreen(
                onOpenBook = { id -> navController.navigateFrom(entry, BookDetail(id)) },
                onAdd = { navController.navigateFrom(entry, BookForm()) },
                onOpenPerformance = { navController.navigateFrom(entry, Performance) }
            )
        }
        composable<BookDetail> { entry ->
            val route = entry.toRoute<BookDetail>()
            DetailScreen(
                onBack = { navController.popFrom(entry) },
                onEdit = { navController.navigateFrom(entry, BookForm(route.bookId)) },
                onContinue = { navController.navigateFrom(entry, ReadingSession(route.bookId)) }
            )
        }
        composable<BookForm> { entry ->
            FormScreen(onClose = { navController.popFrom(entry) })
        }
        composable<ReadingSession> { entry ->
            SessionScreen(onClose = { navController.popFrom(entry) })
        }
        composable<Performance> { entry ->
            PerformanceScreen(
                onBack = { navController.popFrom(entry) },
                onOpenBook = { bookId -> navController.navigateFrom(entry, BookDetail(bookId)) }
            )
        }
    }
}

/**
 * Abre uma rota só se a tela de origem ainda estiver ativa, evitando telas duplicadas com toques repetidos.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu a navegação.
 * @param route destino.
 */
private fun <T : Any> NavHostController.navigateFrom(entry: NavBackStackEntry, route: T) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

/**
 * Volta uma tela só se a tela de origem ainda estiver ativa, para não fechar o Início por engano.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu para voltar.
 */
private fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}
