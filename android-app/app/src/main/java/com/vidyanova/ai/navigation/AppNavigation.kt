package com.vidyanova.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vidyanova.ai.ui.screens.classselection.ClassSelectionScreen
import com.vidyanova.ai.ui.screens.focusmode.FocusModeScreen
import com.vidyanova.ai.ui.screens.home.HomeScreen
import com.vidyanova.ai.ui.screens.onboarding.OnboardingScreen
import com.vidyanova.ai.ui.screens.opportunities.OpportunitiesScreen
import com.vidyanova.ai.ui.screens.opportunities.OpportunityDetailScreen
import com.vidyanova.ai.ui.screens.parent.ParentDashboardScreen
import com.vidyanova.ai.ui.screens.parent.ParentPinScreen
import com.vidyanova.ai.ui.screens.progress.ProgressScreen
import com.vidyanova.ai.ui.screens.quiz.QuizResultScreen
import com.vidyanova.ai.ui.screens.quiz.QuizScreen
import com.vidyanova.ai.ui.screens.scanlearn.ScanLearnScreen
import com.vidyanova.ai.ui.screens.settings.SettingsScreen
import com.vidyanova.ai.ui.screens.splash.SplashScreen
import com.vidyanova.ai.ui.screens.subjects.SubjectScreen
import com.vidyanova.ai.ui.screens.subjects.TopicSelectionScreen
import com.vidyanova.ai.ui.screens.teacherslip.TeacherSlipScreen
import com.vidyanova.ai.ui.screens.tutor.TutorScreen

@Composable
fun VidyaNovaNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash"
) {
    var selectedClassLevel by remember { mutableIntStateOf(10) }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var tutorInitialPrompt by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate("classSelection") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("classSelection") {
            ClassSelectionScreen(
                onContinue = { classLevel ->
                    selectedClassLevel = classLevel
                    navController.navigate("home") {
                        popUpTo("classSelection") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("subjects") {
            SubjectScreen(
                onBack = { navController.popBackStack() },
                onOpenTopic = { subject ->
                    selectedSubject = subject
                    navController.navigate("topics")
                }
            )
        }

        composable("topics") {
            TopicSelectionScreen(
                subject = selectedSubject,
                onBack = { navController.popBackStack() },
                onOpenTutor = { topic ->
                    selectedTopic = topic
                    navController.navigate("tutor")
                }
            )
        }

        composable("tutor") {
            TutorScreen(
                initialPrompt = tutorInitialPrompt,
                classLevel = selectedClassLevel,
                subject = selectedSubject,
                topic = selectedTopic,
                onBack = { navController.popBackStack() },
                onOpenScan = { navController.navigate("scanLearn") }
            )
        }

        composable("scanLearn") {
            ScanLearnScreen(
                onBack = { navController.popBackStack() },
                onAskTutor = { prompt ->
                    tutorInitialPrompt = prompt
                    navController.navigate("tutor")
                }
            )
        }

        composable("quiz") {
            QuizScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.navigate("quizResult")
                }
            )
        }

        composable("quizResult") {
            QuizResultScreen(
                onRetry = {
                    navController.navigate("quiz") {
                        popUpTo("quiz") { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("progress") {
            ProgressScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("parentPin") {
            ParentPinScreen(
                onUnlock = {
                    navController.navigate("parentDashboard") {
                        popUpTo("parentPin") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("parentDashboard") {
            ParentDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("teacherSlip") {
            TeacherSlipScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("opportunities") {
            OpportunitiesScreen(
                onBack = { navController.popBackStack() },
                onOpenOpportunity = { navController.navigate("opportunityDetail") }
            )
        }

        composable("opportunityDetail") {
            OpportunityDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("focusMode") {
            FocusModeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}
