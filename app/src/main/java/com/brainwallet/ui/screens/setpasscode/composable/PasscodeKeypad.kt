package com.brainwallet.ui.screens.setpasscode.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PasscodeKeypad(
    onKeyPressed: (String) -> Unit,
) {
    //TODO: will change using CircleButton from another branch later
    Box(
        modifier = Modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
                .padding(horizontal = 30.dp)
        ) {

            Spacer(modifier = Modifier.weight(1f))
            KeyRow(
                keys = listOf("1", "2", "3"),
                callback = onKeyPressed,
                modifier = Modifier
            )
            KeyRow(
                keys = listOf("4", "5", "6"),
                callback = onKeyPressed,
                modifier = Modifier
            )
            KeyRow(
                keys = listOf("7", "8", "9"),
                callback = onKeyPressed,
                modifier = Modifier
            )
            KeyRow(
                keys = listOf(" ", "C", "<-"),
                callback = onKeyPressed,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

}


@Composable
fun KeyRow(
    keys: List<String>,
    callback: (text: String) -> Any,
    modifier: Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    )
    {

        Spacer(modifier = Modifier.weight(1f))
        keys.forEachIndexed { index, keyValue ->
            KeyButton(
                text = keyValue,
                callback = callback
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun KeyButton(
    text: String,
    callback: (text: String) -> Any,
) {
    Button(
        onClick = {
            callback(text)
        },
        modifier = Modifier
            .size(80.dp)
            .border(2.dp, Color.White, CircleShape),
        colors = ButtonDefaults.buttonColors(Color(0xFF2C2C2C))
    ) {

        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxHeight()
                .padding(3.dp)
                .alpha(0.5f),
            color = Color.White,
            textAlign = TextAlign.Center
        )

    }

}