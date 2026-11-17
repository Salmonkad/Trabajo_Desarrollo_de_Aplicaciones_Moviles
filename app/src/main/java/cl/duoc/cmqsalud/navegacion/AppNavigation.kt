package cl.duoc.cmqsalud.navegacion

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.cmqsalud.model.PerfilUsuario
import cl.duoc.cmqsalud.ui.home.HomeScreen
import cl.duoc.cmqsalud.ui.login.LoginScreen
import cl.duoc.cmqsalud.ui.registro.RegistroScreen
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel
import kotlinx.serialization.Serializable

@Serializable data object LoginRoute
@Serializable data object HomeRoute
@Serializable data object RegistroRoute

@Composable
fun CMQSaludApp(viewModel: DocumentoViewModel = viewModel()) {
    val navController = rememberNavController()
    val documentos by viewModel.documentos.collectAsStateWithLifecycle()

    var usuarioSesion by remember { mutableStateOf("18.452.123-K") }
    var centroSesion by remember { mutableStateOf("CESFAM Quilpué") }
    var perfilSesion by remember { mutableStateOf(PerfilUsuario.FUNCIONARIO) }

    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { usuario, centro, perfil ->
                    usuarioSesion = usuario
                    centroSesion = centro
                    perfilSesion = perfil
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                usuarioActivo = usuarioSesion,
                centroActivo = centroSesion,
                perfilActivo = perfilSesion,
                documentos = documentos,
                onNuevoDocumento = {
                    viewModel.limpiarFormulario()
                    viewModel.onCentroChange(centroSesion)
                    navController.navigate(RegistroRoute)
                },
                onEditarDocumento = { doc ->
                    viewModel.cargarParaEditar(doc)
                    navController.navigate(RegistroRoute)
                },
                onEliminarDocumento = { doc ->
                    viewModel.eliminarDocumento(doc)
                },
                onAprobarDocumento = { doc ->
                    viewModel.aprobarDocumento(doc, perfilSesion.etiqueta)
                },
                onRechazarDocumento = { doc, motivo ->
                    viewModel.rechazarDocumento(doc, motivo, perfilSesion.etiqueta)
                },
                onCerrarSesion = {
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<RegistroRoute> {
            RegistroScreen(
                state = viewModel.uiState,
                onTituloChange = viewModel::onTituloChange,
                onCentroChange = viewModel::onCentroChange,
                onCategoriaChange = viewModel::onCategoriaChange,
                onObservacionesChange = viewModel::onObservacionesChange,
                onFotoCapturada = viewModel::onFotoCapturada,
                onGuardar = {
                    viewModel.guardarDocumento(usuarioSesion) {
                        navController.popBackStack()
                    }
                },
                onVolver = {
                    viewModel.limpiarFormulario()
                    navController.popBackStack()
                }
            )
        }
    }
}