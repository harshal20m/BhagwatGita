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
        val maxYears = prefs[LIFE_MAX_YEARS] ?: 90

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
                    text = if(lang == "HINDI") "जीवन सूचक ($maxYears वर्ष)" else "Life Indicator ($maxYears Years)",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFE2E2E6)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(GlanceModifier.height(8.dp))

                // Compact Day & Month Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Days (7-col grid)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val today = Calendar.getInstance()
                        val daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val currentDay = today.get(Calendar.DAY_OF_MONTH)
                        
                        val dayCols = 7
                        val dayRows = (daysInMonth + dayCols - 1) / dayCols
                        
                        for (r in 0 until dayRows) {
                            Row {
                                for (c in 0 until dayCols) {
                                    val dayIndex = r * dayCols + c
                                    if (dayIndex < daysInMonth) {
                                        val isPassed = dayIndex < currentDay - 1
                                        Box(
                                            modifier = GlanceModifier
                                                .size(6.dp)
                                                .padding(1.dp)
                                                .background(ColorProvider(if (isPassed) Color(0xFFD0BCFF) else Color(0xFF44474E)))
                                                .cornerRadius(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(GlanceModifier.size(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(GlanceModifier.width(12.dp))

                    // Months (7-col grid for parity)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val monthCols = 7
                        val monthRows = (12 + monthCols - 1) / monthCols
                        for (r in 0 until monthRows) {
                            Row {
                                for (c in 0 until monthCols) {
                                    val monthIndex = r * monthCols + c
                                    if (monthIndex < 12) {
                                        val isCompleted = monthIndex < completedMonths
                                        Box(
                                            modifier = GlanceModifier
                                                .size(6.dp)
                                                .padding(1.dp)
                                                .background(ColorProvider(if (isCompleted) Color(0xFFD0BCFF) else Color(0xFF44474E)))
                                                .cornerRadius(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(GlanceModifier.size(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                // Years (10-col grid)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val yearCols = 10
                    val yearRows = (maxYears + yearCols - 1) / yearCols
                    for (r in 0 until yearRows) {
                        Row {
                            for (c in 0 until yearCols) {
                                val yearIndex = r * yearCols + c
                                val yearNum = yearIndex + 1
                                if (yearIndex < maxYears) {
                                    val isCompleted = yearIndex < completedYears
                                    
                                    val isMilestone = yearNum % 10 == 0 || yearNum == maxYears
                                    val milestoneColor = if (isMilestone) {
                                        when {
                                            yearNum <= 30 -> Color(0xFF4CAF50)
                                            yearNum <= 60 -> Color(0xFFFFC107)
                                            else -> Color(0xFFF44336)
                                        }
                                    } else null

                                    Box(
                                        modifier = GlanceModifier
                                            .size(12.dp)
                                            .padding(1.5.dp)
                                            .background(
                                                ColorProvider(
                                                    if (isCompleted) {
                                                        milestoneColor ?: Color(0xFFD0BCFF)
                                                    } else {
                                                        milestoneColor?.copy(alpha = 0.2f) ?: Color(0xFF44474E)
                                                    }
                                                )
                                            )
                                            .cornerRadius(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isMilestone) {
                                            Text(
                                                text = yearNum.toString(),
                                                style = TextStyle(
                                                    color = ColorProvider(if (isCompleted) Color.White else milestoneColor ?: Color.Gray),
                                                    fontSize = 5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(GlanceModifier.size(12.dp))
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
        val LIFE_MAX_YEARS = intPreferencesKey("life_max_years")
    }
}

class LifeIndicatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LifeIndicatorWidget()
}