package com.example.smartmodeswitcher

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.commit
import com.example.smartmodeswitcher.ui.RuleEditFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RuleListFragment())
                .commit()
        }

        findViewById<FloatingActionButton>(R.id.fab_add_rule).setOnClickListener {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, RuleEditFragment())
                addToBackStack(null)
            }
        }
    }
}