package com.cmlee.executiful.letswinmarksix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.hairsp
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_banker
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_num
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.base64ToBitmap
import com.cmlee.executiful.letswinmarksix.model.DrawResultViewModel
import com.cmlee.executiful.letswinmarksix.model.DrawResultViewModelFactory
import com.cmlee.executiful.letswinmarksix.model.tickets.Ticket
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.ui.theme.LetsWinMarkSixTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private val LocalDrawResultViewModel = staticCompositionLocalOf<DrawResultViewModel> { error("no?") }
data class UserPreference(val key: String, val value: Ticket){
    private val df = SimpleDateFormat("ddMMMyy h:mm:ss", Locale.getDefault())
    val keyDate get() = df.format(Date(key.toLong()))
}

class ScanResults : ComponentActivity() {

    private lateinit var drawResultViewMode: DrawResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as App
        val userRepository = app.appContainer.drawResultRepository
        val viewModelFactory = DrawResultViewModelFactory(app.appContainer.sharedFile, userRepository)
        val data = app.appContainer.sharedFile.all.values.map{ it.toString() }
        drawResultViewMode = ViewModelProvider(this, viewModelFactory)[DrawResultViewModel::class.java]
        MobileAds.initialize(this) {}
        setContent {
            CompositionLocalProvider(LocalDrawResultViewModel provides drawResultViewMode) {
                LetsWinMarkSixTheme {
                    PreferencesScreen() { finish() }
/*                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppBarSelectionActions(
                            setOf(
                                1,
                                2,
                                3
                            ), modifier = Modifier
                        )
                    },
                    bottomBar = {BannerAd(LocalContext.current.getString(R.string.banner_ad_unit_id))}
                ) { innerPadding ->

                    ResultOutter(
                        modifier = Modifier.padding(innerPadding)
                    )
                }*/
            }
            }
        }

    }
}

