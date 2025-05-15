package com.ace.playstation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ace.playstation.auth.LoginActivity
import com.ace.playstation.databinding.ActivityAdminBinding
import com.google.android.material.navigation.NavigationView

class AdminActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAdmin.toolbar)

        val drawerLayout: DrawerLayout = binding.adminDrawerLayout
        val navView: NavigationView = binding.adminNavView
        val navController = findNavController(R.id.nav_host_fragment_admin_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_admin_dashboard,
                R.id.nav_admin_riwayat_transaksi,
                R.id.nav_admin_laporan_keuangan,
                R.id.nav_admin_persediaan,
                R.id.nav_admin_pengeluaran,
                R.id.nav_admin_logout,),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.itemIconTintList = null
        navView.setupWithNavController(navController)

        // Menangani klik item pada Navigation Drawer
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_admin_logout -> {
                    logoutUser()
                    true
                }
                else -> {
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_admin_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_admin_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
