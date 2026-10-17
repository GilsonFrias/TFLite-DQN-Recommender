package com.gilsonfrias.recommenderpoc

import android.R.attr.text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.captionBarPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private var recommender = Recommender(this)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                  val padding = 16.dp
                  Scaffold(
                      topBar = {
                          TopAppBar(
                              colors = topAppBarColors(
                                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                                  titleContentColor = MaterialTheme.colorScheme.primary,
                              ),
                              title = {
                                  Text("In-App Recommendation Test")
                              }
                          )
                      }
                  ) {innerPadding ->
                      Column(
                          modifier = Modifier
                              .padding(innerPadding).fillMaxHeight(1f),
                          verticalArrangement = Arrangement.spacedBy(20.dp),
                          //verticalAlignment = Alignment.Bottom
                      ) {

                          AppFeaturesArrangement("Current", 0)
                          PopulateInputTextFields()
                          HorizontalDivider(thickness = 1.dp)
                          PopulatePredictionButton()
                          //AppFeaturesArrangement("Current", 0)
                          //PopulateInputTextFields()
                          HorizontalDivider(thickness = 1.dp)
                          PreviewMarkovChain()
                          HorizontalDivider(thickness = 1.dp)
                          PopulateOutputScrollView()
                      }
                  }

            }
        }
        recommender
            .initialize()
            .addOnFailureListener { e -> Log.e("[MainActivity:onCreate]", "Error initializing recommender instance") }
        val input = arrayOf<String>("31:Dashboard:FLAT","Account_Details", "13:PAYMENT_EXPRESS:PURCHASES:NEGATIVE", "BENEF_INTERBANK_PAYMENT:RENT")
        recommender.classify(input)
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
fun PopulateInputTextFields() {
    val digitsPattern = remember { Regex("^\\d*\$") }
    var textInputSavings by remember { mutableStateOf("500.00")}
    var textInputChecking by remember { mutableStateOf("500.00")}
    val lightGray = Color(0XFFEEEEEE)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        //    .fillMaxHeight(0.1f),
        //    .padding(30.dp),

        //horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(1f)
                .background(lightGray)
        ){
            PopulateDropdownMenu(label="DOM")
        }
        TextField(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(1f)
                .background(Color(0XFFEEEEEE)),
            value = textInputSavings,
            onValueChange = {newValue ->
                if (newValue.isEmpty() || newValue.matches(digitsPattern)) {
                    textInputSavings = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text("SAVINGS")
            }
        )
        TextField(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(1f)
                .background(Color(0XFFEEEEEE)),
            value = textInputChecking,
            onValueChange = {newValue ->
                if (newValue.isEmpty() || newValue.matches(digitsPattern)) {
                    textInputChecking = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text("CHECKING")
            }
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(1f)
                .background(Color(0XFFEEEEEE))
        ){
            PopulateDropdownMenu(label="action")
        }
    }
}


@Composable
fun PopulateDropdownMenu(label: String, minValue: Int = 0, maxValue: Int = 31   ) {
    var expanded by remember { mutableStateOf(false) }
    val domList = List(size=maxValue) { "${it + minValue}" }
    var selectedDOM by remember { mutableStateOf(domList[0]) }
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PopulatePredictionButton() {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val backgroundColor = if (isPressed) Color.Red else Color.Blue
    val lightGray = Color(0XFFEEEEEE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        //    .fillMaxHeight(0.1f),
        //    .padding(30.dp),

        //horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .weight(0.4f)
                //.background(lightGray)
        ) {
            Button(
                onClick = {},
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
            PopulateDropdownMenu(label="Expected next action")
        }
    }
}

@Composable
fun PopulateOutputScrollView() {
    Column(
        modifier = Modifier
            .background(Color(0xFF9C8F80))
            .fillMaxWidth()
            .fillMaxHeight(1f)
            .verticalScroll(rememberScrollState())
    ){
        repeat(15) {
            Text("Item $it", modifier = Modifier.padding(2.dp))
        }
    }
}


// 1. Data Models for States and Transitions
data class MarkovState(
    val id: String,
    val name: String,
    val position: Offset // Pre-defined position or calculated layout
)

data class MarkovTransition(
    val fromStateId: String,
    val toStateId: String,
    val probability: Float
)

@Composable
fun MarkovChainGraph(
    states: List<MarkovState>,
    transitions: List<MarkovTransition>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                    color = Color(0xFF6200EE),
                    radius = nodeRadius,
                    center = state.position
                )
                // Draw Node Border
                drawCircle(
                    color = Color.White,
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
fun PreviewMarkovChain() {
    val sampleStates = listOf(
        MarkovState("S1", "Sunny", Offset(200f, 500f)),
        MarkovState("S2", "Rainy", Offset(700f, 500f))
    )

    val sampleTransitions = listOf(
        MarkovTransition("S1", "S1", 0.8f),  // Sunny stays Sunny
        MarkovTransition("S1", "S2", 0.2f),  // Sunny turns Rainy
        MarkovTransition("S2", "S1", 0.6f),  // Rainy turns Sunny
        MarkovTransition("S2", "S2", 0.4f)   // Rainy stays Rainy
    )

    MarkovChainGraph(states = sampleStates, transitions = sampleTransitions)
}

