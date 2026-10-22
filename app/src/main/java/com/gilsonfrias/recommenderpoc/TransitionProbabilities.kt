package com.gilsonfrias.recommenderpoc

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

/**
 * Room database entity representing transition probabilities from state S0 to destination states in range {S0...S9}.
 *
 * @property destinationStateId Primary key representing the destination state Int identifier (0 for S0, 1 for S1, ..., 9 for S9).
 * @property probability The transition probability float value.
 * @property fromStateId The source state identifier, defaulting to "S0".
 */
@Entity(tableName = "transition_probabilities")
data class TransitionProbabilities(
    @PrimaryKey
    val destinationStateId: Int,
    var probability: Float,
    val fromStateId: String = "S0",
)

/**
 * Data Access Object (DAO) for [TransitionProbabilities] table providing SELECT and INSERT queries.
 */
@Dao
interface TransitionProbabilitiesDao {

    @Query("SELECT * FROM transition_probabilities")
    suspend fun getAllTransitions(): List<TransitionProbabilities>

    @Query("SELECT * FROM transition_probabilities WHERE destinationStateId = :destinationStateId")
    suspend fun getTransitionByDestination(destinationStateId: Int): TransitionProbabilities?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: TransitionProbabilities)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransitions(transitions: List<TransitionProbabilities>)
}

/**
 * Room Database definition for the application.
 */
@Database(entities = [TransitionProbabilities::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transitionProbabilitiesDao(): TransitionProbabilitiesDao
}

/**
 * Converts a [MarkovTransition] to a [TransitionProbabilities] Room entity.
 */
fun MarkovTransition.toTransitionProbabilities(): TransitionProbabilities {
    val destId = toStateId.removePrefix("S").toIntOrNull() ?: 0
    return TransitionProbabilities(
        destinationStateId = destId,
        probability = probability,
        fromStateId = fromStateId,
    )
}

/**
 * Converts a [TransitionProbabilities] Room entity back to a [MarkovTransition].
 */
fun TransitionProbabilities.toMarkovTransition(): MarkovTransition {
    return MarkovTransition(
        fromStateId = fromStateId,
        toStateId = "S$destinationStateId",
        probability = probability,
    )
}

/**
 * Sample transition probabilities derived from sampleTransitions in MainActivity.kt
 */
fun buildTransitionProbabilities(): List<TransitionProbabilities> {
    return listOf(
        TransitionProbabilities(destinationStateId = 0, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 1, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 2, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 3, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 4, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 5, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 6, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 7, probability = 0.0f, fromStateId = "S0"),
        TransitionProbabilities(destinationStateId = 8, probability = 0.0f, fromStateId = "S0"),
    )
}