// Main Top Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    preferences: List<UserPreference>,
    onBack: () -> Unit,
    onPreferenceSelected: (UserPreference) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("User Preferences") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Preferences Menu")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Text(
                        "Select Preference Key",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )

                    HorizontalDivider()

                    preferences.forEach { preference ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(preference.keyDate, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        preference.value.drawID,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            onClick = {
                                onPreferenceSelected(preference)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}

// Detail Top Bar with Delete Button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    onBack: () -> Unit,
    onDelete: () -> Unit,
    date: String
) {
    TopAppBar(
        title = { Text("掃瞄時間:$date") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Preference",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(viewModel: DrawResultViewModel = LocalDrawResultViewModel.current, onBack:()->Unit) {
//    val preferences by viewModel.preferences//.collectAsState()
    val selectedDetail by viewModel.selectedDetail.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val selectedTicket by viewModel.index.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showImageDialog by viewModel.showImageDialog.collectAsState()

    // Load preferences on first launch
    LaunchedEffect(Unit) {
        viewModel.loadPreferences()
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.deleteSelectedPreference() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
    if(showImageDialog) {
        TicketImageDialog(viewModel.preferences[selectedTicket].value.ocr){ viewModel.cancelShowImage() }
    }
    Scaffold(
        topBar = {
            if (selectedDetail .isNotEmpty()) {
                DetailTopBar(
                    date = viewModel.preferences[selectedTicket].keyDate,
                    onBack = { viewModel.clearSelectedDetail() },
                    onDelete = { viewModel.showDeleteConfirmation() }
                )
            } else {
                MainTopBar(
                    preferences = viewModel.preferences,
                    onBack = onBack,
                    onPreferenceSelected = { viewModel.getDetailByPreferenceKey(it) }
                )
            }
        },
        bottomBar = {BannerAd(LocalContext.current.getString(R.string.banner_ad_unit_id))}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if(selectedDetail.isNotEmpty()){
                    UserDetailScreen(viewModel.preferences[selectedTicket].value,details = selectedDetail ){
                        viewModel.showTicketImage()
                                            }
                } else {
                    PlaceholderScreen()
                }
            }
        }
    }
}

// Detail Screen
@Composable
fun UserDetailScreen(ticket:Ticket,details: List<DrawResult>, onDelete: () -> Unit) {
    var selectedOption by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with delete button
        Row(
            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Preference")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.inverseSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text("彩票資料", style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier=Modifier
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(8.dp)
                    .fillMaxWidth())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, color = MaterialTheme.colorScheme.inverseSurface)
                ) {
                    TableRow {
                        TableCell("期數", 1f, true)
                        TableCell(ticket.drawID, 2.5f)
                        TableCell("每注", 1f, true)
                        TableCell("$${ticket.drawUnit}", 2.5f)
                    }
                    TableRow {
                        TableCell("日期", 1f, true)
                        TableCell(ticket.buyDate, 2.5f)
                        TableCell("總額", 1f, true)
                        TableCell("$${ticket.drawTotal}", 2.5f)
                    }
                    TableRow{
                        TableCell("注項", 1f, true)
                    }
                    Column(modifier = Modifier) {
                        TableRadio(ticket, selectedOption){ idx -> selectedOption = idx }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(),border=BorderStroke(1.dp, color = MaterialTheme.colorScheme.inverseSurface)
                ,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text("相關${if(ticket.draws>1) "${ticket.draws}期" else ""}之攪珠結果",style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier=Modifier
                .background(MaterialTheme.colorScheme.inverseSurface)
                .padding(8.dp)
                .fillMaxWidth())
            Column(
//                modifier = Modifier.verticalScroll(state = rememberScrollState())
            ) {
                ResultBalls(details, ticket.drawItemNumbers[selectedOption])
//                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ColumnScope.TableRow(content: @Composable (RowScope.() -> Unit)) {
    Row (modifier = Modifier
        .height(IntrinsicSize.Max)
        .fillMaxWidth(), content = content)
}
@Composable
fun ColumnScope.TableRadio(ticket: Ticket, selectedOption:Int, onSelect:(Int)->Unit={}){
//    var selectedOption by remember { mutableIntStateOf(0) }
    ticket.drawItemNumbers.forEachIndexed { idx, (legs, bans) ->
        val text = buildString {
            append(bans.joinToString(m6_sep_num))
            if (bans.isEmpty().not()) append( m6_sep_banker+hairsp)
            append(legs.joinToString(m6_sep_num+hairsp))
        }
        Row(modifier=Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))) {
            RadioButton(selected = (idx == selectedOption), onClick = { onSelect(idx) })
            Text(text = text, modifier = Modifier.clickable(onClick = { onSelect(idx) }))
        }
    }
}
@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    header: Boolean=false
) {
    Box(
        modifier = Modifier
            .border(BorderStroke(.5.dp, MaterialTheme.colorScheme.outline)) // Apply border to each cell
            .weight(weight)
            .fillMaxHeight()
            .padding(start = 8.dp, end = 8.dp, top = if (header) 2.dp else 4.dp)
        , contentAlignment = Alignment.CenterStart
    ) {
        val withSpace = text.replace("([/+:])".toRegex()) { matchResult -> matchResult.value + hairsp }
        Text(
            text = withSpace, modifier = Modifier.fillMaxHeight(),
            style = if (header) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun TicketImageDialog(bm:String, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            base64ToBitmap(bm)?.let{bitmap->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "TODO()"
                )
            }

        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
// Delete Confirmation Dialog
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Preference") },
        text = {
            Text("Are you sure you want to delete this preference? " +
                    "This will remove the preference key and associated user data.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// Placeholder Screen
@Composable
fun PlaceholderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select a preference key from the menu",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
@Composable
fun ResultRow(results:List<DrawResult>){
    Row(){
        val defaultIcon = painterResource(R.drawable.ballcircle1)
        val dmyFormat = SimpleDateFormat("ddMMMyy", Locale.getDefault())
        results.fastForEachIndexed {idx, dr->
            Column(){
                Text(text = "${dr.id}\n${dr.date}")
            }
            DetailRow("攪珠日期", dmyFormat.format(dr.date))
            DetailRow("攪珠期數", dr.id)
            Row {
                if(idx%2==0) VerticalDivider(thickness = 5.dp)
                dr.no.nos.forEach{ no->
                    ShowBall(Modifier
                        .weight(1f)
                        .rotate(degrees = Random.nextInt(-60, 60).toFloat()), defaultIcon, no)
                }
                VerticalDivider(thickness = 5.dp)
                ShowBall(Modifier
                    .weight(1f)
                    .rotate(degrees = Random.nextInt(-60, 60).toFloat()), defaultIcon, dr.sno)
            }
        }

    }
}

@Composable
fun AdjustableRowText(texts:List<Pair<Float,String>>){
    var maxofline by remember { mutableIntStateOf(1) }
    Row(modifier = Modifier.fillMaxWidth()) {
        texts.forEach {
            Box(modifier = Modifier
                .weight(it.first)
                .padding(2.dp)
                .border(border = BorderStroke(width = .5.dp, color = Color.Red))
                , contentAlignment = Alignment.CenterStart){
                Text(text = "${it.second}$maxofline")
                Text(text = it.second,
                    minLines = maxofline,
//                    overflow = TextOverflow.Ellipsis,
                    color = Color.Red,
                    onTextLayout = { tl ->
                        if (tl.lineCount>maxofline) {
                            maxofline=tl.lineCount
                        }
                    }
                )
            }
        }
    }
}
@Preview()
@Composable
fun TebleBalls(results:List<List<String>> = listOf(
    listOf("ID", "Name", "Description"),
    listOf("1", "John", "Software Engineer with 5 years experience"),
    listOf("2", "Alice", "Product Manager"),
    listOf("3", "Bob", "UI/UX Designer with a passion for clean interfaces Software Engineer with 5 years experience")
) ){
//    var maxliness by remember {mutableListOf(List(results.size){1})}
    Box(modifier=Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(width = .5.dp, color = Color.Red)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "期數", modifier = Modifier.weight(2.5f))
                    Text(text = "日期", modifier = Modifier.weight(2.5f))
                    Text(text = "號碼", modifier = Modifier.weight(5f))
                }
            }
            val r2= results.map{ it.map{ i-> 1.0f to i}}
            r2.forEach { list->
                AdjustableRowText(list)
            }
            results.fastForEachIndexed { idx, dr ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)) {
                    dr.forEach { dc->
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
//                                .padding(2.dp)
                                .fillMaxHeight()
                                .border(border = BorderStroke(width = .5.dp, color = Color.Green)),
                            contentAlignment = Alignment.CenterStart
                        ){
                            Text(text = dc, modifier=Modifier.padding(4.dp))
                        }
                    }
                }

            }
/*            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Fixed(3)
            ) {
                items(results.flatten()) { itm ->
                    Text(itm, modifier = Modifier
                        .weight(2.5f)
                        .padding(2.dp))

                    HorizontalDivider(thickness = 1.dp)
                }
            }*/
//        Row(modifier = Modifier.fillMaxWidth())
        }
    }
}
@Composable
fun ResultBalls(results: List<DrawResult>, first: Pair<List<String>, List<String>>){
    val dmyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val (legs, bankers) =first
    Column(modifier=Modifier.fillMaxWidth()){
        Row(modifier=Modifier
            .fillMaxWidth()
            .border(1.dp, color = MaterialTheme.colorScheme.inverseSurface)
            .height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically){
            TableCell("期數",2f,true)
            TableCell("日期",3f,true)
            TableCell("號碼",5f,true)
        }
    }
    Column(modifier=Modifier
        .fillMaxWidth()
        .verticalScroll(state = rememberScrollState())
        .border(1.dp, color = MaterialTheme.colorScheme.inverseSurface)) {

        results.fastForEachIndexed {idx, dr->
            val nos = dr.no.nos.map{it.toString()}
            val sno = dr.sno.toString()
            val max1 = 6 - bankers.size
            val m6 = nos.intersect(bankers).plus(nos.intersect(legs).take(max1))
            val matchNumbers = nos.map { c -> c in m6 }
            val matchExtra = sno in bankers || sno in legs
            Row (modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)){
                TableCell(dr.id+" "+dr.sbcode,2f)
                TableCell(dmyFormat.format(dr.date),3f)
                TableCell(dr.no.nos.joinToString()+"\n特別號碼：$sno ${if(matchNumbers.count { it }>3)"*${m6.joinToString()}" else ""}",5f)
            }
        }
    }
    Column{ Spacer(modifier = Modifier.height(24.dp)) }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ShowBall(modifier:Modifier= Modifier.rotate(degrees = Random.nextInt(-60,60).toFloat()), icon: Painter=painterResource(R.drawable.ballcircle1), no:Int=12){
    Box(modifier) {
        val col = LocalResources.current.getIntArray(R.array.ball_color_array).get(no-1)
        Image(
            modifier = Modifier.padding(5.dp),
            painter = icon,
            colorFilter = ColorFilter.tint(Color(col), BlendMode.SrcIn),
            contentDescription = "hello",
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = "$no", textAlign = TextAlign.Center, modifier=Modifier.align(
                Alignment.Center)
        )
    }
}

@Composable
fun BannerAd( adUnitId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.MEDIUM_RECTANGLE)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
//:cite[1]:cite[3]:cite[4]
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarSelectionActions(
    selectedItems: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val hasSelection = selectedItems.isNotEmpty()
    val topBarText = if (hasSelection) {
        "Selected ${selectedItems.size} items"
    } else {
        "List of items"
    }

    TopAppBar(
        title = {
            Text(topBarText)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        actions = {
            if (hasSelection) {
                IconButton(onClick = {
                    /* click action */
                }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share items"
                    )
                }
            }
        },
    )
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LetsWinMarkSixTheme {
        Greeting("Android")
    }
}