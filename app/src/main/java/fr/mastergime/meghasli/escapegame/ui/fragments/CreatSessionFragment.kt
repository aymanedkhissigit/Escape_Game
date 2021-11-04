package fr.mastergime.meghasli.escapegame.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastergime.meghasli.escapegame.R
import fr.mastergime.meghasli.escapegame.databinding.FragmentCreatSessionBinding
import fr.mastergime.meghasli.escapegame.databinding.FragmentLogBinding
import fr.mastergime.meghasli.escapegame.viewModels.SessionViewModel
import kotlinx.android.synthetic.main.user_item.*


@AndroidEntryPoint
class CreatSessionFragment : Fragment() {

    private lateinit var binding : FragmentCreatSessionBinding
    private val sessionViewModel : SessionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreatSessionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateSession.setOnClickListener(){
            if(binding.edtNomSession.text.isNotEmpty()){





                sessionViewModel.createSession(binding.edtNomSession.text.toString())

                findNavController().navigate(R.id.action_creatSessionFragment_to_sessionRoomFragment)

                /*val bundle = bundleOf("sessionName" to binding.edtNomSession.text.toString())
                findNavController().navigate(R.id.action_creatSessionFragment_to_sessionRoomFragment,bundle)*/
            }
        }
    }




}