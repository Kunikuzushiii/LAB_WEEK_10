package com.example.lab_week_10

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val db by lazy { prepareDatabase() }

    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    private var lastUpdateDate: String? = null

    companion object {
        const val ID: Long = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()
        prepareViewModel()
    }

    override fun onStart() {
        super.onStart()
        lastUpdateDate?.let { date ->
            Toast.makeText(this, "Last updated: $date", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()

        val date = Date().toString()
        val value = viewModel.total.value ?: 0

        db.totalDao().update(
            Total(
                id = ID,
                total = TotalObject(value, date)
            )
        )
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java,
            "total-database"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }


    private fun initializeValueFromDatabase() {
        val data = db.totalDao().getTotal(ID)

        if (data.isEmpty()) {
            // Data pertama kali dibuat
            db.totalDao().insert(
                Total(
                    id = ID,
                    total = TotalObject(0, "Never Updated")
                )
            )
            viewModel.setTotal(0)
            lastUpdateDate = "Never Updated"

        } else {
            val totalObject = data.first().total
            viewModel.setTotal(totalObject.value)
            lastUpdateDate = totalObject.date
        }
    }

    private fun prepareViewModel() {
        viewModel.total.observe(this) { total ->
            updateText(total)
        }

        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }
}
