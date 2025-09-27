package com.pandora.core.ui.designkit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.icons.PandoraIcons
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Enhanced Search Field with Debouncing
 * Search input with loading state and clear functionality
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Tìm kiếm...",
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = PandoraIcons.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = PandoraIcons.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* Handle search */ }),
        shape = RoundedCornerShape(PandoraTokens.Corner.chip)
    )
}
