package com.gilsonfrias.recommenderpoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.gilsonfrias.recommenderpoc.ui.theme.MyApplicationTheme
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.text.toIntOrNull
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class NavigationDrawerSection(val title: String) {
    INFERENCE_TRAINING("Inference / Training"),
    MODEL_UPDATE("Model update"),
    ABOUT("About")
}

class MainActivity : ComponentActivity() {

    private var recommender = Recommender(this)
    private var probabilities = buildTransitionProbabilities().toMutableStateList()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedSection by remember { mutableStateOf(NavigationDrawerSection.INFERENCE_TRAINING) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var dom by remember { mutableStateOf(1) }
                var currentAction by remember { mutableStateOf(0) }
                var savingsState by remember { mutableStateOf(2) }
                var checkingState by remember { mutableStateOf(2) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Recommender POC",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Navigation Menu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            NavigationDrawerItem(
                                label = { Text("Inference / Training") },
                                selected = selectedSection == NavigationDrawerSection.INFERENCE_TRAINING,
                                onClick = {
                                    selectedSection = NavigationDrawerSection.INFERENCE_TRAINING
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Inference / Training") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                label = { Text("Model update") },
                                selected = selectedSection == NavigationDrawerSection.MODEL_UPDATE,
                                onClick = {
                                    selectedSection = NavigationDrawerSection.MODEL_UPDATE
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.Refresh, contentDescription = "Model update") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                label = { Text("About") },
                                selected = selectedSection == NavigationDrawerSection.ABOUT,
                                onClick = {
                                    selectedSection = NavigationDrawerSection.ABOUT
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors = topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = {
                                    Text(selectedSection.title)
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open navigation drawer"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            when (selectedSection) {
                                NavigationDrawerSection.INFERENCE_TRAINING -> {
                                    InferenceTrainingContent(
                                        recommender = recommender,
                                        probabilities = probabilities,
                                        dom = dom,
                                        currentAction = currentAction,
                                        savingsState = savingsState,
                                        checkingState = checkingState,
                                        onDomChange = { dom = it },
                                        onCurrentActionChange = { currentAction = it },
                                        onSavingsStateChange = { savingsState = it },
                                        onCheckingStateChange = { checkingState = it }
                                    )
                                }
                                NavigationDrawerSection.MODEL_UPDATE -> {
                                    ModelUpdateContent()
                                }
                                NavigationDrawerSection.ABOUT -> {
                                    AboutContent()
                                }
                            }
                        }
                    }
                }
            }
        }
        recommender
            .initialize()
            .addOnFailureListener { e -> Log.e("[MainActivity:onCreate]", "Error initializing recommender instance") }
        //val input = arrayOf<String>("31:Dashboard:FLAT","Account_Details", "13:PAYMENT_EXPRESS:PURCHASES:NEGATIVE", "BENEF_INTERBANK_PAYMENT:RENT")
        val input = arrayOf<Float>(15.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F)
        recommender.predict(input)
    }

    fun createStringByteBuffer(text: String): ByteBuffer {
        val bytes = text.toByteArray(Charsets.UTF_8)
        // Allocate space for byte length header + string capacity
        val buffer = ByteBuffer.allocateDirect(bytes.size + 4)
        buffer.order(ByteOrder.nativeOrder())
        buffer.putInt(bytes.size)
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }

    override fun onDestroy() {
        recommender.close()
        super.onDestroy()
    }
}

@Composable
fun InferenceTrainingContent(
    recommender: Recommender? = null,
    probabilities: List<TransitionProbabilities> = remember { buildTransitionProbabilities().toMutableStateList() },
    dom: Int = 1,
    currentAction: Int = 3,
    savingsState: Int = 2,
    checkingState: Int = 2,
    onDomChange: (Int) -> Unit = {},
    onCurrentActionChange: (Int) -> Unit = {},
    onSavingsStateChange: (Int) -> Unit = {},
    onCheckingStateChange: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AppFeaturesArrangement("Current", 0)
        PopulateInputTextFields(
            dom = dom,
            onDomChange = onDomChange,
            currentAction = currentAction,
            onCurrentActionChange = onCurrentActionChange,
            savingsState = savingsState,
            onSavingsStateChange = onSavingsStateChange,
            checkingState = checkingState,
            onCheckingStateChange = onCheckingStateChange
        )
        HorizontalDivider(thickness = 1.dp)
        PopulatePredictionButton(
            recommender = recommender,
            probabilities = probabilities,
            dom = dom,
            currentAction = currentAction,
            savingsState = savingsState,
            checkingState = checkingState
        )
        HorizontalDivider(thickness = 1.dp)
        PreviewMarkovChain(currentState = currentAction, probabilities = probabilities)
        HorizontalDivider(thickness = 1.dp)
        PopulateTrainingButton()
    }
}

