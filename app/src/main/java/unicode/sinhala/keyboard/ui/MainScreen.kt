package unicode.sinhala.keyboard.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import unicode.sinhala.com.BuildConfig
import unicode.sinhala.com.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isKeyboardEnabled: Boolean,
    isKeyboardSelected: Boolean,
    onEnableKeyboard: () -> Unit,
    onSelectKeyboard: () -> Unit
) {
    Scaffold(
        topBar = {
            if (isKeyboardEnabled && isKeyboardSelected) {
                TopAppBar(
                    title = { Text("Mathra Keyboard") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isKeyboardEnabled && isKeyboardSelected) {
                    SettingsScreen()
                } else {
                    SetupScreen(
                        isKeyboardEnabled = isKeyboardEnabled,
                        onEnableKeyboard = onEnableKeyboard,
                        onSelectKeyboard = onSelectKeyboard
                    )
                }
            }
            AboutFooter()
        }
    }
}

@Composable
fun SetupScreen(
    isKeyboardEnabled: Boolean,
    onEnableKeyboard: () -> Unit,
    onSelectKeyboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Use R.drawable.ic_launcher_foreground instead of mipmap resource which might be causing issues
        // or just use a generic icon if that fails. For now, assuming ic_launcher_foreground exists 
        // as it is referenced in ic_launcher.xml
        // Logo pops in with a springy bounce (artwork itself is untouched, only scaled)
        var popped by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { popped = true }
        val logoScale by animateFloatAsState(
            targetValue = if (popped) 1f else 0.4f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "logoPop"
        )
        Image(
            painter = painterResource(id = R.drawable.mathra_logo),
            contentDescription = "Mathra Keyboard logo",
            modifier = Modifier
                .size(160.dp)
                .scale(logoScale)
                .padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Text(
            text = if (!isKeyboardEnabled) 
                "Mathra Keyboard is not enabled. Please enable it in settings to continue."
            else 
                "Mathra Keyboard is enabled but not selected. Please select it to start typing.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                if (!isKeyboardEnabled) {
                    onEnableKeyboard()
                } else {
                    onSelectKeyboard()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (!isKeyboardEnabled) "Enable Keyboard" else "Select Keyboard",
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun AboutFooter() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mathra Keyboard  \u2022  v${BuildConfig.VERSION_NAME}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Developed by Denuwan Akash",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = {
            // Opens the dialer only; the user decides whether to call.
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0783756055")))
        }) {
            Text("Contact: 0783756055", fontSize = 13.sp)
        }
    }
}
