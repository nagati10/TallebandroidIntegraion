package sim2.app.talleb_5edma.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class NavItem(val label: String, val route: String)

val myNavItems = listOf(
    NavItem("News", "ScreenHome"),
    NavItem("Matches", "ScreenMatches"),
    NavItem("Bookmarks", "ScreenBookMarks"),
)


object SELECTED_ITEM{var value by mutableIntStateOf(0) }