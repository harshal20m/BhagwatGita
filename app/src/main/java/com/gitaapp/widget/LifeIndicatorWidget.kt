package com.gitaapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gitaapp.MainActivity
import java.util.Calendar

class LifeIndicatorWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val name = prefs[PROFILE_NAME] ?: ""
        val day = prefs[PROFILE_DOB_DAY] ?: 1
        val month = prefs[PROFILE_DOB_MONTH] ?: 1
        val year = prefs[PROFILE_DOB_YEAR] ?: 2000
        val lang = prefs[LANGUAGE] ?: "ENGLISH"

        val ageInfo = calculateAge(day, month, year)
        val completedYears = ageInfo.first
        val completedMonths = ageInfo.second

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1A1C1E)))
                .cornerRadius(20.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if(lang == "HINDI") "जीवन सूचक (90 वर्ष)" else "Life Indicator (90 Years)",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFE2E2E6)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(GlanceModifier.height(8.dp))

                // 10x9 Grid for a more standard compact layout
                Column(verticalAlignment = Alignment.CenterVertically) {
                    for (row in 0 until 9) {
                        Row {
                            for (col in 0 until 10) {
                                val yearIndex = row * 10 + col
                                val yearNum = yearIndex + 1
                                val isCompleted = yearIndex < completedYears
                                
                                val milestoneColor = when(yearNum) {
                                    25 -> Color(0xFF4CAF50)
                                    50 -> Color(0xFFFFC107)
                                    75 -> Color(0xFFFF5722)
                                    90 -> Color(0xFFF44336)
                                    else -> null
                                }

                                Box(
                                    modifier = GlanceModifier
                                        .size(12.dp)
                                        .padding(1.5.dp)
                                        .background(
                                            ColorProvider(
                                                milestoneColor ?: if (isCompleted) Color(0xFFD0BCFF) 
                                                else Color(0xFF44474E)
                                            )
                                        )
                                        .cornerRadius(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (milestoneColor != null) {
                                        Text(
                                            text = yearNum.toString(),
                                            style = TextStyle(
                                                color = ColorProvider(Color.White),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = if(lang == "HINDI") 
                        "$completedYears वर्ष, $completedMonths महीने" 
                        else "$completedYears years, $completedMonths months completed",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFC4C6D0)),
                        fontSize = 11.sp
                    )
                )
                
                if (name.isNotBlank()) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFD0BCFF)),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

    private fun calculateAge(day: Int, month: Int, year: Int): Pair<Int, Int> {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { set(year, month - 1, day) }
        
        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
        
        var months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) months--
        if (months < 0) months += 12
        
        return Pair(maxOf(0, age), months)
    }

    companion object {
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_DOB_DAY = intPreferencesKey("profile_dob_day")
        val PROFILE_DOB_MONTH = intPreferencesKey("profile_dob_month")
        val PROFILE_DOB_YEAR = intPreferencesKey("profile_dob_year")
        val LANGUAGE = stringPreferencesKey("language")
    }
}

class LifeIndicatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LifeIndicatorWidget()
}