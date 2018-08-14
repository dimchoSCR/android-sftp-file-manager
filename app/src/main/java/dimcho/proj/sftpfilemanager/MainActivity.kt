package dimcho.proj.sftpfilemanager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Setup FAB menu
        val fabMenu: FabMenu = FabMenu(this, ltMain)
        fab.setOnClickListener { fabMenu.toggleFabMenu() }

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fgContainer, StorageViewFragment(), STORAGE_VIEW_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onResume() {
        Log.wtf("Test" , "Activity resume")
        super.onResume()
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val filesFragment =
                supportFragmentManager.findFragmentByTag(FILES_VIEW_FRAGMENT_TAG)
                        as? FilesViewFragment

        if(filesFragment == null) {
            super.onBackPressed()
        } else if(filesFragment.isVisible) {
            filesFragment.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.wtf("Test", "Activity finishing: $isFinishing")
        Log.wtf("Test", "On Activity Destroy")
    }
}
