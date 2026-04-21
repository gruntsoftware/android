package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.theme.IBMPlexSans

enum class Pointer {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,

    NO_POINTER
}

@Composable
fun CalloutWithPointers(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    calloutWidth: Dp = 190.dp,
    calloutHeight: Dp = 130.dp,
    pointer: Pointer = Pointer.TOP_LEFT
) {
    val pointerSize = 40.dp
    val mainPadding = 22.dp
    val cornerRadius = 10.dp
    Box(
        modifier = Modifier.width(calloutWidth).height(calloutHeight)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }

                            onDrawBehind {
                                drawPath(
                                    path,
                                    color = if (pointer == Pointer.TOP_LEFT) Color.White else Color.Transparent
                                )
                            }
                        }
                        .size(pointerSize)
                )
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val path = Path().apply {
                                moveTo(size.width, 0f)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }

                            onDrawBehind {
                                drawPath(
                                    path,
                                    color = if (pointer == Pointer.TOP_RIGHT) Color.White else Color.Transparent
                                )
                            }
                        }
                        .size(pointerSize)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(0f, size.height)
                                close()
                            }

                            onDrawBehind {
                                drawPath(
                                    path,
                                    color = if (pointer == Pointer.BOTTOM_LEFT) Color.White else Color.Transparent
                                )
                            }
                        }
                        .size(pointerSize)
                )
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width, size.height)
                                close()
                            }

                            onDrawBehind {
                                drawPath(
                                    path,
                                    color = if (pointer == Pointer.BOTTOM_RIGHT) Color.White else Color.Transparent
                                )
                            }
                        }
                        .size(pointerSize)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(top = mainPadding, bottom = mainPadding)
                .fillMaxSize(1f)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.White),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 10.dp, start = 12.dp, bottom = 2.dp),
                text = title,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.Black,
                ),
                textAlign = TextAlign.Start
            )

            Text(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp),
                text = subtitle,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color.Black,
                ),
                textAlign = TextAlign.Start,
                maxLines = 5
            )
        }
    }
}

@Preview
@Composable
private fun CalloutWithPointersPreview() {
    Column {
        Box(modifier = Modifier.size(200.dp)) {
            CalloutWithPointers(title = "Title", subtitle = "Subtitle")
        }
        Box(modifier = Modifier.size(200.dp)) {
            CalloutWithPointers(
                title = "Title",
                subtitle = "Subtitle",
                pointer = Pointer.TOP_RIGHT
            )
        }
        Box(modifier = Modifier.size(200.dp)) {
            CalloutWithPointers(
                title = "Title",
                subtitle = "Subtitle",
                pointer = Pointer.BOTTOM_LEFT
            )
        }
        Box(modifier = Modifier.size(200.dp)) {
            CalloutWithPointers(
                title = "Title",
                subtitle = "Subtitle",
                pointer = Pointer.BOTTOM_RIGHT
            )
        }
    }
}
