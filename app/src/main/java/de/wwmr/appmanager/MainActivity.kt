package de.wwmr.appmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0E17)
private val Panel = Color(0xFF171C2A)
private val Accent = Color(0xFF9B8CFF)
private val Mint = Color(0xFF53DDB5)

data class AppEntry(val label: String, val packageName: String, val version: String, val system: Boolean)

class MainActivity : ComponentActivity() {
    private var refresh by mutableIntStateOf(0)
    private val apkPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { installApk(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppManagerScreen(
                apps = remember(refresh) { loadApps() },
                onInstall = { apkPicker.launch(arrayOf("application/vnd.android.package-archive")) },
                onUninstall = { pkg ->
                    runCatching { startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg"))) }
                },
                onRefresh = { refresh++ }
            )
        }
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { info ->
                runCatching {
                    val p = pm.getPackageInfo(info.packageName, 0)
                    AppEntry(pm.getApplicationLabel(info).toString(), info.packageName,
                        p.versionName ?: "Unbekannt",
                        (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
                }.getOrNull()
            }.filter { it.packageName != packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun installApk(uri: Uri) {
        runCatching {
            startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = uri
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(apps: List<AppEntry>, onInstall: () -> Unit,
                     onUninstall: (String) -> Unit, onRefresh: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var showSystem by remember { mutableStateOf(false) }
    val visible = apps.filter {
        (showSystem || !it.system) &&
        (it.label.contains(query, true) || it.packageName.contains(query, true))
    }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent, secondary = Mint, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = Bg, topBar = {
            TopAppBar(
                title = { Column {
                    Text("APP MANAGER", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("Deine Apps. Deine Kontrolle.", color = Color.LightGray, fontSize = 12.sp)
                } },
                actions = { IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, "Aktualisieren", tint = Accent)
                } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel),
                        shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Apps, null, tint = Accent)
                            Text("${apps.size}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text("Installierte Apps", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel),
                        shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(Icons.Default.SystemUpdateAlt, null, tint = Mint)
                            Text("APK", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text("Neue Version", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onInstall, modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Default.FileOpen, null)
                    Spacer(Modifier.width(8.dp))
                    Text("APK auswählen & installieren", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Apps suchen …", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Accent) },
                    singleLine = true, shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF343B50),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("System-Apps anzeigen", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = showSystem, onCheckedChange = { showSystem = it })
                    Spacer(Modifier.weight(1f))
                    Text("${visible.size} Apps", color = Accent, fontSize = 12.sp)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(visible, key = { it.packageName }) { app ->
                        Card(colors = CardDefaults.cardColors(containerColor = Panel),
                            shape = RoundedCornerShape(14.dp)) {
                            Row(Modifier.fillMaxWidth().padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).background(Color(0xFF292542),
                                    RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Android, null, tint = Accent)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.label, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text("v${app.version} • ${app.packageName}", color = Color.LightGray,
                                        fontSize = 10.sp, maxLines = 1)
                                }
                                if (!app.system) {
                                    IconButton(onClick = { onUninstall(app.packageName) }) {
                                        Icon(Icons.Default.DeleteOutline, "Deinstallieren",
                                            tint = Color(0xFFFF718B))
                                    }
                                } else {
                                    Icon(Icons.Default.Shield, "System-App", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
