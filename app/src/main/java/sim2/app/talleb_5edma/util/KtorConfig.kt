// ==================== PACKAGE DECLARATION ====================
// This tells the computer where this file belongs in our app's organization
// Think of it like putting a file in the "utilities" folder where we keep configuration tools
package sim2.app.talleb_5edma.util

// ==================== IMPORT STATEMENTS ====================
// These are like borrowing tools from Ktor's networking toolkit
// Each import brings in a specific capability we need for internet communication

// This import gives us the main HTTP client - our internet messenger
// Like having a postal service that can send and receive messages over the internet
import io.ktor.client.HttpClient

// This import provides the CIO engine - the power behind our internet messenger
// CIO stands for "Coroutine I/O" - it's efficient at handling multiple internet requests
import io.ktor.client.engine.cio.CIO

// This import helps our app understand different types of data formats
// Like having a translator that can read JSON, XML, and other data languages
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

// This import allows us to set timeout values for network requests
// Like setting a maximum wait time before giving up on a slow connection
import io.ktor.client.plugins.HttpTimeout

// This import specifically handles Google's JSON format (Gson)
// JSON is like a universal language that websites use to exchange information
import io.ktor.serialization.gson.gson

// These imports are for logging network requests and responses
// They help us see what's happening "under the hood" of our internet messenger
//import io.ktor.client.plugins.logging.LogLevel
//import io.ktor.client.plugins.logging.Logging


// ==================== BASE URL CONSTANT ====================

//const val BASE_URL = "http://10.0.2.2:3005"
const val BASE_URL = "https://talleb-5edma.onrender.com"


// ==================== HTTP CLIENT CONFIGURATION ====================
// This creates our internet messenger with specific settings
// Think of it like setting up a mail carrier with special instructions
val client = HttpClient(CIO) {

    // ==================== TIMEOUT CONFIGURATION ====================
    // This sets how long we wait before giving up on a slow connection
    // Like setting a maximum wait time at a restaurant before leaving
    install(HttpTimeout) {
        // Wait up to 30 seconds to connect to the server
        // Default was too short, causing "connect timeout" errors
        connectTimeoutMillis = 30_000

        // Wait up to 60 seconds for the server to respond
        // Gives the server enough time to process login/signup requests
        requestTimeoutMillis = 1200_000

        // Wait up to 30 seconds to receive data from the server
        // Prevents hanging if the server starts sending but is very slow
        socketTimeoutMillis = 1200_000
    }

    // ==================== LOGGING CONFIGURATION ====================
    // This helps us see what's happening with network requests
    /* install(Logging) {
         // Log all network requests and responses
         level = LogLevel.ALL
     }*/

    // ==================== CONTENT NEGOTIATION SETUP ====================
    // This tells our messenger: "Install the ability to understand different data formats"
    // Like giving our mail carrier a universal translator
    install(ContentNegotiation) {

        // This specifically enables Google's JSON format (Gson)
        // JSON is the most common way websites share information
        // Like teaching our translator to speak the most popular computer language
        gson {
            // Configure Gson to be more lenient and handle Kotlin classes properly
            setLenient()

            // Serialize Kotlin null values as JSON nulls
            serializeNulls()

            // Pretty print JSON for easier reading in logs
            setPrettyPrinting()
        }
    }
}