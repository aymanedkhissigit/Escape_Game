package fr.mastergime.meghasli.escapegame.ui.fragments

import android.animation.Animator
import android.graphics.Color
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.mastergime.meghasli.escapegame.R
import fr.mastergime.meghasli.escapegame.databinding.FragmentGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import fr.mastergime.meghasli.escapegame.model.*
import fr.mastergime.meghasli.escapegame.viewmodels.EnigmesViewModel
import fr.mastergime.meghasli.escapegame.viewmodels.SessionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.math.log

@AndroidEntryPoint
class GameFragment : Fragment(), NfcAdapter.ReaderCallback {

    val sessionViewModel: SessionViewModel by viewModels()
    val enigmeViewModel: EnigmesViewModel by viewModels()

    private val job = SupervisorJob()
    private val ioScope by lazy { CoroutineScope(job + Dispatchers.Main) }


    var mNfcAdapter: NfcAdapter? = null
    private lateinit var binding: FragmentGameBinding

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.intro_jeux)

        disableStatusBar()

        sessionViewModel.updateSessionId()
        sessionViewModel.starTimerSession()

        sessionViewModel.endTime.observe(viewLifecycleOwner) { value ->
            Log.d("valueTime", "mainTimer: $value ")
            mainTimer(value)
        }

        binding.quitButton.setOnClickListener {
            binding.quitButton.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            it.isEnabled = false
            sessionViewModel.quitSession()
        }

        sessionViewModel.quitSessionState.observe(viewLifecycleOwner) { value ->
            observeSessionState(value)
        }

        sessionViewModel.sessionId.observe(viewLifecycleOwner) {
            sessionId = it
        }

        createListEnigmaAdapter()
        createListCluesAdapter()
    }

    private fun mainTimer(endTime: Long) {
        val endTime = endTime
        val current = System.currentTimeMillis()
        Log.d("currentTime", "mainTimer: $current -> $endTime ")
        var stay = endTime - current
        Log.d("stayTime", "mainTimer: $stay ")

        object : CountDownTimer(90000, 1000) {
            override fun onTick(p0: Long) {
                stay = p0
                val minute = stay / 60000
                val second = stay % 60000 / 1000
                if (minute < 10) {
                    if (second < 10) {
                        "0$minute:0$second".also {
                            binding.textViewTime.text = it
                        }
                    } else {
                        "0$minute:$second".also {
                            binding.textViewTime.text = it
                        }
                    }
                } else if (second < 10) {
                    "$minute:0$second".also {
                        binding.textViewTime.text = it
                    }
                } else {
                    "$minute:$second".also {
                        binding.textViewTime.text = it
                    }
                }

                if (minute < 1) {
                    binding.textViewTime.setTextColor(Color.RED);
                }
            }

            override fun onFinish() {
                Toast.makeText(requireContext(), "Game Over", Toast.LENGTH_SHORT).show()
                "GAME OVER".also {
                    binding.textViewTime.text = it
                    binding.textViewTime.setTextColor(Color.RED);
                }
            }
        }.start()

    }

    private fun observeSessionState(value: String?) {
        if (value == "Success") {
            binding.quitButton.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            sessionViewModel.notReadyPlayer()
            findNavController().navigate(R.id.action_gameFragment_to_menuFragment)
        } else
            Toast.makeText(
                activity, "Can't leave Session please retry",
                Toast.LENGTH_SHORT
            ).show()
        binding.progressBar.visibility = View.INVISIBLE
        binding.quitButton.visibility = View.VISIBLE
        binding.quitButton.isEnabled = true
    }

    private fun createListEnigmaAdapter() {
        val enigmaList = mutableListOf(
            UserForRecycler("Enigme Optionel", false),
            UserForRecycler("Enigme One", false),
            UserForRecycler("Enigme Two: Part One", false),
            UserForRecycler("Enigme Two: Part Two", false),
            UserForRecycler("Enigme Three", false),
            UserForRecycler("Enigme Final", false),
        )
        val enigmaListAdapter = EnigmaListAdapter {
            when (it) {
                0 -> ioScope.launch {
                    if (!enigmeViewModel.getOptionalEnigmeOpenClos())
                        findNavController().navigate(R.id.action_gameFragment_to_optionel_enigme_fragment)
                    else
                        Toast.makeText(
                            requireContext(),
                            "Enigma Already Done",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                1 -> {
                    loadAnimationSignUpDone("enigme1")
                }
                //  1 -> findNavController().navigate(R.id.action_gameFragment_to_enigme1Fragment)
                2 -> {
                    loadAnimationSignUpDone("enigme21")
                }
                3 -> {
                    loadAnimationSignUpDone("enigme22")
                }
                4 -> findNavController().navigate(R.id.action_gameFragment_to_enigme3Fragment)
                5 -> findNavController().navigate(R.id.action_gameFragment_to_enigme4Fragment)
            }
        }
        enigmaListAdapter.submitList(enigmaList)
        binding.recyclerEnigma.apply {
            setHasFixedSize(true)
            adapter = enigmaListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun createListCluesAdapter() {
        val clueList = mutableListOf(
            UserForRecycler("Clue One", false),
            UserForRecycler("Clue Two", false),
            UserForRecycler("Clue Three", false),
            UserForRecycler("Clue Four", false),
        )

        val cluesListAdapter = ClueListAdapter()
        cluesListAdapter.submitList(clueList)
        binding.recyclerViewClues.apply {
            setHasFixedSize(true)
            adapter = cluesListAdapter
            layoutManager = CenterZoomLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun enableNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {

            mNfcAdapter!!.enableReaderMode(
                this.activity,
                this,
                NfcAdapter.FLAG_READER_NFC_A, Bundle.EMPTY
            )
        } else {
        }
    }

    override fun onTagDiscovered(tag: Tag?) {

        val mNdef: Ndef? = Ndef.get(tag)

        if (mNdef != null) {
            mNdef.connect()
            val mNdefMessage = mNdef.ndefMessage
            val msg = mNdefMessage.records[0].toUri().toString()

            when (msg) {
                "enigme1" -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadAnimationSignUpDone("enigme1")
                    }
                }
                "enigme21" -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadAnimationSignUpDone("enigme21")
                    }
                }
                "enigme22" -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadAnimationSignUpDone("enigme22")
                    }
                }
            }
            mNdef.close()
        } else {
            ReaderMode.message = "FAILED"
        }
    }

    private fun disableStatusBar() {
        (activity as AppCompatActivity).supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }


    override fun onResume() {
        super.onResume()
        enableNfc()
        disableStatusBar()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {
            mNfcAdapter!!.disableReaderMode(activity)
        }

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }

    }

    companion object {
        var sessionId = ""
    }

    private fun loadAnimationSignUpDone(enigmeTag: String) {
        binding.animationViewLoading.setAnimation("done.json")
        binding.animationViewLoading.visibility = View.VISIBLE
        binding.animationViewLoading.playAnimation()
        binding.animationViewLoading.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                binding.textViewTitleOfClues.visibility = View.INVISIBLE
                binding.textViewTitleOfEnigme.visibility = View.INVISIBLE
                binding.recyclerEnigma.visibility = View.INVISIBLE
                binding.recyclerViewClues.visibility = View.INVISIBLE
            }

            override fun onAnimationEnd(p0: Animator?) {
                val bundle = bundleOf("enigmeTag" to enigmeTag)
                when (enigmeTag) {
                    "enigme1" -> findNavController().navigate(
                        R.id.action_gameFragment_to_enigme1Fragment,
                        bundle
                    )
                    "enigme21" -> findNavController().navigate(
                        R.id.action_gameFragment_to_enigme21Fragment,
                        bundle
                    )
                    "enigme22" -> findNavController().navigate(
                        R.id.action_gameFragment_to_enigme22Fragment,
                        bundle
                    )
                    "enigme3" -> findNavController().navigate(
                        R.id.action_gameFragment_to_enigme3Fragment,
                        bundle
                    )
                    "enigme4" -> findNavController().navigate(
                        R.id.action_gameFragment_to_enigme4Fragment,
                        bundle
                    )
                }
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

}