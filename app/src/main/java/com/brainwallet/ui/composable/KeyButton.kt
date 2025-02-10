package com.brainwallet.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun KeyButton (
    number: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .height(44.dp),
        onClick = onClick,
        border = BorderStroke(
            width = 2.dp,
            color =  Color.Gray
        ),
        colors = colors,
        shape = shape,
        content = content
    )
        Text(text = number.toString())
}

//{
//    Text(text = number.toString())
//}
