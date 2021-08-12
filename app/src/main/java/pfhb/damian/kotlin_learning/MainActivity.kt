package pfhb.damian.kotlin_learning

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = FirebaseFirestore.getInstance()
        getDataFromCloud()

    }

    override fun onResume() {
        super.onResume()
        val preferences = getPreferences(MODE_PRIVATE)
        val nick: String? = preferences.getString("username", "user")
        findViewById<EditText>(R.id.username).setText(nick)
    }

    private fun saveData() {
        val preferences = getPreferences(MODE_PRIVATE)
        val editor = preferences.edit()
        val username:String = findViewById<EditText>(R.id.username).text.toString()
        editor.putString("username", username)
        editor.apply()
    }


    private fun getDataFromCloud() {
        val viewTextData: TextView = findViewById(R.id.info)
        viewTextData.text = ""
        val ref = db.collection("Test_data").orderBy("timestamp", Query.Direction.DESCENDING)
        ref.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val date: String = document.id
                    val message: String = document.data["message"].toString()
                    val user: String = document.data["user"].toString()

                    setDataOnScreen(date, message, user)
                }
            }
        runner()
    }


    private fun setDataOnScreen(date: String, message: String, user: String) {
        val splitDate: List<String> = date.split(" ")
        val viewTextData: TextView = findViewById(R.id.info)
        viewTextData.append("${splitDate[0]}\n ", R.color.common_google_signin_btn_text_light)
        viewTextData.append("${splitDate[1]} ", R.color.common_google_signin_btn_text_light)

        viewTextData.append("$user: ", R.color.purple_500)
        viewTextData.append(message, R.color.common_google_signin_btn_text_light)
        viewTextData.append("\n\n")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendData(view: View) {
        saveData()
        val edit: EditText = findViewById(R.id.edittext)
        val message: String = edit.text.toString()
        val user: String = findViewById<EditText>(R.id.username).text.toString()
        val data = hashMapOf(
            "message" to message,
            "user" to user,
            "timestamp" to com.google.firebase.Timestamp.now().toString()
        )

        val ref = db.collection("Test_data")
        ref.document(
            DateTimeFormatter.ofPattern("dd-MM HH:mm:ss")
                .withZone(ZoneOffset.systemDefault()).format(Instant.now()).toString()
        )
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Message sent successfully", Toast.LENGTH_LONG)
                    .show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Cannot send message: $e", Toast.LENGTH_LONG)
                    .show()
            }

        edit.setText("")
//        finish()
//        startActivity(intent)
    }

    private fun runner(){
        //class scope
        val handler = Handler()
        val runnable = Runnable {

            handler.removeCallbacksAndMessages(null)
            getDataFromCloud()
        }
        handler.postDelayed(runnable,10000)

    }
}