package se.umu.fina0006.textscanner

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import se.umu.fina0006.textscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var toolbarMenuInitialized = false
    private lateinit var navController: NavController
    private val pictureActivity = registerForActivityResult(CameraActivity.TakePicture())
    { text: String? -> launchEditFragment(text) }

    private fun launchEditFragment(text: String?) {
        Log.d(TAG, "TEXT: $text")
        // Navigate to SecondFragment and pass the scanned text as an argument
        if (!text.isNullOrEmpty()) {
            val bundle = bundleOf(
                "scannedText" to text,
                "isSaved" to false,
            )
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        setListeners()
    }

    private fun setListeners() {
        setFabListener()
        setDestinationChangedListener()
    }

    private fun setFabListener() {
        binding.fab.setOnClickListener {
            pictureActivity.launch(Unit)
        }
    }

    private fun showToolbarMenu(status: Boolean) {
        val menu = binding.toolbar.menu
        val menuItem = menu.findItem(R.id.action_settings)
        menuItem.isVisible = status
    }

    private fun setDestinationChangedListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (toolbarMenuInitialized) {
                when (destination.id) {
                    R.id.FirstFragment -> {
                        binding.fab.show()
                        showToolbarMenu(true)
                    }
                    R.id.SecondFragment -> {
                        binding.fab.hide()
                        showToolbarMenu(true)
                    }
                    R.id.SettingsFragment -> showToolbarMenu(false)
                    else -> binding.fab.hide()
                }
            }
        }
        toolbarMenuInitialized = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(R.id.SettingsFragment)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "TextScannerMainActivity"
    }
}