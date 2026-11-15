package com.example.hostelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hostelmanager.ui.dashboard.HostelDashboardScreen
import com.example.hostelmanager.ui.hostels.HostelListScreen
import com.example.hostelmanager.ui.residents.AddResidentScreen
import com.example.hostelmanager.ui.residents.ResidentViewModel
import com.example.hostelmanager.ui.rooms.RoomDetailsScreen
import com.example.hostelmanager.ui.theme.HostelManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelManagerTheme {
                val navController = rememberNavController()
                val residentViewModel: ResidentViewModel = viewModel()

                NavHost(navController = navController, startDestination = "hostel_list") {

                    composable("hostel_list") {
                        HostelListScreen(
                            onHostelClick = { hostelId ->
                                navController.navigate("hostel_dashboard/$hostelId")
                            }
                        )
                    }

                    composable(
                        route = "hostel_dashboard/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        HostelDashboardScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onAddResidentClick = { navController.navigate("add_resident/$hostelId") },
                            onRoomClick = { roomId -> navController.navigate("room_details/$hostelId/$roomId") }
                        )
                    }

                    composable(
                        route = "add_resident/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        AddResidentScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // NEW: Room Details Screen Route
                    composable(
                        route = "room_details/{hostelId}/{roomId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("roomId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                        RoomDetailsScreen(
                            roomId = roomId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}