package com.example.meditrack


import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReminderRepository(context: Context) {

    private val db = MediTrackDB.getDatabase(context)
    private val reminderDao = db.reminderDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    fun getReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun addReminder(reminder: ReminderEntity) {
        withContext(Dispatchers.IO) {
            val localId = reminderDao.insertReminder(reminder)

            if (user != null) {
                try {
                    val docRef = firestore.collection("users")
                        .document(user.uid)
                        .collection("reminders")
                        .add(reminder.toMap())
                        .await()

                    reminderDao.updateReminder(reminder.copy(firebaseID = docRef.id))
                } catch (e: Exception) {
                    Log.e("ReminderRepository", "Firebase sync failed: ${e.message}")
                }
            }
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        withContext(Dispatchers.IO) {
            reminderDao.deleteReminder(reminder)
            if (reminder.firebaseID != null && user != null) {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("reminders")
                    .document(reminder.firebaseID)
                    .delete()
                    .addOnFailureListener { Log.e("ReminderRepository", "Failed to delete Firebase") }
            }
        }
    }

    private fun ReminderEntity.toMap(): Map<String, Any> = mapOf(
        "medicine" to medicine,
        "dosage" to dosage,
        "times" to times,
        "frequency" to frequency,
        "startDate" to startDate,
        "endDate" to endDate,
        "createdAt" to createdAt
    )

}