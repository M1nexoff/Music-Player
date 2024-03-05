package uz.gita.mymusicplayer.presentation.ui.screens

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.mymusicplayer.R
import uz.gita.mymusicplayer.utils.MyAppManager
import uz.gita.mymusicplayer.utils.checkPermissions
import uz.gita.mymusicplayer.utils.getMusicsCursor

class SplashScreen : Fragment(R.layout.screen_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().checkPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requireContext().getMusicsCursor().onEach {
                MyAppManager.cursor = it
                findNavController().navigate(R.id.action_splashScreen_to_musicListScreen)
            }.launchIn(lifecycleScope)
        }
    }
}