@Composable
fun ModelUpdateContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Model Management & Updates",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Active TFLite Model",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "File: recommendation_0.1.0-alpha.tflite",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Status: Loaded & Operational",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Check for updates */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Check for Online Model Updates")
        }

        OutlinedButton(
            onClick = { /* Import local model */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import Local Model (.tflite)")
        }
    }
}

@Composable
fun AboutContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "In-App Recommender POC",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Version 1.0.0-alpha",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "This Proof of Concept demonstrates an on-device machine learning recommender engine using TensorFlow Lite and Markov Chains to predict next-step user actions.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "System Stack:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("• Jetpack Compose & Material 3", style = MaterialTheme.typography.bodySmall)
                Text("• TensorFlow Lite Runtime 2.17.0", style = MaterialTheme.typography.bodySmall)
                Text("• Markov Chain State Graph Visualization", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AppFeaturesArrangement(appState: String, t: Int) {
    Text(
        modifier = Modifier.fillMaxWidth(),
            //.fillMaxHeight(0.05f),
        textAlign = TextAlign.Center,
        text = buildAnnotatedString {
            append("$appState App State As")
            withStyle(
                style = SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = 12.sp
                )
            ) {
                append("(t+$t)")
            }
            append(" feature set")
        },
        color = Color.DarkGray,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun PopulateInputTextFields(
    dom: Int = 1,
    currentAction: Int = 1,
    savingsState: Int = 1,
    checkingState: Int = 2,
    onSavingsStateChange: (Int) -> Unit = {},
    onCheckingStateChange: (Int) -> Unit = {},
    onDomChange: (Int) -> Unit = {},
    onCurrentActionChange: (Int) -> Unit = {}
) {
    val digitsPattern = remember { Regex("^\\d*\\.?\\d*$") }
    var textInputSavings by remember { mutableStateOf("500.00") }
    var textInputChecking by remember { mutableStateOf("500.00") }
    var savingsLabelConfig by remember { mutableStateOf(GetAmountLabelConfig("500.00", "500.00")) }
    var checkingLabelConfig by remember { mutableStateOf(GetAmountLabelConfig("500.00", "500.00")) }
    val lightGray = Color(0XFFEEEEEE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.25f)
                .background(lightGray)
        ) {
            PopulateDropdownMenu(
                label = "DOM",
                minValue = 1,
                maxValue = 32,
                selectedValue = dom.toString(),
                onOptionSelected = onDomChange
            )
        }
        TextField(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.33f)
                .background(lightGray),
            value = textInputSavings,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(digitsPattern)) {
                    savingsLabelConfig = GetAmountLabelConfig(newValue, textInputSavings)
                    textInputSavings = newValue
                    newValue.toDoubleOrNull()?.toInt()?.let { intVal ->
                        onSavingsStateChange.invoke(savingsLabelConfig.third)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text(
                    text = "SAVINGS ${savingsLabelConfig.first}",
                    color = savingsLabelConfig.second,
                )
            }
        )
        TextField(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.33f)
                .background(lightGray),
            value = textInputChecking,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(digitsPattern)) {
                    checkingLabelConfig = GetAmountLabelConfig(newValue, textInputChecking)
                    textInputChecking = newValue
                    newValue.toDoubleOrNull()?.toInt()?.let { intVal ->
                        onCheckingStateChange.invoke(intVal)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text(
                    text = "CHECK ${checkingLabelConfig.first}",
                    color = checkingLabelConfig.second
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.25f)
                //.width(0.20f)
                .background(lightGray)
        ) {
            PopulateDropdownMenu(
                label = "act",
                minValue = 0,
                maxValue = 9,
                selectedValue = currentAction.toString(),
                onOptionSelected = onCurrentActionChange
            )
        }
    }
}

