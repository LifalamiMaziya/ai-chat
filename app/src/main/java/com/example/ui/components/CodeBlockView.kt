package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBg
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke
import com.example.ui.theme.TokenComment
import com.example.ui.theme.TokenFunction
import com.example.ui.theme.TokenKeyword
import com.example.ui.theme.TokenNumber
import com.example.ui.theme.TokenOperator
import com.example.ui.theme.TokenString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, SurfaceStroke, RoundedCornerShape(8.dp))
            .background(CodeBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .border(width = 1.dp, color = SurfaceStroke, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.lowercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("copy_code_button")
            ) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Code", code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    if (isCopied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = SecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCopied) "Copied!" else "Copy code",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (isCopied) SecondaryContainer else OnSurfaceVariant
                )
            }
        }

        // Code Content
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = highlightSyntax(code, language),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.5.sp,
                lineHeight = 22.sp,
                color = OnSurface
            )
        }
    }
}

private val TOKEN_REGEX = Regex(
    "(\"\"\"[\\s\\S]*?\"\"\"|\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'|[a-zA-Z_][a-zA-Z0-9_]*|\\d+|==|!=|<=|>=|=>|->|::|[+\\-*/%=&|^!~?:;,().{}\\[\\]])"
)

/**
 * Lightweight token-based syntax highlighter for Python, Kotlin, TS/JS, JSON, and generic code
 */
fun highlightSyntax(code: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()
        val lang = language.lowercase()

        val keywords = when (lang) {
            "python", "py" -> setOf(
                "import", "from", "def", "return", "yield", "class", "try", "except",
                "finally", "if", "else", "elif", "for", "while", "in", "as", "with",
                "async", "await", "lambda", "None", "True", "False", "is", "not", "and", "or"
            )
            "kotlin", "kt" -> setOf(
                "package", "import", "fun", "val", "var", "class", "interface", "data",
                "object", "sealed", "return", "if", "else", "when", "for", "while",
                "try", "catch", "suspend", "override", "private", "public", "true", "false", "null"
            )
            "typescript", "javascript", "ts", "js" -> setOf(
                "import", "export", "from", "const", "let", "var", "function", "return",
                "interface", "type", "async", "await", "if", "else", "for", "while",
                "try", "catch", "true", "false", "null", "undefined", "new", "class"
            )
            else -> setOf("import", "function", "return", "class", "def", "if", "else", "val", "const")
        }

        val builtIns = setOf("str", "int", "float", "bool", "list", "dict", "set", "String", "Int", "Boolean", "List", "Map", "Iterator", "Dict", "Any")

        lines.forEachIndexed { lineIdx, line ->
            // Check if comment
            val commentStart = when {
                lang in listOf("python", "py") && line.trimStart().startsWith("#") -> line.indexOf("#")
                line.trimStart().startsWith("//") -> line.indexOf("//")
                else -> -1
            }

            if (commentStart != -1) {
                // Line comment
                val beforeComment = line.substring(0, commentStart)
                append(beforeComment)
                val commentText = line.substring(commentStart)
                pushStyle(SpanStyle(color = TokenComment, fontStyle = FontStyle.Italic))
                append(commentText)
                pop()
            } else {
                // Token parse line
                val matches = TOKEN_REGEX.findAll(line)

                var lastPos = 0
                for (match in matches) {
                    if (match.range.first > lastPos) {
                        append(line.substring(lastPos, match.range.first))
                    }
                    val token = match.value

                    when {
                        token.startsWith("\"\"\"") || token.startsWith("\"") || token.startsWith("'") -> {
                            pushStyle(SpanStyle(color = TokenString))
                            append(token)
                            pop()
                        }
                        token in keywords -> {
                            pushStyle(SpanStyle(color = TokenKeyword, fontWeight = FontWeight.Bold))
                            append(token)
                            pop()
                        }
                        token in builtIns -> {
                            pushStyle(SpanStyle(color = TokenFunction))
                            append(token)
                            pop()
                        }
                        token.all { it.isDigit() } -> {
                            pushStyle(SpanStyle(color = TokenNumber))
                            append(token)
                            pop()
                        }
                        token in setOf("->", "=>", ":", "=", "+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=") -> {
                            pushStyle(SpanStyle(color = TokenOperator))
                            append(token)
                            pop()
                        }
                        token.endsWith("(") || line.getOrNull(match.range.last + 1) == '(' -> {
                            pushStyle(SpanStyle(color = TokenFunction))
                            append(token)
                            pop()
                        }
                        else -> {
                            append(token)
                        }
                    }
                    lastPos = match.range.last + 1
                }
                if (lastPos < line.length) {
                    append(line.substring(lastPos))
                }
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
