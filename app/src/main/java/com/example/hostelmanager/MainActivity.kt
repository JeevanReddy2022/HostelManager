package com.example.hostelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hostelmanager.ui.dashboard.HostelDashboardScreen
import com.example.hostelmanager.ui.expenses.AddExpenseScreen
import com.example.hostelmanager.ui.hostels.HostelListScreen
import com.example.hostelmanager.ui.payments.PaymentHistoryScreen
import com.example.hostelmanager.ui.payments.RecordPaymentScreen
import com.example.hostelmanager.ui.residents.AddResidentScreen
import com.example.hostelmanager.ui.residents.CheckoutScreen
import com.example.hostelmanager.ui.residents.EditResidentScreen
import com.example.hostelmanager.ui.residents.PastResidentsScreen
import com.example.hostelmanager.ui.residents.ReAdmitResidentScreen
import com.example.hostelmanager.ui.residents.ResidentDetailsScreen
import com.example.hostelmanager.ui.residents.TransferRoomScreen
import com.example.hostelmanager.ui.rooms.ArchivedRoomsScreen
import com.example.hostelmanager.ui.rooms.RoomDetailsScreen
import com.example.hostelmanager.ui.theme.HostelManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelManagerTheme {
                val navController = rememberNavController()

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
                            onRoomClick = { roomId -> navController.navigate("room_details/$hostelId/$roomId") },
                            onResidentClick = { personId -> navController.navigate("resident_details/$personId") },
                            onArchivedRoomsClick = { navController.navigate("archived_rooms/$hostelId") },
                            onPastResidentsClick = { navController.navigate("past_residents/$hostelId") },
                            onRecordPaymentClick = { personId ->
                                navController.navigate("record_payment/$hostelId/$personId")
                            },
                            onPaymentHistoryClick = { navController.navigate("payment_history/$hostelId") },
                            onAddExpenseClick = { navController.navigate("add_expense/$hostelId") }
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
                            onBackClick = { navController.popBackStack() },
                            onResidentClick = { personId -> navController.navigate("resident_details/$personId") }
                        )
                    }

                    composable(
                        route = "resident_details/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        ResidentDetailsScreen(
                            personId = personId,
                            onBackClick = { navController.popBackStack() },
                            onEditClick = { navController.navigate("edit_resident/$personId") },
                            onTransferClick = { navController.navigate("transfer_room/$personId") },
                            onCheckoutClick = { navController.navigate("checkout/$personId") }
                        )
                    }

                    composable(
                        route = "edit_resident/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        EditResidentScreen(
                            personId = personId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "transfer_room/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        TransferRoomScreen(
                            personId = personId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "checkout/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        CheckoutScreen(
                            personId = personId,
                            onBackClick = { navController.popBackStack() },
                            onCheckoutComplete = {
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "archived_rooms/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        ArchivedRoomsScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "past_residents/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        PastResidentsScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onReAdmitClick = { personId ->
                                navController.navigate("readmit_resident/$hostelId/$personId")
                            }
                        )
                    }

                    composable(
                        route = "readmit_resident/{hostelId}/{personId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("personId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        ReAdmitResidentScreen(
                            personId = personId,
                            hostelId = hostelId,
                            onBackClick = {
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "record_payment/{hostelId}/{personId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("personId") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        RecordPaymentScreen(
                            hostelId = hostelId,
                            prefilledPersonId = personId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "payment_history/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        PaymentHistoryScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // NEW: Add Expense screen
                    composable(
                        route = "add_expense/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        AddExpenseScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}