package fr.mastergime.meghasli.escapegame.ui.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import fr.mastergime.meghasli.escapegame.R
import fr.mastergime.meghasli.escapegame.databinding.FragmentLogBinding
import fr.mastergime.meghasli.escapegame.viewmodels.AuthViewModel

@AndroidEntryPoint
class LogFragment : Fragment() {

    private lateinit var binding: FragmentLogBinding
    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disableStatusBar()

        binding.logFragment.setOnClickListener {
            hideKeyBoard()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailTextInput.editText?.text.toString()
            val password = binding.passwordTextInput.editText?.text.toString()

            if (test(email)) {
                binding.progressBar.visibility = View.VISIBLE
                authViewModel.login(email, password)
                binding.progressBar.visibility = View.INVISIBLE
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_logFragment_to_signUpFragment)
        }

        authViewModel.stateLogin.observe(viewLifecycleOwner) { state ->
            if (state == "success") {
                findNavController().navigate(R.id.action_logFragment_to_menuFragment)
                Toast.makeText(activity, "Authentication Succeed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, state, Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_logFragment_to_signUpFragment)
        }

        backCallBack()
    }

    private fun backCallBack() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //TODO QUITTER L'Application
                }
            })
    }

    fun test(email: String): Boolean {
        if (binding.emailTextInput.editText?.text.toString().isEmpty()) {
            binding.emailTextInput.error = "enter email"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInput.error = "enter a valid email"
            return false
        }
        if (binding.passwordTextInput.editText?.text.toString().length < 6) {
            binding.emailTextInput.error = null
            binding.passwordTextInput.error = "password should have at least 6 characters"
            return false
        }
        return true
    }

    fun hideKeyBoard() {
        val inputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        binding.emailTextInput.clearFocus()
        binding.passwordTextInput.clearFocus()
    }

    private fun disableStatusBar(){
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
        disableStatusBar()
    }

}