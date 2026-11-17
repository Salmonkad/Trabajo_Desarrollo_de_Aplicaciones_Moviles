package cl.duoc.cmqsalud.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.cmqsalud.model.PerfilUsuario
import cl.duoc.cmqsalud.ui.componentes.DarkNavyColor
import cl.duoc.cmqsalud.ui.componentes.TealColor

fun validarRutChileno(rutCompleto: String): Boolean {
    val limpio = rutCompleto.trim().replace(".", "").replace("-", "").uppercase()
    if (limpio.length !in 8..9) return false

    val cuerpo = limpio.substring(0, limpio.length - 1)
    val dv = limpio.last()

    if (!cuerpo.all { it.isDigit() }) return false

    var suma = 0
    var multiplicador = 2
    for (i in cuerpo.indices.reversed()) {
        suma += (cuerpo[i] - '0') * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val resto = 11 - (suma % 11)
    val dvEsperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.digitToChar()
    }

    return dv == dvEsperado
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (usuario: String, centro: String, perfil: PerfilUsuario) -> Unit) {
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var centroSeleccionado by remember { mutableStateOf("CESFAM Quilpué") }
    var perfilSeleccionado by remember { mutableStateOf(PerfilUsuario.FUNCIONARIO) }

    var expandidoCentro by remember { mutableStateOf(false) }
    var expandidoPerfil by remember { mutableStateOf(false) }
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }

    val centros = listOf("CESFAM Quilpué", "CESFAM Belloto Sur", "CESFAM Aviador Acevedo", "SAPU Pompeya")

    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorPass by remember { mutableStateOf<String?>(null) }

    val campoTextoColores = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkNavyColor,
        unfocusedTextColor = DarkNavyColor,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = TealColor,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = TealColor,
        unfocusedBorderColor = Color.LightGray
    )

    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperar = false },
            containerColor = Color.White,
            title = { Text("Gestión de Acceso", fontWeight = FontWeight.Bold, color = DarkNavyColor) },
            text = {
                Text(
                    "La habilitación de cuentas, restablecimiento de claves y asignación de perfiles se gestiona directamente con el área de Recursos Humanos (RR. HH.).",
                    color = DarkNavyColor
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoRecuperar = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F9FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = TealColor,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("CMQ Salud", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkNavyColor)
                Text("Gestión Documental APS", fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = rut,
                    onValueChange = { nuevoTexto ->
                        val filtrado = nuevoTexto.filter { it.isDigit() || it == '-' || it == '.' || it == 'k' || it == 'K' }
                        if (filtrado.length <= 12) {
                            rut = filtrado
                            errorRut = null
                        }
                    },
                    label = { Text("RUT Funcionario (ej: 12345678-K)") },
                    placeholder = { Text("12345678-K", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealColor) },
                    isError = errorRut != null,
                    supportingText = { errorRut?.let { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = campoTextoColores,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorPass = null },
                    label = { Text("Contraseña Institucional") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealColor) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorPass != null,
                    supportingText = { errorPass?.let { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = campoTextoColores,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandidoPerfil,
                    onExpandedChange = { expandidoPerfil = !expandidoPerfil },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = perfilSeleccionado.etiqueta,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perfil de Acceso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoPerfil) },
                        shape = RoundedCornerShape(12.dp),
                        colors = campoTextoColores,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoPerfil,
                        onDismissRequest = { expandidoPerfil = false }
                    ) {
                        PerfilUsuario.values().forEach { perfil ->
                            DropdownMenuItem(
                                text = { Text(perfil.etiqueta) },
                                onClick = {
                                    perfilSeleccionado = perfil
                                    expandidoPerfil = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandidoCentro,
                    onExpandedChange = { expandidoCentro = !expandidoCentro },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = centroSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sucursal / Centro de Salud") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCentro) },
                        shape = RoundedCornerShape(12.dp),
                        colors = campoTextoColores,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoCentro,
                        onDismissRequest = { expandidoCentro = false }
                    ) {
                        centros.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    centroSeleccionado = opcion
                                    expandidoCentro = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val rutValido = validarRutChileno(rut)
                        val passErr = if (password.isBlank() || password.length < 4) "Contraseña mínima 4 caracteres" else null

                        errorRut = if (!rutValido) "RUT inválido (formato o dígito verificador erróneo)" else null
                        errorPass = passErr

                        if (rutValido && passErr == null) {
                            onLoginSuccess(rut.trim().uppercase(), centroSeleccionado, perfilSeleccionado)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Ingresar al Sistema", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { mostrarDialogoRecuperar = true }) {
                    Text("¿Problemas de acceso o habilitación de cuenta?", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Text(
                "Acceso exclusivo para funcionarios con contrato vigente.\nCMQ Salud • Municipalidad de Quilpué",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }
    }
}