fun GetAmountLabelConfig(currentValue: String, previousValue: String) : Triple<String, Color, Int> {
    val current = currentValue.toDoubleOrNull()
    val previous = previousValue.toDoubleOrNull()
    if (current != null && previous != null) {
        if (current > previous) {
            return Triple("▲", Color(0xFF2E7D32), 3)
        } else if (current < previous) {
            return Triple("▼", Color(0xFFFF0000), 1)
        } else {
            return Triple("➖", Color(0xFF757575), 2)
        }
    }
    return Triple("➖", Color(0xFF757575), 2)
}

@Composable
fun PopulateDropdownMenu(
    label: String,
    minValue: Int = 0,
    maxValue: Int = 31,
    selectedValue: String? = null,
    onOptionSelected: ((Int) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val domList = List(size=maxValue+1) { "${it + minValue}" }
    var selectedDOM by remember { mutableStateOf(selectedValue ?: domList[0]) }
    Row(

        //verticalAlignment = Alignment.CenterVertically,
        //horizontalArrangement = Arrangement.End
        Modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.weight(2f),
            value = selectedDOM,
            onValueChange = {},
            //state = rememberTextFieldState(initialText = selectedDOM),
            readOnly = true,
            label = {
                Text(label)
            }
        )
        Box(
            modifier = Modifier
                .weight(0.5f)
                .padding(3.dp)
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                domList.forEach { dom ->
                    DropdownMenuItem(
                        text = { Text(dom) },
                        onClick = {
                            selectedDOM = dom
                            expanded = false
                            dom.toIntOrNull()?.let { intVal ->
                                onOptionSelected?.invoke(intVal)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PopulatePredictionButton(recommender: Recommender? = null, probabilities: List<TransitionProbabilities>, dom: Int = 1, currentAction: Int = 1, savingsState: Int = 2, checkingState: Int = 2) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val backgroundColor = if (isPressed) Color.Red else Color.Blue
    val lightGray = Color(0XFFEEEEEE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.4f)
                //.background(lightGray)
        ) {
            Button(
                onClick = {
                    //Convert to float and pad with zeroes remaining params
                    val input = arrayOf<Float>(
                        currentAction.toFloat(),
                        dom.toFloat(),
                        savingsState.toFloat(),
                        checkingState.toFloat(),
                        0.0F,
                        0.0F
                    )
                    val result = recommender?.predict(input, normalizeSoftmax = true)
                    Log.d("[PopulatePredictionButton]", "Result: ${result.contentToString()}")
                    //update transition probabilities with result distribution
                    if (result?.isNotEmpty() == true && probabilities.size == result.size) {
                        for ((i, probability) in probabilities.withIndex()) {
                            probability.probability = result[i]
                            if (probabilities is SnapshotStateList) {
                                probabilities[i] = probability.copy(probability = result[i])
                            } else if (probabilities is MutableList) {
                                probabilities[i] = probability.copy(probability = result[i])
                            }
                        }
                    }
                },
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor
                )
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = buildAnnotatedString {
                        append("Predict next state As")
                        withStyle(
                            style = SpanStyle(
                                baselineShift = BaselineShift.Subscript,
                                fontSize = 12.sp
                            )
                        ) {
                            append("(t+1)")
                        }
                    },
                    //color = Color.DarkGray,
                    //fontSize = 18.sp,
                    //fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.5f)
                .background(lightGray)
        ) {
            PopulateDropdownMenu(label="Expected next action", minValue = 0, maxValue = 9)
        }
    }
}

// 1. Data Models for States and Transitions
enum class MarkovStateEnum(val id: String, val stateName: String) {
    S0("S0", "As(t+0)"),
    S1("S1", "App \nLogin"),
    S2("S2", "Payment \nRent"),
    S3("S3", "Payment \nLoan"),
    S4("S4", "Payment \nElectricity"),
    S5("S5", "Payment \nInternet"),
    S6("S6", "Payment \nPhone"),
    S7("S7", "Payment \nNetflix"),
    S8("S8", "Payment \nWater"),
    S9("S9", "Payment \nPurchases")
}

data class MarkovState(
    val id: String,
    val name: String,
    val position: Offset // Pre-defined position or calculated layout
) {
    constructor(stateEnum: MarkovStateEnum, position: Offset) : this(stateEnum.id, stateEnum.stateName, position)
}

data class MarkovTransition(
    val fromStateId: String,
    val toStateId: String,
    val probability: Float
)

@Composable
fun MarkovChainGraph(
    states: List<MarkovState>,
    transitions: List<MarkovTransition>,
    transitionProbabilities: List<TransitionProbabilities>,
    currentState: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Zoom and pan state
    var scale by remember { mutableStateOf(0.8f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Obtain state with maximum transition probability
    val maxProbabilityState = transitionProbabilities.withIndex().maxByOrNull { it.value.probability }?.index
    Log.d("[MarkovChainGraph]", "Max Probability MaxState: $maxProbabilityState")
    Log.d("[MarkovChainGraph]", "Prob. Distribution: $transitionProbabilities")

    Box(
        modifier = modifier
            .fillMaxHeight(0.7f)
            .fillMaxWidth(1f)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Apply zoom and pan transformations
            drawContext.transform.translate(offset.x, offset.y)
            drawContext.transform.scale(scale, scale, Offset.Zero)

            val nodeRadius = 40.dp.toPx()
            val stateMap = states.associateBy { it.id }

            // Step 1: Draw Transitions (Edges)
            transitions.forEach { transition ->
                val fromNode = stateMap[transition.fromStateId]
                val toNode = stateMap[transition.toStateId]

                if (fromNode != null && toNode != null) {
                    if (fromNode.id == toNode.id) {
                        // Self-loop
                        drawSelfLoop(fromNode.position, nodeRadius, transition.probability, textMeasurer)
                    } else {
                        // Check if a reverse transition exists to avoid overlapping straight lines
                        val hasReverse = transitions.any {
                            it.fromStateId == toNode.id && it.toStateId == fromNode.id
                        }
                        drawEdge(fromNode.position, toNode.position, nodeRadius, transition.probability, hasReverse, textMeasurer)
                    }
                }
            }

            // Step 2: Draw States (Nodes)
            states.forEach { state ->
                // Draw State Node
                drawCircle(
                    color = if (state.id == "S${maxProbabilityState?.plus(1)}") Color.Green else Color(0xFF6200EE),
                    radius = nodeRadius,
                    center = state.position
                )
                // Draw Node Border
                drawCircle(
                    color = if (state.id == MarkovStateEnum.S0.id) Color.Blue else Color.White,
                    radius = nodeRadius,
                    center = state.position,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Draw State Name Text
                val textLayoutResult = textMeasurer.measure(
                    text = state.name,
                    style = TextStyle(color = Color.White, fontSize = 14.sp)
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        state.position.x - textLayoutResult.size.width / 2f,
                        state.position.y - textLayoutResult.size.height / 2f
                    )
                )
                //Draw text field only for root/origin state
                if (state.id== MarkovStateEnum.S0.id){
                    val currentStateEnum = MarkovStateEnum.valueOf(currentState)
                    val textLayoutResult = textMeasurer.measure(
                        text = "Current State\n$currentStateEnum",
                        style = TextStyle(color = Color.Blue, fontSize = 14.sp)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            state.position.x - 120 - textLayoutResult.size.width / 2f,
                            state.position.y + 150 - textLayoutResult.size.height / 2f
                        )
                    )
                }
            }
        }
    }
}

// Helper to draw a directed edge between two states
private fun DrawScope.drawEdge(
    from: Offset,
    to: Offset,
    radius: Float,
    probability: Float,
    curved: Boolean,
    textMeasurer: TextMeasurer
) {
    val arrowLength = 15.dp.toPx()
    val arrowAngleOffset = Math.toRadians(30.0)

    val path = Path()
    val labelPos: Offset
    val arrowTip: Offset
    val angle: Double

    if (curved) {
        // Curve the path using a cubic control point offset to the side
        val midX = (from.x + to.x) / 2f
        val midY = (from.y + to.y) / 2f
        val dx = to.x - from.x
        val dy = to.y - from.y
        val normalX = -dy
        val normalY = dx
        val normalLength = sqrt(normalX * normalX + normalY * normalY)

        // Control offset distance for curvature
        val offsetDist = 40.dp.toPx()
        val cx = midX + (normalX / normalLength) * offsetDist
        val cy = midY + (normalY / normalLength) * offsetDist
        val controlPoint = Offset(cx, cy)

        // Interpolate approximate intersection point on target boundary due to curve
        angle = atan2(to.y - cy, to.x - cx).toDouble()
        arrowTip = Offset(
            to.x - radius * cos(angle).toFloat(),
            to.y - radius * sin(angle).toFloat()
        )

        // Calculate a cleaner starting node boundary
        val startAngle = atan2(cy - from.y, cx - from.x).toDouble()
        val startPoint = Offset(
            from.x + radius * cos(startAngle).toFloat(),
            from.y + radius * sin(startAngle).toFloat()
        )

        path.moveTo(startPoint.x, startPoint.y)
        path.quadraticTo(controlPoint.x, controlPoint.y, arrowTip.x, arrowTip.y)

        // Midpoint of quadratic bezier for label
        labelPos = Offset(
            0.25f * startPoint.x + 0.5f * controlPoint.x + 0.25f * arrowTip.x,
            0.25f * startPoint.y + 0.5f * controlPoint.y + 0.25f * arrowTip.y
        )
    } else {
        // Straight line directional calculation
        angle = atan2(to.y - from.y, to.x - from.x).toDouble()
        val startPoint = Offset(
            from.x + radius * cos(angle).toFloat(),
            from.y + radius * sin(angle).toFloat()
        )
        arrowTip = Offset(
            to.x - radius * cos(angle).toFloat(),
            to.y - radius * sin(angle).toFloat()
        )

        path.moveTo(startPoint.x, startPoint.y)
        path.lineTo(arrowTip.x, arrowTip.y)
        labelPos = Offset((startPoint.x + arrowTip.x) / 2, (startPoint.y + arrowTip.y) / 2)
    }

    // Draw edge line
    drawPath(
        path = path,
        color = Color.Gray,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Draw Arrowhead
    val arrowPath = Path().apply {
        moveTo(arrowTip.x, arrowTip.y)
        lineTo(
            (arrowTip.x - arrowLength * cos(angle - arrowAngleOffset)).toFloat(),
            (arrowTip.y - arrowLength * sin(angle - arrowAngleOffset)).toFloat()
        )
        moveTo(arrowTip.x, arrowTip.y)
        lineTo(
            (arrowTip.x - arrowLength * cos(angle + arrowAngleOffset)).toFloat(),
            (arrowTip.y - arrowLength * sin(angle + arrowAngleOffset)).toFloat()
        )
    }
    drawPath(path = arrowPath, color = Color.Gray, style = Stroke(width = 2.dp.toPx()))

    // Draw Probability text label
    val textLayout = textMeasurer.measure(
        text = "%.2f".format(probability),
        style = TextStyle(color = Color.DarkGray, fontSize = 12.sp)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(labelPos.x - textLayout.size.width / 2f, labelPos.y - textLayout.size.height - 4f)
    )
}

// Helper to draw a self-loop (Transition back to self)
private fun DrawScope.drawSelfLoop(
    center: Offset,
    radius: Float,
    probability: Float,
    textMeasurer: TextMeasurer
) {
    val loopRadius = 24.dp.toPx()
    // Position loop above the node structure
    val loopCenter = Offset(center.x, center.y - radius)

    drawCircle(
        color = Color.Gray,
        radius = loopRadius,
        center = loopCenter,
        style = Stroke(width = 2.dp.toPx())
    )

    // Add an arrowhead to the self loop manually at its side intercept
    val arrowTip = Offset(center.x + 8.dp.toPx(), center.y - radius)
    val arrowPath = Path().apply {
        moveTo(arrowTip.x, arrowTip.y)
        lineTo(arrowTip.x + 12.dp.toPx(), arrowTip.y - 4.dp.toPx())
        moveTo(arrowTip.x, arrowTip.y)
        lineTo(arrowTip.x + 4.dp.toPx(), arrowTip.y - 12.dp.toPx())
    }
    drawPath(path = arrowPath, color = Color.Gray, style = Stroke(width = 2.dp.toPx()))

    // Text Label above the self loop
    val textLayout = textMeasurer.measure(
        text = "%.2f".format(probability),
        style = TextStyle(color = Color.DarkGray, fontSize = 12.sp)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(loopCenter.x - textLayout.size.width / 2f, loopCenter.y - loopRadius - textLayout.size.height)
    )
}

@Composable
fun PreviewMarkovChain(
    currentState: Int = 0,
    probabilities: List<TransitionProbabilities>,
    nextState: String = "S2",
    onCurrentStateChange: (Int) -> Unit = {},
) {
    var sanitizedCurrentState = currentState
    if (currentState>MarkovStateEnum.entries.size-1 || currentState<0) {
        sanitizedCurrentState = 0
    }

    val markovCurrentState = MarkovStateEnum.valueOf("S$sanitizedCurrentState")

    Log.d("Enum:Size", markovCurrentState.toString())
    val sampleStates = listOf(
        MarkovState(MarkovStateEnum.S0, Offset(240f, 550f)),
        MarkovState(MarkovStateEnum.S1, Offset(350f, 100f)),
        MarkovState(MarkovStateEnum.S2, Offset(600f, 100f)),
        MarkovState(MarkovStateEnum.S3, Offset(850f, 150f)),
        MarkovState(MarkovStateEnum.S4, Offset(1050f, 300f)),
        MarkovState(MarkovStateEnum.S5, Offset(1050f, 550f)),
        MarkovState(MarkovStateEnum.S6, Offset(1050f, 800f)),
        MarkovState(MarkovStateEnum.S7, Offset(850f, 950f)),
        MarkovState(MarkovStateEnum.S8, Offset(600f, 1000f)),
        MarkovState(MarkovStateEnum.S9, Offset(350f, 1000f))
    )

    val transitions = if (probabilities.isNotEmpty()) {
        probabilities.mapIndexed { index, prob ->
            MarkovTransition(
                fromStateId = prob.fromStateId,
                toStateId = "S${prob.destinationStateId + 1}",
                probability = prob.probability
            )
        }
    } else {
        listOf(
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S1.id, 0.2f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S2.id, 0.6f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S3.id, 0.4f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S4.id, 0.8f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S5.id, 0.2f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S6.id, 0.6f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S7.id, 0.4f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S8.id, 0.8f),
            MarkovTransition(MarkovStateEnum.S0.id, MarkovStateEnum.S9.id, 0.2f),
        )
    }

    MarkovChainGraph(
        states = sampleStates,
        transitions = transitions,
        currentState = "S$sanitizedCurrentState",
        transitionProbabilities = probabilities
    )
}

@Composable
fun PopulateTrainingButton() {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val backgroundColor = if (isPressed) Color.Red else Color.Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(0.32f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            /*
            Image(
                painter = painterResource(id = R.drawable.database_24px),
                contentDescription = "Memory-Replay-Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(15.dp)
            )
             */
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = "3/5",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = {
                    Text(
                        text = "Replay memory size",
                        fontSize = 11.sp
                    )
                }
            )
        }

        Column(
            modifier = Modifier.weight(0.32f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            /*
            Image(
                painter = painterResource(id = R.drawable.leaderboard_24px),
                contentDescription = "Reward-Function-Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(15.dp)
            )
             */
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = "0.1",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = {
                    Text(
                        text = "Current episode reward",
                        fontSize = 11.sp
                    )
                }
            )
        }

        Button(
            onClick = {},
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            ),
            modifier = Modifier
                .weight(0.36f)
                .height(56.dp)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    append("Train DQN model")
                }
            )
        }
    }
}

